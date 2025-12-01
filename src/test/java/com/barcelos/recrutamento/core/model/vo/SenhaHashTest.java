package com.barcelos.recrutamento.core.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SenhaHashTest {

    @Test
    void deveCriarSenhaHashValida() {
        SenhaHash senhaHash = new SenhaHash("$2a$10$hash123");

        assertThat(senhaHash.value()).isEqualTo("$2a$10$hash123");
    }

    @Test
    void naoDeveCriarSenhaHashNula() {
        assertThatThrownBy(() -> new SenhaHash(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Senha hash não pode ser nula");
    }

    @Test
    void naoDeveCriarSenhaHashVazia() {
        assertThatThrownBy(() -> new SenhaHash(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha hash inválida");
    }

    @Test
    void naoDeveCriarSenhaHashEmBranco() {
        assertThatThrownBy(() -> new SenhaHash("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha hash inválida");
    }

    @Test
    void deveCompararSenhasHashCorretamente() {
        SenhaHash hash1 = new SenhaHash("$2a$10$hash123");
        SenhaHash hash2 = new SenhaHash("$2a$10$hash123");
        SenhaHash hash3 = new SenhaHash("$2a$10$hash456");

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).isNotEqualTo(hash3);
    }

    @Test
    void deveRetornarStringCorreto() {
        SenhaHash senhaHash = new SenhaHash("$2a$10$hash123");

        assertThat(senhaHash.toString()).isEqualTo("$2a$10$hash123");
    }
}

