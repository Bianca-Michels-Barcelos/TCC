package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.Cep;
import com.barcelos.recrutamento.core.model.vo.Endereco;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PerfilCandidatoTest {

    @Test
    void deveCriarNovoPerfilComAtributosPadrao() {
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataNascimento = LocalDate.of(1990, 5, 15);
        Endereco endereco = criarEnderecoPadrao();

        PerfilCandidato perfil = PerfilCandidato.novo(usuarioId, dataNascimento, endereco);

        assertThat(perfil).isNotNull();
        assertThat(perfil.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(perfil.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(perfil.getEndereco()).isEqualTo(endereco);
        assertThat(perfil.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarPerfilExistente() {
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataNascimento = LocalDate.of(1985, 10, 20);
        Endereco endereco = criarEnderecoPadrao();

        PerfilCandidato perfil = PerfilCandidato.rehydrate(UUID.randomUUID(), usuarioId, 
                dataNascimento, endereco, false);

        assertThat(perfil.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(perfil.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(perfil.getEndereco()).isEqualTo(endereco);
        assertThat(perfil.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarDataNascimentoMantendoImutabilidade() {
        PerfilCandidato original = criarPerfilPadrao();
        LocalDate novaData = LocalDate.of(1995, 3, 10);
        
        PerfilCandidato atualizado = original.comDataNascimento(novaData);

        assertThat(original.getDataNascimento()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(atualizado.getDataNascimento()).isEqualTo(novaData);
        assertThat(atualizado.getUsuarioId()).isEqualTo(original.getUsuarioId());
    }

    @Test
    void deveAtualizarEnderecoMantendoImutabilidade() {
        PerfilCandidato original = criarPerfilPadrao();
        Endereco novoEndereco = new Endereco("Av Brasil", "Bloco B", "500", 
                new Cep("20000000"), "Rio de Janeiro", new Sigla("RJ"));
        
        PerfilCandidato atualizado = original.comEndereco(novoEndereco);

        assertThat(original.getEndereco().cidade()).isEqualTo("São Paulo");
        assertThat(atualizado.getEndereco()).isEqualTo(novoEndereco);
    }

    @Test
    void deveAtivarPerfil() {
        PerfilCandidato perfil = criarPerfilPadrao().desativar();
        
        PerfilCandidato ativado = perfil.ativar();

        assertThat(perfil.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSePerfilJaAtivo() {
        PerfilCandidato perfil = criarPerfilPadrao();
        
        PerfilCandidato resultado = perfil.ativar();

        assertThat(resultado).isSameAs(perfil);
    }

    @Test
    void deveDesativarPerfil() {
        PerfilCandidato perfil = criarPerfilPadrao();
        
        PerfilCandidato desativado = perfil.desativar();

        assertThat(perfil.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSePerfilJaInativo() {
        PerfilCandidato perfil = criarPerfilPadrao().desativar();
        
        PerfilCandidato resultado = perfil.desativar();

        assertThat(resultado).isSameAs(perfil);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataNascimento = LocalDate.of(1990, 5, 15);
        Endereco endereco = criarEnderecoPadrao();

        assertThatThrownBy(() -> PerfilCandidato.novo(null, dataNascimento, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("usuarioId must not be null");

        assertThatThrownBy(() -> PerfilCandidato.novo(usuarioId, null, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("dataNascimento must not be null");

        assertThatThrownBy(() -> PerfilCandidato.novo(usuarioId, dataNascimento, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("endereco must not be null");
    }

    private PerfilCandidato criarPerfilPadrao() {
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataNascimento = LocalDate.of(1990, 5, 15);
        Endereco endereco = criarEnderecoPadrao();
        return PerfilCandidato.novo(usuarioId, dataNascimento, endereco);
    }

    private Endereco criarEnderecoPadrao() {
        return new Endereco("Rua das Flores", "Apto 101", "123", 
                new Cep("01234567"), "São Paulo", new Sigla("SP"));
    }
}

