package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.HistoricoEtapaProcessoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HistoricoEtapaProcessoJpaRepository extends JpaRepository<HistoricoEtapaProcessoEntity, UUID> {
    List<HistoricoEtapaProcessoEntity> findByProcesso_IdOrderByDataMudancaDesc(UUID processoId);

    
    @Query("SELECT h FROM HistoricoEtapaProcessoEntity h " +
           "WHERE h.processo.candidatura.candidato.id = :candidatoId " +
           "AND h.dataMudanca >= :dataInicio " +
           "ORDER BY h.dataMudanca DESC")
    List<HistoricoEtapaProcessoEntity> findRecentByCandidato(
        @Param("candidatoId") UUID candidatoId,
        @Param("dataInicio") LocalDateTime dataInicio,
        Pageable pageable
    );

    
    @Query("SELECT h FROM HistoricoEtapaProcessoEntity h " +
           "WHERE h.processo.candidatura.vaga.recrutador.id = :recrutadorUsuarioId " +
           "AND h.dataMudanca >= :dataInicio " +
           "ORDER BY h.dataMudanca DESC")
    List<HistoricoEtapaProcessoEntity> findRecentByRecrutador(
        @Param("recrutadorUsuarioId") UUID recrutadorUsuarioId,
        @Param("dataInicio") LocalDateTime dataInicio,
        Pageable pageable
    );
}
