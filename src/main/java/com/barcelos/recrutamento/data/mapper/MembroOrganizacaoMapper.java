package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.MembroOrganizacao;
import com.barcelos.recrutamento.data.entity.MembroOrganizacaoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembroOrganizacaoMapper {

    default MembroOrganizacao toDomain(MembroOrganizacaoEntity entity) {
        if (entity == null) return null;

        return MembroOrganizacao.rehydrate(
            entity.getOrganizacaoId(),
            entity.getUsuarioId(),
            entity.getPapel(),
            entity.isAtivo()
        );
    }

    default MembroOrganizacaoEntity toEntity(MembroOrganizacao domain) {
        if (domain == null) return null;

        var entity = new MembroOrganizacaoEntity();
        entity.setOrganizacaoId(domain.getOrganizacaoId());
        entity.setUsuarioId(domain.getUsuarioId());
        entity.setPapel(domain.getPapel());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
