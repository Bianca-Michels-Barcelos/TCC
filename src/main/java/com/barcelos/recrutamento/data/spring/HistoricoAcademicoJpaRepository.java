package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.HistoricoAcademicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoAcademicoJpaRepository extends JpaRepository<HistoricoAcademicoEntity, UUID> {
    List<HistoricoAcademicoEntity> findByPerfilCandidato_Id(UUID usuarioId);
}