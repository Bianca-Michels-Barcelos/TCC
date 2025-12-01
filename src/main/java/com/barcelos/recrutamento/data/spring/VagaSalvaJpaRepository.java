package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.VagaSalvaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VagaSalvaJpaRepository extends JpaRepository<VagaSalvaEntity, UUID> {
    Optional<VagaSalvaEntity> findByVaga_IdAndUsuario_Id(UUID vagaId, UUID usuarioId);

    List<VagaSalvaEntity> findByUsuario_Id(UUID usuarioId);

    long countByUsuario_Id(UUID usuarioId);

    void deleteByVaga_Id(UUID vagaId);

    boolean existsByVaga_IdAndUsuario_Id(UUID vagaId, UUID usuarioId);
}
