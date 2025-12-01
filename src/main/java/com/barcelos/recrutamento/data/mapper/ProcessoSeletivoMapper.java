package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.ProcessoSeletivo;
import com.barcelos.recrutamento.data.entity.CandidaturaEntity;
import com.barcelos.recrutamento.data.entity.EtapaProcessoEntity;
import com.barcelos.recrutamento.data.entity.ProcessoSeletivoEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProcessoSeletivoMapper {

    
    @Mapping(target = "candidaturaId", source = "candidatura.id")
    @Mapping(target = "etapaProcessoAtualId", source = "etapaProcessoAtual.id")
    default ProcessoSeletivo toDomain(ProcessoSeletivoEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProcessoSeletivo.rehydrate(
            entity.getId(),
            entity.getCandidatura().getId(),
            entity.getEtapaProcessoAtual() != null ? entity.getEtapaProcessoAtual().getId() : null,
            entity.getDataInicio(),
            entity.getDataFim(),
            entity.getDataUltimaMudanca()
        );
    }

    
    @Mapping(target = "candidatura", source = "candidatura")
    @Mapping(target = "etapaProcessoAtual", source = "etapaProcessoAtual")
    default ProcessoSeletivoEntity toEntity(ProcessoSeletivo domain,
                                           @Context CandidaturaEntity candidatura,
                                           @Context EtapaProcessoEntity etapaProcessoAtual) {
        if (domain == null) {
            return null;
        }

        ProcessoSeletivoEntity entity = new ProcessoSeletivoEntity();
        entity.setId(domain.getId());
        entity.setCandidatura(candidatura);
        entity.setEtapaProcessoAtual(etapaProcessoAtual);
        entity.setDataInicio(domain.getDataInicio());
        entity.setDataFim(domain.getDataFim());
        entity.setDataUltimaMudanca(domain.getDataUltimaMudanca());

        return entity;
    }
}
