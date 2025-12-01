package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.ModeloCurriculoEnum;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.model.VagaExterna;
import com.barcelos.recrutamento.core.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurriculoAIService {

    private static final Logger log = LoggerFactory.getLogger(CurriculoAIService.class);

    private final ChatClient chatClient;
    private final PerfilCandidatoRepository perfilCandidatoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ExperienciaProfissionalRepository experienciaRepository;
    private final HistoricoAcademicoRepository historicoAcademicoRepository;
    private final PortfolioRepository portfolioRepository;
    private final UsuarioRepository usuarioRepository;

    public CurriculoAIService(
            ChatClient chatClient,
            PerfilCandidatoRepository perfilCandidatoRepository,
            CompetenciaRepository competenciaRepository,
            ExperienciaProfissionalRepository experienciaRepository,
            HistoricoAcademicoRepository historicoAcademicoRepository,
            PortfolioRepository portfolioRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.chatClient = chatClient;
        this.perfilCandidatoRepository = perfilCandidatoRepository;
        this.competenciaRepository = competenciaRepository;
        this.experienciaRepository = experienciaRepository;
        this.historicoAcademicoRepository = historicoAcademicoRepository;
        this.portfolioRepository = portfolioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    
    public String gerarCurriculoOtimizado(
            UUID candidatoUsuarioId,
            Vaga vaga,
            ModeloCurriculoEnum modelo,
            String observacoes
    ) {

        String dadosCandidato = coletarDadosCandidato(candidatoUsuarioId);

        String prompt = construirPrompt(dadosCandidato, vaga, modelo, observacoes);

        try {

            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Erro ao gerar currículo com IA: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar currículo com IA: " + e.getMessage(), e);
        }
    }

    public String gerarCurriculoOtimizadoParaVagaExterna(
            VagaExterna vagaExterna,
            ModeloCurriculoEnum modelo,
            String observacoes
    ) {

        String dadosCandidato = coletarDadosCandidato(vagaExterna.getCandidatoUsuarioId());

        String prompt = construirPromptParaVagaExterna(dadosCandidato, vagaExterna, modelo, observacoes);

        try {

            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Erro ao gerar currículo com IA: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar currículo com IA: " + e.getMessage(), e);
        }
    }

    private String coletarDadosCandidato(UUID candidatoUsuarioId) {
        StringBuilder dados = new StringBuilder();

        var usuario = usuarioRepository.findById(candidatoUsuarioId).orElse(null);
        if (usuario != null) {
            dados.append("DADOS PESSOAIS:\n");
            dados.append("Nome: ").append(usuario.getNome()).append("\n");
            dados.append("Email: ").append(usuario.getEmail().value()).append("\n\n");
        }

        var perfilOpt = perfilCandidatoRepository.findByUsuarioId(candidatoUsuarioId);
        if (perfilOpt.isPresent()) {
            var perfil = perfilOpt.get();
            if (perfil.getEndereco() != null) {
                dados.append("Cidade: ").append(perfil.getEndereco().cidade()).append("\n\n");
            }

            var competencias = competenciaRepository.listByPerfilCandidato(perfil.getUsuarioId());
            if (!competencias.isEmpty()) {
                dados.append("COMPETÊNCIAS:\n");
                competencias.forEach(c ->
                        dados.append("- ").append(c.getTitulo())
                              .append(" (Nível: ").append(c.getNivel()).append(")\n")
                );
                dados.append("\n");
            }

            var portfolios = portfolioRepository.listByPerfilCandidato(perfil.getUsuarioId());
            if (!portfolios.isEmpty()) {
                dados.append("PORTFÓLIO:\n");
                portfolios.forEach(p ->
                        dados.append("- ").append(p.getTitulo())
                              .append(": ").append(p.getLink()).append("\n")
                );
                dados.append("\n");
            }
        }

        var experiencias = experienciaRepository.listByUsuario(candidatoUsuarioId);
        if (!experiencias.isEmpty()) {
            dados.append("EXPERIÊNCIA PROFISSIONAL:\n");
            experiencias.forEach(exp -> {
                dados.append("Cargo: ").append(exp.getCargo()).append("\n");
                dados.append("Empresa: ").append(exp.getEmpresa()).append("\n");
                dados.append("Período: ").append(exp.getDataInicio());
                if (exp.getDataFim() != null) {
                    dados.append(" a ").append(exp.getDataFim());
                } else {
                    dados.append(" até o momento");
                }
                dados.append("\n");

                if (exp.getDescricao() != null && !exp.getDescricao().isBlank()) {
                    dados.append("Descrição: ").append(exp.getDescricao()).append("\n");
                }
                dados.append("\n");
            });
        }

        var historicos = historicoAcademicoRepository.listByUsuario(candidatoUsuarioId);
        if (!historicos.isEmpty()) {
            dados.append("FORMAÇÃO ACADÊMICA:\n");
            historicos.forEach(h -> {
                dados.append("Curso: ").append(h.getTitulo()).append("\n");
                dados.append("Instituição: ").append(h.getInstituicao()).append("\n");
                dados.append("Período: ").append(h.getDataInicio());
                if (h.getDataFim() != null) {
                    dados.append(" a ").append(h.getDataFim());
                }
                dados.append("\n\n");
            });
        }

        return dados.toString();
    }

    private String construirPrompt(
            String dadosCandidato,
            Vaga vaga,
            ModeloCurriculoEnum modelo,
            String observacoes
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um especialista em elaboração de currículos profissionais.\n\n");

        prompt.append("TAREFA: Crie um currículo otimizado no formato ").append(modelo.getNome())
              .append(" (").append(modelo.getDescricao()).append(").\n\n");

        prompt.append("DADOS DO CANDIDATO:\n");
        prompt.append(dadosCandidato).append("\n");

        prompt.append("VAGA ALVO:\n");
        prompt.append("Título: ").append(vaga.getTitulo()).append("\n");
        prompt.append("Descrição: ").append(vaga.getDescricao()).append("\n");
        prompt.append("Requisitos: ").append(vaga.getRequisitos()).append("\n\n");

        if (observacoes != null && !observacoes.isBlank()) {
            prompt.append("OBSERVAÇÕES DO CANDIDATO:\n");
            prompt.append(observacoes).append("\n\n");
        }

        prompt.append("INSTRUÇÕES:\n");
        prompt.append("IMPORTANTE: Nunca gere informações inexistentes nos dados do candidato.\n");
        prompt.append("1. Destaque as experiências e competências mais relevantes para a vaga\n");
        prompt.append("2. Use verbos de ação e resultados quantificáveis quando possível\n");
        prompt.append("3. Organize as informações de forma clara e profissional\n");
        prompt.append("4. Adapte o tom e formato ao modelo ").append(modelo.getNome()).append("\n");
        prompt.append("5. O currículo deve ter entre 1-2 páginas (aproximadamente 500-800 palavras)\n");
        prompt.append("6. Priorize informações relevantes para a vaga específica\n\n");
        
        prompt.append("FORMATO DE SAÍDA:\n");
        prompt.append("- Gere o currículo em HTML válido\n");
        prompt.append("- Use tags semânticas: <h1>, <h2>, <h3>, <p>, <ul>, <li>, <strong>, <em>\n");
        prompt.append("- NÃO use Markdown (**, *, #, ---, etc.)\n");
        prompt.append("- NÃO inclua tags <html>, <head> ou <body> - apenas o conteúdo\n");
        prompt.append("- Use <h1> para o nome do candidato\n");
        prompt.append("- Use <h2> para seções principais (Objetivo, Experiência, Formação, etc.)\n");
        prompt.append("- Use <h3> para subtítulos (nome de empresas, cargos, etc.)\n");
        prompt.append("- Use <strong> para destacar informações importantes\n");
        prompt.append("- Use <ul> e <li> para listas\n\n");

        prompt.append("Gere o currículo completo em HTML agora:");

        return prompt.toString();
    }

    private String construirPromptParaVagaExterna(
            String dadosCandidato,
            VagaExterna vaga,
            ModeloCurriculoEnum modelo,
            String observacoes
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um especialista em elaboração de currículos profissionais.\n\n");

        prompt.append("TAREFA: Crie um currículo otimizado no formato ").append(modelo.getNome())
                .append(" (").append(modelo.getDescricao()).append(").\n\n");

        prompt.append("DADOS DO CANDIDATO:\n");
        prompt.append(dadosCandidato).append("\n");

        prompt.append("VAGA ALVO:\n");
        prompt.append("Título: ").append(vaga.getTitulo()).append("\n");
        prompt.append("Descrição: ").append(vaga.getDescricao()).append("\n");
        prompt.append("Requisitos: ").append(vaga.getRequisitos()).append("\n\n");

        if (observacoes != null && !observacoes.isBlank()) {
            prompt.append("OBSERVAÇÕES DO CANDIDATO:\n");
            prompt.append(observacoes).append("\n\n");
        }

        prompt.append("INSTRUÇÕES:\n");
        prompt.append("IMPORTANTE: Nunca gere informações inexistentes nos dados do candidato.\n");
        prompt.append("1. Destaque as experiências e competências mais relevantes para a vaga\n");
        prompt.append("2. Use verbos de ação e resultados quantificáveis quando possível\n");
        prompt.append("3. Organize as informações de forma clara e profissional\n");
        prompt.append("4. Adapte o tom e formato ao modelo ").append(modelo.getNome()).append("\n");
        prompt.append("5. O currículo deve ter entre 1-2 páginas (aproximadamente 500-800 palavras)\n");
        prompt.append("6. Priorize informações relevantes para a vaga específica\n\n");
        
        prompt.append("FORMATO DE SAÍDA:\n");
        prompt.append("- Gere o currículo em HTML válido\n");
        prompt.append("- Use tags semânticas: <h1>, <h2>, <h3>, <p>, <ul>, <li>, <strong>, <em>\n");
        prompt.append("- NÃO use Markdown (**, *, #, ---, etc.)\n");
        prompt.append("- NÃO inclua tags <html>, <head> ou <body> - apenas o conteúdo\n");
        prompt.append("- Use <h1> para o nome do candidato\n");
        prompt.append("- Use <h2> para seções principais (Objetivo, Experiência, Formação, etc.)\n");
        prompt.append("- Use <h3> para subtítulos (nome de empresas, cargos, etc.)\n");
        prompt.append("- Use <strong> para destacar informações importantes\n");
        prompt.append("- Use <ul> e <li> para listas\n\n");

        prompt.append("Gere o currículo completo em HTML agora:");

        return prompt.toString();
    }
}
