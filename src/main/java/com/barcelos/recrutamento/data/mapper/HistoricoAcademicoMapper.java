package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.HistoricoAcademico;
import com.barcelos.recrutamento.data.entity.HistoricoAcademicoEntity;
import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HistoricoAcademicoMapper {

    default HistoricoAcademico toDomain(HistoricoAcademicoEntity entity) {
        if (entity == null) return null;

        UUID usuarioId = entity.getPerfilCandidato().getId();
        return HistoricoAcademico.rehydrate(
            entity.getId(),
            usuarioId,
            entity.getTitulo(),
            entity.getDescricao(),
            entity.getInstituicao(),
            entity.getDataInicio(),
            entity.getDataFim(),
            entity.isAtivo()
        );
    }

    default HistoricoAcademicoEntity toEntity(HistoricoAcademico domain, @Context PerfilCandidatoEntity perfil) {
        if (domain == null) return null;

        var entity = new HistoricoAcademicoEntity();
        entity.setId(domain.getId());
        entity.setPerfilCandidato(perfil);
        entity.setTitulo(domain.getTitulo());
        entity.setDescricao(domain.getDescricao());
        entity.setInstituicao(domain.getInstituicao());
        entity.setDataInicio(domain.getDataInicio());
        entity.setDataFim(domain.getDataFim());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
