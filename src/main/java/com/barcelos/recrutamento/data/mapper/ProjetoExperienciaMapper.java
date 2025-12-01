package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.ProjetoExperiencia;
import com.barcelos.recrutamento.data.entity.ExperienciaProfissionalEntity;
import com.barcelos.recrutamento.data.entity.ProjetoExperienciaEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProjetoExperienciaMapper {

    default ProjetoExperiencia toDomain(ProjetoExperienciaEntity entity) {
        if (entity == null) return null;

        UUID experienciaId = entity.getExperienciaProfissional().getId();
        return ProjetoExperiencia.rehydrate(
            entity.getId(),
            experienciaId,
            entity.getTitulo(),
            entity.getDescricao(),
            entity.isAtivo()
        );
    }

    default ProjetoExperienciaEntity toEntity(ProjetoExperiencia domain, @Context ExperienciaProfissionalEntity experiencia) {
        if (domain == null) return null;

        var entity = new ProjetoExperienciaEntity();
        entity.setId(domain.getId());
        entity.setExperienciaProfissional(experiencia);
        entity.setTitulo(domain.getNome());
        entity.setDescricao(domain.getDescricao());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
