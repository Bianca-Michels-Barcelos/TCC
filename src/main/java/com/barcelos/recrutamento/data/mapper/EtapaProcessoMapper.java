package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.EtapaProcesso;
import com.barcelos.recrutamento.data.entity.EtapaProcessoEntity;
import com.barcelos.recrutamento.data.entity.VagaEntity;
import org.springframework.stereotype.Component;

@Component
public class EtapaProcessoMapper {

    public EtapaProcesso toDomain(EtapaProcessoEntity entity) {
        return EtapaProcesso.rehydrate(
                entity.getId(),
                entity.getVaga().getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getTipo(),
                entity.getOrdem(),
                entity.getStatus(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getDataCriacao()
        );
    }

    public EtapaProcessoEntity toEntity(EtapaProcesso domain, VagaEntity vaga) {
        var entity = new EtapaProcessoEntity();
        entity.setId(domain.getId());
        entity.setVaga(vaga);
        entity.setNome(domain.getNome());
        entity.setDescricao(domain.getDescricao());
        entity.setTipo(domain.getTipo());
        entity.setOrdem(domain.getOrdem());
        entity.setStatus(domain.getStatus());
        entity.setDataInicio(domain.getDataInicio());
        entity.setDataFim(domain.getDataFim());
        entity.setDataCriacao(domain.getDataCriacao());
        return entity;
    }
}
