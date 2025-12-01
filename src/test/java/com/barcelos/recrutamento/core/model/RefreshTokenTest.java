package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RefreshTokenTest {

    @Test
    void deveCriarNovoTokenComAtributosPadrao() {
        String token = "refresh_token_123";
        UUID usuarioId = UUID.randomUUID();
        Instant expiraEm = Instant.now().plusSeconds(604800);

        RefreshToken refreshToken = RefreshToken.novo(token, usuarioId, expiraEm);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getId()).isNull();
        assertThat(refreshToken.getToken()).isEqualTo(token);
        assertThat(refreshToken.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(refreshToken.getExpiraEm()).isEqualTo(expiraEm);
        assertThat(refreshToken.getCriadoEm()).isNotNull();
        assertThat(refreshToken.isRevogado()).isFalse();
    }

    @Test
    void deveReconstituirTokenExistente() {
        UUID id = UUID.randomUUID();
        String token = "refresh_token_456";
        UUID usuarioId = UUID.randomUUID();
        Instant expiraEm = Instant.now().plusSeconds(604800);
        Instant criadoEm = Instant.now().minusSeconds(3600);

        RefreshToken refreshToken = RefreshToken.reconstituir(id, token, usuarioId, 
                expiraEm, criadoEm, true);

        assertThat(refreshToken.getId()).isEqualTo(id);
        assertThat(refreshToken.getToken()).isEqualTo(token);
        assertThat(refreshToken.getCriadoEm()).isEqualTo(criadoEm);
        assertThat(refreshToken.isRevogado()).isTrue();
    }

    @Test
    void deveRevogarTokenMantendoImutabilidade() {
        RefreshToken original = criarTokenPadrao();
        
        RefreshToken revogado = original.revogar();

        assertThat(original.isRevogado()).isFalse();
        assertThat(revogado.isRevogado()).isTrue();
        assertThat(revogado.getToken()).isEqualTo(original.getToken());
    }

    @Test
    void deveVerificarSeTokenEstaExpirado() {
        UUID usuarioId = UUID.randomUUID();
        Instant expiraEm = Instant.now().minusSeconds(3600);

        RefreshToken tokenExpirado = RefreshToken.novo("token", usuarioId, expiraEm);

        assertThat(tokenExpirado.isExpirado()).isTrue();
    }

    @Test
    void deveVerificarSeTokenNaoEstaExpirado() {
        RefreshToken token = criarTokenPadrao();

        assertThat(token.isExpirado()).isFalse();
    }

    @Test
    void deveVerificarSeTokenEhValido() {
        RefreshToken token = criarTokenPadrao();

        assertThat(token.isValido()).isTrue();
    }

    @Test
    void naoDeveSerValidoSeRevogado() {
        RefreshToken token = criarTokenPadrao().revogar();

        assertThat(token.isValido()).isFalse();
    }

    @Test
    void naoDeveSerValidoSeExpirado() {
        UUID usuarioId = UUID.randomUUID();
        Instant expiraEm = Instant.now().minusSeconds(3600);
        RefreshToken token = RefreshToken.novo("token", usuarioId, expiraEm);

        assertThat(token.isValido()).isFalse();
    }

    private RefreshToken criarTokenPadrao() {
        String token = "refresh_token_123";
        UUID usuarioId = UUID.randomUUID();
        Instant expiraEm = Instant.now().plusSeconds(604800);
        return RefreshToken.novo(token, usuarioId, expiraEm);
    }
}

