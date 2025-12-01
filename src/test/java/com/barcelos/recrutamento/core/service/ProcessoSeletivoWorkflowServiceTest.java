package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoSeletivoWorkflowServiceTest {

    @Mock
    private ProcessoSeletivoRepository processoRepository;

    @Mock
    private HistoricoEtapaProcessoRepository historicoRepository;

    @Mock
    private EtapaProcessoRepository etapaProcessoRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private ProcessoSeletivoWorkflowService service;

    private UUID processoId;
    private UUID candidaturaId;
    private UUID vagaId;
    private UUID usuarioId;
    private ProcessoSeletivo processo;
    private Candidatura candidatura;
    private EtapaProcesso etapa1;
    private EtapaProcesso etapa2;
    private EtapaProcesso etapa3;

    @BeforeEach
    void setUp() {
        processoId = UUID.randomUUID();
        candidaturaId = UUID.randomUUID();
        vagaId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        etapa1 = EtapaProcesso.rehydrate(
            UUID.randomUUID(),
            vagaId,
            "Triagem",
            "Primeira etapa",
            TipoEtapa.TRIAGEM_CURRICULO,
            1,
            StatusEtapa.EM_ANDAMENTO,
            null,
            null,
            LocalDateTime.now()
        );

        etapa2 = EtapaProcesso.rehydrate(
            UUID.randomUUID(),
            vagaId,
            "Entrevista",
            "Segunda etapa",
            TipoEtapa.ENTREVISTA_ONLINE,
            2,
            StatusEtapa.PENDENTE,
            null,
            null,
            LocalDateTime.now()
        );

        etapa3 = EtapaProcesso.rehydrate(
            UUID.randomUUID(),
            vagaId,
            "Final",
            "Etapa final",
            TipoEtapa.PROPOSTA_SALARIAL,
            3,
            StatusEtapa.PENDENTE,
            null,
            null,
            LocalDateTime.now()
        );

        processo = ProcessoSeletivo.rehydrate(
            processoId,
            candidaturaId,
            etapa1.getId(),
            LocalDateTime.now(),
            null,
            LocalDateTime.now()
        );

        candidatura = Candidatura.rehydrate(
            candidaturaId,
            vagaId,
            UUID.randomUUID(),
            StatusCandidatura.EM_PROCESSO,
            LocalDate.now(),
            null,
            null
        );
    }

    @Test
    void deveAvancarParaProximaEtapa() {
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(new ArrayList<>(List.of(etapa1, etapa2, etapa3)));
        when(historicoRepository.save(any(HistoricoEtapaProcesso.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(processoRepository.save(any(ProcessoSeletivo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ProcessoSeletivo resultado = service.avancarParaProximaEtapa(processoId, usuarioId, "Aprovado na triagem");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEtapaProcessoAtualId()).isEqualTo(etapa2.getId());

        verify(historicoRepository).save(any(HistoricoEtapaProcesso.class));
        verify(processoRepository).save(any(ProcessoSeletivo.class));
    }

    @Test
    void naoDeveAvancarQuandoProcessoNaoExiste() {
        when(processoRepository.findById(processoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.avancarParaProximaEtapa(processoId, usuarioId, "Feedback"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Processo seletivo não encontrado");

        verify(processoRepository, never()).save(any());
    }

    @Test
    void naoDeveAvancarQuandoProcessoJaFinalizado() {
        ProcessoSeletivo processoFinalizado = processo.finalizar();

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processoFinalizado));

        assertThatThrownBy(() -> service.avancarParaProximaEtapa(processoId, usuarioId, "Feedback"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Não é possível avançar um processo já finalizado");

        verify(processoRepository, never()).save(any());
    }

    @Test
    void naoDeveAvancarQuandoJaEstaNaUltimaEtapa() {
        ProcessoSeletivo processoNaUltimaEtapa = processo.avancarParaEtapa(etapa3.getId());

        List<EtapaProcesso> etapas = new ArrayList<>(List.of(etapa1, etapa2, etapa3));
        
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processoNaUltimaEtapa));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(etapas);

        assertThatThrownBy(() -> service.avancarParaProximaEtapa(processoId, usuarioId, "Feedback"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("já está na última etapa");

        verify(processoRepository, never()).save(any());
    }

    @Test
    void deveAvancarParaEtapaEspecifica() {
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(new ArrayList<>(List.of(etapa1, etapa2, etapa3)));
        when(historicoRepository.save(any(HistoricoEtapaProcesso.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(processoRepository.save(any(ProcessoSeletivo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ProcessoSeletivo resultado = service.avancarParaEtapa(processoId, etapa3.getId(), usuarioId, "Pulando para final");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEtapaProcessoAtualId()).isEqualTo(etapa3.getId());

        verify(historicoRepository).save(any(HistoricoEtapaProcesso.class));
        verify(processoRepository).save(any(ProcessoSeletivo.class));
    }

    @Test
    void naoDeveAvancarParaEtapaQueNaoPertenceAVaga() {
        UUID etapaOutraVaga = UUID.randomUUID();

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(new ArrayList<>(List.of(etapa1, etapa2, etapa3)));

        assertThatThrownBy(() -> service.avancarParaEtapa(processoId, etapaOutraVaga, usuarioId, "Feedback"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Etapa não pertence à vaga");

        verify(processoRepository, never()).save(any());
    }

    @Test
    void naoDeveAvancarParaMesmaEtapa() {
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(new ArrayList<>(List.of(etapa1, etapa2, etapa3)));

        assertThatThrownBy(() -> service.avancarParaEtapa(processoId, etapa1.getId(), usuarioId, "Feedback"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Processo já está na etapa especificada");

        verify(processoRepository, never()).save(any());
    }

    @Test
    void deveRetornarParaEtapaAnterior() {
        ProcessoSeletivo processoNaEtapa2 = processo.avancarParaEtapa(etapa2.getId());

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processoNaEtapa2));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(List.of(etapa1, etapa2, etapa3));
        when(historicoRepository.save(any(HistoricoEtapaProcesso.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(processoRepository.save(any(ProcessoSeletivo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ProcessoSeletivo resultado = service.retornarParaEtapa(processoId, etapa1.getId(), usuarioId, "Retornando");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEtapaProcessoAtualId()).isEqualTo(etapa1.getId());

        verify(historicoRepository).save(any(HistoricoEtapaProcesso.class));
        verify(processoRepository).save(any(ProcessoSeletivo.class));
    }

    @Test
    void deveFinalizarProcessoComAprovaçao() {
        ProcessoSeletivo processoNaUltimaEtapa = processo.avancarParaEtapa(etapa3.getId());
        List<EtapaProcesso> etapas = new ArrayList<>(List.of(etapa1, etapa2, etapa3));

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processoNaUltimaEtapa));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(etapas);
        when(historicoRepository.save(any(HistoricoEtapaProcesso.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(etapaProcessoRepository.save(any(EtapaProcesso.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(processoRepository.save(any(ProcessoSeletivo.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(candidaturaRepository.save(any(Candidatura.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ProcessoSeletivo resultado = service.finalizar(processoId, usuarioId, "Parabéns!");

        assertThat(resultado).isNotNull();
        assertThat(resultado.isFinalizado()).isTrue();

        verify(candidaturaRepository).save(argThat(c -> c.getStatus() == StatusCandidatura.ACEITA));
        verify(historicoRepository).save(any(HistoricoEtapaProcesso.class));
    }

    @Test
    void naoDeveFinalizarProcessoJaFinalizado() {
        ProcessoSeletivo processoFinalizado = processo.finalizar();

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processoFinalizado));

        assertThatThrownBy(() -> service.finalizar(processoId, usuarioId, "Feedback"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Processo já foi finalizado");
    }

    @Test
    void deveReprovarCandidato() {
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(historicoRepository.save(any(HistoricoEtapaProcesso.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(processoRepository.save(any(ProcessoSeletivo.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(candidaturaRepository.save(any(Candidatura.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ProcessoSeletivo resultado = service.reprovar(processoId, usuarioId, "Não atende requisitos");

        assertThat(resultado).isNotNull();
        assertThat(resultado.isFinalizado()).isTrue();

        verify(candidaturaRepository).save(argThat(c -> c.getStatus() == StatusCandidatura.REJEITADA));
        verify(historicoRepository).save(any(HistoricoEtapaProcesso.class));
    }

    @Test
    void naoDeveReprovarProcessoJaFinalizado() {
        ProcessoSeletivo processoFinalizado = processo.finalizar();

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processoFinalizado));

        assertThatThrownBy(() -> service.reprovar(processoId, usuarioId, "Feedback"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Processo já foi finalizado");
    }

    @Test
    void deveBuscarProcessoPorCandidatura() {
        when(processoRepository.findByCandidaturaId(candidaturaId)).thenReturn(Optional.of(processo));

        ProcessoSeletivo resultado = service.buscarPorCandidatura(candidaturaId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCandidaturaId()).isEqualTo(candidaturaId);
        verify(processoRepository).findByCandidaturaId(candidaturaId);
    }

    @Test
    void naoDeveBuscarProcessoInexistentePorCandidatura() {
        when(processoRepository.findByCandidaturaId(candidaturaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorCandidatura(candidaturaId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Processo seletivo não encontrado");
    }

    @Test
    void deveBuscarProcessoPorId() {
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));

        ProcessoSeletivo resultado = service.buscarPorId(processoId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(processoId);
        verify(processoRepository).findById(processoId);
    }

    @Test
    void deveAtualizarStatusCandidaturaAoAvancar() {
        Candidatura candidaturaPendente = Candidatura.rehydrate(
            candidaturaId,
            vagaId,
            UUID.randomUUID(),
            StatusCandidatura.PENDENTE,
            LocalDate.now(),
            null,
            null
        );

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidaturaPendente));
        when(etapaProcessoRepository.findByVagaId(vagaId)).thenReturn(List.of(etapa1, etapa2, etapa3));
        when(historicoRepository.save(any(HistoricoEtapaProcesso.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(processoRepository.save(any(ProcessoSeletivo.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(candidaturaRepository.save(any(Candidatura.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        service.avancarParaEtapa(processoId, etapa2.getId(), usuarioId, "Avançando");

        verify(candidaturaRepository).save(argThat(c -> c.getStatus() == StatusCandidatura.EM_PROCESSO));
    }
}

