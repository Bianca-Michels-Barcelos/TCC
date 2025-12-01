package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class VagaBeneficioTest {

    @Test
    void deveCriarNovaVagaBeneficio() {
        UUID vagaId = UUID.randomUUID();
        UUID beneficioId = UUID.randomUUID();

        VagaBeneficio vagaBeneficio = VagaBeneficio.novo(vagaId, beneficioId);

        assertThat(vagaBeneficio).isNotNull();
        assertThat(vagaBeneficio.getVagaId()).isEqualTo(vagaId);
        assertThat(vagaBeneficio.getBeneficioId()).isEqualTo(beneficioId);
    }

    @Test
    void deveRehydratarVagaBeneficioExistente() {
        UUID vagaId = UUID.randomUUID();
        UUID beneficioId = UUID.randomUUID();

        VagaBeneficio vagaBeneficio = VagaBeneficio.rehydrate(vagaId, beneficioId);

        assertThat(vagaBeneficio.getVagaId()).isEqualTo(vagaId);
        assertThat(vagaBeneficio.getBeneficioId()).isEqualTo(beneficioId);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID vagaId = UUID.randomUUID();
        UUID beneficioId = UUID.randomUUID();

        assertThatThrownBy(() -> VagaBeneficio.novo(null, beneficioId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("vagaId must not be null");

        assertThatThrownBy(() -> VagaBeneficio.novo(vagaId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("beneficioId must not be null");
    }

    @Test
    void deveCompararVagaBeneficioCorretamente() {
        UUID vagaId = UUID.randomUUID();
        UUID beneficioId = UUID.randomUUID();

        VagaBeneficio vb1 = VagaBeneficio.novo(vagaId, beneficioId);
        VagaBeneficio vb2 = VagaBeneficio.novo(vagaId, beneficioId);
        VagaBeneficio vb3 = VagaBeneficio.novo(UUID.randomUUID(), beneficioId);

        assertThat(vb1).isEqualTo(vb2);
        assertThat(vb1).isNotEqualTo(vb3);
        assertThat(vb1.hashCode()).isEqualTo(vb2.hashCode());
    }
}

