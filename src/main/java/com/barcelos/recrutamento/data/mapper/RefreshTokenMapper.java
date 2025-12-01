package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.RefreshToken;
import com.barcelos.recrutamento.data.entity.RefreshTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    public RefreshTokenEntity toEntity(RefreshToken token) {
        var entity = new RefreshTokenEntity();
        entity.setId(token.getId());
        entity.setToken(token.getToken());
        entity.setUsuarioId(token.getUsuarioId());
        entity.setExpiraEm(token.getExpiraEm());
        entity.setCriadoEm(token.getCriadoEm());
        entity.setRevogado(token.isRevogado());
        return entity;
    }

    public RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.reconstituir(
                entity.getId(),
                entity.getToken(),
                entity.getUsuarioId(),
                entity.getExpiraEm(),
                entity.getCriadoEm(),
                entity.isRevogado()
        );
    }
}
