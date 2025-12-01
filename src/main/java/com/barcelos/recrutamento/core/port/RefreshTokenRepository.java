package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUsuarioId(UUID usuarioId);
    void deleteByUsuarioId(UUID usuarioId);
    void deleteExpired();
}
