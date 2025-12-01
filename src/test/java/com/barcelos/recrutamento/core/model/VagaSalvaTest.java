package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class VagaSalvaTest {

    @Test
    void deveCriarNovaVagaSalvaComAtributosPadrao() {
        UUID vagaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        VagaSalva vagaSalva = VagaSalva.nova(vagaId, usuarioId);

        assertThat(vagaSalva).isNotNull();
        assertThat(vagaSalva.getId()).isNotNull();
        assertThat(vagaSalva.getVagaId()).isEqualTo(vagaId);
        assertThat(vagaSalva.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(vagaSalva.getSalvaEm()).isNotNull();
    }

    @Test
    void deveRehydratarVagaSalvaExistente() {
        UUID id = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime salvaEm = LocalDateTime.of(2025, 1, 15, 10, 30);

        VagaSalva vagaSalva = VagaSalva.rehydrate(id, vagaId, usuarioId, salvaEm);

        assertThat(vagaSalva.getId()).isEqualTo(id);
        assertThat(vagaSalva.getVagaId()).isEqualTo(vagaId);
        assertThat(vagaSalva.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(vagaSalva.getSalvaEm()).isEqualTo(salvaEm);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID vagaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        assertThatThrownBy(() -> VagaSalva.nova(null, usuarioId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("vagaId must not be null");

        assertThatThrownBy(() -> VagaSalva.nova(vagaId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("usuarioId must not be null");
    }

    @Test
    void deveValidarCamposObrigatoriosAoRehydratar() {
        UUID id = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime salvaEm = LocalDateTime.now();

        assertThatThrownBy(() -> VagaSalva.rehydrate(null, vagaId, usuarioId, salvaEm))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VagaSalva.rehydrate(id, null, usuarioId, salvaEm))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VagaSalva.rehydrate(id, vagaId, null, salvaEm))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VagaSalva.rehydrate(id, vagaId, usuarioId, null))
                .isInstanceOf(NullPointerException.class);
    }
}

