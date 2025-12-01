package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CepTest {

    @Test
    void deveCriarCepValido() {
        Cep cep = new Cep("01234567");

        assertThat(cep.value()).isEqualTo("01234567");
    }

    @Test
    void deveRemoverMascaraDoCep() {
        Cep cep = new Cep("01234-567");

        assertThat(cep.value()).isEqualTo("01234567");
    }

    @Test
    void deveRemoverEspacosECaracteresEspeciais() {
        Cep cep = new Cep("012 345 67");

        assertThat(cep.value()).isEqualTo("01234567");
    }

    @Test
    void naoDeveCriarCepNulo() {
        assertThatThrownBy(() -> new Cep(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("CEP não pode ser nulo");
    }

    @Test
    void naoDeveCriarCepComMenosDe8Digitos() {
        assertThatThrownBy(() -> new Cep("0123456"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("CEP inválido");
    }

    @Test
    void naoDeveCriarCepComMaisDe8Digitos() {
        assertThatThrownBy(() -> new Cep("012345678"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("CEP inválido");
    }

    @Test
    void deveCompararCepsCorretamente() {
        Cep cep1 = new Cep("01234567");
        Cep cep2 = new Cep("01234-567");
        Cep cep3 = new Cep("98765432");

        assertThat(cep1).isEqualTo(cep2);
        assertThat(cep1).isNotEqualTo(cep3);
    }

    @Test
    void deveRetornarStringCorreto() {
        Cep cep = new Cep("01234-567");

        assertThat(cep.toString()).isEqualTo("01234567");
    }
}

