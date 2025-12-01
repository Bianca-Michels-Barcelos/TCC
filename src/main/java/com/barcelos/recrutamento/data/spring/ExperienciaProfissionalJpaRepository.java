package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.ExperienciaProfissionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExperienciaProfissionalJpaRepository extends JpaRepository<ExperienciaProfissionalEntity, UUID> {
    List<ExperienciaProfissionalEntity> findByPerfilCandidato_Id(UUID usuarioId);
}