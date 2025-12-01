package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PortfolioJpaRepository extends JpaRepository<PortfolioEntity, UUID> {

    @Query("SELECT p FROM PortfolioEntity p WHERE p.perfilCandidato.id = :perfilCandidatoId")
    List<PortfolioEntity> findByPerfilCandidatoId(@Param("perfilCandidatoId") UUID perfilCandidatoId);
}
