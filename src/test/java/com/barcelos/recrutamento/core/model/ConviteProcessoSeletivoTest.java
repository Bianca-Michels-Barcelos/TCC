package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ConviteProcessoSeletivoTest {

    @Test
    void deveCriarNovoConviteComAtributosPadrao() {
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        String mensagem = "Gostaríamos de convidá-lo para participar do processo seletivo";

        ConviteProcessoSeletivo convite = ConviteProcessoSeletivo.criar(vagaId, recrutadorId, candidatoId, mensagem);

        assertThat(convite).isNotNull();
        assertThat(convite.getId()).isNotNull();
        assertThat(convite.getVagaId()).isEqualTo(vagaId);
        assertThat(convite.getRecrutadorUsuarioId()).isEqualTo(recrutadorId);
        assertThat(convite.getCandidatoUsuarioId()).isEqualTo(candidatoId);
        assertThat(convite.getMensagem()).isEqualTo(mensagem);
        assertThat(convite.getStatus()).isEqualTo(StatusConviteProcesso.PENDENTE);
        assertThat(convite.getDataEnvio()).isNotNull();
        assertThat(convite.getDataExpiracao()).isNotNull();
        assertThat(convite.getDataExpiracao()).isAfter(convite.getDataEnvio());
        assertThat(convite.getDataResposta()).isNull();
    }

    @Test
    void deveRehydratarConviteExistente() {
        UUID id = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dataExpiracao = LocalDateTime.of(2025, 1, 8, 10, 0);
        LocalDateTime dataResposta = LocalDateTime.of(2025, 1, 5, 15, 0);

        ConviteProcessoSeletivo convite = ConviteProcessoSeletivo.rehydrate(id, vagaId, recrutadorId,
                candidatoId, "Mensagem", StatusConviteProcesso.ACEITO, dataEnvio, dataExpiracao, dataResposta);

        assertThat(convite.getId()).isEqualTo(id);
        assertThat(convite.getStatus()).isEqualTo(StatusConviteProcesso.ACEITO);
        assertThat(convite.getDataResposta()).isEqualTo(dataResposta);
    }

    @Test
    void deveAceitarConvitePendente() {
        ConviteProcessoSeletivo convite = criarConvitePadrao();
        
        ConviteProcessoSeletivo aceito = convite.aceitar();

        assertThat(convite.getStatus()).isEqualTo(StatusConviteProcesso.PENDENTE);
        assertThat(convite.getDataResposta()).isNull();
        assertThat(aceito.getStatus()).isEqualTo(StatusConviteProcesso.ACEITO);
        assertThat(aceito.getDataResposta()).isNotNull();
    }

    @Test
    void naoDeveAceitarConviteJaAceito() {
        ConviteProcessoSeletivo convite = criarConvitePadrao().aceitar();

        assertThatThrownBy(() -> convite.aceitar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apenas convites pendentes podem ser aceitos");
    }

    @Test
    void naoDeveAceitarConviteRecusado() {
        ConviteProcessoSeletivo convite = criarConvitePadrao().recusar();

        assertThatThrownBy(() -> convite.aceitar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apenas convites pendentes podem ser aceitos");
    }

    @Test
    void naoDeveAceitarConviteExpirado() {
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.now().minusDays(10);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusDays(3);

        ConviteProcessoSeletivo convite = ConviteProcessoSeletivo.rehydrate(
                UUID.randomUUID(), vagaId, recrutadorId, candidatoId, "Mensagem",
                StatusConviteProcesso.PENDENTE, dataEnvio, dataExpiracao, null);

        assertThatThrownBy(() -> convite.aceitar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Este convite expirou");
    }

    @Test
    void deveRecusarConvitePendente() {
        ConviteProcessoSeletivo convite = criarConvitePadrao();
        
        ConviteProcessoSeletivo recusado = convite.recusar();

        assertThat(convite.getStatus()).isEqualTo(StatusConviteProcesso.PENDENTE);
        assertThat(recusado.getStatus()).isEqualTo(StatusConviteProcesso.RECUSADO);
        assertThat(recusado.getDataResposta()).isNotNull();
    }

    @Test
    void naoDeveRecusarConviteJaAceito() {
        ConviteProcessoSeletivo convite = criarConvitePadrao().aceitar();

        assertThatThrownBy(() -> convite.recusar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apenas convites pendentes podem ser recusados");
    }

    @Test
    void deveExpirarConvite() {
        ConviteProcessoSeletivo convite = criarConvitePadrao();
        
        ConviteProcessoSeletivo expirado = convite.expirar();

        assertThat(convite.getStatus()).isEqualTo(StatusConviteProcesso.PENDENTE);
        assertThat(expirado.getStatus()).isEqualTo(StatusConviteProcesso.EXPIRADO);
        assertThat(expirado.getDataResposta()).isNotNull();
    }

    @Test
    void deveVerificarSeConviteEstaExpirado() {
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.now().minusDays(10);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusDays(1);

        ConviteProcessoSeletivo convite = ConviteProcessoSeletivo.rehydrate(
                UUID.randomUUID(), vagaId, recrutadorId, candidatoId, "Mensagem",
                StatusConviteProcesso.PENDENTE, dataEnvio, dataExpiracao, null);

        assertThat(convite.estaExpirado()).isTrue();
    }

    @Test
    void deveVerificarQueConviteNaoExpirado() {
        ConviteProcessoSeletivo convite = criarConvitePadrao();

        assertThat(convite.estaExpirado()).isFalse();
    }

    @Test
    void deveVerificarQueConviteAceitoNaoEstaExpirado() {
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.now().minusDays(10);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusDays(1);
        LocalDateTime dataResposta = LocalDateTime.now().minusDays(5);

        ConviteProcessoSeletivo convite = ConviteProcessoSeletivo.rehydrate(
                UUID.randomUUID(), vagaId, recrutadorId, candidatoId, "Mensagem",
                StatusConviteProcesso.ACEITO, dataEnvio, dataExpiracao, dataResposta);

        assertThat(convite.estaExpirado()).isFalse();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();

        assertThatThrownBy(() -> ConviteProcessoSeletivo.criar(null, recrutadorId, candidatoId, "Mensagem"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("vagaId não pode ser nulo");

        assertThatThrownBy(() -> ConviteProcessoSeletivo.criar(vagaId, null, candidatoId, "Mensagem"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("recrutadorUsuarioId não pode ser nulo");

        assertThatThrownBy(() -> ConviteProcessoSeletivo.criar(vagaId, recrutadorId, null, "Mensagem"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("candidatoUsuarioId não pode ser nulo");
    }

    @Test
    void deveValidarCamposObrigatoriosAoRehydratar() {
        UUID id = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        LocalDateTime dataEnvio = LocalDateTime.now();
        LocalDateTime dataExpiracao = LocalDateTime.now().plusDays(7);

        assertThatThrownBy(() -> ConviteProcessoSeletivo.rehydrate(null, vagaId, recrutadorId,
                candidatoId, "Mensagem", StatusConviteProcesso.PENDENTE, dataEnvio, dataExpiracao, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ConviteProcessoSeletivo.rehydrate(id, vagaId, recrutadorId,
                candidatoId, "Mensagem", null, dataEnvio, dataExpiracao, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ConviteProcessoSeletivo.rehydrate(id, vagaId, recrutadorId,
                candidatoId, "Mensagem", StatusConviteProcesso.PENDENTE, null, dataExpiracao, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ConviteProcessoSeletivo.rehydrate(id, vagaId, recrutadorId,
                candidatoId, "Mensagem", StatusConviteProcesso.PENDENTE, dataEnvio, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    private ConviteProcessoSeletivo criarConvitePadrao() {
        UUID vagaId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        return ConviteProcessoSeletivo.criar(vagaId, recrutadorId, candidatoId, "Mensagem padrão");
    }
}

