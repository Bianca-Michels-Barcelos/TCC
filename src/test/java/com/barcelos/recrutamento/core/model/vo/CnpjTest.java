package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CnpjTest {

    @Test
    void deveCriarCnpjValido() {
        Cnpj cnpj = new Cnpj("12345678000190");

        assertThat(cnpj.value()).isEqualTo("12345678000190");
    }

    @Test
    void deveRemoverMascaraDoCnpj() {
        Cnpj cnpj = new Cnpj("12.345.678/0001-90");

        assertThat(cnpj.value()).isEqualTo("12345678000190");
    }

    @Test
    void deveRemoverEspacosECaracteresEspeciais() {
        Cnpj cnpj = new Cnpj("12 345 678 0001 90");

        assertThat(cnpj.value()).isEqualTo("12345678000190");
    }

    @Test
    void naoDeveCriarCnpjNulo() {
        assertThatThrownBy(() -> new Cnpj(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("CNPJ não pode ser nulo");
    }

    @Test
    void naoDeveCriarCnpjComMenosDe14Digitos() {
        assertThatThrownBy(() -> new Cnpj("1234567800019"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("CNPJ inválido");
    }

    @Test
    void naoDeveCriarCnpjComMaisDe14Digitos() {
        assertThatThrownBy(() -> new Cnpj("123456780001901"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("CNPJ inválido");
    }

    @Test
    void deveCompararCnpjsCorretamente() {
        Cnpj cnpj1 = new Cnpj("12345678000190");
        Cnpj cnpj2 = new Cnpj("12.345.678/0001-90");
        Cnpj cnpj3 = new Cnpj("98765432000191");

        assertThat(cnpj1).isEqualTo(cnpj2);
        assertThat(cnpj1).isNotEqualTo(cnpj3);
    }

    @Test
    void deveRetornarStringCorreto() {
        Cnpj cnpj = new Cnpj("12.345.678/0001-90");

        assertThat(cnpj.toString()).isEqualTo("12345678000190");
    }
}

