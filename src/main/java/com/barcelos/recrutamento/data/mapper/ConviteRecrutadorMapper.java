package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.ConviteRecrutador;
import com.barcelos.recrutamento.data.entity.ConviteRecrutadorEntity;
import org.springframework.stereotype.Component;

@Component
public class ConviteRecrutadorMapper {

    public ConviteRecrutador toDomain(ConviteRecrutadorEntity entity) {
        if (entity == null) {
            return null;
        }

        return ConviteRecrutador.reconstruir(
                entity.getId(),
                entity.getOrganizacaoId(),
                entity.getEmail(),
                entity.getToken(),
                entity.getStatus(),
                entity.getDataEnvio(),
                entity.getDataExpiracao(),
                entity.getDataAceite()
        );
    }

    public ConviteRecrutadorEntity toEntity(ConviteRecrutador domain) {
        if (domain == null) {
            return null;
        }

        return new ConviteRecrutadorEntity(
                domain.getId(),
                domain.getOrganizacaoId(),
                domain.getEmail(),
                domain.getToken(),
                domain.getStatus(),
                domain.getDataEnvio(),
                domain.getDataExpiracao(),
                domain.getDataAceite()
        );
    }
}

