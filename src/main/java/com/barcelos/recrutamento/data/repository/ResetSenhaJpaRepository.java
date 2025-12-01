package com.barcelos.recrutamento.data.repository;

import com.barcelos.recrutamento.data.entity.ResetSenhaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResetSenhaJpaRepository extends JpaRepository<ResetSenhaEntity, UUID> {

    Optional<ResetSenhaEntity> findByToken(String token);

    Optional<ResetSenhaEntity> findByUsuarioIdAndStatus(UUID usuarioId, String status);

    boolean existsByUsuarioIdAndStatus(UUID usuarioId, String status);
}

