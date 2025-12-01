package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.api.dto.CompatibilidadeResponse;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompatibilidadeServiceTest {

    @Mock
    private CompatibilidadeCacheService cacheService;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CompatibilidadeService service;

    private UUID candidatoId;
    private UUID vagaId;
    private Usuario candidato;
    private Vaga vaga;
    private CompatibilidadeCache cache;

    @BeforeEach
    void setUp() {
        candidatoId = UUID.randomUUID();
        vagaId = UUID.randomUUID();

        candidato = Usuario.rehydrate(
            candidatoId, "João Silva", new Email("joao@example.com"),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

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
    void deveCalcularCompatibilidadeComSucesso() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(cacheService.obterOuCalcular(candidatoId, vagaId)).thenReturn(cache);

        CompatibilidadeResponse resultado = service.calcularCompatibilidade(candidatoId, vagaId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.candidatoUsuarioId()).isEqualTo(candidatoId);
        assertThat(resultado.vagaId()).isEqualTo(vagaId);
        assertThat(resultado.percentualCompatibilidade()).isEqualTo(85);
        assertThat(resultado.justificativa()).isEqualTo("Alta compatibilidade");
        assertThat(resultado.usouIA()).isTrue();
        verify(usuarioRepository).findById(candidatoId);
        verify(vagaRepository).findById(vagaId);
        verify(cacheService).obterOuCalcular(candidatoId, vagaId);
    }

    @Test
    void naoDeveCalcularQuandoCandidatoNaoExiste() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calcularCompatibilidade(candidatoId, vagaId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Candidato");

        verify(vagaRepository, never()).findById(any());
        verify(cacheService, never()).obterOuCalcular(any(), any());
    }

    @Test
    void naoDeveCalcularQuandoVagaNaoExiste() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calcularCompatibilidade(candidatoId, vagaId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vaga");

        verify(cacheService, never()).obterOuCalcular(any(), any());
    }

    @Test
    void deveRetornarCompatibilidadeAltaParaCandidatoQualificado() {
        CompatibilidadeCache cacheAlta = CompatibilidadeCache.rehydrate(
            UUID.randomUUID(), candidatoId, vagaId, new BigDecimal("95.0"),
            "Excelente compatibilidade", LocalDate.now().atStartOfDay(), null
        );

        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(cacheService.obterOuCalcular(candidatoId, vagaId)).thenReturn(cacheAlta);

        CompatibilidadeResponse resultado = service.calcularCompatibilidade(candidatoId, vagaId);

        assertThat(resultado.percentualCompatibilidade()).isEqualTo(95);
    }

    @Test
    void deveRetornarCompatibilidadeBaixaParaCandidatoNaoQualificado() {
        CompatibilidadeCache cacheBaixa = CompatibilidadeCache.rehydrate(
            UUID.randomUUID(), candidatoId, vagaId, new BigDecimal("30.0"),
            "Baixa compatibilidade", LocalDate.now().atStartOfDay(), null
        );

        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(cacheService.obterOuCalcular(candidatoId, vagaId)).thenReturn(cacheBaixa);

        CompatibilidadeResponse resultado = service.calcularCompatibilidade(candidatoId, vagaId);

        assertThat(resultado.percentualCompatibilidade()).isEqualTo(30);
    }

    @Test
    void deveUtilizarCacheQuandoDisponivel() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(cacheService.obterOuCalcular(candidatoId, vagaId)).thenReturn(cache);

        service.calcularCompatibilidade(candidatoId, vagaId);

        verify(cacheService).obterOuCalcular(candidatoId, vagaId);
    }
}

