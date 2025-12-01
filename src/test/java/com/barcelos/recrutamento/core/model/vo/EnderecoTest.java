package com.barcelos.recrutamento.core.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EnderecoTest {

    @Test
    void deveCriarEnderecoCompleto() {
        Cep cep = new Cep("01234567");
        Sigla uf = new Sigla("SP");

        Endereco endereco = new Endereco("Rua das Flores", "Apto 101", "123", cep, "São Paulo", uf);

        assertThat(endereco.logradouro()).isEqualTo("Rua das Flores");
        assertThat(endereco.complemento()).isEqualTo("Apto 101");
        assertThat(endereco.numero()).isEqualTo("123");
        assertThat(endereco.cep()).isEqualTo(cep);
        assertThat(endereco.cidade()).isEqualTo("São Paulo");
        assertThat(endereco.uf()).isEqualTo(uf);
    }

    @Test
    void deveCriarEnderecoSemComplemento() {
        Cep cep = new Cep("01234567");
        Sigla uf = new Sigla("SP");

        Endereco endereco = new Endereco("Rua das Flores", null, "123", cep, "São Paulo", uf);

        assertThat(endereco.complemento()).isNull();
    }

    @Test
    void deveCompararEnderecosCorretamente() {
        Cep cep = new Cep("01234567");
        Sigla uf = new Sigla("SP");

        Endereco endereco1 = new Endereco("Rua das Flores", "Apto 101", "123", cep, "São Paulo", uf);
        Endereco endereco2 = new Endereco("Rua das Flores", "Apto 101", "123", cep, "São Paulo", uf);
        Endereco endereco3 = new Endereco("Av Brasil", null, "500", cep, "São Paulo", uf);

        assertThat(endereco1).isEqualTo(endereco2);
        assertThat(endereco1).isNotEqualTo(endereco3);
    }
}

