package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AvaliacaoOrganizacaoTest {

    @Test
    void deveCriarNovaAvaliacaoComAtributosPadrao() {
        UUID processoId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        int nota = 5;
        String comentario = "Excelente processo seletivo";

        AvaliacaoOrganizacao avaliacao = AvaliacaoOrganizacao.nova(processoId, candidatoId, 
                organizacaoId, nota, comentario);

        assertThat(avaliacao).isNotNull();
        assertThat(avaliacao.getId()).isNotNull();
        assertThat(avaliacao.getProcessoId()).isEqualTo(processoId);
        assertThat(avaliacao.getCandidatoUsuarioId()).isEqualTo(candidatoId);
        assertThat(avaliacao.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(avaliacao.getNota()).isEqualTo(nota);
        assertThat(avaliacao.getComentario()).isEqualTo(comentario);
        assertThat(avaliacao.getCriadoEm()).isNotNull();
        assertThat(avaliacao.getAtualizadoEm()).isNotNull();
    }

    @Test
    void deveRehydratarAvaliacaoExistente() {
        UUID id = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        LocalDateTime criadoEm = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime atualizadoEm = LocalDateTime.of(2025, 1, 16, 11, 0);

        AvaliacaoOrganizacao avaliacao = AvaliacaoOrganizacao.rehydrate(id, processoId, 
                candidatoId, organizacaoId, 4, "Bom processo", criadoEm, atualizadoEm);

        assertThat(avaliacao.getId()).isEqualTo(id);
        assertThat(avaliacao.getCriadoEm()).isEqualTo(criadoEm);
        assertThat(avaliacao.getAtualizadoEm()).isEqualTo(atualizadoEm);
    }

    @Test
    void deveAtualizarAvaliacaoMantendoImutabilidade() {
        AvaliacaoOrganizacao original = criarAvaliacaoPadrao();
        
        AvaliacaoOrganizacao atualizada = original.atualizar(3, "Comentário atualizado");

        assertThat(original.getNota()).isEqualTo(5);
        assertThat(original.getComentario()).isEqualTo("Excelente processo seletivo");
        assertThat(atualizada.getNota()).isEqualTo(3);
        assertThat(atualizada.getComentario()).isEqualTo("Comentário atualizado");
        assertThat(atualizada.getCriadoEm()).isEqualTo(original.getCriadoEm());
        assertThat(atualizada.getAtualizadoEm()).isAfter(original.getAtualizadoEm());
    }

    @Test
    void deveVerificarSeAvaliacaoEhPositiva() {
        UUID processoId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        AvaliacaoOrganizacao avaliacao4 = AvaliacaoOrganizacao.nova(processoId, candidatoId, 
                organizacaoId, 4, "Comentário");
        AvaliacaoOrganizacao avaliacao5 = AvaliacaoOrganizacao.nova(processoId, candidatoId, 
                organizacaoId, 5, "Comentário");
        AvaliacaoOrganizacao avaliacao3 = AvaliacaoOrganizacao.nova(processoId, candidatoId, 
                organizacaoId, 3, "Comentário");

        assertThat(avaliacao4.isPositiva()).isTrue();
        assertThat(avaliacao5.isPositiva()).isTrue();
        assertThat(avaliacao3.isPositiva()).isFalse();
    }

    @Test
    void deveVerificarSeAvaliacaoEhNegativa() {
        UUID processoId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        AvaliacaoOrganizacao avaliacao1 = AvaliacaoOrganizacao.nova(processoId, candidatoId, 
                organizacaoId, 1, "Comentário");
        AvaliacaoOrganizacao avaliacao2 = AvaliacaoOrganizacao.nova(processoId, candidatoId, 
                organizacaoId, 2, "Comentário");
        AvaliacaoOrganizacao avaliacao3 = AvaliacaoOrganizacao.nova(processoId, candidatoId, 
                organizacaoId, 3, "Comentário");

        assertThat(avaliacao1.isNegativa()).isTrue();
        assertThat(avaliacao2.isNegativa()).isTrue();
        assertThat(avaliacao3.isNegativa()).isFalse();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID processoId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        assertThatThrownBy(() -> AvaliacaoOrganizacao.nova(null, candidatoId, organizacaoId, 
                5, "Comentário"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> AvaliacaoOrganizacao.nova(processoId, null, organizacaoId, 
                5, "Comentário"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> AvaliacaoOrganizacao.nova(processoId, candidatoId, null, 
                5, "Comentário"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> AvaliacaoOrganizacao.nova(processoId, candidatoId, organizacaoId, 
                5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("comentario must not be null or blank");

        assertThatThrownBy(() -> AvaliacaoOrganizacao.nova(processoId, candidatoId, organizacaoId, 
                5, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("comentario must not be null or blank");
    }

    @Test
    void deveValidarNotaEntre1E5() {
        UUID processoId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        assertThatThrownBy(() -> AvaliacaoOrganizacao.nova(processoId, candidatoId, organizacaoId, 
                0, "Comentário"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nota must be between 1 and 5");

        assertThatThrownBy(() -> AvaliacaoOrganizacao.nova(processoId, candidatoId, organizacaoId, 
                6, "Comentário"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nota must be between 1 and 5");
    }

    @Test
    void deveValidarNotaAoAtualizar() {
        AvaliacaoOrganizacao avaliacao = criarAvaliacaoPadrao();

        assertThatThrownBy(() -> avaliacao.atualizar(0, "Comentário"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nota must be between 1 and 5");

        assertThatThrownBy(() -> avaliacao.atualizar(6, "Comentário"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nota must be between 1 and 5");
    }

    private AvaliacaoOrganizacao criarAvaliacaoPadrao() {
        UUID processoId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        return AvaliacaoOrganizacao.nova(processoId, candidatoId, organizacaoId, 
                5, "Excelente processo seletivo");
    }
}

