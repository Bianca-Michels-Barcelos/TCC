package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.data.entity.PapelOrganizacao;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class MembroOrganizacaoTest {

    @Test
    void deveCriarNovoMembroComAtributosPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        MembroOrganizacao membro = MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.RECRUTADOR);

        assertThat(membro).isNotNull();
        assertThat(membro.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(membro.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(membro.getPapel()).isEqualTo(PapelOrganizacao.RECRUTADOR);
        assertThat(membro.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarMembroExistente() {
        UUID organizacaoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        MembroOrganizacao membro = MembroOrganizacao.rehydrate(organizacaoId, usuarioId, 
                PapelOrganizacao.ADMIN, false);

        assertThat(membro.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(membro.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(membro.getPapel()).isEqualTo(PapelOrganizacao.ADMIN);
        assertThat(membro.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarPapelMantendoImutabilidade() {
        MembroOrganizacao original = criarMembroPadrao();
        
        MembroOrganizacao atualizado = original.comPapel(PapelOrganizacao.ADMIN);

        assertThat(original.getPapel()).isEqualTo(PapelOrganizacao.RECRUTADOR);
        assertThat(atualizado.getPapel()).isEqualTo(PapelOrganizacao.ADMIN);
        assertThat(atualizado.getOrganizacaoId()).isEqualTo(original.getOrganizacaoId());
        assertThat(atualizado.getUsuarioId()).isEqualTo(original.getUsuarioId());
    }

    @Test
    void deveAtivarMembro() {
        MembroOrganizacao membro = criarMembroPadrao().desativar();
        
        MembroOrganizacao ativado = membro.ativar();

        assertThat(membro.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeMembroJaAtivo() {
        MembroOrganizacao membro = criarMembroPadrao();
        
        MembroOrganizacao resultado = membro.ativar();

        assertThat(resultado).isSameAs(membro);
    }

    @Test
    void deveDesativarMembro() {
        MembroOrganizacao membro = criarMembroPadrao();
        
        MembroOrganizacao desativado = membro.desativar();

        assertThat(membro.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeMembroJaInativo() {
        MembroOrganizacao membro = criarMembroPadrao().desativar();
        
        MembroOrganizacao resultado = membro.desativar();

        assertThat(resultado).isSameAs(membro);
    }

    @Test
    void deveVerificarSeMembroEhAdmin() {
        UUID organizacaoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        MembroOrganizacao admin = MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.ADMIN);
        MembroOrganizacao recrutador = MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.RECRUTADOR);

        assertThat(admin.isAdmin()).isTrue();
        assertThat(recrutador.isAdmin()).isFalse();
    }

    @Test
    void deveVerificarSeMembroEhRecrutador() {
        UUID organizacaoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        MembroOrganizacao admin = MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.ADMIN);
        MembroOrganizacao recrutador = MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.RECRUTADOR);

        assertThat(recrutador.isRecrutador()).isTrue();
        assertThat(admin.isRecrutador()).isFalse();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID organizacaoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        assertThatThrownBy(() -> MembroOrganizacao.novo(null, usuarioId, PapelOrganizacao.RECRUTADOR))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("organizacaoId must not be null");

        assertThatThrownBy(() -> MembroOrganizacao.novo(organizacaoId, null, PapelOrganizacao.RECRUTADOR))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("usuarioId must not be null");

        assertThatThrownBy(() -> MembroOrganizacao.novo(organizacaoId, usuarioId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("papel must not be null");
    }

    @Test
    void deveCompararMembrosCorretamente() {
        UUID organizacaoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        MembroOrganizacao membro1 = MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.RECRUTADOR);
        MembroOrganizacao membro2 = MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.ADMIN);
        MembroOrganizacao membro3 = MembroOrganizacao.novo(UUID.randomUUID(), usuarioId, PapelOrganizacao.RECRUTADOR);

        assertThat(membro1).isEqualTo(membro2);
        assertThat(membro1).isNotEqualTo(membro3);
        assertThat(membro1.hashCode()).isEqualTo(membro2.hashCode());
    }

    private MembroOrganizacao criarMembroPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        return MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.RECRUTADOR);
    }
}

