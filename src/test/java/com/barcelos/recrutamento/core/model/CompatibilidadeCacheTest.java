package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CompatibilidadeCacheTest {

    @Test
    void deveCriarNovoCompatibilidadeCacheComAtributosPadrao() {
        UUID candidatoId = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        BigDecimal percentual = new BigDecimal("85.5");
        String justificativa = "Candidato possui todas as competências requisitadas";

        CompatibilidadeCache cache = CompatibilidadeCache.novo(candidatoId, vagaId, 
                percentual, justificativa);

        assertThat(cache).isNotNull();
        assertThat(cache.getId()).isNotNull();
        assertThat(cache.getCandidatoUsuarioId()).isEqualTo(candidatoId);
        assertThat(cache.getVagaId()).isEqualTo(vagaId);
        assertThat(cache.getPercentualCompatibilidade()).isEqualTo(percentual);
        assertThat(cache.getJustificativa()).isEqualTo(justificativa);
        assertThat(cache.getDataCalculo()).isNotNull();
        assertThat(cache.getDataAtualizacao()).isNull();
        assertThat(cache.foiAtualizado()).isFalse();
    }

    @Test
    void deveRehydratarCompatibilidadeCacheExistente() {
        UUID id = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        BigDecimal percentual = new BigDecimal("90.0");
        LocalDateTime dataCalculo = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime dataAtualizacao = LocalDateTime.of(2025, 1, 16, 11, 0);

        CompatibilidadeCache cache = CompatibilidadeCache.rehydrate(id, candidatoId, vagaId, 
                percentual, "Justificativa", dataCalculo, dataAtualizacao);

        assertThat(cache.getId()).isEqualTo(id);
        assertThat(cache.getDataCalculo()).isEqualTo(dataCalculo);
        assertThat(cache.getDataAtualizacao()).isEqualTo(dataAtualizacao);
        assertThat(cache.foiAtualizado()).isTrue();
    }

    @Test
    void deveAtualizarCompatibilidadeMantendoImutabilidade() {
        CompatibilidadeCache original = criarCachePadrao();
        BigDecimal novoPercentual = new BigDecimal("95.0");
        String novaJustificativa = "Justificativa atualizada";
        
        CompatibilidadeCache atualizado = original.atualizar(novoPercentual, novaJustificativa);

        assertThat(original.getPercentualCompatibilidade()).isEqualTo(new BigDecimal("85.5"));
        assertThat(original.getDataAtualizacao()).isNull();
        assertThat(atualizado.getPercentualCompatibilidade()).isEqualTo(novoPercentual);
        assertThat(atualizado.getJustificativa()).isEqualTo(novaJustificativa);
        assertThat(atualizado.getDataAtualizacao()).isNotNull();
        assertThat(atualizado.foiAtualizado()).isTrue();
        assertThat(atualizado.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveVerificarSeFoiAtualizado() {
        CompatibilidadeCache naoAtualizado = criarCachePadrao();
        CompatibilidadeCache atualizado = naoAtualizado.atualizar(new BigDecimal("90.0"), "Nova justificativa");

        assertThat(naoAtualizado.foiAtualizado()).isFalse();
        assertThat(atualizado.foiAtualizado()).isTrue();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID candidatoId = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        BigDecimal percentual = new BigDecimal("85.5");

        assertThatThrownBy(() -> CompatibilidadeCache.novo(null, vagaId, percentual, "Justificativa"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> CompatibilidadeCache.novo(candidatoId, null, percentual, "Justificativa"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> CompatibilidadeCache.novo(candidatoId, vagaId, null, "Justificativa"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveCompararCompatibilidadeCacheCorretamente() {
        UUID id = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        BigDecimal percentual = new BigDecimal("85.5");

        CompatibilidadeCache cache1 = CompatibilidadeCache.rehydrate(id, candidatoId, vagaId, 
                percentual, "Just1", LocalDateTime.now(), null);
        CompatibilidadeCache cache2 = CompatibilidadeCache.rehydrate(id, candidatoId, vagaId, 
                percentual, "Just2", LocalDateTime.now(), null);
        CompatibilidadeCache cache3 = CompatibilidadeCache.novo(candidatoId, vagaId, 
                percentual, "Just3");

        assertThat(cache1).isEqualTo(cache2);
        assertThat(cache1).isNotEqualTo(cache3);
        assertThat(cache1.hashCode()).isEqualTo(cache2.hashCode());
    }

    private CompatibilidadeCache criarCachePadrao() {
        UUID candidatoId = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        BigDecimal percentual = new BigDecimal("85.5");
        return CompatibilidadeCache.novo(candidatoId, vagaId, percentual, 
                "Candidato possui todas as competências requisitadas");
    }
}

