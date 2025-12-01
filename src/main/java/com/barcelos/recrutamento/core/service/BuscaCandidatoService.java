package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.PerfilCandidato;
import com.barcelos.recrutamento.core.model.Usuario;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BuscaCandidatoService {

    private static final Logger log = LoggerFactory.getLogger(BuscaCandidatoService.class);

    private final ChatClient chatClient;
    private final UsuarioRepository usuarioRepository;
    private final PerfilCandidatoRepository perfilCandidatoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ExperienciaProfissionalRepository experienciaRepository;
    private final VagaRepository vagaRepository;
    private final CompatibilidadeCacheService compatibilidadeCacheService;
    private final CompatibilidadeCacheRepository compatibilidadeCacheRepository;

    public BuscaCandidatoService(
            ChatClient chatClient,
            UsuarioRepository usuarioRepository,
            PerfilCandidatoRepository perfilCandidatoRepository,
            CompetenciaRepository competenciaRepository,
            ExperienciaProfissionalRepository experienciaRepository,
            VagaRepository vagaRepository,
            CompatibilidadeCacheService compatibilidadeCacheService,
            CompatibilidadeCacheRepository compatibilidadeCacheRepository
    ) {
        this.chatClient = chatClient;
        this.usuarioRepository = usuarioRepository;
        this.perfilCandidatoRepository = perfilCandidatoRepository;
        this.competenciaRepository = competenciaRepository;
        this.experienciaRepository = experienciaRepository;
        this.vagaRepository = vagaRepository;
        this.compatibilidadeCacheService = compatibilidadeCacheService;
        this.compatibilidadeCacheRepository = compatibilidadeCacheRepository;
    }

    
    public ResultadoPaginado buscarComPaginacao(UUID vagaId, String consultaTexto, int page, int size) {

        List<CandidatoComScore> todosCandidatos = buscarTodos(vagaId, consultaTexto);
        

        long totalElements = todosCandidatos.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, todosCandidatos.size());
        

        List<CandidatoComScore> paginaAtual = start < todosCandidatos.size() 
                ? todosCandidatos.subList(start, end) 
                : List.of();
        
        return new ResultadoPaginado(paginaAtual, page, totalPages, totalElements, size);
    }

    
    private List<CandidatoComScore> buscarTodos(UUID vagaId, String consultaTexto) {

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada: " + vagaId));

        if (consultaTexto == null || consultaTexto.isBlank()) {
            return buscarTodosPorCompatibilidade(vagaId);
        }

        String termoNormalizado = consultaTexto.toLowerCase().trim();
        CriteriosBusca criterios = extrairCriterios(consultaTexto);
        List<PerfilCandidato> todosPerfis = perfilCandidatoRepository.findAll();
        
        log.debug("Buscando candidatos com termo: '{}'", termoNormalizado);

        List<CandidatoComScore> candidatosFiltrados = todosPerfis.stream()
                .map(perfil -> {
                    var usuario = usuarioRepository.findById(perfil.getUsuarioId()).orElse(null);
                    if (usuario == null) return null;

                    String nomeCompleto = usuario.getNome().toLowerCase();
                    boolean matchNome = false;
                    

                    if (nomeCompleto.contains(termoNormalizado)) {
                        matchNome = true;
                        log.debug("Match completo: {} contém '{}'", usuario.getNome(), termoNormalizado);
                    } else {

                        String[] palavrasBusca = termoNormalizado.split("\\s+");
                        for (String palavra : palavrasBusca) {
                            if (palavra.length() >= 3 && nomeCompleto.contains(palavra)) {
                                matchNome = true;
                                log.debug("Match parcial: {} contém palavra '{}'", usuario.getNome(), palavra);
                                break;
                            }
                        }
                    }
                    

                    if (matchNome) {
                        int scoreKeywords = calcularRelevancia(perfil, usuario, criterios);

                        int scoreFinal = Math.max(80, scoreKeywords);
                        log.debug("Candidato {} - Score final: {}", usuario.getNome(), scoreFinal);
                        return new CandidatoComScore(usuario, perfil, scoreFinal, null);
                    }
                    

                    int scoreKeywords = calcularRelevancia(perfil, usuario, criterios);
                    return new CandidatoComScore(usuario, perfil, scoreKeywords, null);
                })
                .filter(c -> c != null && c.score() > 0)
                .collect(Collectors.toList());
        
        log.debug("Total de candidatos filtrados: {}", candidatosFiltrados.size());

        if (candidatosFiltrados.isEmpty()) {
            return List.of();
        }

        List<CandidatoComScore> candidatosComCompatibilidade = candidatosFiltrados.stream()
                .map(candidato -> {
                    try {

                        var cacheOpt = compatibilidadeCacheService.obterDoCache(
                                candidato.usuario().getId(), vaga.getId());

                        if (cacheOpt.isPresent()) {
                            var cache = cacheOpt.get();
                            int scoreCache = cache.getPercentualCompatibilidade().intValue();
                            

                            int scoreFinal = candidato.score() >= 80 
                                    ? Math.max(scoreCache, candidato.score())
                                    : scoreCache;
                            
                            return new CandidatoComScore(
                                    candidato.usuario(),
                                    candidato.perfil(),
                                    scoreFinal,
                                    cache.getJustificativa()
                            );
                        } else {

                            return new CandidatoComScore(
                                    candidato.usuario(),
                                    candidato.perfil(),
                                    candidato.score(),
                                    "Compatibilidade em cache não disponível"
                            );
                        }
                    } catch (Exception e) {
                        log.error("Erro ao obter compatibilidade para candidato {}: {}", 
                                 candidato.usuario().getId(), e.getMessage(), e);

                        return new CandidatoComScore(
                                candidato.usuario(),
                                candidato.perfil(),
                                candidato.score(),
                                "Erro ao obter compatibilidade"
                        );
                    }
                })
                .collect(Collectors.toList());

        return candidatosComCompatibilidade.stream()
                .sorted(Comparator.comparingInt(CandidatoComScore::score).reversed())
                .collect(Collectors.toList());
    }

    
    private List<CandidatoComScore> buscarTodosPorCompatibilidade(UUID vagaId) {

        var caches = compatibilidadeCacheRepository.findByVaga(vagaId);

        List<CandidatoComScore> candidatos = caches.stream()
                .map(cache -> {
                    try {
                        var usuario = usuarioRepository.findById(cache.getCandidatoUsuarioId()).orElse(null);
                        var perfil = perfilCandidatoRepository.findByUsuarioId(cache.getCandidatoUsuarioId()).orElse(null);

                        if (usuario == null || perfil == null) {
                            return null;
                        }

                        return new CandidatoComScore(
                                usuario,
                                perfil,
                                cache.getPercentualCompatibilidade().intValue(),
                                cache.getJustificativa()
                        );
                    } catch (Exception e) {
                        log.error("Erro ao processar cache de compatibilidade: {}", e.getMessage(), e);
                        return null;
                    }
                })
                .filter(c -> c != null)
                .sorted(Comparator.comparingInt(CandidatoComScore::score).reversed())
                .collect(Collectors.toList());

        return candidatos;
    }

    
    private CriteriosBusca extrairCriterios(String consultaTexto) {
        String prompt = construirPromptExtracao(consultaTexto);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseCriterios(response);
        } catch (Exception e) {
            log.error("Erro ao extrair critérios com IA: {}", e.getMessage(), e);

            return new CriteriosBusca(List.of(consultaTexto), null, null);
        }
    }

    private String construirPromptExtracao(String consultaTexto) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um assistente especializado em análise de consultas de busca de candidatos.\n\n");
        prompt.append("TAREFA: Analise a consulta abaixo e extraia os critérios de busca.\n\n");
        prompt.append("CONSULTA: \"").append(consultaTexto).append("\"\n\n");
        prompt.append("INSTRUÇÕES:\n");
        prompt.append("1. Identifique palavras-chave sobre habilidades, tecnologias, cargos\n");
        prompt.append("2. Identifique localização se mencionada (cidade, estado)\n");
        prompt.append("3. Identifique senioridade se mencionada (JUNIOR, PLENO, SENIOR)\n\n");
        prompt.append("FORMATO DE RESPOSTA:\n");
        prompt.append("KEYWORDS: palavra1, palavra2, palavra3\n");
        prompt.append("LOCALIZACAO: cidade ou NENHUMA\n");
        prompt.append("SENIORIDADE: [JUNIOR|PLENO|SENIOR|NENHUMA]\n\n");
        prompt.append("Responda APENAS no formato acima.\n");

        return prompt.toString();
    }

    private CriteriosBusca parseCriterios(String response) {
        List<String> keywords = new ArrayList<>();
        String localizacao = null;
        String senioridade = null;

        String[] linhas = response.split("\n");
        for (String linha : linhas) {
            linha = linha.trim();

            if (linha.startsWith("KEYWORDS:")) {
                String keywordsStr = linha.substring("KEYWORDS:".length()).trim();
                if (!keywordsStr.equalsIgnoreCase("NENHUMA") && !keywordsStr.isEmpty()) {
                    String[] kws = keywordsStr.split(",");
                    for (String kw : kws) {
                        keywords.add(kw.trim().toLowerCase());
                    }
                }
            } else if (linha.startsWith("LOCALIZACAO:")) {
                String loc = linha.substring("LOCALIZACAO:".length()).trim();
                if (!loc.equalsIgnoreCase("NENHUMA")) {
                    localizacao = loc.toLowerCase();
                }
            } else if (linha.startsWith("SENIORIDADE:")) {
                String sen = linha.substring("SENIORIDADE:".length()).trim();
                if (!sen.equalsIgnoreCase("NENHUMA")) {
                    senioridade = sen;
                }
            }
        }

        return new CriteriosBusca(keywords, localizacao, senioridade);
    }

    
    private int calcularRelevancia(PerfilCandidato perfil, Usuario usuario, CriteriosBusca criterios) {
        int score = 0;

        if (!criterios.keywords().isEmpty()) {
            var competencias = competenciaRepository.listByPerfilCandidato(perfil.getUsuarioId());
            var experiencias = experienciaRepository.listByUsuario(usuario.getId());

            int matchCount = 0;

            String nomeCompleto = usuario.getNome().toLowerCase();
            for (String keyword : criterios.keywords()) {
                if (nomeCompleto.contains(keyword)) {
                    matchCount += 2;
                    break;
                }
            }

            for (var comp : competencias) {
                String titulo = comp.getTitulo().toLowerCase();
                for (String keyword : criterios.keywords()) {
                    if (titulo.contains(keyword)) {
                        matchCount++;
                        break;
                    }
                }
            }

            for (var exp : experiencias) {
                String cargo = exp.getCargo().toLowerCase();
                String descricao = exp.getDescricao() != null ? exp.getDescricao().toLowerCase() : "";

                for (String keyword : criterios.keywords()) {
                    if (cargo.contains(keyword) || descricao.contains(keyword)) {
                        matchCount++;
                        break;
                    }
                }
            }

            if (matchCount > 0) {
                score += Math.min(60, matchCount * 15);
            }
        } else {
            score += 30;
        }

        if (criterios.localizacao() != null) {
            if (perfil.getEndereco() != null &&
                perfil.getEndereco().cidade().toLowerCase().contains(criterios.localizacao())) {
                score += 20;
            }
        } else {
            score += 10;
        }

        if (criterios.senioridade() != null) {
            var experiencias = experienciaRepository.listByUsuario(usuario.getId());

            for (var exp : experiencias) {
                String cargo = exp.getCargo().toLowerCase();
                if (cargo.contains(criterios.senioridade().toLowerCase())) {
                    score += 20;
                    break;
                }
            }
        } else {
            score += 10;
        }

        return Math.min(score, 100);
    }

    
    public record CriteriosBusca(
            List<String> keywords,
            String localizacao,
            String senioridade
    ) {}

    
    public record CandidatoComScore(
            Usuario usuario,
            PerfilCandidato perfil,
            int score,
            String resumo
    ) {}

    
    public record ResultadoPaginado(
            List<CandidatoComScore> content,
            int currentPage,
            int totalPages,
            long totalElements,
            int size
    ) {}
}
