package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.Cpf;
import com.barcelos.recrutamento.core.model.vo.Email;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UsuarioTest {

    @Test
    void deveCriarNovoUsuarioComAtributosPadrao() {
        String nome = "João Silva";
        Email email = new Email("joao@example.com");
        Cpf cpf = new Cpf("12345678901");
        String senhaHash = "$2a$10$hash123";

        Usuario usuario = Usuario.novo(nome, email, cpf, senhaHash);

        assertThat(usuario).isNotNull();
        assertThat(usuario.getId()).isNull();
        assertThat(usuario.getNome()).isEqualTo(nome);
        assertThat(usuario.getEmail()).isEqualTo(email);
        assertThat(usuario.getCpf()).isEqualTo(cpf);
        assertThat(usuario.getSenhaHash()).isEqualTo(senhaHash);
        assertThat(usuario.isEmailVerificado()).isFalse();
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarUsuarioExistente() {
        UUID id = UUID.randomUUID();
        String nome = "Maria Santos";
        Email email = new Email("maria@example.com");
        Cpf cpf = new Cpf("98765432109");
        String senhaHash = "$2a$10$hash456";

        Usuario usuario = Usuario.rehydrate(id, nome, email, cpf, senhaHash, true, false);

        assertThat(usuario.getId()).isEqualTo(id);
        assertThat(usuario.getNome()).isEqualTo(nome);
        assertThat(usuario.getEmail()).isEqualTo(email);
        assertThat(usuario.getCpf()).isEqualTo(cpf);
        assertThat(usuario.getSenhaHash()).isEqualTo(senhaHash);
        assertThat(usuario.isEmailVerificado()).isTrue();
        assertThat(usuario.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarNomeMantendoImutabilidade() {
        Usuario original = criarUsuarioPadrao();
        
        Usuario atualizado = original.comNome("Novo Nome");

        assertThat(original.getNome()).isEqualTo("João Silva");
        assertThat(atualizado.getNome()).isEqualTo("Novo Nome");
        assertThat(atualizado.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarEmailMantendoImutabilidade() {
        Usuario original = criarUsuarioPadrao();
        Email novoEmail = new Email("novo@example.com");
        
        Usuario atualizado = original.comEmail(novoEmail);

        assertThat(original.getEmail().value()).isEqualTo("joao@example.com");
        assertThat(atualizado.getEmail()).isEqualTo(novoEmail);
        assertThat(atualizado.isEmailVerificado()).isFalse();
    }

    @Test
    void deveManterEmailVerificadoSeEmailNaoMudar() {
        Usuario usuario = criarUsuarioPadrao().verificarEmail();
        
        Usuario resultado = usuario.comEmail(new Email("joao@example.com"));

        assertThat(resultado).isEqualTo(usuario);
        assertThat(resultado.isEmailVerificado()).isTrue();
    }

    @Test
    void deveAtualizarSenhaHashMantendoImutabilidade() {
        Usuario original = criarUsuarioPadrao();
        String novaSenhaHash = "$2a$10$newhash";
        
        Usuario atualizado = original.comSenhaHash(novaSenhaHash);

        assertThat(original.getSenhaHash()).isEqualTo("$2a$10$hash123");
        assertThat(atualizado.getSenhaHash()).isEqualTo(novaSenhaHash);
    }

    @Test
    void deveVerificarEmail() {
        Usuario usuario = criarUsuarioPadrao();
        
        Usuario verificado = usuario.verificarEmail();

        assertThat(usuario.isEmailVerificado()).isFalse();
        assertThat(verificado.isEmailVerificado()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeEmailJaVerificado() {
        Usuario usuario = criarUsuarioPadrao().verificarEmail();
        
        Usuario resultado = usuario.verificarEmail();

        assertThat(resultado).isSameAs(usuario);
    }

    @Test
    void deveAtivarUsuario() {
        Usuario usuario = criarUsuarioPadrao();
        Usuario desativado = usuario.desativar();
        
        Usuario ativado = desativado.ativar();

        assertThat(desativado.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeUsuarioJaAtivo() {
        Usuario usuario = criarUsuarioPadrao();
        
        Usuario resultado = usuario.ativar();

        assertThat(resultado).isSameAs(usuario);
    }

    @Test
    void deveDesativarUsuario() {
        Usuario usuario = criarUsuarioPadrao();
        
        Usuario desativado = usuario.desativar();

        assertThat(usuario.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeUsuarioJaInativo() {
        Usuario usuario = criarUsuarioPadrao().desativar();
        
        Usuario resultado = usuario.desativar();

        assertThat(resultado).isSameAs(usuario);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        Email email = new Email("teste@example.com");
        Cpf cpf = new Cpf("12345678901");
        String senhaHash = "$2a$10$hash";

        assertThatThrownBy(() -> Usuario.novo(null, email, cpf, senhaHash))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("nome must not be null");

        assertThatThrownBy(() -> Usuario.novo("Nome", null, cpf, senhaHash))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("email must not be null");

        assertThatThrownBy(() -> Usuario.novo("Nome", email, null, senhaHash))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("cpf must not be null");

        assertThatThrownBy(() -> Usuario.novo("Nome", email, cpf, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("senhaHash must not be null");
    }

    @Test
    void deveValidarIdObrigatorioAoRehydratar() {
        Email email = new Email("teste@example.com");
        Cpf cpf = new Cpf("12345678901");
        String senhaHash = "$2a$10$hash";

        assertThatThrownBy(() -> Usuario.rehydrate(null, "Nome", email, cpf, senhaHash, false, true))
                .isInstanceOf(NullPointerException.class);
    }

    private Usuario criarUsuarioPadrao() {
        UUID id = UUID.randomUUID();
        String nome = "João Silva";
        Email email = new Email("joao@example.com");
        Cpf cpf = new Cpf("12345678901");
        String senhaHash = "$2a$10$hash123";

        return Usuario.rehydrate(id, nome, email, cpf, senhaHash, false, true);
    }
}

