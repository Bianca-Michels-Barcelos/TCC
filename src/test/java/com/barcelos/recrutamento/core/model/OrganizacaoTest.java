package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.Cep;
import com.barcelos.recrutamento.core.model.vo.Cnpj;
import com.barcelos.recrutamento.core.model.vo.Endereco;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrganizacaoTest {

    @Test
    void deveCriarNovaOrganizacaoComAtributosPadrao() {
        Cnpj cnpj = new Cnpj("12345678000190");
        String nome = "Tech Company";
        Endereco endereco = criarEnderecoPadrao();

        Organizacao organizacao = Organizacao.novo(cnpj, nome, endereco);

        assertThat(organizacao).isNotNull();
        assertThat(organizacao.getId()).isNull();
        assertThat(organizacao.getCnpj()).isEqualTo(cnpj);
        assertThat(organizacao.getNome()).isEqualTo(nome);
        assertThat(organizacao.getEndereco()).isEqualTo(endereco);
        assertThat(organizacao.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarOrganizacaoExistente() {
        UUID id = UUID.randomUUID();
        Cnpj cnpj = new Cnpj("98765432000191");
        String nome = "Software Corp";
        Endereco endereco = criarEnderecoPadrao();

        Organizacao organizacao = Organizacao.rehydrate(id, cnpj, nome, endereco, false);

        assertThat(organizacao.getId()).isEqualTo(id);
        assertThat(organizacao.getCnpj()).isEqualTo(cnpj);
        assertThat(organizacao.getNome()).isEqualTo(nome);
        assertThat(organizacao.getEndereco()).isEqualTo(endereco);
        assertThat(organizacao.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarNomeMantendoImutabilidade() {
        Organizacao original = criarOrganizacaoPadrao();
        
        Organizacao atualizada = original.comNome("Novo Nome");

        assertThat(original.getNome()).isEqualTo("Tech Company");
        assertThat(atualizada.getNome()).isEqualTo("Novo Nome");
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarEnderecoMantendoImutabilidade() {
        Organizacao original = criarOrganizacaoPadrao();
        Endereco novoEndereco = new Endereco("Av Brasil", "Sala 200", "500", 
                new Cep("20000000"), "Rio de Janeiro", new Sigla("RJ"));
        
        Organizacao atualizada = original.comEndereco(novoEndereco);

        assertThat(original.getEndereco().cidade()).isEqualTo("São Paulo");
        assertThat(atualizada.getEndereco()).isEqualTo(novoEndereco);
    }

    @Test
    void deveAtivarOrganizacao() {
        Organizacao organizacao = criarOrganizacaoPadrao().desativar();
        
        Organizacao ativada = organizacao.ativar();

        assertThat(organizacao.isAtivo()).isFalse();
        assertThat(ativada.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeOrganizacaoJaAtiva() {
        Organizacao organizacao = criarOrganizacaoPadrao();
        
        Organizacao resultado = organizacao.ativar();

        assertThat(resultado).isSameAs(organizacao);
    }

    @Test
    void deveDesativarOrganizacao() {
        Organizacao organizacao = criarOrganizacaoPadrao();
        
        Organizacao desativada = organizacao.desativar();

        assertThat(organizacao.isAtivo()).isTrue();
        assertThat(desativada.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeOrganizacaoJaInativa() {
        Organizacao organizacao = criarOrganizacaoPadrao().desativar();
        
        Organizacao resultado = organizacao.desativar();

        assertThat(resultado).isSameAs(organizacao);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        Cnpj cnpj = new Cnpj("12345678000190");
        Endereco endereco = criarEnderecoPadrao();

        assertThatThrownBy(() -> Organizacao.novo(null, "Nome", endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("cnpj must not be null");

        assertThatThrownBy(() -> Organizacao.novo(cnpj, null, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("nome must not be null");

        assertThatThrownBy(() -> Organizacao.novo(cnpj, "Nome", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("endereco must not be null");
    }

    @Test
    void deveValidarIdObrigatorioAoRehydratar() {
        Cnpj cnpj = new Cnpj("12345678000190");
        Endereco endereco = criarEnderecoPadrao();

        assertThatThrownBy(() -> Organizacao.rehydrate(null, cnpj, "Nome", endereco, true))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id é obrigatório na rehidratação");
    }

    private Organizacao criarOrganizacaoPadrao() {
        UUID id = UUID.randomUUID();
        Cnpj cnpj = new Cnpj("12345678000190");
        Endereco endereco = criarEnderecoPadrao();
        return Organizacao.rehydrate(id, cnpj, "Tech Company", endereco, true);
    }

    private Endereco criarEnderecoPadrao() {
        return new Endereco("Rua das Flores", "Apto 101", "123", 
                new Cep("01234567"), "São Paulo", new Sigla("SP"));
    }
}

