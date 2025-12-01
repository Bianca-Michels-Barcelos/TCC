package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.ExperienciaProfissional;
import com.barcelos.recrutamento.core.port.ExperienciaProfissionalRepository;
import com.barcelos.recrutamento.data.mapper.ExperienciaProfissionalMapper;
import com.barcelos.recrutamento.data.spring.ExperienciaProfissionalJpaRepository;
import com.barcelos.recrutamento.data.spring.PerfilCandidatoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExperienciaProfissionalRepositoryImpl implements ExperienciaProfissionalRepository {

    private final ExperienciaProfissionalJpaRepository jpa;
    private final PerfilCandidatoJpaRepository perfilJpa;
    private final ExperienciaProfissionalMapper mapper;

    public ExperienciaProfissionalRepositoryImpl(ExperienciaProfissionalJpaRepository jpa,
                                                 PerfilCandidatoJpaRepository perfilJpa,
                                                 ExperienciaProfissionalMapper mapper) {
        this.jpa = jpa;
        this.perfilJpa = perfilJpa;
        this.mapper = mapper;
    }

    @Override
    public ExperienciaProfissional save(ExperienciaProfissional exp) {
        var perfil = perfilJpa.getReferenceById(exp.getUsuarioId());
        var entity = mapper.toEntity(exp, perfil);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<ExperienciaProfissional> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ExperienciaProfissional> listByUsuario(UUID usuarioId) {
        return jpa.findByPerfilCandidato_Id(usuarioId).stream().map(mapper::toDomain).toList();
    }
}
