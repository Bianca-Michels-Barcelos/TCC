package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.VagaExternaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VagaExternaJpaRepository extends JpaRepository<VagaExternaEntity, UUID> {
    List<VagaExternaEntity> findByCandidato_UsuarioId(UUID usuarioId);
}
