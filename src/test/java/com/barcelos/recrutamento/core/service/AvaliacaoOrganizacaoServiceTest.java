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
class AvaliacaoOrganizacaoServiceTest {

    @Mock
    private AvaliacaoOrganizacaoRepository avaliacaoRepository;

    @Mock
    private ProcessoSeletivoRepository processoRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private VagaRepository vagaRepository;

    @InjectMocks
    private AvaliacaoOrganizacaoService service;

    private UUID processoId;
    private UUID candidatoId;
    private UUID organizacaoId;
    private UUID vagaId;
    private UUID candidaturaId;
    private ProcessoSeletivo processo;
    private Candidatura candidatura;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        processoId = UUID.randomUUID();
        candidatoId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();
        vagaId = UUID.randomUUID();
        candidaturaId = UUID.randomUUID();

        candidatura = Candidatura.rehydrate(
            candidaturaId, vagaId, candidatoId, StatusCandidatura.ACEITA,
            LocalDate.now(), null, new BigDecimal("85.5")
        );

        vaga = Vaga.rehydrate(
            vagaId, organizacaoId, UUID.randomUUID(), "Desenvolvedor", "Desc", "Req",
            new BigDecimal("5000"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        processo = ProcessoSeletivo.rehydrate(
            processoId, candidaturaId, UUID.randomUUID(), 
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );
    }

    @Test
    void deveCriarAvaliacaoComSucesso() {
        AvaliacaoOrganizacao avaliacao = AvaliacaoOrganizacao.rehydrate(
            UUID.randomUUID(), processoId, candidatoId, organizacaoId, 5, "Excelente processo", 
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(avaliacaoRepository.existsByProcessoId(processoId)).thenReturn(false);
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(avaliacaoRepository.save(any(AvaliacaoOrganizacao.class))).thenReturn(avaliacao);

        AvaliacaoOrganizacao resultado = service.criar(processoId, candidatoId, 5, "Excelente processo");

        assertThat(resultado).isNotNull();
        verify(avaliacaoRepository).save(any(AvaliacaoOrganizacao.class));
    }

    @Test
    void naoDeveCriarAvaliacaoQuandoProcessoNaoFinalizado() {
        ProcessoSeletivo processoNaoFinalizado = ProcessoSeletivo.rehydrate(
            processoId, candidaturaId, UUID.randomUUID(), 
            java.time.LocalDateTime.now(), null, java.time.LocalDateTime.now()
        );

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processoNaoFinalizado));

        assertThatThrownBy(() -> service.criar(processoId, candidatoId, 5, "Comentário"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("após a conclusão do processo");

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarAvaliacaoDuplicada() {
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(processo));
        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Optional.of(candidatura));
        when(avaliacaoRepository.existsByProcessoId(processoId)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(processoId, candidatoId, 5, "Comentário"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Já existe uma avaliação");

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarAvaliacaoComSucesso() {
        AvaliacaoOrganizacao avaliacao = AvaliacaoOrganizacao.rehydrate(
            UUID.randomUUID(), processoId, candidatoId, organizacaoId, 4, "Bom", 
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(avaliacaoRepository.findById(any())).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(any(AvaliacaoOrganizacao.class))).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoOrganizacao resultado = service.atualizar(UUID.randomUUID(), candidatoId, 5, "Excelente");

        assertThat(resultado).isNotNull();
        verify(avaliacaoRepository).save(any(AvaliacaoOrganizacao.class));
    }

    @Test
    void deveCalcularNotaMedia() {
        when(avaliacaoRepository.findAverageNotaByOrganizacaoId(organizacaoId)).thenReturn(4.5);

        Double resultado = service.calcularNotaMedia(organizacaoId);

        assertThat(resultado).isEqualTo(4.5);
        verify(avaliacaoRepository).findAverageNotaByOrganizacaoId(organizacaoId);
    }

    @Test
    void deveRetornarZeroQuandoNaoHaAvaliacoes() {
        when(avaliacaoRepository.findAverageNotaByOrganizacaoId(organizacaoId)).thenReturn(null);

        Double resultado = service.calcularNotaMedia(organizacaoId);

        assertThat(resultado).isEqualTo(0.0);
    }

    @Test
    void deveContarAvaliacoes() {
        when(avaliacaoRepository.countByOrganizacaoId(organizacaoId)).thenReturn(10L);

        long resultado = service.contarAvaliacoes(organizacaoId);

        assertThat(resultado).isEqualTo(10);
        verify(avaliacaoRepository).countByOrganizacaoId(organizacaoId);
    }
}

