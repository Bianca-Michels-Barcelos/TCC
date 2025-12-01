package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.BeneficioOrg;
import com.barcelos.recrutamento.data.entity.BeneficioOrgEntity;
import com.barcelos.recrutamento.data.entity.OrganizacaoEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BeneficioOrgMapper {

    default BeneficioOrg toDomain(BeneficioOrgEntity entity) {
        if (entity == null) return null;
        return BeneficioOrg.rehydrate(
            entity.getId(),
            entity.getOrganizacao().getId(),
            entity.getTitulo(),
            entity.getDescricao()
        );
    }

    default BeneficioOrgEntity toEntity(BeneficioOrg domain, @Context OrganizacaoEntity organizacao) {
        if (domain == null) return null;
        BeneficioOrgEntity entity = new BeneficioOrgEntity();
        entity.setId(domain.getId());
        entity.setOrganizacao(organizacao);
        entity.setTitulo(domain.getNome());
        entity.setDescricao(domain.getDescricao());
        return entity;
    }
}
