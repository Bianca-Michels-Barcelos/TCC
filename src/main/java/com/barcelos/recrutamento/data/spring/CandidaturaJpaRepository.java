package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.CandidaturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidaturaJpaRepository extends JpaRepository<CandidaturaEntity, UUID> {
    boolean existsByVaga_IdAndCandidato_Id(UUID vagaId, UUID candidatoUsuarioId);

    Optional<CandidaturaEntity> findByVaga_IdAndCandidato_Id(UUID vagaId, UUID candidatoUsuarioId);

    List<CandidaturaEntity> findByVaga_Id(UUID vagaId);

    List<CandidaturaEntity> findByCandidato_Id(UUID candidatoUsuarioId);

    long countByVaga_Id(UUID vagaId);

    @Query("SELECT COUNT(c) FROM CandidaturaEntity c WHERE c.vaga.id = :vagaId AND CAST(c.status AS string) = :status")
    long countByVaga_IdAndStatus(@Param("vagaId") UUID vagaId, @Param("status") String status);

    long countByCandidato_Id(UUID candidatoUsuarioId);

    @Query("SELECT COUNT(c) FROM CandidaturaEntity c WHERE c.candidato.id = :candidatoUsuarioId AND CAST(c.status AS string) = :status")
    long countByCandidato_IdAndStatus(@Param("candidatoUsuarioId") UUID candidatoUsuarioId, @Param("status") String status);

    
    @Query("SELECT c.vaga.id, COUNT(c) FROM CandidaturaEntity c " +
           "WHERE c.vaga.id IN :vagaIds AND CAST(c.status AS string) = 'ACEITA' " +
           "GROUP BY c.vaga.id")
    List<Object[]> countCandidatosAceitosByVagaIds(@Param("vagaIds") List<UUID> vagaIds);

    
    @Query("SELECT c.vaga.id, COUNT(c) FROM CandidaturaEntity c " +
           "WHERE c.vaga.id IN :vagaIds AND CAST(c.status AS string) = 'REJEITADA' " +
           "GROUP BY c.vaga.id")
    List<Object[]> countCandidatosRejeitadosByVagaIds(@Param("vagaIds") List<UUID> vagaIds);

    
    @Query("SELECT COUNT(c) FROM CandidaturaEntity c " +
           "WHERE c.vaga.recrutador.id = :recrutadorUsuarioId " +
           "AND CAST(c.status AS string) = 'PENDENTE'")
    long countPendingByRecrutador(@Param("recrutadorUsuarioId") UUID recrutadorUsuarioId);

    
    @Query("SELECT c FROM CandidaturaEntity c " +
           "WHERE c.vaga.recrutador.id = :recrutadorUsuarioId " +
           "AND c.dataCandidatura >= :dataInicio " +
           "ORDER BY c.dataCandidatura DESC")
    List<CandidaturaEntity> findRecentByRecrutador(
        @Param("recrutadorUsuarioId") UUID recrutadorUsuarioId,
        @Param("dataInicio") LocalDate dataInicio
    );

    
    @Query("SELECT COUNT(c) FROM CandidaturaEntity c " +
           "WHERE c.candidato.id = :candidatoId " +
           "AND CAST(c.status AS string) IN ('PENDENTE', 'EM_PROCESSO')")
    long countActiveApplications(@Param("candidatoId") UUID candidatoUsuarioId);

    
    @Query("SELECT AVG(c.compatibilidade) FROM CandidaturaEntity c " +
           "WHERE c.candidato.id = :candidatoId " +
           "AND c.compatibilidade IS NOT NULL")
    Double findAverageCompatibility(@Param("candidatoId") UUID candidatoId);
}
