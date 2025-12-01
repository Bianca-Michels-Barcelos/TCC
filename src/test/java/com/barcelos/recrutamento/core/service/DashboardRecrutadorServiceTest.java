package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.api.dto.dashboard.DashboardRecrutadorResponse;
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
class DashboardRecrutadorServiceTest {

    @Mock
    private VagaJpaRepository vagaRepository;

    @Mock
    private CandidaturaJpaRepository candidaturaRepository;

    @Mock
    private HistoricoEtapaProcessoJpaRepository historicoRepository;

    @Mock
    private ConviteProcessoSeletivoJpaRepository conviteRepository;

    @Mock
    private ProcessoSeletivoJpaRepository processoRepository;

    @InjectMocks
    private DashboardRecrutadorService service;

    private UUID recrutadorId;

    @BeforeEach
    void setUp() {
        recrutadorId = UUID.randomUUID();
    }

    @Test
    void deveGerarDashboardComSucesso() {
        when(vagaRepository.countByRecrutadorAndStatus(recrutadorId, "ABERTA")).thenReturn(3L);
        when(candidaturaRepository.countPendingByRecrutador(recrutadorId)).thenReturn(5L);
        when(vagaRepository.findByRecrutador_Id(recrutadorId)).thenReturn(List.of());
        when(candidaturaRepository.findRecentByRecrutador(any(), any())).thenReturn(List.of());
        when(historicoRepository.findRecentByRecrutador(any(), any(), any())).thenReturn(List.of());
        when(conviteRepository.findUpcomingByRecrutador(recrutadorId)).thenReturn(List.of());

        DashboardRecrutadorResponse resultado = service.gerarDashboard(recrutadorId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.vagasAtivasCount()).isEqualTo(3);
        assertThat(resultado.candidaturasPendentesCount()).isEqualTo(5);
        verify(vagaRepository).countByRecrutadorAndStatus(recrutadorId, "ABERTA");
    }

    @Test
    void deveCalcularTaxaConversaoCorretamente() {
        when(vagaRepository.countByRecrutadorAndStatus(recrutadorId, "ABERTA")).thenReturn(2L);
        when(candidaturaRepository.countPendingByRecrutador(recrutadorId)).thenReturn(10L);
        when(vagaRepository.findByRecrutador_Id(recrutadorId)).thenReturn(List.of());
        when(candidaturaRepository.findRecentByRecrutador(any(), any())).thenReturn(List.of());
        when(historicoRepository.findRecentByRecrutador(any(), any(), any())).thenReturn(List.of());
        when(conviteRepository.findUpcomingByRecrutador(recrutadorId)).thenReturn(List.of());

        DashboardRecrutadorResponse resultado = service.gerarDashboard(recrutadorId);

        assertThat(resultado.taxaConversao()).isEqualTo(0.0);
    }

    @Test
    void deveBuscarAtividadesRecentes() {
        when(candidaturaRepository.findRecentByRecrutador(any(), any())).thenReturn(List.of());
        when(historicoRepository.findRecentByRecrutador(any(), any(), any())).thenReturn(List.of());

        var resultado = service.buscarAtividadesRecentes(recrutadorId, 10);

        assertThat(resultado).isNotNull();
        verify(candidaturaRepository).findRecentByRecrutador(any(), any());
    }

    @Test
    void deveBuscarVagasAtencao() {
        when(vagaRepository.findByRecrutador_Id(recrutadorId)).thenReturn(List.of());

        var resultado = service.buscarVagasAtencao(recrutadorId);

        assertThat(resultado).isNotNull();
        verify(vagaRepository).findByRecrutador_Id(recrutadorId);
    }

    @Test
    void deveBuscarEntrevistasProximas() {
        when(conviteRepository.findUpcomingByRecrutador(recrutadorId)).thenReturn(List.of());

        var resultado = service.buscarEntrevistasProximas(recrutadorId);

        assertThat(resultado).isNotNull();
        verify(conviteRepository).findUpcomingByRecrutador(recrutadorId);
    }
}

