package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.ResetSenha;
import com.barcelos.recrutamento.core.model.StatusResetSenha;
import com.barcelos.recrutamento.data.entity.ResetSenhaEntity;
import org.springframework.stereotype.Component;

@Component
public class ResetSenhaMapper {

    
    public ResetSenha toDomain(ResetSenhaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ResetSenha.reconstruir(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getToken(),
                StatusResetSenha.valueOf(entity.getStatus()),
                entity.getDataSolicitacao(),
                entity.getDataExpiracao(),
                entity.getDataUso()
        );
    }

    
    public ResetSenhaEntity toEntity(ResetSenha model) {
        if (model == null) {
            return null;
        }

        return new ResetSenhaEntity(
                model.getId(),
                model.getUsuarioId(),
                model.getToken(),
                model.getStatus().name(),
                model.getDataSolicitacao(),
                model.getDataExpiracao(),
                model.getDataUso()
        );
    }
}

