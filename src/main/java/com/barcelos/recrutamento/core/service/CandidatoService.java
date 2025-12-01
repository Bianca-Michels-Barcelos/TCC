package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.event.PerfilCandidatoAtualizadoEvent;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import com.barcelos.recrutamento.data.entity.NivelCompetencia;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CandidatoService {

    public record AdicionarProjetoExperienciaCommand(
            UUID experienciaId,
            String nome,
            String descricao
    ) {}

    public record AdicionarProjetoExperienciaResult(
            UUID id, UUID experienciaId, String nome, String descricao
    ) {}

    public record AdicionarExperienciaCommand(
            UUID usuarioId,
            String cargo,
            String empresa,
            String descricao,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
    }

    public record AdicionarExperienciaResult(
            UUID id, UUID usuarioId, String cargo, String empresa,
            LocalDate dataInicio, LocalDate dataFim, boolean ativo
    ) {
    }

    public record AdicionarHistoricoCommand(
            UUID usuarioId,
            String titulo,
            String descricao,
            String instituicao,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
    }

    public record AdicionarHistoricoResult(
            UUID id, UUID usuarioId, String titulo, String instituicao,
            LocalDate dataInicio, LocalDate dataFim, boolean ativo
    ) {
    }

    public record RegistrarCommand(
            String nome, String cpf, String email, String senha, LocalDate dataNascimento,
            String logradouro, String numero, String complemento, String cep, String cidade, String uf
    ) {
    }

    public record RegistrarResult(UUID usuarioId, String nome, String email) {
    }

    public record AdicionarCompetenciaCommand(
            UUID perfilCandidatoId,
            String titulo,
            String descricao,
            NivelCompetencia nivel
    ) {
    }

    public record CompetenciaResult(
            UUID id,
            UUID perfilCandidatoId,
            String titulo,
            String descricao,
            NivelCompetencia nivel,
            boolean ativo
    ) {
    }

    public record AdicionarPortfolioCommand(
            UUID perfilCandidatoId,
            String titulo,
            String link
    ) {
    }

    public record PortfolioResult(
            UUID id,
            UUID perfilCandidatoId,
            String titulo,
            String link,
            boolean ativo
    ) {
    }

    public record ExperienciaResult(
            UUID id,
            UUID usuarioId,
            String cargo,
            String empresa,
            String descricao,
            LocalDate dataInicio,
            LocalDate dataFim,
            boolean ativo
    ) {
    }

    public record HistoricoResult(
            UUID id,
            UUID usuarioId,
            String titulo,
            String descricao,
            String instituicao,
            LocalDate dataInicio,
            LocalDate dataFim,
            boolean ativo
    ) {
    }

    public record AdicionarCertificadoCommand(
            UUID perfilCandidatoId,
            String titulo,
            String instituicao,
            LocalDate dataEmissao,
            LocalDate dataValidade,
            String descricao
    ) {
    }

    public record CertificadoResult(
            UUID id,
            UUID perfilCandidatoId,
            String titulo,
            String instituicao,
            LocalDate dataEmissao,
            LocalDate dataValidade,
            String descricao,
            boolean ativo
    ) {
    }

    private final UsuarioRepository usuarioRepository;
    private final PerfilCandidatoRepository perfilCandidatoRepository;
    private final HistoricoAcademicoRepository historicoAcademicoRepository;
    private final ExperienciaProfissionalRepository experienciaProfissionalRepository;
    private final ProjetoExperienciaRepository projetoExperienciaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final CertificadoRepository certificadoRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompatibilidadeCacheService compatibilidadeCacheService;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    public CandidatoService(
            UsuarioRepository usuarioRepository,
            PerfilCandidatoRepository perfilCandidatoRepository,
            HistoricoAcademicoRepository historicoAcademicoRepository,
            ExperienciaProfissionalRepository experienciaProfissionalRepository,
            ProjetoExperienciaRepository projetoExperienciaRepository,
            CompetenciaRepository competenciaRepository,
            CertificadoRepository certificadoRepository,
            PortfolioRepository portfolioRepository,
            PasswordEncoder passwordEncoder,
            CompatibilidadeCacheService compatibilidadeCacheService,
            ApplicationEventPublisher eventPublisher,
            EmailService emailService,
            EmailTemplateService emailTemplateService) {
        this.usuarioRepository = usuarioRepository;
        this.perfilCandidatoRepository = perfilCandidatoRepository;
        this.historicoAcademicoRepository = historicoAcademicoRepository;
        this.experienciaProfissionalRepository = experienciaProfissionalRepository;
        this.projetoExperienciaRepository = projetoExperienciaRepository;
        this.competenciaRepository = competenciaRepository;
        this.certificadoRepository = certificadoRepository;
        this.portfolioRepository = portfolioRepository;
        this.passwordEncoder = passwordEncoder;
        this.compatibilidadeCacheService = compatibilidadeCacheService;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Transactional
    public RegistrarResult registrar(RegistrarCommand cmd) {
        var senhaHash = passwordEncoder.encode(cmd.senha);
        var usuario = Usuario.novo(cmd.nome, new Email(cmd.email), new Cpf(cmd.cpf), senhaHash);
        usuario = usuarioRepository.save(usuario);

        var endereco = new Endereco(cmd.logradouro, cmd.complemento, cmd.numero, new Cep(cmd.cep), cmd.cidade, new Sigla(cmd.uf));
        var perfil = PerfilCandidato.novo(usuario.getId(), cmd.dataNascimento, endereco);
        perfilCandidatoRepository.save(perfil);

        compatibilidadeCacheService.calcularParaTodasVagas(usuario.getId());

        try {
            String htmlContent = emailTemplateService.renderConfirmacaoCadastro(
                    usuario.getNome(),
                    usuario.getEmail().value()
            );
            emailService.sendHtmlEmailAsync(
                    usuario.getEmail().value(),
                    "Bem-vindo(a) ao Sistema de Recrutamento!",
                    htmlContent
            );
        } catch (Exception e) {

            org.slf4j.LoggerFactory.getLogger(CandidatoService.class)
                    .error("Erro ao enviar email de confirmação para {}: {}", usuario.getEmail().value(), e.getMessage());
        }

        return new RegistrarResult(usuario.getId(), usuario.getNome(), usuario.getEmail().value());
    }

    @Transactional
    public AdicionarHistoricoResult adicionarHistorico(AdicionarHistoricoCommand cmd) {
        usuarioRepository.findById(cmd.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", cmd.usuarioId()));

        var historico = HistoricoAcademico.novo(
                UUID.randomUUID(),
                cmd.usuarioId(),
                cmd.titulo(),
                cmd.descricao(),
                cmd.instituicao(),
                cmd.dataInicio(),
                cmd.dataFim()
        );
        var saved = historicoAcademicoRepository.save(historico);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(cmd.usuarioId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(cmd.usuarioId()));
        
        return new AdicionarHistoricoResult(
                saved.getId(), saved.getUsuarioId(), saved.getTitulo(), saved.getInstituicao(),
                saved.getDataInicio(), saved.getDataFim(), saved.isAtivo()
        );
    }

    @Transactional
    public AdicionarExperienciaResult adicionarExperiencia(AdicionarExperienciaCommand cmd) {
        usuarioRepository.findById(cmd.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", cmd.usuarioId()));
        var exp = ExperienciaProfissional.novo(
                UUID.randomUUID(),
                cmd.usuarioId(),
                cmd.cargo(),
                cmd.empresa(),
                cmd.descricao(),
                cmd.dataInicio(),
                cmd.dataFim()
        );
        var saved = experienciaProfissionalRepository.save(exp);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(cmd.usuarioId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(cmd.usuarioId()));
        
        return new AdicionarExperienciaResult(
                saved.getId(), saved.getUsuarioId(), saved.getCargo(), saved.getEmpresa(),
                saved.getDataInicio(), saved.getDataFim(), saved.isAtivo()
        );
    }

    @Transactional
    public AdicionarProjetoExperienciaResult adicionarProjeto(AdicionarProjetoExperienciaCommand cmd) {
        var projeto = ProjetoExperiencia.novo(
                UUID.randomUUID(),
                cmd.experienciaId(),
                cmd.nome(),
                cmd.descricao()
        );
        var salvo = projetoExperienciaRepository.save(projeto);
        return new AdicionarProjetoExperienciaResult(
                salvo.getId(), salvo.getExperienciaProfissionalId(), salvo.getNome(), salvo.getDescricao()
        );
    }

    @Transactional
    public CompetenciaResult adicionarCompetencia(AdicionarCompetenciaCommand cmd) {
        var competencia = Competencia.nova(
                cmd.perfilCandidatoId(),
                cmd.titulo(),
                cmd.descricao(),
                cmd.nivel()
        );
        var salva = competenciaRepository.save(competencia);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(cmd.perfilCandidatoId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(cmd.perfilCandidatoId()));
        
        return new CompetenciaResult(
                salva.getId(),
                salva.getPerfilCandidatoId(),
                salva.getTitulo(),
                salva.getDescricao(),
                salva.getNivel(),
                salva.isAtivo()
        );
    }

    @Transactional(readOnly = true)
    public List<CompetenciaResult> listarCompetencias(UUID perfilCandidatoId) {
        return competenciaRepository.listByPerfilCandidato(perfilCandidatoId).stream()
                .map(c -> new CompetenciaResult(
                        c.getId(),
                        c.getPerfilCandidatoId(),
                        c.getTitulo(),
                        c.getDescricao(),
                        c.getNivel(),
                        c.isAtivo()
                ))
                .toList();
    }

    @Transactional
    public CompetenciaResult atualizarCompetencia(UUID competenciaId, String titulo, String descricao, NivelCompetencia nivel) {
        var competencia = competenciaRepository.findById(competenciaId)
                .orElseThrow(() -> new RuntimeException("Competência não encontrada"));

        var atualizada = competencia
                .comTitulo(titulo)
                .comDescricao(descricao)
                .comNivel(nivel);

        var salva = competenciaRepository.save(atualizada);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(salva.getPerfilCandidatoId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(salva.getPerfilCandidatoId()));
        
        return new CompetenciaResult(
                salva.getId(),
                salva.getPerfilCandidatoId(),
                salva.getTitulo(),
                salva.getDescricao(),
                salva.getNivel(),
                salva.isAtivo()
        );
    }

    @Transactional
    public void removerCompetencia(UUID competenciaId) {
        var competencia = competenciaRepository.findById(competenciaId)
                .orElseThrow(() -> new RuntimeException("Competência não encontrada"));
        
        UUID perfilId = competencia.getPerfilCandidatoId();
        competenciaRepository.delete(competenciaId);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(perfilId);
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(perfilId));
    }

    @Transactional(readOnly = true)
    public List<ExperienciaResult> listarExperiencias(UUID usuarioId) {
        return experienciaProfissionalRepository.listByUsuario(usuarioId).stream()
                .map(exp -> new ExperienciaResult(
                        exp.getId(),
                        exp.getUsuarioId(),
                        exp.getCargo(),
                        exp.getEmpresa(),
                        exp.getDescricao(),
                        exp.getDataInicio(),
                        exp.getDataFim(),
                        exp.isAtivo()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoResult> listarHistoricos(UUID usuarioId) {
        return historicoAcademicoRepository.listByUsuario(usuarioId).stream()
                .map(hist -> new HistoricoResult(
                        hist.getId(),
                        hist.getUsuarioId(),
                        hist.getTitulo(),
                        hist.getDescricao(),
                        hist.getInstituicao(),
                        hist.getDataInicio(),
                        hist.getDataFim(),
                        hist.isAtivo()
                ))
                .toList();
    }

    @Transactional
    public PortfolioResult adicionarPortfolio(AdicionarPortfolioCommand cmd) {
        var portfolio = Portfolio.novo(
                cmd.perfilCandidatoId(),
                cmd.titulo(),
                cmd.link()
        );
        var salvo = portfolioRepository.save(portfolio);
        return new PortfolioResult(
                salvo.getId(),
                salvo.getUsuarioId(),
                salvo.getTitulo(),
                salvo.getLink(),
                salvo.isAtivo()
        );
    }

    @Transactional(readOnly = true)
    public List<PortfolioResult> listarPortfolios(UUID perfilCandidatoId) {
        return portfolioRepository.listByPerfilCandidato(perfilCandidatoId).stream()
                .map(p -> new PortfolioResult(
                        p.getId(),
                        p.getUsuarioId(),
                        p.getTitulo(),
                        p.getLink(),
                        p.isAtivo()
                ))
                .toList();
    }

    @Transactional
    public CertificadoResult adicionarCertificado(AdicionarCertificadoCommand cmd) {
        var certificado = Certificado.novo(
                cmd.perfilCandidatoId(),
                cmd.titulo(),
                cmd.instituicao(),
                cmd.dataEmissao(),
                cmd.dataValidade(),
                cmd.descricao()
        );
        var salvo = certificadoRepository.save(certificado);
        return new CertificadoResult(
                salvo.getId(),
                salvo.getPerfilCandidatoId(),
                salvo.getTitulo(),
                salvo.getInstituicao(),
                salvo.getDataEmissao(),
                salvo.getDataValidade(),
                salvo.getDescricao(),
                salvo.isAtivo()
        );
    }

    @Transactional(readOnly = true)
    public List<CertificadoResult> listarCertificados(UUID perfilCandidatoId) {
        return certificadoRepository.listByPerfilCandidato(perfilCandidatoId).stream()
                .map(c -> new CertificadoResult(
                        c.getId(),
                        c.getPerfilCandidatoId(),
                        c.getTitulo(),
                        c.getInstituicao(),
                        c.getDataEmissao(),
                        c.getDataValidade(),
                        c.getDescricao(),
                        c.isAtivo()
                ))
                .toList();
    }

    @Transactional
    public CertificadoResult atualizarCertificado(UUID certificadoId, String titulo, String instituicao,
                                                  LocalDate dataEmissao, LocalDate dataValidade, String descricao) {
        var certificado = certificadoRepository.findById(certificadoId)
                .orElseThrow(() -> new RuntimeException("Certificado não encontrado"));

        var atualizado = certificado
                .comTitulo(titulo)
                .comInstituicao(instituicao)
                .comDataEmissao(dataEmissao)
                .comDataValidade(dataValidade)
                .comDescricao(descricao);

        var salvo = certificadoRepository.save(atualizado);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(salvo.getPerfilCandidatoId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(salvo.getPerfilCandidatoId()));
        
        return new CertificadoResult(
                salvo.getId(),
                salvo.getPerfilCandidatoId(),
                salvo.getTitulo(),
                salvo.getInstituicao(),
                salvo.getDataEmissao(),
                salvo.getDataValidade(),
                salvo.getDescricao(),
                salvo.isAtivo()
        );
    }

    @Transactional
    public void removerCertificado(UUID certificadoId) {
        var certificado = certificadoRepository.findById(certificadoId)
                .orElseThrow(() -> new RuntimeException("Certificado não encontrado"));
        
        UUID perfilId = certificado.getPerfilCandidatoId();
        certificadoRepository.delete(certificadoId);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(perfilId);
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(perfilId));
    }

    @Transactional
    public PortfolioResult atualizarPortfolio(UUID portfolioId, String titulo, String link) {
        var portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfólio não encontrado"));

        var atualizado = portfolio
                .comTitulo(titulo)
                .comLink(link);

        var salvo = portfolioRepository.save(atualizado);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(salvo.getUsuarioId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(salvo.getUsuarioId()));
        
        return new PortfolioResult(
                salvo.getId(),
                salvo.getUsuarioId(),
                salvo.getTitulo(),
                salvo.getLink(),
                salvo.isAtivo()
        );
    }

    @Transactional
    public void removerPortfolio(UUID portfolioId) {
        var portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfólio não encontrado"));
        
        UUID usuarioId = portfolio.getUsuarioId();
        portfolioRepository.delete(portfolioId);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(usuarioId);
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(usuarioId));
    }

    @Transactional
    public ExperienciaResult atualizarExperiencia(UUID experienciaId, String cargo, String empresa, 
                                                  String descricao, LocalDate dataInicio, LocalDate dataFim) {
        var experiencia = experienciaProfissionalRepository.findById(experienciaId)
                .orElseThrow(() -> new RuntimeException("Experiência não encontrada"));

        var atualizada = experiencia
                .comCargo(cargo)
                .comEmpresa(empresa)
                .comDescricao(descricao)
                .comDataInicio(dataInicio)
                .comDataFim(dataFim);

        var salva = experienciaProfissionalRepository.save(atualizada);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(salva.getUsuarioId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(salva.getUsuarioId()));
        
        return new ExperienciaResult(
                salva.getId(),
                salva.getUsuarioId(),
                salva.getCargo(),
                salva.getEmpresa(),
                salva.getDescricao(),
                salva.getDataInicio(),
                salva.getDataFim(),
                salva.isAtivo()
        );
    }

    @Transactional
    public HistoricoResult atualizarHistorico(UUID historicoId, String titulo, String instituicao,
                                             String descricao, LocalDate dataInicio, LocalDate dataFim) {
        var historico = historicoAcademicoRepository.findById(historicoId)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));

        var atualizado = historico
                .comTitulo(titulo)
                .comInstituicao(instituicao)
                .comDescricao(descricao)
                .comDataInicio(dataInicio)
                .comDataFim(dataFim);

        var salvo = historicoAcademicoRepository.save(atualizado);
        

        compatibilidadeCacheService.invalidarCacheCandidatoSync(salvo.getUsuarioId());
        eventPublisher.publishEvent(new PerfilCandidatoAtualizadoEvent(salvo.getUsuarioId()));
        
        return new HistoricoResult(
                salvo.getId(),
                salvo.getUsuarioId(),
                salvo.getTitulo(),
                salvo.getDescricao(),
                salvo.getInstituicao(),
                salvo.getDataInicio(),
                salvo.getDataFim(),
                salvo.isAtivo()
        );
    }

}
