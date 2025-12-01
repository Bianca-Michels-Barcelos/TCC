package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.CompetenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CompetenciaJpaRepository extends JpaRepository<CompetenciaEntity, UUID> {

    @Query("SELECT c FROM CompetenciaEntity c WHERE c.perfilCandidato.id = :perfilCandidatoId")
    List<CompetenciaEntity> findByPerfilCandidatoId(@Param("perfilCandidatoId") UUID perfilCandidatoId);
}
