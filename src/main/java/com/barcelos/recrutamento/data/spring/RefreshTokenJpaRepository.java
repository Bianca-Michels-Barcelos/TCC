package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);
    List<RefreshTokenEntity> findByUsuarioId(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.usuarioId = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiraEm < :now")
    void deleteByExpiraEmBefore(@Param("now") Instant now);
}
