package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.ConviteProcessoSeletivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConviteProcessoSeletivoJpaRepository extends JpaRepository<ConviteProcessoSeletivoEntity, UUID> {

    @Query("SELECT c FROM ConviteProcessoSeletivoEntity c WHERE c.candidato.id = :candidatoUsuarioId")
    List<ConviteProcessoSeletivoEntity> findByCandidatoUsuarioId(@Param("candidatoUsuarioId") UUID candidatoUsuarioId);

    @Query("SELECT c FROM ConviteProcessoSeletivoEntity c WHERE c.vaga.id = :vagaId")
    List<ConviteProcessoSeletivoEntity> findByVagaId(@Param("vagaId") UUID vagaId);

    @Query("SELECT c FROM ConviteProcessoSeletivoEntity c WHERE c.candidato.id = :candidatoUsuarioId AND CAST(c.status AS string) = :status")
    List<ConviteProcessoSeletivoEntity> findByCandidatoUsuarioIdAndStatus(
            @Param("candidatoUsuarioId") UUID candidatoUsuarioId,
            @Param("status") String status
    );

    @Query("SELECT c FROM ConviteProcessoSeletivoEntity c WHERE c.vaga.id = :vagaId AND c.candidato.id = :candidatoUsuarioId")
    List<ConviteProcessoSeletivoEntity> findByVagaIdAndCandidatoUsuarioId(
            @Param("vagaId") UUID vagaId,
            @Param("candidatoUsuarioId") UUID candidatoUsuarioId
    );

    
    @Query("SELECT c FROM ConviteProcessoSeletivoEntity c " +
           "WHERE c.vaga.recrutador.id = :recrutadorUsuarioId " +
           "AND CAST(c.status AS string) IN ('PENDENTE', 'ACEITO') " +
           "AND c.dataExpiracao >= CURRENT_TIMESTAMP " +
           "ORDER BY c.dataEnvio ASC")
    List<ConviteProcessoSeletivoEntity> findUpcomingByRecrutador(@Param("recrutadorUsuarioId") UUID recrutadorUsuarioId);

    
    @Query("SELECT c FROM ConviteProcessoSeletivoEntity c " +
           "WHERE c.candidato.id = :candidatoId " +
           "AND CAST(c.status AS string) IN ('PENDENTE', 'ACEITO') " +
           "AND c.dataExpiracao >= CURRENT_TIMESTAMP " +
           "ORDER BY c.dataExpiracao ASC")
    List<ConviteProcessoSeletivoEntity> findPendingByCandidato(@Param("candidatoId") UUID candidatoId);
}
