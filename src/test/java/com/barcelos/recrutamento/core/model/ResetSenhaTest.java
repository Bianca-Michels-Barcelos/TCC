package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ResetSenhaTest {

    @Test
    void deveCriarNovoResetSenhaComAtributosPadrao() {
        UUID usuarioId = UUID.randomUUID();
        String token = "reset_token_123";

        ResetSenha resetSenha = ResetSenha.criar(usuarioId, token);

        assertThat(resetSenha).isNotNull();
        assertThat(resetSenha.getId()).isNotNull();
        assertThat(resetSenha.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(resetSenha.getToken()).isEqualTo(token);
        assertThat(resetSenha.getStatus()).isEqualTo(StatusResetSenha.PENDENTE);
        assertThat(resetSenha.getDataSolicitacao()).isNotNull();
        assertThat(resetSenha.getDataExpiracao()).isNotNull();
        assertThat(resetSenha.getDataUso()).isNull();
    }

    @Test
    void deveReconstruirResetSenhaExistente() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime dataSolicitacao = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime dataExpiracao = LocalDateTime.of(2025, 1, 16, 10, 0);
        LocalDateTime dataUso = LocalDateTime.of(2025, 1, 15, 15, 0);

        ResetSenha resetSenha = ResetSenha.reconstruir(id, usuarioId, "token", 
                StatusResetSenha.USADO, dataSolicitacao, dataExpiracao, dataUso);

        assertThat(resetSenha.getId()).isEqualTo(id);
        assertThat(resetSenha.getStatus()).isEqualTo(StatusResetSenha.USADO);
        assertThat(resetSenha.getDataUso()).isEqualTo(dataUso);
    }

    @Test
    void deveEstenderValidadeDoTokenPendente() {
        ResetSenha resetSenha = criarResetSenhaPadrao();
        
        ResetSenha estendido = resetSenha.estenderValidade();

        assertThat(estendido.getDataExpiracao()).isAfter(resetSenha.getDataExpiracao());
        assertThat(estendido.getStatus()).isEqualTo(StatusResetSenha.PENDENTE);
        assertThat(estendido.getId()).isEqualTo(resetSenha.getId());
    }

    @Test
    void naoDeveEstenderValidadeDeTokenNaoPendente() {
        ResetSenha resetSenha = criarResetSenhaPadrao().marcarComoUsado();

        assertThatThrownBy(() -> resetSenha.estenderValidade())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apenas tokens pendentes podem ter a validade estendida");
    }

    @Test
    void deveMarcarTokenComoUsado() {
        ResetSenha resetSenha = criarResetSenhaPadrao();
        
        ResetSenha usado = resetSenha.marcarComoUsado();

        assertThat(resetSenha.getStatus()).isEqualTo(StatusResetSenha.PENDENTE);
        assertThat(usado.getStatus()).isEqualTo(StatusResetSenha.USADO);
        assertThat(usado.getDataUso()).isNotNull();
    }

    @Test
    void naoDeveMarcarTokenNaoPendenteComoUsado() {
        ResetSenha resetSenha = criarResetSenhaPadrao().marcarComoUsado();

        assertThatThrownBy(() -> resetSenha.marcarComoUsado())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token não está pendente");
    }

    @Test
    void naoDeveMarcarTokenExpiradoComoUsado() {
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime dataSolicitacao = LocalDateTime.now().minusDays(2);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusDays(1);

        ResetSenha resetSenha = ResetSenha.reconstruir(UUID.randomUUID(), usuarioId, 
                "token", StatusResetSenha.PENDENTE, dataSolicitacao, dataExpiracao, null);

        assertThatThrownBy(() -> resetSenha.marcarComoUsado())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token expirado");
    }

    @Test
    void deveMarcarTokenComoExpirado() {
        ResetSenha resetSenha = criarResetSenhaPadrao();
        
        ResetSenha expirado = resetSenha.marcarComoExpirado();

        assertThat(resetSenha.getStatus()).isEqualTo(StatusResetSenha.PENDENTE);
        assertThat(expirado.getStatus()).isEqualTo(StatusResetSenha.EXPIRADO);
    }

    @Test
    void deveVerificarSeTokenEstaExpirado() {
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime dataSolicitacao = LocalDateTime.now().minusDays(2);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusHours(1);

        ResetSenha resetSenha = ResetSenha.reconstruir(UUID.randomUUID(), usuarioId, 
                "token", StatusResetSenha.PENDENTE, dataSolicitacao, dataExpiracao, null);

        assertThat(resetSenha.isExpirado()).isTrue();
    }

    @Test
    void deveVerificarSeTokenNaoEstaExpirado() {
        ResetSenha resetSenha = criarResetSenhaPadrao();

        assertThat(resetSenha.isExpirado()).isFalse();
    }

    @Test
    void deveVerificarSeTokenEhValido() {
        ResetSenha resetSenha = criarResetSenhaPadrao();

        assertThat(resetSenha.isValido()).isTrue();
    }

    @Test
    void naoDeveSerValidoSeUsado() {
        ResetSenha resetSenha = criarResetSenhaPadrao().marcarComoUsado();

        assertThat(resetSenha.isValido()).isFalse();
    }

    @Test
    void naoDeveSerValidoSeExpirado() {
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime dataSolicitacao = LocalDateTime.now().minusDays(2);
        LocalDateTime dataExpiracao = LocalDateTime.now().minusHours(1);

        ResetSenha resetSenha = ResetSenha.reconstruir(UUID.randomUUID(), usuarioId, 
                "token", StatusResetSenha.PENDENTE, dataSolicitacao, dataExpiracao, null);

        assertThat(resetSenha.isValido()).isFalse();
    }

    private ResetSenha criarResetSenhaPadrao() {
        UUID usuarioId = UUID.randomUUID();
        return ResetSenha.criar(usuarioId, "reset_token_123");
    }
}

