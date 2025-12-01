package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.EtapaProcessoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EtapaProcessoJpaRepository extends JpaRepository<EtapaProcessoEntity, UUID> {

    @Query("SELECT e FROM EtapaProcessoEntity e WHERE e.vaga.id = :vagaId ORDER BY e.ordem")
    List<EtapaProcessoEntity> findByVagaIdOrderByOrdem(@Param("vagaId") UUID vagaId);

    
    @Query("SELECT e.vaga.id, COUNT(e) FROM EtapaProcessoEntity e " +
           "WHERE e.vaga.id IN :vagaIds " +
           "GROUP BY e.vaga.id")
    List<Object[]> countEtapasByVagaIds(@Param("vagaIds") List<UUID> vagaIds);
}
