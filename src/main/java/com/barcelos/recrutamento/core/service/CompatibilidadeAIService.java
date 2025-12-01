package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CompatibilidadeAIService {

    private static final Logger log = LoggerFactory.getLogger(CompatibilidadeAIService.class);

    private final ChatClient chatClient;
    private final PerfilCandidatoRepository perfilCandidatoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ExperienciaProfissionalRepository experienciaRepository;
    private final HistoricoAcademicoRepository historicoAcademicoRepository;
    private final UsuarioRepository usuarioRepository;

    public CompatibilidadeAIService(
            ChatClient chatClient,
            PerfilCandidatoRepository perfilCandidatoRepository,
            CompetenciaRepository competenciaRepository,
            ExperienciaProfissionalRepository experienciaRepository,
            HistoricoAcademicoRepository historicoAcademicoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.chatClient = chatClient;
        this.perfilCandidatoRepository = perfilCandidatoRepository;
        this.competenciaRepository = competenciaRepository;
        this.experienciaRepository = experienciaRepository;
        this.historicoAcademicoRepository = historicoAcademicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    
    public ResultadoCompatibilidade calcularCompatibilidade(UUID candidatoUsuarioId, Vaga vaga) {

        String perfilCandidato = construirPerfilCandidato(candidatoUsuarioId);

        String prompt = construirPrompt(perfilCandidato, vaga);

        try {

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseResposta(response);
        } catch (Exception e) {
            log.error("Erro ao calcular compatibilidade com IA: {}", e.getMessage(), e);

            return new ResultadoCompatibilidade(50, "Erro ao processar análise com IA: " + e.getMessage());
        }
    }

    private String construirPerfilCandidato(UUID candidatoUsuarioId) {
        StringBuilder perfil = new StringBuilder();

        var usuario = usuarioRepository.findById(candidatoUsuarioId).orElse(null);
        if (usuario != null) {
            perfil.append("Nome: ").append(usuario.getNome()).append("\n");
        }

        var perfilOpt = perfilCandidatoRepository.findByUsuarioId(candidatoUsuarioId);
        if (perfilOpt.isPresent()) {
            var competencias = competenciaRepository.listByPerfilCandidato(perfilOpt.get().getUsuarioId());
            if (!competencias.isEmpty()) {
                perfil.append("\nCompetências:\n");
                competencias.forEach(c ->
                        perfil.append("- ").append(c.getTitulo())
                              .append(" (Nível: ").append(c.getNivel()).append(")\n")
                );
            }
        }

        var experiencias = experienciaRepository.listByUsuario(candidatoUsuarioId);
        if (!experiencias.isEmpty()) {
            perfil.append("\nExperiências Profissionais:\n");
            experiencias.forEach(exp -> {
                perfil.append("- ").append(exp.getCargo())
                      .append(" na ").append(exp.getEmpresa());

                if (exp.getDescricao() != null && !exp.getDescricao().isBlank()) {
                    perfil.append(": ").append(exp.getDescricao());
                }
                perfil.append("\n");
            });
        }

        var historicos = historicoAcademicoRepository.listByUsuario(candidatoUsuarioId);
        if (!historicos.isEmpty()) {
            perfil.append("\nFormação Acadêmica:\n");
            historicos.forEach(h ->
                    perfil.append("- ").append(h.getTitulo())
                          .append(" - ").append(h.getInstituicao()).append("\n")
            );
        }

        return perfil.toString();
    }

    private String construirPrompt(String perfilCandidato, Vaga vaga) {
        return """
                Você é um especialista em recrutamento e seleção. Analise a compatibilidade entre o perfil do candidato e os requisitos da vaga.

                PERFIL DO CANDIDATO:
                %s

                VAGA:
                Título: %s
                Descrição: %s
                Requisitos: %s
                Tipo de Contrato: %s
                Modalidade: %s

                TAREFA:
                1. Analise a compatibilidade entre o perfil do candidato e a vaga
                2. Considere: competências técnicas, experiência, formação, alinhamento com requisitos
                3. Retorne APENAS no seguinte formato (sem markdown, sem formatação extra):

                SCORE: [número de 0 a 100]
                JUSTIFICATIVA: [explicação clara e objetiva em 2-3 frases sobre a compatibilidade]

                Seja objetivo e analítico. O score deve refletir o quão adequado o candidato é para a vaga.
                """.formatted(
                perfilCandidato,
                vaga.getTitulo(),
                vaga.getDescricao(),
                vaga.getRequisitos(),
                vaga.getTipoContrato() != null ? vaga.getTipoContrato().name() : "N/A",
                vaga.getModalidade() != null ? vaga.getModalidade().name() : "N/A"
        );
    }

    private ResultadoCompatibilidade parseResposta(String response) {
        try {

            Pattern scorePattern = Pattern.compile("SCORE:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher scoreMatcher = scorePattern.matcher(response);

            int score = 50;
            if (scoreMatcher.find()) {
                score = Math.min(100, Math.max(0, Integer.parseInt(scoreMatcher.group(1))));
            }

            Pattern justPattern = Pattern.compile("JUSTIFICATIVA:\\s*(.+?)(?=\\n\\n|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher justMatcher = justPattern.matcher(response);

            String justificativa = "Análise realizada com sucesso.";
            if (justMatcher.find()) {
                justificativa = justMatcher.group(1).trim();
            }

            return new ResultadoCompatibilidade(score, justificativa);
        } catch (Exception e) {
            log.error("Erro ao fazer parse da resposta da IA: {}", e.getMessage(), e);
            return new ResultadoCompatibilidade(50, response);
        }
    }

    
    public record ResultadoCompatibilidade(int score, String justificativa) {}
}
