package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.VagaBeneficioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VagaBeneficioJpaRepository extends JpaRepository<VagaBeneficioEntity, Void> {
    List<VagaBeneficioEntity> findByVagaId(UUID vagaId);

    boolean existsByVagaIdAndBeneficioId(UUID vagaId, UUID beneficioId);

    void deleteByVagaIdAndBeneficioId(UUID vagaId, UUID beneficioId);

    
    @Query("SELECT vb.vagaId, COUNT(vb) FROM VagaBeneficioEntity vb " +
           "WHERE vb.vagaId IN :vagaIds " +
           "GROUP BY vb.vagaId")
    List<Object[]> countBeneficiosByVagaIds(@Param("vagaIds") List<UUID> vagaIds);
}