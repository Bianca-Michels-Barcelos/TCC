package com.barcelos.recrutamento.core.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EnderecoSimplesTest {

    @Test
    void deveCriarEnderecoSimples() {
        Sigla uf = new Sigla("SP");

        EnderecoSimples endereco = new EnderecoSimples("São Paulo", uf);

        assertThat(endereco.cidade()).isEqualTo("São Paulo");
        assertThat(endereco.uf()).isEqualTo(uf);
    }

    @Test
    void deveCompararEnderecosCorretamente() {
        Sigla uf1 = new Sigla("SP");
        Sigla uf2 = new Sigla("RJ");

        EnderecoSimples endereco1 = new EnderecoSimples("São Paulo", uf1);
        EnderecoSimples endereco2 = new EnderecoSimples("São Paulo", uf1);
        EnderecoSimples endereco3 = new EnderecoSimples("Rio de Janeiro", uf2);

        assertThat(endereco1).isEqualTo(endereco2);
        assertThat(endereco1).isNotEqualTo(endereco3);
    }

    @Test
    void deveCriarEnderecoComDiferentesUFs() {
        EnderecoSimples sp = new EnderecoSimples("São Paulo", new Sigla("SP"));
        EnderecoSimples rj = new EnderecoSimples("Rio de Janeiro", new Sigla("RJ"));
        EnderecoSimples mg = new EnderecoSimples("Belo Horizonte", new Sigla("MG"));

        assertThat(sp.uf().value()).isEqualTo("SP");
        assertThat(rj.uf().value()).isEqualTo("RJ");
        assertThat(mg.uf().value()).isEqualTo("MG");
    }
}

