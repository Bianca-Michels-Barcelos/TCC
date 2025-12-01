package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.api.dto.ProcessoSeletivoComCandidato;
import com.barcelos.recrutamento.core.model.ProcessoSeletivo;
import com.barcelos.recrutamento.core.port.ProcessoSeletivoRepository;
import com.barcelos.recrutamento.data.mapper.ProcessoSeletivoMapper;
import com.barcelos.recrutamento.data.spring.CandidaturaJpaRepository;
import com.barcelos.recrutamento.data.spring.EtapaProcessoJpaRepository;
import com.barcelos.recrutamento.data.spring.ProcessoSeletivoJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProcessoSeletivoRepositoryImpl implements ProcessoSeletivoRepository {

    private final ProcessoSeletivoJpaRepository jpa;
    private final CandidaturaJpaRepository candidaturaJpa;
    private final EtapaProcessoJpaRepository etapaProcessoJpa;
    private final ProcessoSeletivoMapper mapper;

    public ProcessoSeletivoRepositoryImpl(ProcessoSeletivoJpaRepository jpa,
                                          CandidaturaJpaRepository candidaturaJpa,
                                          EtapaProcessoJpaRepository etapaProcessoJpa,
                                          ProcessoSeletivoMapper mapper) {
        this.jpa = jpa;
        this.candidaturaJpa = candidaturaJpa;
        this.etapaProcessoJpa = etapaProcessoJpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<ProcessoSeletivo> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public ProcessoSeletivo save(ProcessoSeletivo processo) {
        var candidatura = candidaturaJpa.getReferenceById(processo.getCandidaturaId());
        var etapaProcesso = etapaProcessoJpa.getReferenceById(processo.getEtapaProcessoAtualId());
        var entity = mapper.toEntity(processo, candidatura, etapaProcesso);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public List<ProcessoSeletivo> findByVagaId(UUID vagaId) {
        return jpa.findByVagaId(vagaId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProcessoSeletivo> findByCandidaturaId(UUID candidaturaId) {
        return jpa.findByCandidaturaId(candidaturaId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ProcessoSeletivoComCandidato> findProcessosComCandidatosByVagaId(UUID vagaId) {
        List<Map<String, Object>> results = jpa.findProcessosComCandidatosByVagaId(vagaId);
        return results.stream()
                .map(this::mapToProcessoSeletivoComCandidato)
                .toList();
    }

    @Override
    public Optional<ProcessoSeletivoComCandidato> findProcessoComCandidatoById(UUID processoId) {
        List<Map<String, Object>> results = jpa.findProcessoComCandidatoById(processoId);
        return results.stream()
                .findFirst()
                .map(this::mapToProcessoSeletivoComCandidato);
    }

    private ProcessoSeletivoComCandidato mapToProcessoSeletivoComCandidato(Map<String, Object> map) {
        return new ProcessoSeletivoComCandidato(
                (UUID) map.get("processoId"),
                (UUID) map.get("candidaturaId"),
                map.get("dataInicio") != null ? convertToLocalDateTime(map.get("dataInicio")) : null,
                map.get("dataFim") != null ? convertToLocalDateTime(map.get("dataFim")) : null,
                map.get("dataUltimaMudanca") != null ? convertToLocalDateTime(map.get("dataUltimaMudanca")) : null,
                (UUID) map.get("etapaAtualId"),
                (String) map.get("etapaAtualNome"),
                (String) map.get("etapaAtualDescricao"),
                (Integer) map.get("etapaAtualOrdem"),
                map.get("etapaAtualStatus") != null ? map.get("etapaAtualStatus").toString() : null,
                (UUID) map.get("candidatoUsuarioId"),
                (String) map.get("candidatoNome"),
                (String) map.get("candidatoEmail"),
                map.get("statusCandidatura") != null ? map.get("statusCandidatura").toString() : null,
                map.get("dataCandidatura") != null ? convertToLocalDateTime(map.get("dataCandidatura")) : null,
                (BigDecimal) map.get("compatibilidade"),
                (String) map.get("arquivoCurriculo"),
                (UUID) map.get("vagaId"),
                (String) map.get("vagaTitulo")
        );
    }

    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate().atStartOfDay();
        }
        if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneId.systemDefault());
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to LocalDateTime");
    }
}
