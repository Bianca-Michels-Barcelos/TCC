package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class BeneficioOrgTest {

    @Test
    void deveCriarNovoBeneficioComAtributosPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        String nome = "Vale Refeição";
        String descricao = "Vale refeição de R$ 30,00 por dia";

        BeneficioOrg beneficio = BeneficioOrg.novo(organizacaoId, nome, descricao);

        assertThat(beneficio).isNotNull();
        assertThat(beneficio.getId()).isNotNull();
        assertThat(beneficio.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(beneficio.getNome()).isEqualTo(nome);
        assertThat(beneficio.getDescricao()).isEqualTo(descricao);
    }

    @Test
    void deveRehydratarBeneficioExistente() {
        UUID id = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        BeneficioOrg beneficio = BeneficioOrg.rehydrate(id, organizacaoId, "Plano de Saúde", 
                "Cobertura nacional");

        assertThat(beneficio.getId()).isEqualTo(id);
        assertThat(beneficio.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(beneficio.getNome()).isEqualTo("Plano de Saúde");
        assertThat(beneficio.getDescricao()).isEqualTo("Cobertura nacional");
    }

    @Test
    void deveAtualizarBeneficio() {
        UUID id = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        BeneficioOrg atualizado = BeneficioOrg.atualizar(id, organizacaoId, "Novo Nome", 
                "Nova Descrição");

        assertThat(atualizado.getId()).isEqualTo(id);
        assertThat(atualizado.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(atualizado.getNome()).isEqualTo("Novo Nome");
        assertThat(atualizado.getDescricao()).isEqualTo("Nova Descrição");
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID organizacaoId = UUID.randomUUID();

        assertThatThrownBy(() -> BeneficioOrg.novo(null, "Nome", "Descrição"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("organizacaoId must not be null");

        assertThatThrownBy(() -> BeneficioOrg.novo(organizacaoId, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome must not be blank");

        assertThatThrownBy(() -> BeneficioOrg.novo(organizacaoId, "   ", "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome must not be blank");

        assertThatThrownBy(() -> BeneficioOrg.novo(organizacaoId, "Nome", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");

        assertThatThrownBy(() -> BeneficioOrg.novo(organizacaoId, "Nome", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");
    }

    @Test
    void deveValidarTamanhoMaximoDoNome() {
        UUID organizacaoId = UUID.randomUUID();
        String nomeLongo = "a".repeat(81);

        assertThatThrownBy(() -> BeneficioOrg.novo(organizacaoId, nomeLongo, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome max 80 chars");
    }
}

