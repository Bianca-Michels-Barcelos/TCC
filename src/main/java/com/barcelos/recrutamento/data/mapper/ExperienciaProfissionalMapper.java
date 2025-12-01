package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.ExperienciaProfissional;
import com.barcelos.recrutamento.data.entity.ExperienciaProfissionalEntity;
import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ExperienciaProfissionalMapper {

    default ExperienciaProfissional toDomain(ExperienciaProfissionalEntity entity) {
        if (entity == null) return null;

        UUID usuarioId = entity.getPerfilCandidato().getId();
        return ExperienciaProfissional.rehydrate(
            entity.getId(),
            usuarioId,
            entity.getCargo(),
            entity.getEmpresa(),
            entity.getDescricao(),
            entity.getDataInicio(),
            entity.getDataFim(),
            entity.isAtivo()
        );
    }

    default ExperienciaProfissionalEntity toEntity(ExperienciaProfissional domain, @Context PerfilCandidatoEntity perfil) {
        if (domain == null) return null;

        var entity = new ExperienciaProfissionalEntity();
        entity.setId(domain.getId());
        entity.setPerfilCandidato(perfil);
        entity.setCargo(domain.getCargo());
        entity.setEmpresa(domain.getEmpresa());
        entity.setDescricao(domain.getDescricao());
        entity.setDataInicio(domain.getDataInicio());
        entity.setDataFim(domain.getDataFim());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
