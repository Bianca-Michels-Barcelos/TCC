package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.Candidatura;
import com.barcelos.recrutamento.core.model.StatusCandidatura;
import com.barcelos.recrutamento.core.port.CandidaturaRepository;
import com.barcelos.recrutamento.data.mapper.CandidaturaMapper;
import com.barcelos.recrutamento.data.spring.CandidaturaJpaRepository;
import com.barcelos.recrutamento.data.spring.UsuarioJpaRepository;
import com.barcelos.recrutamento.data.spring.VagaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CandidaturaRepositoryImpl implements CandidaturaRepository {

    private final CandidaturaJpaRepository jpaRepository;
    private final VagaJpaRepository vagaJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final CandidaturaMapper mapper;

    public CandidaturaRepositoryImpl(CandidaturaJpaRepository jpaRepository,
                                     VagaJpaRepository vagaJpaRepository,
                                     UsuarioJpaRepository usuarioJpaRepository,
                                     CandidaturaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.vagaJpaRepository = vagaJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Candidatura save(Candidatura candidatura) {
        var vaga = vagaJpaRepository.getReferenceById(candidatura.getVagaId());
        var candidato = usuarioJpaRepository.getReferenceById(candidatura.getCandidatoUsuarioId());
        var entity = mapper.toEntity(candidatura, vaga, candidato);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByVagaAndCandidato(UUID vagaId, UUID candidatoUsuarioId) {
        return jpaRepository.existsByVaga_IdAndCandidato_Id(vagaId, candidatoUsuarioId);
    }

    @Override
    public Optional<Candidatura> findById(UUID candidaturaId) {
        return jpaRepository.findById(candidaturaId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Candidatura> findByVagaIdAndCandidatoUsuarioId(UUID vagaId, UUID candidatoUsuarioId) {
        return jpaRepository.findByVaga_IdAndCandidato_Id(vagaId, candidatoUsuarioId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Candidatura> listByVaga(UUID vagaId) {
        return jpaRepository.findByVaga_Id(vagaId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Candidatura> findByCandidatoUsuarioId(UUID candidatoUsuarioId) {
        return jpaRepository.findByCandidato_Id(candidatoUsuarioId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
