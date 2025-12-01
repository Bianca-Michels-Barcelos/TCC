package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.ProcessoSeletivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ProcessoSeletivoJpaRepository extends JpaRepository<ProcessoSeletivoEntity, UUID> {

    @Query("SELECT p FROM ProcessoSeletivoEntity p WHERE p.candidatura.vaga.id = :vagaId")
    List<ProcessoSeletivoEntity> findByVagaId(@Param("vagaId") UUID vagaId);

    @Query("SELECT p FROM ProcessoSeletivoEntity p WHERE p.candidatura.id = :candidaturaId")
    Optional<ProcessoSeletivoEntity> findByCandidaturaId(@Param("candidaturaId") UUID candidaturaId);

    
    @Query("SELECT p.candidatura.vaga.id, COUNT(p) FROM ProcessoSeletivoEntity p " +
           "WHERE p.candidatura.vaga.id IN :vagaIds " +
           "GROUP BY p.candidatura.vaga.id")
    List<Object[]> countProcessosByVagaIds(@Param("vagaIds") List<UUID> vagaIds);

    
    @Query("SELECT p.candidatura.vaga.id, COUNT(p) FROM ProcessoSeletivoEntity p " +
           "WHERE p.candidatura.vaga.id IN :vagaIds AND p.dataFim IS NULL " +
           "GROUP BY p.candidatura.vaga.id")
    List<Object[]> countProcessosAtivosByVagaIds(@Param("vagaIds") List<UUID> vagaIds);

    
    @Query(value = """
        SELECT
            ps.id as processoId,
            ps.candidatura_id as candidaturaId,
            ps.data_inicio as dataInicio,
            ps.data_fim as dataFim,
            ps.data_ultima_mudanca as dataUltimaMudanca,
            ps.etapa_processo_atual_id as etapaAtualId,
            ep.nome as etapaAtualNome,
            ep.descricao as etapaAtualDescricao,
            ep.ordem as etapaAtualOrdem,
            ep.status as etapaAtualStatus,
            c.candidato_usuario_id as candidatoUsuarioId,
            u.nome as candidatoNome,
            u.email as candidatoEmail,
            c.status as statusCandidatura,
            c.data_candidatura as dataCandidatura,
            c.compatibilidade as compatibilidade,
            c.arquivo_curriculo as arquivoCurriculo,
            c.vaga_id as vagaId,
            v.titulo as vagaTitulo
        FROM processo_seletivo ps
        INNER JOIN candidatura c ON ps.candidatura_id = c.id
        INNER JOIN usuario u ON c.candidato_usuario_id = u.id
        INNER JOIN etapas_processo ep ON ps.etapa_processo_atual_id = ep.id
        INNER JOIN vaga v ON c.vaga_id = v.id
        WHERE c.vaga_id = :vagaId
        ORDER BY ps.data_ultima_mudanca DESC
        """, nativeQuery = true)
    List<Map<String, Object>> findProcessosComCandidatosByVagaId(@Param("vagaId") UUID vagaId);

    
    @Query(value = """
        SELECT
            ps.id as processoId,
            ps.candidatura_id as candidaturaId,
            ps.data_inicio as dataInicio,
            ps.data_fim as dataFim,
            ps.data_ultima_mudanca as dataUltimaMudanca,
            ps.etapa_processo_atual_id as etapaAtualId,
            ep.nome as etapaAtualNome,
            ep.descricao as etapaAtualDescricao,
            ep.ordem as etapaAtualOrdem,
            ep.status as etapaAtualStatus,
            c.candidato_usuario_id as candidatoUsuarioId,
            u.nome as candidatoNome,
            u.email as candidatoEmail,
            c.status as statusCandidatura,
            c.data_candidatura as dataCandidatura,
            c.compatibilidade as compatibilidade,
            c.arquivo_curriculo as arquivoCurriculo,
            c.vaga_id as vagaId,
            v.titulo as vagaTitulo
        FROM processo_seletivo ps
        INNER JOIN candidatura c ON ps.candidatura_id = c.id
        INNER JOIN usuario u ON c.candidato_usuario_id = u.id
        INNER JOIN etapas_processo ep ON ps.etapa_processo_atual_id = ep.id
        INNER JOIN vaga v ON c.vaga_id = v.id
        WHERE ps.id = :processoId
        """, nativeQuery = true)
    List<Map<String, Object>> findProcessoComCandidatoById(@Param("processoId") UUID processoId);

    
    @Query("SELECT p FROM ProcessoSeletivoEntity p " +
           "WHERE p.candidatura.candidato.id = :candidatoId " +
           "AND p.dataFim IS NULL " +
           "ORDER BY p.dataUltimaMudanca DESC")
    List<ProcessoSeletivoEntity> findActiveProcessesByCandidato(@Param("candidatoId") UUID candidatoId);
}