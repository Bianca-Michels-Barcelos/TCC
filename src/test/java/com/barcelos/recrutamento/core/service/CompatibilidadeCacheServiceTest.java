package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.CompatibilidadeCacheRepository;
import com.barcelos.recrutamento.core.port.PerfilCandidatoRepository;
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
class CompatibilidadeCacheServiceTest {

    @Mock
    private CompatibilidadeCacheRepository cacheRepository;

    @Mock
    private CompatibilidadeAIService aiService;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @InjectMocks
    private CompatibilidadeCacheService service;

    private UUID candidatoId;
    private UUID vagaId;
    private Vaga vaga;
    private CompatibilidadeCache cache;

    @BeforeEach
    void setUp() {
        candidatoId = UUID.randomUUID();
        vagaId = UUID.randomUUID();

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
    void deveRetornarCacheQuandoExiste() {
        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));

        Optional<CompatibilidadeCache> resultado = service.obterDoCache(candidatoId, vagaId);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCandidatoUsuarioId()).isEqualTo(candidatoId);
        assertThat(resultado.get().getVagaId()).isEqualTo(vagaId);
        verify(cacheRepository).findByCandidatoAndVaga(candidatoId, vagaId);
    }

    @Test
    void deveRetornarVazioQuandoCacheNaoExiste() {
        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.empty());

        Optional<CompatibilidadeCache> resultado = service.obterDoCache(candidatoId, vagaId);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveUtilizarCacheQuandoExiste() {
        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));

        CompatibilidadeCache resultado = service.obterOuCalcular(candidatoId, vagaId);

        assertThat(resultado).isEqualTo(cache);
        verify(cacheRepository).findByCandidatoAndVaga(candidatoId, vagaId);
        verify(aiService, never()).calcularCompatibilidade(any(), any());
    }

    @Test
    void deveCalcularQuandoCacheNaoExiste() {
        CompatibilidadeAIService.ResultadoCompatibilidade resultadoIA = 
            new CompatibilidadeAIService.ResultadoCompatibilidade(85, "Alta compatibilidade");

        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.empty());
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(aiService.calcularCompatibilidade(candidatoId, vaga)).thenReturn(resultadoIA);
        when(cacheRepository.save(any(CompatibilidadeCache.class))).thenReturn(cache);

        CompatibilidadeCache resultado = service.obterOuCalcular(candidatoId, vagaId);

        assertThat(resultado).isNotNull();
        verify(aiService).calcularCompatibilidade(candidatoId, vaga);
        verify(cacheRepository).save(any(CompatibilidadeCache.class));
    }

    @Test
    void deveCalcularEArmazenarNoCache() {
        CompatibilidadeAIService.ResultadoCompatibilidade resultadoIA = 
            new CompatibilidadeAIService.ResultadoCompatibilidade(85, "Alta compatibilidade");

        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.empty());
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(aiService.calcularCompatibilidade(candidatoId, vaga)).thenReturn(resultadoIA);
        when(cacheRepository.save(any(CompatibilidadeCache.class))).thenReturn(cache);

        CompatibilidadeCache resultado = service.calcularEArmazenar(candidatoId, vagaId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getPercentualCompatibilidade()).isEqualByComparingTo(new BigDecimal("85.5"));
        verify(cacheRepository).save(any(CompatibilidadeCache.class));
    }

    @Test
    void naoDeveCalcularQuandoVagaNaoExiste() {
        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.empty());
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calcularEArmazenar(candidatoId, vagaId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Vaga não encontrada");

        verify(aiService, never()).calcularCompatibilidade(any(), any());
        verify(cacheRepository, never()).save(any());
    }

    @Test
    void deveInvalidarCacheDeVaga() {
        service.invalidarCacheVaga(vagaId);

        verify(cacheRepository).deleteByVaga(vagaId);
    }

    @Test
    void deveInvalidarCacheDeCandidato() {
        service.invalidarCacheCandidatoSync(candidatoId);

        verify(cacheRepository).deleteByCandidato(candidatoId);
    }

    @Test
    void deveTratarRaceConditionNaSalvagem() {
        CompatibilidadeAIService.ResultadoCompatibilidade resultadoIA = 
            new CompatibilidadeAIService.ResultadoCompatibilidade(85, "Alta compatibilidade");

        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(cache));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(aiService.calcularCompatibilidade(candidatoId, vaga)).thenReturn(resultadoIA);
        when(cacheRepository.save(any(CompatibilidadeCache.class)))
            .thenThrow(new RuntimeException("Duplicate key uk_cache_candidato_vaga"));

        CompatibilidadeCache resultado = service.calcularEArmazenar(candidatoId, vagaId);

        assertThat(resultado).isNotNull();
        verify(cacheRepository, times(2)).findByCandidatoAndVaga(candidatoId, vagaId);
    }

    @Test
    void deveEvitarCacheDuplicado() {
        when(cacheRepository.findByCandidatoAndVaga(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));

        CompatibilidadeCache resultado = service.calcularEArmazenar(candidatoId, vagaId);

        assertThat(resultado).isEqualTo(cache);
        verify(aiService, never()).calcularCompatibilidade(any(), any());
        verify(cacheRepository, never()).save(any());
    }

    @Test
    void deveCalcularParaTodosCandidatos() {
        UUID perfilId = UUID.randomUUID();
        Endereco endereco = new Endereco("Rua", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        PerfilCandidato perfil = PerfilCandidato.rehydrate(
            perfilId, candidatoId, LocalDate.of(1990, 1, 1), endereco, true
        );

        CompatibilidadeAIService.ResultadoCompatibilidade resultadoIA = 
            new CompatibilidadeAIService.ResultadoCompatibilidade(85, "Alta compatibilidade");

        when(perfilCandidatoRepository.findAll()).thenReturn(List.of(perfil));
        when(cacheRepository.existsByCandidatoAndVaga(candidatoId, vagaId)).thenReturn(false);
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(aiService.calcularCompatibilidade(candidatoId, vaga)).thenReturn(resultadoIA);
        when(cacheRepository.save(any(CompatibilidadeCache.class))).thenReturn(cache);

        service.calcularParaTodosCandidatos(vagaId);

        verify(perfilCandidatoRepository).findAll();
        verify(cacheRepository).existsByCandidatoAndVaga(candidatoId, vagaId);
    }

    @Test
    void deveCalcularParaTodasVagas() {
        when(vagaRepository.listPublicas()).thenReturn(List.of(vaga));
        when(cacheRepository.existsByCandidatoAndVaga(candidatoId, vagaId)).thenReturn(false);
        
        CompatibilidadeAIService.ResultadoCompatibilidade resultadoIA = 
            new CompatibilidadeAIService.ResultadoCompatibilidade(85, "Alta compatibilidade");
        
        lenient().when(aiService.calcularCompatibilidade(candidatoId, vaga)).thenReturn(resultadoIA);

        service.calcularParaTodasVagas(candidatoId);

        verify(vagaRepository).listPublicas();
        verify(cacheRepository).existsByCandidatoAndVaga(candidatoId, vagaId);
    }

    @Test
    void deveRecalcularVagaInvalidandoCache() {
        when(perfilCandidatoRepository.findAll()).thenReturn(List.of());

        service.recalcularVaga(vagaId);

        verify(cacheRepository).deleteByVaga(vagaId);
    }
}

