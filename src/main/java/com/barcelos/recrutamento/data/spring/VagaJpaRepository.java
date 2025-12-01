package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.VagaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VagaJpaRepository extends JpaRepository<VagaEntity, UUID> {
    List<VagaEntity> findByOrganizacao_Id(UUID organizacaoId);

    List<VagaEntity> findByRecrutador_Id(UUID recrutadorUsuarioId);

    @Query("SELECT v FROM VagaEntity v WHERE CAST(v.status AS string) = :status AND v.ativo = true")
    List<VagaEntity> findByStatusAndAtivoTrue(@Param("status") String status);

    
    @Query(value = """
        SELECT v.* FROM vaga v
        WHERE v.organizacao_id = :organizacaoId
        AND (:status IS NULL OR v.status::varchar = :status)
        AND (:modalidade IS NULL OR v.modalidade::varchar = :modalidade)
        AND (:search IS NULL OR
             LOWER(v.titulo::text) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(v.descricao::text) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY v.data_publicacao DESC
    """,
    countQuery = """
        SELECT COUNT(v.id) FROM vaga v
        WHERE v.organizacao_id = :organizacaoId
        AND (:status IS NULL OR v.status::varchar = :status)
        AND (:modalidade IS NULL OR v.modalidade::varchar = :modalidade)
        AND (:search IS NULL OR
             LOWER(v.titulo::text) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(v.descricao::text) LIKE LOWER(CONCAT('%', :search, '%')))
    """,
    nativeQuery = true)
    Page<VagaEntity> findWithStatsByOrganizacao(
        @Param("organizacaoId") UUID organizacaoId,
        @Param("status") String status,
        @Param("modalidade") String modalidade,
        @Param("search") String search,
        Pageable pageable
    );

    
    @Query(value = """
        SELECT v.* FROM vaga v
        WHERE v.recrutador_usuario_id = :recrutadorUsuarioId
        AND (:status IS NULL OR v.status::varchar = :status)
        AND (:modalidade IS NULL OR v.modalidade::varchar = :modalidade)
        AND (:search IS NULL OR
             LOWER(v.titulo::text) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(v.descricao::text) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY v.data_publicacao DESC
    """,
    countQuery = """
        SELECT COUNT(v.id) FROM vaga v
        WHERE v.recrutador_usuario_id = :recrutadorUsuarioId
        AND (:status IS NULL OR v.status::varchar = :status)
        AND (:modalidade IS NULL OR v.modalidade::varchar = :modalidade)
        AND (:search IS NULL OR
             LOWER(v.titulo::text) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(v.descricao::text) LIKE LOWER(CONCAT('%', :search, '%')))
    """,
    nativeQuery = true)
    Page<VagaEntity> findWithStatsByRecrutador(
        @Param("recrutadorUsuarioId") UUID recrutadorUsuarioId,
        @Param("status") String status,
        @Param("modalidade") String modalidade,
        @Param("search") String search,
        Pageable pageable
    );

    
    @Query("SELECT COUNT(v) FROM VagaEntity v " +
           "WHERE v.recrutador.id = :recrutadorUsuarioId " +
           "AND CAST(v.status AS string) = :status")
    long countByRecrutadorAndStatus(
        @Param("recrutadorUsuarioId") UUID recrutadorUsuarioId,
        @Param("status") String status
    );
}
