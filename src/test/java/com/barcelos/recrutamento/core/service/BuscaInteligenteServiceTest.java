package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.VagaRepository;
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
class BuscaInteligenteServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private CompatibilidadeCacheService compatibilidadeCacheService;

    @InjectMocks
    private BuscaInteligenteService service;

    private UUID vagaId;
    private UUID candidatoId;
    private Vaga vaga;
    private CompatibilidadeCache cache;

    @BeforeEach
    void setUp() {
        vagaId = UUID.randomUUID();
        candidatoId = UUID.randomUUID();

        vaga = Vaga.rehydrate(
            vagaId, UUID.randomUUID(), UUID.randomUUID(), "Desenvolvedor Java", "Descrição", "Requisitos",
            new BigDecimal("5000.00"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        cache = CompatibilidadeCache.rehydrate(
            UUID.randomUUID(), candidatoId, vagaId, new BigDecimal("85.5"),
            "Alta compatibilidade", LocalDate.now().atStartOfDay(), null
        );
    }

    @Test
    void deveBuscarTodasVagasQuandoConsultaVazia() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar(null, 50, null);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).vaga()).isEqualTo(vaga);
        verify(vagaRepository).listPublicas();
    }

    @Test
    void deveFiltrarVagasPorTitulo() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar("Java", 50, null);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).vaga().getTitulo()).contains("Java");
    }

    @Test
    void deveFiltrarVagasPorDescricao() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar("Descrição", 50, null);

        assertThat(resultado).isNotEmpty();
        verify(vagaRepository).listPublicas();
    }

    @Test
    void deveRetornarVagasSemCompatibilidadeQuandoCandidatoNaoInformado() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar("Java", 50, null);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).percentualCompatibilidade()).isNull();
        assertThat(resultado.get(0).usouIA()).isFalse();
    }

    @Test
    void deveCalcularCompatibilidadeQuandoCandidatoInformado() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar("Java", 50, candidatoId);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).percentualCompatibilidade()).isEqualTo(85);
        assertThat(resultado.get(0).usouIA()).isTrue();
        verify(compatibilidadeCacheService).obterDoCache(candidatoId, vagaId);
    }

    @Test
    void deveRespeitarLimiteDeResultados() {
        Vaga vaga2 = Vaga.rehydrate(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Desenvolvedor Python", "Desc", "Req",
            new BigDecimal("6000.00"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga, vaga2));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar(null, 1, null);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void deveUsarLimitePadrao50QuandoNaoInformado() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar(null, null, null);

        assertThat(resultado).isNotEmpty();
        verify(vagaRepository).listPublicas();
    }

    @Test
    void deveRetornarListaVaziaQuandoNenhumaVagaCorresponde() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar("Python", 50, null);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveOrdenarVagasPorCompatibilidade() {
        UUID vagaId2 = UUID.randomUUID();
        Vaga vaga2 = Vaga.rehydrate(
            vagaId2, UUID.randomUUID(), UUID.randomUUID(), "Desenvolvedor Python", "Desc", "Req",
            new BigDecimal("6000.00"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        CompatibilidadeCache cache2 = CompatibilidadeCache.rehydrate(
            UUID.randomUUID(), candidatoId, vagaId2, new BigDecimal("90.0"),
            "Altíssima compatibilidade", LocalDate.now().atStartOfDay(), null
        );

        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga, vaga2));
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId2))
            .thenReturn(Optional.of(cache2));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar(null, 50, candidatoId);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).percentualCompatibilidade()).isGreaterThan(resultado.get(1).percentualCompatibilidade());
    }

    @Test
    void deveListarVagasSemCacheComoNaoUsouIA() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId))
            .thenReturn(Optional.empty());

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar(null, 50, candidatoId);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).usouIA()).isFalse();
        assertThat(resultado.get(0).justificativa()).contains("Calculando compatibilidade");
    }

    @Test
    void deveTratarErroAoObterCache() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId))
            .thenThrow(new RuntimeException("Erro no cache"));

        List<BuscaInteligenteService.VagaComScoreCompleto> resultado = service.buscar(null, 50, candidatoId);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).justificativa()).contains("Erro ao carregar compatibilidade");
    }
}

