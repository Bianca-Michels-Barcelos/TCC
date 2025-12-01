package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SiglaTest {

    @Test
    void deveCriarSiglaValida() {
        Sigla sigla = new Sigla("SP");

        assertThat(sigla.value()).isEqualTo("SP");
    }

    @Test
    void deveConverterSiglaParaUpperCase() {
        Sigla sigla = new Sigla("sp");

        assertThat(sigla.value()).isEqualTo("SP");
    }

    @Test
    void deveAceitarSiglasComDoisCaracteres() {
        assertThatNoException().isThrownBy(() -> new Sigla("RJ"));
        assertThatNoException().isThrownBy(() -> new Sigla("MG"));
        assertThatNoException().isThrownBy(() -> new Sigla("BA"));
    }

    @Test
    void naoDeveCriarSiglaNula() {
        assertThatThrownBy(() -> new Sigla(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Sigla não pode ser nula");
    }

    @Test
    void naoDeveCriarSiglaComMenosDeDoisCaracteres() {
        assertThatThrownBy(() -> new Sigla("S"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Sigla precisa ter dois caracteres alfabéticos");
    }

    @Test
    void naoDeveCriarSiglaComMaisDeDoisCaracteres() {
        assertThatThrownBy(() -> new Sigla("SPP"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Sigla precisa ter dois caracteres alfabéticos");
    }

    @Test
    void naoDeveCriarSiglaComNumeros() {
        assertThatThrownBy(() -> new Sigla("S1"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Sigla precisa ter dois caracteres alfabéticos");
    }

    @Test
    void deveCompararSiglasCorretamente() {
        Sigla sigla1 = new Sigla("SP");
        Sigla sigla2 = new Sigla("sp");
        Sigla sigla3 = new Sigla("RJ");

        assertThat(sigla1).isEqualTo(sigla2);
        assertThat(sigla1).isNotEqualTo(sigla3);
    }

    @Test
    void deveRetornarStringCorreto() {
        Sigla sigla = new Sigla("sp");

        assertThat(sigla.toString()).isEqualTo("SP");
    }
}

