package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @Test
    void deveCriarEmailValido() {
        Email email = new Email("teste@example.com");

        assertThat(email.value()).isEqualTo("teste@example.com");
    }

    @Test
    void deveConverterEmailParaLowerCase() {
        Email email = new Email("TESTE@EXAMPLE.COM");

        assertThat(email.value()).isEqualTo("teste@example.com");
    }

    @Test
    void deveAceitarEmailsComFormatosDiversos() {
        assertThatNoException().isThrownBy(() -> new Email("user@domain.com"));
        assertThatNoException().isThrownBy(() -> new Email("user.name@domain.com"));
        assertThatNoException().isThrownBy(() -> new Email("user+tag@domain.com"));
        assertThatNoException().isThrownBy(() -> new Email("user@subdomain.domain.com"));
    }

    @Test
    void naoDeveCriarEmailNulo() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Email não pode ser nulo");
    }

    @Test
    void naoDeveCriarEmailInvalido() {
        assertThatThrownBy(() -> new Email("invalid"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email inválido");

        assertThatThrownBy(() -> new Email("@example.com"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email inválido");

        assertThatThrownBy(() -> new Email("user@"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email inválido");

        assertThatThrownBy(() -> new Email("user"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email inválido");
    }

    @Test
    void deveCompararEmailsCorretamente() {
        Email email1 = new Email("teste@example.com");
        Email email2 = new Email("TESTE@EXAMPLE.COM");
        Email email3 = new Email("outro@example.com");

        assertThat(email1).isEqualTo(email2);
        assertThat(email1).isNotEqualTo(email3);
    }

    @Test
    void deveRetornarStringCorrect() {
        Email email = new Email("teste@example.com");

        assertThat(email.toString()).isEqualTo("teste@example.com");
    }
}

