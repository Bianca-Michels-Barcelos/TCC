package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.port.*;
import com.barcelos.recrutamento.data.entity.NivelCompetencia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class CurriculoService {

    private static final Logger log = LoggerFactory.getLogger(CurriculoService.class);

    private final UsuarioRepository usuarioRepository;
    private final PerfilCandidatoRepository perfilCandidatoRepository;
    private final ExperienciaProfissionalRepository experienciaRepository;
    private final HistoricoAcademicoRepository historicoRepository;
    private final PortfolioRepository portfolioRepository;
    private final CompetenciaRepository competenciaRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final CurriculoAIService curriculoAIService;
    private final VagaRepository vagaRepository;
    private final CurriculoPDFService curriculoPDFService;

    @Value("${app.curriculos.diretorio:./storage/curriculos}")
    private String diretorioCurriculos;

    public CurriculoService(
            UsuarioRepository usuarioRepository,
            PerfilCandidatoRepository perfilCandidatoRepository,
            ExperienciaProfissionalRepository experienciaRepository,
            HistoricoAcademicoRepository historicoRepository,
            PortfolioRepository portfolioRepository,
            CompetenciaRepository competenciaRepository,
            CandidaturaRepository candidaturaRepository,
            CurriculoAIService curriculoAIService,
            VagaRepository vagaRepository,
            CurriculoPDFService curriculoPDFService) {
        this.usuarioRepository = usuarioRepository;
        this.perfilCandidatoRepository = perfilCandidatoRepository;
        this.experienciaRepository = experienciaRepository;
        this.historicoRepository = historicoRepository;
        this.portfolioRepository = portfolioRepository;
        this.competenciaRepository = competenciaRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.curriculoAIService = curriculoAIService;
        this.vagaRepository = vagaRepository;
        this.curriculoPDFService = curriculoPDFService;
    }

    
    @Async("curriculoTaskExecutor")
    @Transactional
    public CompletableFuture<String> gerarEAtualizarCurriculo(UUID candidaturaId) {
        log.info("Iniciando geração de currículo para candidatura {}", candidaturaId);

        try {

            Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidatura", candidaturaId));

            UUID candidatoUsuarioId = candidatura.getCandidatoUsuarioId();

            Usuario usuario = usuarioRepository.findById(candidatoUsuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", candidatoUsuarioId));

            Optional<PerfilCandidato> perfilOpt = perfilCandidatoRepository.findByUsuarioId(candidatoUsuarioId);

            List<ExperienciaProfissional> experiencias = experienciaRepository.listByUsuario(candidatoUsuarioId);
            List<HistoricoAcademico> formacoes = historicoRepository.listByUsuario(candidatoUsuarioId);

            List<Portfolio> portfolios = List.of();
            List<Competencia> competencias = List.of();

            if (perfilOpt.isPresent()) {
                UUID perfilId = perfilOpt.get().getUsuarioId();
                portfolios = portfolioRepository.listByPerfilCandidato(perfilId);
                competencias = competenciaRepository.listByPerfilCandidato(perfilId);
            }

            String markdown = gerarMarkdown(usuario, perfilOpt, experiencias, formacoes, portfolios, competencias);

            String caminhoRelativo = salvarArquivo(candidatoUsuarioId, markdown);

            Candidatura candidaturaAtualizada = candidatura.comArquivoCurriculo(caminhoRelativo);
            candidaturaRepository.save(candidaturaAtualizada);

            log.info("Currículo gerado com sucesso para candidatura {}: {}", candidaturaId, caminhoRelativo);

            return CompletableFuture.completedFuture(caminhoRelativo);

        } catch (Exception e) {
            log.error("Erro ao gerar currículo para candidatura {}", candidaturaId, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    
    private String gerarMarkdown(
            Usuario usuario,
            Optional<PerfilCandidato> perfilOpt,
            List<ExperienciaProfissional> experiencias,
            List<HistoricoAcademico> formacoes,
            List<Portfolio> portfolios,
            List<Competencia> competencias) {

        StringBuilder md = new StringBuilder();

        md.append("# ").append(usuario.getNome()).append("\n\n");

        md.append("**Email:** ").append(usuario.getEmail().value());
        md.append(" | **CPF:** ").append(usuario.getCpf().value());

        if (perfilOpt.isPresent()) {
            var perfil = perfilOpt.get();
            var endereco = perfil.getEndereco();
            md.append(" | **Localização:** ").append(endereco.cidade()).append(", ").append(endereco.uf().value());
            md.append("  \n**Data de Nascimento:** ").append(formatarData(perfil.getDataNascimento()));
        }

        md.append("\n\n---\n\n");

        md.append("## 📋 Experiência Profissional\n\n");
        if (experiencias.isEmpty()) {
            md.append("*Não informado*\n\n");
        } else {
            experiencias.stream()
                    .sorted(Comparator.comparing(ExperienciaProfissional::getDataInicio).reversed())
                    .forEach(exp -> {
                        md.append("### ").append(exp.getCargo()).append(" - ").append(exp.getEmpresa()).append("\n");
                        md.append("**Período:** ").append(formatarPeriodo(exp.getDataInicio(), exp.getDataFim())).append("\n\n");
                        md.append(exp.getDescricao()).append("\n\n");
                    });
        }

        md.append("---\n\n");

        md.append("## 🎓 Formação Acadêmica\n\n");
        if (formacoes.isEmpty()) {
            md.append("*Não informado*\n\n");
        } else {
            formacoes.stream()
                    .sorted(Comparator.comparing(HistoricoAcademico::getDataInicio).reversed())
                    .forEach(form -> {
                        md.append("### ").append(form.getTitulo()).append(" - ").append(form.getInstituicao()).append("\n");
                        md.append("**Período:** ").append(formatarPeriodo(form.getDataInicio(), form.getDataFim())).append("\n\n");
                        md.append(form.getDescricao()).append("\n\n");
                    });
        }

        md.append("---\n\n");

        if (!portfolios.isEmpty()) {
            md.append("## 💼 Portfólio\n\n");
            portfolios.forEach(port -> {
                md.append("- **").append(port.getTitulo()).append("**  \n");
                md.append("  Link: ").append(port.getLink()).append("\n\n");
            });
            md.append("---\n\n");
        }

        if (!competencias.isEmpty()) {
            md.append("## 🔧 Competências\n\n");
            competencias.stream()
                    .sorted(Comparator.comparing(Competencia::getNivel).reversed())
                    .forEach(comp -> {
                        md.append("- **").append(comp.getTitulo()).append("** ");
                        md.append("(Nível: ").append(formatarNivel(comp.getNivel())).append(")");
                        if (comp.getDescricao() != null && !comp.getDescricao().isBlank()) {
                            md.append(" - ").append(comp.getDescricao());
                        }
                        md.append("\n");
                    });
            md.append("\n---\n\n");
        }

        md.append("*Currículo gerado automaticamente em ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("*\n");

        return md.toString();
    }

    
    private String salvarArquivo(UUID usuarioId, String markdown) throws IOException {
        return salvarArquivo(usuarioId, markdown, "md");
    }

    
    private String salvarArquivo(UUID usuarioId, String conteudo, String extensao) throws IOException {

        Path dirPath = Paths.get(diretorioCurriculos);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            log.info("Diretório de currículos criado: {}", dirPath.toAbsolutePath());
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nomeArquivo = String.format("curriculo-%s-%s.%s", usuarioId, timestamp, extensao);

        Path arquivoPath = dirPath.resolve(nomeArquivo);

        Files.writeString(arquivoPath, conteudo, StandardCharsets.UTF_8);

        log.info("Currículo salvo em: {}", arquivoPath.toAbsolutePath());

        return diretorioCurriculos + "/" + nomeArquivo;
    }

    private String formatarData(LocalDate data) {
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatarPeriodo(LocalDate inicio, LocalDate fim) {
        String inicioStr = inicio.format(DateTimeFormatter.ofPattern("MM/yyyy"));
        String fimStr = (fim == null) ? "Atual" : fim.format(DateTimeFormatter.ofPattern("MM/yyyy"));
        return inicioStr + " - " + fimStr;
    }

    private String formatarNivel(NivelCompetencia nivel) {
        return switch (nivel) {
            case BASICO -> "Básico";
            case INTERMEDIARIO -> "Intermediário";
            case AVANCADO -> "Avançado";
        };
    }

    
    @Transactional(readOnly = true)
    public String gerarCurriculoComIA(
            UUID candidatoUsuarioId,
            UUID vagaId,
            ModeloCurriculoEnum modelo,
            String observacoes) {

        log.info("Gerando currículo com IA para candidato {} e vaga {}", candidatoUsuarioId, vagaId);

        usuarioRepository.findById(candidatoUsuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", candidatoUsuarioId));

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", vagaId));

        try {
            String curriculo = curriculoAIService.gerarCurriculoOtimizado(
                    candidatoUsuarioId,
                    vaga,
                    modelo,
                    observacoes
            );

            log.info("Currículo gerado com IA com sucesso para candidato {}", candidatoUsuarioId);
            return curriculo;

        } catch (Exception e) {
            log.error("Erro ao gerar currículo com IA para candidato {}", candidatoUsuarioId, e);
            throw new RuntimeException("Erro ao gerar currículo com IA: " + e.getMessage(), e);
        }
    }

    @Async("curriculoTaskExecutor")
    @Transactional
    public void gerarCurriculoPersonalizado(UUID candidaturaId, String modeloCurriculoStr, String conteudoPersonalizado) {
        log.info("Gerando currículo personalizado para candidatura {} com modelo {}", candidaturaId, modeloCurriculoStr);

        try {

            Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidatura", candidaturaId));

            UUID candidatoUsuarioId = candidatura.getCandidatoUsuarioId();

            ModeloCurriculoEnum modelo = modeloCurriculoStr != null 
                ? ModeloCurriculoEnum.valueOf(modeloCurriculoStr) 
                : ModeloCurriculoEnum.PROFISSIONAL;

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String nomeArquivoPDF = String.format("curriculo-%s-%s.pdf", candidatoUsuarioId, timestamp);
            String caminhoCompletoPDF = diretorioCurriculos + "/" + nomeArquivoPDF;

            File arquivoPDF = curriculoPDFService.gerarPDF(conteudoPersonalizado, modelo, caminhoCompletoPDF);

            String caminhoRelativo = diretorioCurriculos + "/" + nomeArquivoPDF;

            Candidatura candidaturaAtualizada = candidatura.comArquivoCurriculo(caminhoRelativo);
            candidaturaRepository.save(candidaturaAtualizada);

            log.info("Currículo personalizado (PDF) gerado com sucesso para candidatura {}: {}", candidaturaId, caminhoRelativo);

        } catch (Exception e) {
            log.error("Erro ao gerar currículo personalizado para candidatura {}", candidaturaId, e);

            throw new RuntimeException("Erro ao gerar currículo personalizado: " + e.getMessage(), e);
        }
    }
}
