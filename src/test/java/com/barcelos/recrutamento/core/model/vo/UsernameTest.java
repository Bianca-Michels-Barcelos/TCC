package com.barcelos.recrutamento.core.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UsernameTest {

    @Test
    void deveCriarUsernameValido() {
        Username username = new Username("johndoe");

        assertThat(username.value()).isEqualTo("johndoe");
    }

    @Test
    void deveRemoverEspacosEmBranco() {
        Username username = new Username("  johndoe  ");

        assertThat(username.value()).isEqualTo("johndoe");
    }

    @Test
    void naoDeveCriarUsernameNulo() {
        assertThatThrownBy(() -> new Username(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Username nāo pode ser nulo");
    }

    @Test
    void naoDeveCriarUsernameVazio() {
        assertThatThrownBy(() -> new Username(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username inválido");
    }

    @Test
    void naoDeveCriarUsernameEmBranco() {
        assertThatThrownBy(() -> new Username("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username inválido");
    }

    @Test
    void naoDeveCriarUsernameComMaisDe50Caracteres() {
        String usernameLongo = "a".repeat(51);

        assertThatThrownBy(() -> new Username(usernameLongo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username inválido");
    }

    @Test
    void deveAceitarUsernameCom50Caracteres() {
        String username50 = "a".repeat(50);

        assertThatNoException().isThrownBy(() -> new Username(username50));
    }

    @Test
    void deveCompararUsernamesCorretamente() {
        Username username1 = new Username("johndoe");
        Username username2 = new Username("  johndoe  ");
        Username username3 = new Username("janedoe");

        assertThat(username1).isEqualTo(username2);
        assertThat(username1).isNotEqualTo(username3);
    }

    @Test
    void deveRetornarStringCorreto() {
        Username username = new Username("  johndoe  ");

        assertThat(username.toString()).isEqualTo("johndoe");
    }
}

