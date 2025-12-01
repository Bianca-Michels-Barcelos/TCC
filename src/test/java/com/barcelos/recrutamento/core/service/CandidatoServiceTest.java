package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import com.barcelos.recrutamento.data.entity.NivelCompetencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidatoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @Mock
    private HistoricoAcademicoRepository historicoAcademicoRepository;

    @Mock
    private ExperienciaProfissionalRepository experienciaProfissionalRepository;

    @Mock
    private ProjetoExperienciaRepository projetoExperienciaRepository;

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private CertificadoRepository certificadoRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CompatibilidadeCacheService compatibilidadeCacheService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private CandidatoService service;

    private UUID usuarioId;
    private UUID perfilId;
    private Usuario usuario;
    private PerfilCandidato perfil;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        perfilId = UUID.randomUUID();

        usuario = Usuario.rehydrate(
            usuarioId,
            "João Silva",
            new Email("joao@example.com"),
            new Cpf("12345678901"),
            "$2a$10$hashedPassword",
            true,
            true
        );

        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        perfil = PerfilCandidato.rehydrate(
            perfilId,
            usuarioId,
            LocalDate.of(1990, 1, 1),
            endereco,
            true
        );
    }

    @Test
    void deveRegistrarCandidatoComSucesso() {
        CandidatoService.RegistrarCommand cmd = new CandidatoService.RegistrarCommand(
            "João Silva",
            "12345678901",
            "joao@example.com",
            "senha123",
            LocalDate.of(1990, 1, 1),
            "Rua Teste",
            "100",
            null,
            "01310100",
            "São Paulo",
            "SP"
        );

        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(perfilCandidatoRepository.save(any(PerfilCandidato.class))).thenReturn(perfil);
        when(emailTemplateService.renderConfirmacaoCadastro(any(), any())).thenReturn("<html>Email</html>");

        CandidatoService.RegistrarResult resultado = service.registrar(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.nome()).isEqualTo("João Silva");
        assertThat(resultado.email()).isEqualTo("joao@example.com");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(perfilCandidatoRepository).save(any(PerfilCandidato.class));
        verify(compatibilidadeCacheService).calcularParaTodasVagas(usuarioId);
        verify(emailService).sendHtmlEmailAsync(eq("joao@example.com"), any(), any());
    }

    @Test
    void deveAdicionarHistoricoAcademico() {
        CandidatoService.AdicionarHistoricoCommand cmd = new CandidatoService.AdicionarHistoricoCommand(
            usuarioId,
            "Ciência da Computação",
            "Bacharelado em CC",
            "Universidade Federal",
            LocalDate.of(2015, 1, 1),
            LocalDate.of(2019, 12, 31)
        );

        HistoricoAcademico historico = HistoricoAcademico.rehydrate(
            UUID.randomUUID(),
            usuarioId,
            "Ciência da Computação",
            "Bacharelado em CC",
            "Universidade Federal",
            LocalDate.of(2015, 1, 1),
            LocalDate.of(2019, 12, 31),
            true
        );

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(historicoAcademicoRepository.save(any(HistoricoAcademico.class))).thenReturn(historico);

        CandidatoService.AdicionarHistoricoResult resultado = service.adicionarHistorico(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("Ciência da Computação");
        assertThat(resultado.instituicao()).isEqualTo("Universidade Federal");
        verify(historicoAcademicoRepository).save(any(HistoricoAcademico.class));
        verify(compatibilidadeCacheService).invalidarCacheCandidatoSync(usuarioId);
        verify(eventPublisher).publishEvent(any(com.barcelos.recrutamento.core.event.PerfilCandidatoAtualizadoEvent.class));
    }

    @Test
    void naoDeveAdicionarHistoricoQuandoUsuarioNaoExiste() {
        CandidatoService.AdicionarHistoricoCommand cmd = new CandidatoService.AdicionarHistoricoCommand(
            usuarioId, "Título", "Desc", "Instituição", LocalDate.now(), null
        );

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adicionarHistorico(cmd))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Usuário");

        verify(historicoAcademicoRepository, never()).save(any());
    }

    @Test
    void deveAdicionarExperienciaProfissional() {
        CandidatoService.AdicionarExperienciaCommand cmd = new CandidatoService.AdicionarExperienciaCommand(
            usuarioId,
            "Desenvolvedor Java",
            "Empresa XYZ",
            "Desenvolvimento de APIs",
            LocalDate.of(2020, 1, 1),
            null
        );

        ExperienciaProfissional experiencia = ExperienciaProfissional.rehydrate(
            UUID.randomUUID(),
            usuarioId,
            "Desenvolvedor Java",
            "Empresa XYZ",
            "Desenvolvimento de APIs",
            LocalDate.of(2020, 1, 1),
            null,
            true
        );

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(experienciaProfissionalRepository.save(any(ExperienciaProfissional.class))).thenReturn(experiencia);

        CandidatoService.AdicionarExperienciaResult resultado = service.adicionarExperiencia(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.cargo()).isEqualTo("Desenvolvedor Java");
        assertThat(resultado.empresa()).isEqualTo("Empresa XYZ");
        verify(experienciaProfissionalRepository).save(any(ExperienciaProfissional.class));
        verify(compatibilidadeCacheService).invalidarCacheCandidatoSync(usuarioId);
    }

    @Test
    void deveAdicionarProjetoExperiencia() {
        UUID experienciaId = UUID.randomUUID();

        CandidatoService.AdicionarProjetoExperienciaCommand cmd = 
            new CandidatoService.AdicionarProjetoExperienciaCommand(
                experienciaId,
                "Sistema de Vendas",
                "Sistema completo de vendas online"
            );

        ProjetoExperiencia projeto = ProjetoExperiencia.rehydrate(
            UUID.randomUUID(),
            experienciaId,
            "Sistema de Vendas",
            "Sistema completo de vendas online",
            true
        );

        when(projetoExperienciaRepository.save(any(ProjetoExperiencia.class))).thenReturn(projeto);

        CandidatoService.AdicionarProjetoExperienciaResult resultado = service.adicionarProjeto(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("Sistema de Vendas");
        verify(projetoExperienciaRepository).save(any(ProjetoExperiencia.class));
    }

    @Test
    void deveAdicionarCompetencia() {
        CandidatoService.AdicionarCompetenciaCommand cmd = new CandidatoService.AdicionarCompetenciaCommand(
            perfilId,
            "Java",
            "Linguagem de programação",
            NivelCompetencia.AVANCADO
        );

        Competencia competencia = Competencia.rehydrate(
            UUID.randomUUID(),
            perfilId,
            "Java",
            "Linguagem de programação",
            NivelCompetencia.AVANCADO,
            true
        );

        when(competenciaRepository.save(any(Competencia.class))).thenReturn(competencia);

        CandidatoService.CompetenciaResult resultado = service.adicionarCompetencia(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("Java");
        assertThat(resultado.nivel()).isEqualTo(NivelCompetencia.AVANCADO);
        verify(competenciaRepository).save(any(Competencia.class));
        verify(compatibilidadeCacheService).invalidarCacheCandidatoSync(perfilId);
    }

    @Test
    void deveListarCompetencias() {
        Competencia comp1 = Competencia.rehydrate(
            UUID.randomUUID(), perfilId, "Java", "Desc", NivelCompetencia.AVANCADO, true
        );
        Competencia comp2 = Competencia.rehydrate(
            UUID.randomUUID(), perfilId, "Python", "Desc", NivelCompetencia.INTERMEDIARIO, true
        );

        when(competenciaRepository.listByPerfilCandidato(perfilId)).thenReturn(List.of(comp1, comp2));

        List<CandidatoService.CompetenciaResult> resultado = service.listarCompetencias(perfilId);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).titulo()).isEqualTo("Java");
        assertThat(resultado.get(1).titulo()).isEqualTo("Python");
        verify(competenciaRepository).listByPerfilCandidato(perfilId);
    }

    @Test
    void deveAtualizarCompetencia() {
        UUID competenciaId = UUID.randomUUID();
        Competencia competencia = Competencia.rehydrate(
            competenciaId, perfilId, "Java", "Desc", NivelCompetencia.INTERMEDIARIO, true
        );

        when(competenciaRepository.findById(competenciaId)).thenReturn(Optional.of(competencia));
        when(competenciaRepository.save(any(Competencia.class))).thenAnswer(inv -> inv.getArgument(0));

        CandidatoService.CompetenciaResult resultado = service.atualizarCompetencia(
            competenciaId, "Java", "Nova descrição", NivelCompetencia.AVANCADO
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.nivel()).isEqualTo(NivelCompetencia.AVANCADO);
        verify(competenciaRepository).save(any(Competencia.class));
        verify(compatibilidadeCacheService).invalidarCacheCandidatoSync(perfilId);
    }

    @Test
    void deveRemoverCompetencia() {
        UUID competenciaId = UUID.randomUUID();
        Competencia competencia = Competencia.rehydrate(
            competenciaId, perfilId, "Java", "Desc", NivelCompetencia.AVANCADO, true
        );

        when(competenciaRepository.findById(competenciaId)).thenReturn(Optional.of(competencia));

        service.removerCompetencia(competenciaId);

        verify(competenciaRepository).delete(competenciaId);
        verify(compatibilidadeCacheService).invalidarCacheCandidatoSync(perfilId);
    }

    @Test
    void deveAdicionarPortfolio() {
        CandidatoService.AdicionarPortfolioCommand cmd = new CandidatoService.AdicionarPortfolioCommand(
            perfilId,
            "Meu Portfólio",
            "https://portfolio.com"
        );

        Portfolio portfolio = Portfolio.rehydrate(
            UUID.randomUUID(),
            perfilId,
            "Meu Portfólio",
            "https://portfolio.com",
            true
        );

        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);

        CandidatoService.PortfolioResult resultado = service.adicionarPortfolio(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("Meu Portfólio");
        assertThat(resultado.link()).isEqualTo("https://portfolio.com");
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void deveListarPortfolios() {
        Portfolio p1 = Portfolio.rehydrate(
            UUID.randomUUID(), perfilId, "Portfolio 1", "https://p1.com", true
        );
        Portfolio p2 = Portfolio.rehydrate(
            UUID.randomUUID(), perfilId, "Portfolio 2", "https://p2.com", true
        );

        when(portfolioRepository.listByPerfilCandidato(perfilId)).thenReturn(List.of(p1, p2));

        List<CandidatoService.PortfolioResult> resultado = service.listarPortfolios(perfilId);

        assertThat(resultado).hasSize(2);
        verify(portfolioRepository).listByPerfilCandidato(perfilId);
    }

    @Test
    void deveAdicionarCertificado() {
        CandidatoService.AdicionarCertificadoCommand cmd = new CandidatoService.AdicionarCertificadoCommand(
            perfilId,
            "AWS Certified",
            "Amazon",
            LocalDate.now(),
            LocalDate.now().plusYears(3),
            "Certificação AWS"
        );

        Certificado certificado = Certificado.rehydrate(
            UUID.randomUUID(),
            perfilId,
            "AWS Certified",
            "Amazon",
            LocalDate.now(),
            LocalDate.now().plusYears(3),
            "Certificação AWS",
            true
        );

        when(certificadoRepository.save(any(Certificado.class))).thenReturn(certificado);

        CandidatoService.CertificadoResult resultado = service.adicionarCertificado(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("AWS Certified");
        assertThat(resultado.instituicao()).isEqualTo("Amazon");
        verify(certificadoRepository).save(any(Certificado.class));
    }

    @Test
    void deveListarCertificados() {
        Certificado c1 = Certificado.rehydrate(
            UUID.randomUUID(), perfilId, "Cert 1", "Inst 1", LocalDate.now(), null, "Desc 1", true
        );

        when(certificadoRepository.listByPerfilCandidato(perfilId)).thenReturn(List.of(c1));

        List<CandidatoService.CertificadoResult> resultado = service.listarCertificados(perfilId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Cert 1");
        verify(certificadoRepository).listByPerfilCandidato(perfilId);
    }

    @Test
    void deveListarExperiencias() {
        ExperienciaProfissional exp1 = ExperienciaProfissional.rehydrate(
            UUID.randomUUID(), usuarioId, "Dev Java", "Empresa A", "Desc", LocalDate.now(), null, true
        );

        when(experienciaProfissionalRepository.listByUsuario(usuarioId)).thenReturn(List.of(exp1));

        List<CandidatoService.ExperienciaResult> resultado = service.listarExperiencias(usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).cargo()).isEqualTo("Dev Java");
        verify(experienciaProfissionalRepository).listByUsuario(usuarioId);
    }

    @Test
    void deveListarHistoricos() {
        HistoricoAcademico hist1 = HistoricoAcademico.rehydrate(
            UUID.randomUUID(), usuarioId, "CC", "Desc", "UFMG", LocalDate.now(), null, true
        );

        when(historicoAcademicoRepository.listByUsuario(usuarioId)).thenReturn(List.of(hist1));

        List<CandidatoService.HistoricoResult> resultado = service.listarHistoricos(usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("CC");
        verify(historicoAcademicoRepository).listByUsuario(usuarioId);
    }
}

