package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
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
class ConviteProcessoSeletivoServiceTest {

    @Mock
    private ConviteProcessoSeletivoRepository conviteRepository;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private EtapaProcessoRepository etapaProcessoRepository;

    @Mock
    private ProcessoSeletivoRepository processoSeletivoRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private CompatibilidadeService compatibilidadeService;

    @Mock
    private CurriculoService curriculoService;

    @InjectMocks
    private ConviteProcessoSeletivoService service;

    private UUID vagaId;
    private UUID recrutadorId;
    private UUID candidatoId;
    private UUID conviteId;
    private Vaga vaga;
    private Usuario candidato;
    private ConviteProcessoSeletivo convite;
    private EtapaProcesso etapa;

    @BeforeEach
    void setUp() {
        vagaId = UUID.randomUUID();
        recrutadorId = UUID.randomUUID();
        candidatoId = UUID.randomUUID();
        conviteId = UUID.randomUUID();

        vaga = Vaga.rehydrate(
            vagaId, UUID.randomUUID(), recrutadorId, "Desenvolvedor Java", "Descrição", "Requisitos",
            new BigDecimal("5000.00"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        candidato = Usuario.rehydrate(
            candidatoId, "João Silva", new Email("joao@example.com"),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

        convite = ConviteProcessoSeletivo.rehydrate(
            conviteId, vagaId, recrutadorId, candidatoId, "Mensagem do convite",
            StatusConviteProcesso.PENDENTE, java.time.LocalDateTime.now(), 
            java.time.LocalDateTime.now().plusDays(7), null
        );

        etapa = EtapaProcesso.rehydrate(
            UUID.randomUUID(), vagaId, "Triagem", "Primeira etapa", TipoEtapa.TRIAGEM_CURRICULO,
            1, StatusEtapa.EM_ANDAMENTO, null, null, java.time.LocalDateTime.now()
        );
    }

    @Test
    void deveEnviarConviteComSucesso() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(candidaturaRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoId))
            .thenReturn(Optional.empty());
        when(conviteRepository.findByCandidatoUsuarioIdAndStatus(candidatoId, StatusConviteProcesso.PENDENTE))
            .thenReturn(List.of());
        when(conviteRepository.save(any(ConviteProcessoSeletivo.class))).thenReturn(convite);
        when(emailTemplateService.renderConviteProcesso(any(), any(), any(), any()))
            .thenReturn("<html>Email</html>");

        ConviteProcessoSeletivo resultado = service.enviarConvite(vagaId, recrutadorId, candidatoId, "Mensagem");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusConviteProcesso.PENDENTE);
        verify(conviteRepository).save(any(ConviteProcessoSeletivo.class));
        verify(emailService).sendHtmlEmailAsync(eq("joao@example.com"), any(), any());
    }

