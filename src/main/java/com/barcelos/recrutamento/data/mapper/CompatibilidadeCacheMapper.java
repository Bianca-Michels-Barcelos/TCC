package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.CompatibilidadeCache;
import com.barcelos.recrutamento.data.entity.CompatibilidadeCacheEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompatibilidadeCacheMapper {

    default CompatibilidadeCache toDomain(CompatibilidadeCacheEntity entity) {
        if (entity == null) {
            return null;
        }

        return CompatibilidadeCache.rehydrate(
            entity.getId(),
            entity.getCandidatoUsuarioId(),
            entity.getVagaId(),
            entity.getPercentualCompatibilidade(),
            entity.getJustificativa(),
            entity.getDataCalculo(),
            entity.getDataAtualizacao()
        );
    }

    default CompatibilidadeCacheEntity toEntity(CompatibilidadeCache domain) {
        if (domain == null) {
            return null;
        }

        return new CompatibilidadeCacheEntity(
            domain.getId(),
            domain.getCandidatoUsuarioId(),
            domain.getVagaId(),
            domain.getPercentualCompatibilidade(),
            domain.getJustificativa(),
            domain.getDataCalculo(),
            domain.getDataAtualizacao()
        );
    }
}

