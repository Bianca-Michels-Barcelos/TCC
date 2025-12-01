package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilCandidatoJpaRepository extends JpaRepository<PerfilCandidatoEntity, UUID> {
    Optional<PerfilCandidatoEntity> findByUsuarioId(UUID usuarioId);
}
