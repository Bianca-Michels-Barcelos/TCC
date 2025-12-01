package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.Competencia;
import com.barcelos.recrutamento.core.port.CompetenciaRepository;
import com.barcelos.recrutamento.data.mapper.CompetenciaMapper;
import com.barcelos.recrutamento.data.spring.CompetenciaJpaRepository;
import com.barcelos.recrutamento.data.spring.PerfilCandidatoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CompetenciaRepositoryImpl implements CompetenciaRepository {

    private final CompetenciaJpaRepository jpa;
    private final PerfilCandidatoJpaRepository perfilCandidatoJpa;
    private final CompetenciaMapper mapper;

    public CompetenciaRepositoryImpl(
            CompetenciaJpaRepository jpa,
            PerfilCandidatoJpaRepository perfilCandidatoJpa,
            CompetenciaMapper mapper) {
        this.jpa = jpa;
        this.perfilCandidatoJpa = perfilCandidatoJpa;
        this.mapper = mapper;
    }

    @Override
    public Competencia save(Competencia competencia) {
        var perfilCandidato = perfilCandidatoJpa.getReferenceById(competencia.getPerfilCandidatoId());
        var entity = mapper.toEntity(competencia, perfilCandidato);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Competencia> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Competencia> listByPerfilCandidato(UUID perfilCandidatoId) {
        return jpa.findByPerfilCandidatoId(perfilCandidatoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }
}
