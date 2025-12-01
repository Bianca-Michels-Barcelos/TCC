package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.ProjetoExperiencia;
import com.barcelos.recrutamento.core.port.ProjetoExperienciaRepository;
import com.barcelos.recrutamento.data.mapper.ProjetoExperienciaMapper;
import com.barcelos.recrutamento.data.spring.ExperienciaProfissionalJpaRepository;
import com.barcelos.recrutamento.data.spring.ProjetoExperienciaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ProjetoExperienciaRepositoryImpl implements ProjetoExperienciaRepository {

    private final ProjetoExperienciaJpaRepository jpa;
    private final ExperienciaProfissionalJpaRepository experienciaJpa;
    private final ProjetoExperienciaMapper mapper;

    public ProjetoExperienciaRepositoryImpl(ProjetoExperienciaJpaRepository jpa,
                                            ExperienciaProfissionalJpaRepository experienciaJpa,
                                            ProjetoExperienciaMapper mapper) {
        this.jpa = jpa;
        this.experienciaJpa = experienciaJpa;
        this.mapper = mapper;
    }

    @Override
    public ProjetoExperiencia save(ProjetoExperiencia projeto) {
        var exp = experienciaJpa.getReferenceById(projeto.getExperienciaProfissionalId());
        var entity = mapper.toEntity(projeto, exp);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
