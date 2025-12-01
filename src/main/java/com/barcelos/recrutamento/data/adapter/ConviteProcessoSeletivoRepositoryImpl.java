package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.ConviteProcessoSeletivo;
import com.barcelos.recrutamento.core.model.StatusConviteProcesso;
import com.barcelos.recrutamento.core.port.ConviteProcessoSeletivoRepository;
import com.barcelos.recrutamento.data.mapper.ConviteProcessoSeletivoMapper;
import com.barcelos.recrutamento.data.spring.ConviteProcessoSeletivoJpaRepository;
import com.barcelos.recrutamento.data.spring.UsuarioJpaRepository;
import com.barcelos.recrutamento.data.spring.VagaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ConviteProcessoSeletivoRepositoryImpl implements ConviteProcessoSeletivoRepository {

    private final ConviteProcessoSeletivoJpaRepository jpa;
    private final VagaJpaRepository vagaJpa;
    private final UsuarioJpaRepository usuarioJpa;
    private final ConviteProcessoSeletivoMapper mapper;

    public ConviteProcessoSeletivoRepositoryImpl(
            ConviteProcessoSeletivoJpaRepository jpa,
            VagaJpaRepository vagaJpa,
            UsuarioJpaRepository usuarioJpa,
            ConviteProcessoSeletivoMapper mapper
    ) {
        this.jpa = jpa;
        this.vagaJpa = vagaJpa;
        this.usuarioJpa = usuarioJpa;
        this.mapper = mapper;
    }

    @Override
    public ConviteProcessoSeletivo save(ConviteProcessoSeletivo convite) {
        var vaga = vagaJpa.getReferenceById(convite.getVagaId());
        var recrutador = usuarioJpa.getReferenceById(convite.getRecrutadorUsuarioId());
        var candidato = usuarioJpa.getReferenceById(convite.getCandidatoUsuarioId());

        var entity = mapper.toEntity(convite, vaga, recrutador, candidato);
        var saved = jpa.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ConviteProcessoSeletivo> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ConviteProcessoSeletivo> findByCandidatoUsuarioId(UUID candidatoUsuarioId) {
        return jpa.findByCandidatoUsuarioId(candidatoUsuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConviteProcessoSeletivo> findByVagaId(UUID vagaId) {
        return jpa.findByVagaId(vagaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConviteProcessoSeletivo> findByCandidatoUsuarioIdAndStatus(
            UUID candidatoUsuarioId,
            StatusConviteProcesso status
    ) {
        return jpa.findByCandidatoUsuarioIdAndStatus(candidatoUsuarioId, status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConviteProcessoSeletivo> findByVagaIdAndCandidatoUsuarioId(
            UUID vagaId,
            UUID candidatoUsuarioId
    ) {
        return jpa.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoUsuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
