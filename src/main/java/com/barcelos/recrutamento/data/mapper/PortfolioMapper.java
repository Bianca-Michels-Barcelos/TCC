package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.Portfolio;
import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import com.barcelos.recrutamento.data.entity.PortfolioEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {

    default Portfolio toDomain(PortfolioEntity entity) {
        if (entity == null) return null;

        return Portfolio.rehydrate(
            entity.getId(),
            entity.getPerfilCandidato().getId(),
            entity.getTitulo(),
            entity.getLink(),
            entity.isAtivo()
        );
    }

    default PortfolioEntity toEntity(Portfolio domain, @Context PerfilCandidatoEntity perfilCandidato) {
        if (domain == null) return null;

        var entity = new PortfolioEntity();
        entity.setId(domain.getId());
        entity.setPerfilCandidato(perfilCandidato);
        entity.setTitulo(domain.getTitulo());
        entity.setLink(domain.getLink());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
