package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.CompatibilidadeCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompatibilidadeCacheJpaRepository extends JpaRepository<CompatibilidadeCacheEntity, UUID> {

    @Query("SELECT c FROM CompatibilidadeCacheEntity c WHERE c.candidatoUsuarioId = :candidatoId AND c.vagaId = :vagaId")
    Optional<CompatibilidadeCacheEntity> findByCandidatoAndVaga(
            @Param("candidatoId") UUID candidatoUsuarioId,
            @Param("vagaId") UUID vagaId
    );

    @Query("SELECT c FROM CompatibilidadeCacheEntity c WHERE c.candidatoUsuarioId = :candidatoId")
    List<CompatibilidadeCacheEntity> findByCandidato(@Param("candidatoId") UUID candidatoUsuarioId);

    @Query("SELECT c FROM CompatibilidadeCacheEntity c WHERE c.vagaId = :vagaId")
    List<CompatibilidadeCacheEntity> findByVaga(@Param("vagaId") UUID vagaId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CompatibilidadeCacheEntity c " +
           "WHERE c.candidatoUsuarioId = :candidatoId AND c.vagaId = :vagaId")
    boolean existsByCandidatoAndVaga(
            @Param("candidatoId") UUID candidatoUsuarioId,
            @Param("vagaId") UUID vagaId
    );

    @Modifying
    @Query("DELETE FROM CompatibilidadeCacheEntity c WHERE c.vagaId = :vagaId")
    void deleteByVaga(@Param("vagaId") UUID vagaId);

    @Modifying
    @Query("DELETE FROM CompatibilidadeCacheEntity c WHERE c.candidatoUsuarioId = :candidatoId")
    void deleteByCandidato(@Param("candidatoId") UUID candidatoUsuarioId);
}