    @Test
    void naoDeveEnviarConviteQuandoVagaNaoExiste() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enviarConvite(vagaId, recrutadorId, candidatoId, "Mensagem"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vaga não encontrada");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void naoDeveEnviarConviteQuandoCandidatoJaSeCandidatou() {
        Candidatura candidaturaExistente = Candidatura.rehydrate(
            UUID.randomUUID(), vagaId, candidatoId, StatusCandidatura.PENDENTE,
            LocalDate.now(), null, new BigDecimal("85.5")
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(candidaturaRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoId))
            .thenReturn(Optional.of(candidaturaExistente));

        assertThatThrownBy(() -> service.enviarConvite(vagaId, recrutadorId, candidatoId, "Mensagem"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Candidato já se candidatou");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void naoDeveEnviarConviteDuplicado() {
        ConviteProcessoSeletivo conviteExistente = ConviteProcessoSeletivo.rehydrate(
            UUID.randomUUID(), vagaId, recrutadorId, candidatoId, "Mensagem",
            StatusConviteProcesso.PENDENTE, java.time.LocalDateTime.now(), 
            java.time.LocalDateTime.now().plusDays(7), null
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(candidaturaRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoId))
            .thenReturn(Optional.empty());
        when(conviteRepository.findByCandidatoUsuarioIdAndStatus(candidatoId, StatusConviteProcesso.PENDENTE))
            .thenReturn(List.of(conviteExistente));

        assertThatThrownBy(() -> service.enviarConvite(vagaId, recrutadorId, candidatoId, "Mensagem"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Já existe um convite pendente");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void naoDeveAceitarConviteDeOutroCandidato() {
        UUID outroCandidatoId = UUID.randomUUID();

        when(conviteRepository.findById(conviteId)).thenReturn(Optional.of(convite));

        assertThatThrownBy(() -> service.aceitarConvite(conviteId, outroCandidatoId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Este convite não é para você");

        verify(candidaturaRepository, never()).save(any());
    }

    @Test
    void naoDeveAceitarConviteQuandoJaPossuiCandidatura() {
        Candidatura candidaturaExistente = Candidatura.rehydrate(
            UUID.randomUUID(), vagaId, candidatoId, StatusCandidatura.EM_PROCESSO,
            LocalDate.now(), null, new BigDecimal("85.5")
        );

        when(conviteRepository.findById(conviteId)).thenReturn(Optional.of(convite));
        when(candidaturaRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoId))
            .thenReturn(Optional.of(candidaturaExistente));

        assertThatThrownBy(() -> service.aceitarConvite(conviteId, candidatoId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Você já possui uma candidatura");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void deveRecusarConvite() {
        when(conviteRepository.findById(conviteId)).thenReturn(Optional.of(convite));
        when(conviteRepository.save(any(ConviteProcessoSeletivo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ConviteProcessoSeletivo resultado = service.recusarConvite(conviteId, candidatoId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusConviteProcesso.RECUSADO);
        verify(conviteRepository).save(any(ConviteProcessoSeletivo.class));
    }

    @Test
    void naoDeveRecusarConviteDeOutroCandidato() {
        UUID outroCandidatoId = UUID.randomUUID();

        when(conviteRepository.findById(conviteId)).thenReturn(Optional.of(convite));

        assertThatThrownBy(() -> service.recusarConvite(conviteId, outroCandidatoId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Este convite não é para você");
    }

    @Test
    void deveListarConvitesPorCandidato() {
        when(conviteRepository.findByCandidatoUsuarioId(candidatoId)).thenReturn(List.of(convite));

        List<ConviteProcessoSeletivo> resultado = service.listarConvitesPorCandidato(candidatoId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(conviteId);
        verify(conviteRepository).findByCandidatoUsuarioId(candidatoId);
    }

    @Test
    void deveListarConvitesPorVaga() {
        when(conviteRepository.findByVagaId(vagaId)).thenReturn(List.of(convite));

        List<ConviteProcessoSeletivo> resultado = service.listarConvitesPorVaga(vagaId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getVagaId()).isEqualTo(vagaId);
        verify(conviteRepository).findByVagaId(vagaId);
    }

    @Test
    void deveVerificarExistenciaDeConvitePendente() {
        ConviteProcessoSeletivo convitePendente = ConviteProcessoSeletivo.rehydrate(
            UUID.randomUUID(), vagaId, recrutadorId, candidatoId, "Mensagem",
            StatusConviteProcesso.PENDENTE, java.time.LocalDateTime.now(), 
            java.time.LocalDateTime.now().plusDays(7), null
        );

        when(conviteRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoId))
            .thenReturn(List.of(convitePendente));

        boolean resultado = service.existeConvitePendente(vagaId, candidatoId);

        assertThat(resultado).isTrue();
        verify(conviteRepository).findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoId);
    }

    @Test
    void naoDeveEncontrarConvitePendenteQuandoNaoExiste() {
        when(conviteRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoId))
            .thenReturn(List.of());

        boolean resultado = service.existeConvitePendente(vagaId, candidatoId);

        assertThat(resultado).isFalse();
    }
}

