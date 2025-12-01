package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.AvaliacaoOrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoOrganizacaoJpaRepository extends JpaRepository<AvaliacaoOrganizacaoEntity, UUID> {
    Optional<AvaliacaoOrganizacaoEntity> findByProcesso_Id(UUID processoId);

    List<AvaliacaoOrganizacaoEntity> findByOrganizacao_IdOrderByCriadoEmDesc(UUID organizacaoId);

    long countByOrganizacao_Id(UUID organizacaoId);

    @Query("SELECT AVG(a.nota) FROM AvaliacaoOrganizacaoEntity a WHERE a.organizacao.id = :organizacaoId")
    Double findAverageNotaByOrganizacaoId(@Param("organizacaoId") UUID organizacaoId);

    boolean existsByProcesso_Id(UUID processoId);
}
