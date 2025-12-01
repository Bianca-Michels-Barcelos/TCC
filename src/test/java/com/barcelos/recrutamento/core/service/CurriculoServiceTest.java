package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @Mock
    private ExperienciaProfissionalRepository experienciaRepository;

    @Mock
    private HistoricoAcademicoRepository historicoRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private CurriculoAIService curriculoAIService;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private CurriculoPDFService curriculoPDFService;

    @InjectMocks
    private CurriculoService service;

    private UUID candidatoId;
    private UUID candidaturaId;
    private UUID vagaId;
    private Usuario usuario;
    private Candidatura candidatura;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        candidatoId = UUID.randomUUID();
        candidaturaId = UUID.randomUUID();
        vagaId = UUID.randomUUID();

        usuario = Usuario.rehydrate(
            candidatoId, "João Silva", new Email("joao@example.com"),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

        candidatura = Candidatura.rehydrate(
            candidaturaId, vagaId, candidatoId, StatusCandidatura.PENDENTE,
            LocalDate.now(), null, new BigDecimal("85.5")
        );

        vaga = Vaga.rehydrate(
            vagaId, UUID.randomUUID(), UUID.randomUUID(), "Desenvolvedor", "Desc", "Req",
            new BigDecimal("5000"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );
    }

    @Test
    void deveGerarCurriculoComIAComSucesso() {
        String curriculoHTML = "<h1>João Silva</h1>";

        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(usuario));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(curriculoAIService.gerarCurriculoOtimizado(any(), any(), any(), any()))
            .thenReturn(curriculoHTML);

        String resultado = service.gerarCurriculoComIA(candidatoId, vagaId, ModeloCurriculoEnum.PROFISSIONAL, "Observações");

        assertThat(resultado).isEqualTo(curriculoHTML);
        verify(curriculoAIService).gerarCurriculoOtimizado(candidatoId, vaga, ModeloCurriculoEnum.PROFISSIONAL, "Observações");
    }

    @Test
    void naoDeveGerarCurriculoQuandoCandidatoNaoExiste() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gerarCurriculoComIA(candidatoId, vagaId, ModeloCurriculoEnum.PROFISSIONAL, null))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Usuário");

        verify(curriculoAIService, never()).gerarCurriculoOtimizado(any(), any(), any(), any());
    }

    @Test
    void naoDeveGerarCurriculoQuandoVagaNaoExiste() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(usuario));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gerarCurriculoComIA(candidatoId, vagaId, ModeloCurriculoEnum.PROFISSIONAL, null))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vaga");

        verify(curriculoAIService, never()).gerarCurriculoOtimizado(any(), any(), any(), any());
    }

    @Test
    void deveTratarErroAoGerarCurriculoComIA() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(usuario));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(curriculoAIService.gerarCurriculoOtimizado(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("API Error"));

        assertThatThrownBy(() -> service.gerarCurriculoComIA(candidatoId, vagaId, ModeloCurriculoEnum.PROFISSIONAL, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Erro ao gerar currículo com IA");
    }
}

