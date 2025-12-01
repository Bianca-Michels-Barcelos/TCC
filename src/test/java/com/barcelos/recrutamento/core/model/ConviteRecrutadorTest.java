package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ConviteRecrutadorTest {

    @Test
    void deveCriarNovoConviteComAtributosPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        String email = "recrutador@example.com";
        String token = "token123";

        ConviteRecrutador convite = ConviteRecrutador.criar(organizacaoId, email, token);

        assertThat(convite).isNotNull();
        assertThat(convite.getId()).isNotNull();
        assertThat(convite.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(convite.getEmail()).isEqualTo(email);
        assertThat(convite.getToken()).isEqualTo(token);
        assertThat(convite.getStatus()).isEqualTo(StatusConvite.PENDENTE);
        assertThat(convite.getDataEnvio()).isNotNull();
        assertThat(convite.getDataExpiracao()).isNotNull();
        assertThat(convite.getDataExpiracao()).isAfter(convite.getDataEnvio());
        assertThat(convite.getDataAceite()).isNull();
    }

    @Test
    void deveReconstruirConviteExistente() {
        UUID id = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dataExpiracao = LocalDateTime.of(2025, 1, 8, 10, 0);
        LocalDateTime dataAceite = LocalDateTime.of(2025, 1, 5, 15, 0);

        ConviteRecrutador convite = ConviteRecrutador.reconstruir(id, organizacaoId, "email@example.com",
                "token", StatusConvite.ACEITO, dataEnvio, dataExpiracao, dataAceite);

        assertThat(convite.getId()).isEqualTo(id);
        assertThat(convite.getStatus()).isEqualTo(StatusConvite.ACEITO);
        assertThat(convite.getDataAceite()).isEqualTo(dataAceite);
    }

    @Test
    void deveAceitarConvitePendente() {
        ConviteRecrutador convite = criarConvitePadrao();
        
        ConviteRecrutador aceito = convite.aceitar();

        assertThat(convite.getStatus()).isEqualTo(StatusConvite.PENDENTE);
        assertThat(convite.getDataAceite()).isNull();
        assertThat(aceito.getStatus()).isEqualTo(StatusConvite.ACEITO);
        assertThat(aceito.getDataAceite()).isNotNull();
    }

    @Test
    void naoDeveAceitarConviteJaAceito() {
        ConviteRecrutador convite = criarConvitePadrao().aceitar();

        assertThatThrownBy(() -> convite.aceitar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Convite não está pendente");
    }

    @Test
    void naoDeveAceitarConviteRecusado() {
        ConviteRecrutador convite = criarConvitePadrao().recusar();

        assertThatThrownBy(() -> convite.aceitar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Convite não está pendente");
    }

    @Test
    void naoDeveAceitarConviteExpirado() {
        UUID organizacaoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.now().minusDays(10);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusDays(3);

        ConviteRecrutador convite = ConviteRecrutador.reconstruir(
                UUID.randomUUID(), organizacaoId, "email@example.com", "token",
                StatusConvite.PENDENTE, dataEnvio, dataExpiracao, null);

        assertThatThrownBy(() -> convite.aceitar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Convite expirado");
    }

    @Test
    void deveRecusarConvitePendente() {
        ConviteRecrutador convite = criarConvitePadrao();
        
        ConviteRecrutador recusado = convite.recusar();

        assertThat(convite.getStatus()).isEqualTo(StatusConvite.PENDENTE);
        assertThat(recusado.getStatus()).isEqualTo(StatusConvite.RECUSADO);
        assertThat(recusado.getDataAceite()).isNull();
    }

    @Test
    void naoDeveRecusarConviteJaAceito() {
        ConviteRecrutador convite = criarConvitePadrao().aceitar();

        assertThatThrownBy(() -> convite.recusar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Convite não está pendente");
    }

    @Test
    void deveVerificarSeConviteEstaExpirado() {
        UUID organizacaoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.now().minusDays(10);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusDays(1);

        ConviteRecrutador convite = ConviteRecrutador.reconstruir(
                UUID.randomUUID(), organizacaoId, "email@example.com", "token",
                StatusConvite.PENDENTE, dataEnvio, dataExpiracao, null);

        assertThat(convite.isExpirado()).isTrue();
    }

    @Test
    void deveVerificarQueConviteNaoExpirado() {
        ConviteRecrutador convite = criarConvitePadrao();

        assertThat(convite.isExpirado()).isFalse();
    }

    @Test
    void deveVerificarSeConviteEhValido() {
        ConviteRecrutador convite = criarConvitePadrao();

        assertThat(convite.isValido()).isTrue();
    }

    @Test
    void deveVerificarQueConviteAceitoNaoEhValido() {
        ConviteRecrutador convite = criarConvitePadrao().aceitar();

        assertThat(convite.isValido()).isFalse();
    }

    @Test
    void deveVerificarQueConviteRecusadoNaoEhValido() {
        ConviteRecrutador convite = criarConvitePadrao().recusar();

        assertThat(convite.isValido()).isFalse();
    }

    @Test
    void deveVerificarQueConviteExpiradoNaoEhValido() {
        UUID organizacaoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.now().minusDays(10);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusDays(1);

        ConviteRecrutador convite = ConviteRecrutador.reconstruir(
                UUID.randomUUID(), organizacaoId, "email@example.com", "token",
                StatusConvite.PENDENTE, dataEnvio, dataExpiracao, null);

        assertThat(convite.isValido()).isFalse();
    }

    private ConviteRecrutador criarConvitePadrao() {
        UUID organizacaoId = UUID.randomUUID();
        return ConviteRecrutador.criar(organizacaoId, "recrutador@example.com", "token123");
    }
}

