package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BuscaInteligenteService {

    private static final Logger log = LoggerFactory.getLogger(BuscaInteligenteService.class);

    private final VagaRepository vagaRepository;
    private final CompatibilidadeCacheService compatibilidadeCacheService;

    public BuscaInteligenteService(
            VagaRepository vagaRepository,
            CompatibilidadeCacheService compatibilidadeCacheService) {
        this.vagaRepository = vagaRepository;
        this.compatibilidadeCacheService = compatibilidadeCacheService;
    }

    
    public List<VagaComScoreCompleto> buscar(String consultaTexto, Integer limite, UUID candidatoUsuarioId) {
        int limiteEfetivo = limite != null ? limite : 50;

        List<Vaga> todasVagas = vagaRepository.listPublicas();

        List<Vaga> vagasFiltradas;
        if (consultaTexto != null && !consultaTexto.isBlank()) {
            String termoBusca = consultaTexto.toLowerCase().trim();
            vagasFiltradas = todasVagas.stream()
                    .filter(vaga -> {
                        String titulo = vaga.getTitulo() != null ? vaga.getTitulo().toLowerCase() : "";
                        String descricao = vaga.getDescricao() != null ? vaga.getDescricao().toLowerCase() : "";
                        String requisitos = vaga.getRequisitos() != null ? vaga.getRequisitos().toLowerCase() : "";

                        return titulo.contains(termoBusca) || 
                               descricao.contains(termoBusca) || 
                               requisitos.contains(termoBusca);
                    })
                    .collect(Collectors.toList());
        } else {
            vagasFiltradas = todasVagas;
        }

        if (candidatoUsuarioId != null) {
            return calcularCompatibilidadeParaTodasVagas(vagasFiltradas, candidatoUsuarioId, limiteEfetivo);
        } else {

            return vagasFiltradas.stream()
                    .limit(limiteEfetivo)
                    .map(v -> new VagaComScoreCompleto(v, 0, null, null, false))
                    .collect(Collectors.toList());
        }
    }

    
    private List<VagaComScoreCompleto> calcularCompatibilidadeParaTodasVagas(
            List<Vaga> vagas,
            UUID candidatoUsuarioId,
            int limite) {

        List<VagaComScoreCompleto> vagasComCompatibilidade = new ArrayList<>();
        List<UUID> vagasSemCache = new ArrayList<>();

        for (Vaga vaga : vagas) {
            try {

                var cacheOpt = compatibilidadeCacheService.obterDoCache(candidatoUsuarioId, vaga.getId());

                if (cacheOpt.isPresent()) {
                    var cache = cacheOpt.get();
                    vagasComCompatibilidade.add(new VagaComScoreCompleto(
                            vaga,
                            0,
                            cache.getPercentualCompatibilidade().intValue(),
                            cache.getJustificativa(),
                            true
                    ));
                } else {

                    vagasComCompatibilidade.add(new VagaComScoreCompleto(
                            vaga,
                            0,
                            null,
                            "Calculando compatibilidade...",
                            false
                    ));
                    vagasSemCache.add(vaga.getId());
                }
            } catch (Exception e) {
                log.error("Erro ao obter compatibilidade do cache para vaga {}: {}", 
                         vaga.getId(), e.getMessage(), e);

                vagasComCompatibilidade.add(new VagaComScoreCompleto(
                        vaga,
                        0,
                        null,
                        "Erro ao carregar compatibilidade",
                        false
                ));
            }
        }

        if (!vagasSemCache.isEmpty()) {
            log.info("Cache MISS para {} vagas. Cálculo será feito em background.", vagasSemCache.size());

        }

        return vagasComCompatibilidade.stream()
                .sorted((v1, v2) -> {

                    if (v1.percentualCompatibilidade() != null && v2.percentualCompatibilidade() == null) return -1;
                    if (v1.percentualCompatibilidade() == null && v2.percentualCompatibilidade() != null) return 1;

                    if (v1.percentualCompatibilidade() != null && v2.percentualCompatibilidade() != null) {
                        return Integer.compare(v2.percentualCompatibilidade(), v1.percentualCompatibilidade());
                    }

                    return 0;
                })
                .limit(limite)
                .collect(Collectors.toList());
    }

    
    public record VagaComScoreCompleto(
            Vaga vaga,
            int scoreRelevancia,
            Integer percentualCompatibilidade,
            String justificativa,
            boolean usouIA
    ) {}
}
