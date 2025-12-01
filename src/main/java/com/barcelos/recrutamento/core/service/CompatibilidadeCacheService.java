package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.CompatibilidadeCache;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.port.CompatibilidadeCacheRepository;
import com.barcelos.recrutamento.core.port.PerfilCandidatoRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CompatibilidadeCacheService {

    private static final Logger log = LoggerFactory.getLogger(CompatibilidadeCacheService.class);

    private final CompatibilidadeCacheRepository cacheRepository;
    private final CompatibilidadeAIService aiService;
    private final VagaRepository vagaRepository;
    private final PerfilCandidatoRepository perfilCandidatoRepository;

    public CompatibilidadeCacheService(
            CompatibilidadeCacheRepository cacheRepository,
            CompatibilidadeAIService aiService,
            VagaRepository vagaRepository,
            PerfilCandidatoRepository perfilCandidatoRepository) {
        this.cacheRepository = cacheRepository;
        this.aiService = aiService;
        this.vagaRepository = vagaRepository;
        this.perfilCandidatoRepository = perfilCandidatoRepository;
    }

    
    @Transactional(readOnly = true)
    public Optional<CompatibilidadeCache> obterDoCache(UUID candidatoUsuarioId, UUID vagaId) {
        return cacheRepository.findByCandidatoAndVaga(candidatoUsuarioId, vagaId);
    }

    
    @Transactional
    public CompatibilidadeCache obterOuCalcular(UUID candidatoUsuarioId, UUID vagaId) {

        Optional<CompatibilidadeCache> cacheOpt = cacheRepository.findByCandidatoAndVaga(candidatoUsuarioId, vagaId);
        
        if (cacheOpt.isPresent()) {
            log.debug("Cache HIT: Compatibilidade já calculada para candidato {} e vaga {}", 
                     candidatoUsuarioId, vagaId);
            return cacheOpt.get();
        }

        log.info("Cache MISS: Calculando compatibilidade para candidato {} e vaga {}", 
                candidatoUsuarioId, vagaId);
        return calcularEArmazenar(candidatoUsuarioId, vagaId);
    }

    
    @Transactional
    public CompatibilidadeCache calcularEArmazenar(UUID candidatoUsuarioId, UUID vagaId) {

        Optional<CompatibilidadeCache> cacheExistente = cacheRepository.findByCandidatoAndVaga(candidatoUsuarioId, vagaId);
        if (cacheExistente.isPresent()) {
            log.debug("Cache já existe (race condition evitada) para candidato {} e vaga {}", 
                     candidatoUsuarioId, vagaId);
            return cacheExistente.get();
        }

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada: " + vagaId));

        CompatibilidadeAIService.ResultadoCompatibilidade resultado = 
                aiService.calcularCompatibilidade(candidatoUsuarioId, vaga);

        CompatibilidadeCache cache = CompatibilidadeCache.novo(
            candidatoUsuarioId,
            vagaId,
            BigDecimal.valueOf(resultado.score()),
            resultado.justificativa()
        );

        try {
            return cacheRepository.save(cache);
        } catch (Exception e) {

            if (e.getMessage() != null && e.getMessage().contains("uk_cache_candidato_vaga")) {
                log.debug("Duplicata detectada (race condition), buscando cache existente");
                return cacheRepository.findByCandidatoAndVaga(candidatoUsuarioId, vagaId)
                        .orElseThrow(() -> new RuntimeException("Cache não encontrado após duplicata"));
            }
            throw e;
        }
    }

    
    @Async
    @Transactional
    public CompletableFuture<Void> calcularParaTodosCandidatos(UUID vagaId) {
        log.info("Iniciando cálculo PARALELO de compatibilidade para vaga {}", vagaId);
        

        List<UUID> candidatosIds = perfilCandidatoRepository.findAll().stream()
                .map(perfil -> perfil.getUsuarioId())
                .collect(Collectors.toList());

        log.info("Encontrados {} candidatos para calcular compatibilidade", candidatosIds.size());

        List<UUID> candidatosSemCache = candidatosIds.stream()
                .filter(candidatoId -> !cacheRepository.existsByCandidatoAndVaga(candidatoId, vagaId))
                .collect(Collectors.toList());
        
        log.info("Calculando compatibilidade para {} candidatos (outros já têm cache)", candidatosSemCache.size());

        long inicio = System.currentTimeMillis();
        
        Map<String, Long> resultados = candidatosSemCache.parallelStream()
                .map(candidatoId -> {
                    try {
                        calcularEArmazenar(candidatoId, vagaId);
                        return "sucesso";
                    } catch (Exception e) {

                        if (e.getMessage() != null && e.getMessage().contains("uk_cache_candidato_vaga")) {
                            return "duplicata";
                        }
                        log.error("Erro ao calcular compatibilidade para candidato {}: {}", 
                                 candidatoId, e.getMessage(), e);
                        return "erro";
                    }
                })
                .collect(Collectors.groupingBy(r -> r, Collectors.counting()));

        long duracao = System.currentTimeMillis() - inicio;
        long calculados = resultados.getOrDefault("sucesso", 0L);
        long duplicatas = resultados.getOrDefault("duplicata", 0L);
        long erros = resultados.getOrDefault("erro", 0L);

        log.info("Cálculo PARALELO concluído para vaga {}. Calculados: {}, Duplicatas (race condition): {}, Erros: {}, Tempo: {}ms ({}s)", 
                vagaId, calculados, duplicatas, erros, duracao, (duracao/1000.0));

        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> calcularParaTodasVagas(UUID candidatoUsuarioId) {
        log.info("Iniciando cálculo PARALELO de compatibilidade para candidato {}", candidatoUsuarioId);
        

        List<Vaga> vagas = vagaRepository.listPublicas();
        log.info("Encontradas {} vagas abertas para calcular compatibilidade", vagas.size());

        List<Vaga> vagasSemCache = vagas.stream()
                .filter(vaga -> !cacheRepository.existsByCandidatoAndVaga(candidatoUsuarioId, vaga.getId()))
                .collect(Collectors.toList());
        
        log.info("Calculando compatibilidade para {} vagas (outras já têm cache)", vagasSemCache.size());

        long inicio = System.currentTimeMillis();
        
        Map<String, Long> resultados = vagasSemCache.parallelStream()
                .map(vaga -> {
                    try {
                        calcularEArmazenar(candidatoUsuarioId, vaga.getId());
                        return "sucesso";
                    } catch (Exception e) {

                        if (e.getMessage() != null && e.getMessage().contains("uk_cache_candidato_vaga")) {
                            return "duplicata";
                        }
                        return "erro";
                    }
                })
                .collect(Collectors.groupingBy(r -> r, Collectors.counting()));

        long duracao = System.currentTimeMillis() - inicio;
        long calculados = resultados.getOrDefault("sucesso", 0L);
        long duplicatas = resultados.getOrDefault("duplicata", 0L);
        long erros = resultados.getOrDefault("erro", 0L);

        log.info("Cálculo PARALELO concluído para candidato {}. Calculados: {}, Duplicatas (race condition): {}, Erros: {}, Tempo: {}ms ({}s)", 
                candidatoUsuarioId, calculados, duplicatas, erros, duracao, (duracao/1000.0));

        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void invalidarCacheVaga(UUID vagaId) {
        log.info("Invalidando cache de compatibilidade para vaga {}", vagaId);
        cacheRepository.deleteByVaga(vagaId);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> recalcularVaga(UUID vagaId) {
        invalidarCacheVaga(vagaId);
        return calcularParaTodosCandidatos(vagaId);
    }

    @Transactional
    public void invalidarCacheCandidatoSync(UUID candidatoUsuarioId) {
        log.info("Invalidando cache SYNC para candidato {}", candidatoUsuarioId);
        cacheRepository.deleteByCandidato(candidatoUsuarioId);
    }
}
