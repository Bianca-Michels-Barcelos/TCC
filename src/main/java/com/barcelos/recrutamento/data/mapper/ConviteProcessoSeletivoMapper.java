package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.ConviteProcessoSeletivo;
import com.barcelos.recrutamento.data.entity.ConviteProcessoSeletivoEntity;
import com.barcelos.recrutamento.data.entity.UsuarioEntity;
import com.barcelos.recrutamento.data.entity.VagaEntity;
import org.springframework.stereotype.Component;

@Component
public class ConviteProcessoSeletivoMapper {

    public ConviteProcessoSeletivo toDomain(ConviteProcessoSeletivoEntity entity) {
        if (entity == null) {
            return null;
        }

        return ConviteProcessoSeletivo.rehydrate(
                entity.getId(),
                entity.getVaga().getId(),
                entity.getRecrutador().getId(),
                entity.getCandidato().getId(),
                entity.getMensagem(),
                entity.getStatus(),
                entity.getDataEnvio(),
                entity.getDataExpiracao(),
                entity.getDataResposta()
        );
    }

    public ConviteProcessoSeletivoEntity toEntity(
            ConviteProcessoSeletivo domain,
            VagaEntity vaga,
            UsuarioEntity recrutador,
            UsuarioEntity candidato
    ) {
        if (domain == null) {
            return null;
        }

        var entity = new ConviteProcessoSeletivoEntity();
        entity.setId(domain.getId());
        entity.setVaga(vaga);
        entity.setRecrutador(recrutador);
        entity.setCandidato(candidato);
        entity.setMensagem(domain.getMensagem());
        entity.setStatus(domain.getStatus());
        entity.setDataEnvio(domain.getDataEnvio());
        entity.setDataExpiracao(domain.getDataExpiracao());
        entity.setDataResposta(domain.getDataResposta());

        return entity;
    }
}
