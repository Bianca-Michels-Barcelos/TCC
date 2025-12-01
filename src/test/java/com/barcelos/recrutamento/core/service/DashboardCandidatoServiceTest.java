package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.api.dto.dashboard.DashboardCandidatoResponse;
import com.barcelos.recrutamento.core.port.VagaSalvaRepository;
import com.barcelos.recrutamento.data.spring.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardCandidatoServiceTest {

    @Mock
    private CandidaturaJpaRepository candidaturaRepository;

    @Mock
    private VagaSalvaRepository vagaSalvaRepository;

    @Mock
    private HistoricoEtapaProcessoJpaRepository historicoRepository;

    @Mock
    private ProcessoSeletivoJpaRepository processoRepository;

    @Mock
    private ConviteProcessoSeletivoJpaRepository conviteRepository;

    @Mock
    private VagaJpaRepository vagaRepository;

    @InjectMocks
    private DashboardCandidatoService service;

    private UUID candidatoId;

    @BeforeEach
    void setUp() {
        candidatoId = UUID.randomUUID();
    }

    @Test
    void deveGerarDashboardComSucesso() {
        when(candidaturaRepository.countActiveApplications(candidatoId)).thenReturn(5L);
        when(vagaSalvaRepository.countByUsuarioId(candidatoId)).thenReturn(3L);
        when(candidaturaRepository.countByCandidato_Id(candidatoId)).thenReturn(10L);
        when(candidaturaRepository.countByCandidato_IdAndStatus(candidatoId, "PENDENTE")).thenReturn(3L);
        when(candidaturaRepository.findAverageCompatibility(candidatoId)).thenReturn(75.5);
        when(historicoRepository.findRecentByCandidato(any(), any(), any())).thenReturn(List.of());
        when(candidaturaRepository.findByCandidato_Id(candidatoId)).thenReturn(List.of());
        when(processoRepository.findActiveProcessesByCandidato(candidatoId)).thenReturn(List.of());
        when(conviteRepository.findPendingByCandidato(candidatoId)).thenReturn(List.of());

        DashboardCandidatoResponse resultado = service.gerarDashboard(candidatoId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.candidaturasAtivasCount()).isEqualTo(5);
        assertThat(resultado.vagasSalvasCount()).isEqualTo(3);
        assertThat(resultado.taxaResposta()).isGreaterThan(0);
        assertThat(resultado.compatibilidadeMedia()).isEqualTo(75.5);
        verify(candidaturaRepository).countActiveApplications(candidatoId);
        verify(vagaSalvaRepository).countByUsuarioId(candidatoId);
    }

    @Test
    void deveCalcularTaxaRespostaCorretamente() {
        when(candidaturaRepository.countActiveApplications(candidatoId)).thenReturn(0L);
        when(vagaSalvaRepository.countByUsuarioId(candidatoId)).thenReturn(0L);
        when(candidaturaRepository.countByCandidato_Id(candidatoId)).thenReturn(20L);
        when(candidaturaRepository.countByCandidato_IdAndStatus(candidatoId, "PENDENTE")).thenReturn(5L);
        when(candidaturaRepository.findAverageCompatibility(candidatoId)).thenReturn(null);
        when(historicoRepository.findRecentByCandidato(any(), any(), any())).thenReturn(List.of());
        when(candidaturaRepository.findByCandidato_Id(candidatoId)).thenReturn(List.of());
        when(processoRepository.findActiveProcessesByCandidato(candidatoId)).thenReturn(List.of());
        when(conviteRepository.findPendingByCandidato(candidatoId)).thenReturn(List.of());

        DashboardCandidatoResponse resultado = service.gerarDashboard(candidatoId);

        assertThat(resultado.taxaResposta()).isEqualTo(75.0);
    }

    @Test
    void deveTratarTaxaRespostaQuandoNaoHaCandidaturas() {
        when(candidaturaRepository.countActiveApplications(candidatoId)).thenReturn(0L);
        when(vagaSalvaRepository.countByUsuarioId(candidatoId)).thenReturn(0L);
        when(candidaturaRepository.countByCandidato_Id(candidatoId)).thenReturn(0L);
        when(candidaturaRepository.countByCandidato_IdAndStatus(candidatoId, "PENDENTE")).thenReturn(0L);
        when(candidaturaRepository.findAverageCompatibility(candidatoId)).thenReturn(null);
        when(historicoRepository.findRecentByCandidato(any(), any(), any())).thenReturn(List.of());
        when(candidaturaRepository.findByCandidato_Id(candidatoId)).thenReturn(List.of());
        when(processoRepository.findActiveProcessesByCandidato(candidatoId)).thenReturn(List.of());
        when(conviteRepository.findPendingByCandidato(candidatoId)).thenReturn(List.of());

        DashboardCandidatoResponse resultado = service.gerarDashboard(candidatoId);

        assertThat(resultado.taxaResposta()).isEqualTo(0.0);
    }

    @Test
    void deveBuscarAtualizacoesRecentes() {
        when(historicoRepository.findRecentByCandidato(any(), any(), any())).thenReturn(List.of());
        when(candidaturaRepository.findByCandidato_Id(candidatoId)).thenReturn(List.of());

        var resultado = service.buscarAtualizacoesRecentes(candidatoId, 10);

        assertThat(resultado).isNotNull();
        verify(historicoRepository).findRecentByCandidato(any(), any(), any());
    }

    @Test
    void deveBuscarProximasEtapas() {
        when(processoRepository.findActiveProcessesByCandidato(candidatoId)).thenReturn(List.of());
        when(conviteRepository.findPendingByCandidato(candidatoId)).thenReturn(List.of());

        var resultado = service.buscarProximasEtapas(candidatoId);

        assertThat(resultado).isNotNull();
        verify(processoRepository).findActiveProcessesByCandidato(candidatoId);
        verify(conviteRepository).findPendingByCandidato(candidatoId);
    }
}

