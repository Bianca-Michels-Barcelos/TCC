package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CpfTest {

    @Test
    void deveCriarCpfValido() {
        Cpf cpf = new Cpf("12345678901");

        assertThat(cpf.value()).isEqualTo("12345678901");
    }

    @Test
    void deveRemoverMascaraDoCpf() {
        Cpf cpf = new Cpf("123.456.789-01");

        assertThat(cpf.value()).isEqualTo("12345678901");
    }

    @Test
    void deveRemoverEspacosECaracteresEspeciais() {
        Cpf cpf = new Cpf("123 456 789 01");

        assertThat(cpf.value()).isEqualTo("12345678901");
    }

    @Test
    void naoDeveCriarCpfNulo() {
        assertThatThrownBy(() -> new Cpf(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Cpf não pode ser nulo");
    }

    @Test
    void naoDeveCriarCpfComMenosDe11Digitos() {
        assertThatThrownBy(() -> new Cpf("1234567890"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Cpf inválido");
    }

    @Test
    void naoDeveCriarCpfComMaisDe11Digitos() {
        assertThatThrownBy(() -> new Cpf("123456789012"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Cpf inválido");
    }

    @Test
    void deveCompararCpfsCorretamente() {
        Cpf cpf1 = new Cpf("12345678901");
        Cpf cpf2 = new Cpf("123.456.789-01");
        Cpf cpf3 = new Cpf("98765432109");

        assertThat(cpf1).isEqualTo(cpf2);
        assertThat(cpf1).isNotEqualTo(cpf3);
    }

    @Test
    void deveRetornarStringCorreto() {
        Cpf cpf = new Cpf("123.456.789-01");

        assertThat(cpf.toString()).isEqualTo("12345678901");
    }
}

