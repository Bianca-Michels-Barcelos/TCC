package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.Competencia;
import com.barcelos.recrutamento.data.entity.CompetenciaEntity;
import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompetenciaMapper {

    default Competencia toDomain(CompetenciaEntity entity) {
        if (entity == null) return null;

        return Competencia.rehydrate(
            entity.getId(),
            entity.getPerfilCandidato().getId(),
            entity.getTitulo(),
            entity.getDescricao(),
            entity.getNivel(),
            entity.isAtivo()
        );
    }

    default CompetenciaEntity toEntity(Competencia domain, @Context PerfilCandidatoEntity perfilCandidato) {
        if (domain == null) return null;

        var entity = new CompetenciaEntity();
        entity.setId(domain.getId());
        entity.setPerfilCandidato(perfilCandidato);
        entity.setTitulo(domain.getTitulo());
        entity.setDescricao(domain.getDescricao());
        entity.setNivel(domain.getNivel());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
