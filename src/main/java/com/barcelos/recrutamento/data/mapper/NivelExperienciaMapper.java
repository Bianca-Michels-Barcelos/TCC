package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.NivelExperiencia;
import com.barcelos.recrutamento.data.entity.NivelExperienciaEntity;
import com.barcelos.recrutamento.data.entity.OrganizacaoEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NivelExperienciaMapper {

    
    default NivelExperiencia toDomain(NivelExperienciaEntity entity) {
        if (entity == null) {
            return null;
        }
        return NivelExperiencia.rehydrate(
            entity.getId(),
            entity.getOrganizacao().getId(),
            entity.getDescricao(),
            entity.isAtivo()
        );
    }

    
    default NivelExperienciaEntity toEntity(NivelExperiencia domain,
                                            @Context OrganizacaoEntity organizacao) {
        if (domain == null) {
            return null;
        }

        NivelExperienciaEntity entity = new NivelExperienciaEntity();
        entity.setId(domain.getId());
        entity.setOrganizacao(organizacao);
        entity.setDescricao(domain.getDescricao());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
