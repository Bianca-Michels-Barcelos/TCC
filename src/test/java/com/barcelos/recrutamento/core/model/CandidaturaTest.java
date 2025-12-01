package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o domain model Candidatura.
 * Testa comportamento de negócio sem dependências de framework.
 */
class CandidaturaTest {

    @Test
    void deveCriarNovaCandidaturaComStatusPendente() {
        UUID vagaId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        String curriculo = "curriculo.pdf";

        Candidatura candidatura = Candidatura.nova(vagaId, candidatoId, curriculo);

        assertNotNull(candidatura);
        assertNotNull(candidatura.getId());
        assertEquals(vagaId, candidatura.getVagaId());
        assertEquals(candidatoId, candidatura.getCandidatoUsuarioId());
        assertEquals(StatusCandidatura.PENDENTE, candidatura.getStatus());
        assertEquals(LocalDate.now(), candidatura.getDataCandidatura());
        assertEquals(curriculo, candidatura.getArquivoCurriculo());
        assertNull(candidatura.getCompatibilidade());
        assertTrue(candidatura.isPendente());
    }

    @Test
    void deveRehydratarCandidaturaExistente() {
        UUID id = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2025, 1, 15);
        BigDecimal compatibilidade = new BigDecimal("85.5");

        Candidatura candidatura = Candidatura.rehydrate(
            id, vagaId, candidatoId,
            StatusCandidatura.ACEITA, data,
            "curriculo.pdf", compatibilidade
        );

        assertEquals(id, candidatura.getId());
        assertEquals(StatusCandidatura.ACEITA, candidatura.getStatus());
        assertEquals(data, candidatura.getDataCandidatura());
        assertEquals(compatibilidade, candidatura.getCompatibilidade());
        assertTrue(candidatura.isAceita());
    }

    @Test
    void deveAtualizarStatusMantendoImutabilidade() {
        Candidatura original = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        );

        Candidatura atualizada = original.comStatus(StatusCandidatura.ACEITA);

        // Original permanece inalterado
        assertEquals(StatusCandidatura.PENDENTE, original.getStatus());
        assertTrue(original.isPendente());

        // Nova instância com status atualizado
        assertEquals(StatusCandidatura.ACEITA, atualizada.getStatus());
        assertTrue(atualizada.isAceita());
        assertEquals(original.getId(), atualizada.getId());
    }

    @Test
    void deveAceitarCandidaturaPendente() {
        Candidatura candidatura = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        );

        Candidatura aceita = candidatura.aceitar();

        assertTrue(aceita.isAceita());
        assertEquals(StatusCandidatura.ACEITA, aceita.getStatus());
    }

    @Test
    void naoDeveAceitarCandidaturaJaRejeitada() {
        Candidatura candidatura = Candidatura.nova(
                UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        ).rejeitar();

        assertThrows(IllegalStateException.class, candidatura::aceitar);
    }

    @Test
    void deveRejeitarCandidaturaPendente() {
        Candidatura candidatura = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        );

        Candidatura rejeitada = candidatura.rejeitar();

        assertTrue(rejeitada.isRejeitada());
        assertEquals(StatusCandidatura.REJEITADA, rejeitada.getStatus());
    }

    @Test
    void naoDeveRejeitarCandidaturaJaAceita() {
        Candidatura candidatura = Candidatura.nova(
                UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        ).aceitar();

        assertThrows(IllegalStateException.class, candidatura::rejeitar);
    }

    @Test
    void deveAtualizarCompatibilidadeMantendoImutabilidade() {
        Candidatura original = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        );

        BigDecimal compatibilidade = new BigDecimal("92.5");
        Candidatura atualizada = original.comCompatibilidade(compatibilidade);

        assertNull(original.getCompatibilidade());
        assertFalse(original.possuiCompatibilidadeCalculada());

        assertEquals(compatibilidade, atualizada.getCompatibilidade());
        assertTrue(atualizada.possuiCompatibilidadeCalculada());
    }

    @Test
    void deveVerificarSeTemCurriculo() {
        Candidatura comCurriculo = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        );
        assertTrue(comCurriculo.possuiCurriculo());

        Candidatura semCurriculo = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), null
        );
        assertFalse(semCurriculo.possuiCurriculo());

        Candidatura curriculoVazio = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), "   "
        );
        assertFalse(curriculoVazio.possuiCurriculo());
    }

    @Test
    void deveVerificarStatusPendente() {
        Candidatura candidatura = Candidatura.nova(
            UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
        );

        assertTrue(candidatura.isPendente());
        assertFalse(candidatura.isAceita());
        assertFalse(candidatura.isRejeitada());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        assertThrows(NullPointerException.class, () ->
            Candidatura.nova(null, UUID.randomUUID(), "curriculo.pdf")
        );

        assertThrows(NullPointerException.class, () ->
            Candidatura.nova(UUID.randomUUID(), null, "curriculo.pdf")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            Candidatura candidatura = Candidatura.nova(
                UUID.randomUUID(), UUID.randomUUID(), "curriculo.pdf"
            );
            candidatura.comStatus(null);
        });
    }
}
