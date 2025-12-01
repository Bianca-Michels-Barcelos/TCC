package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.HistoricoEtapaProcesso;
import com.barcelos.recrutamento.data.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HistoricoEtapaProcessoMapper {

    
    default HistoricoEtapaProcesso toDomain(HistoricoEtapaProcessoEntity entity) {
        if (entity == null) {
            return null;
        }
        return HistoricoEtapaProcesso.rehydrate(
            entity.getId(),
            entity.getProcesso().getId(),
            entity.getEtapaAnterior() != null ? entity.getEtapaAnterior().getId() : null,
            entity.getEtapaNova().getId(),
            entity.getUsuario().getId(),
            entity.getFeedback(),
            entity.getDataMudanca()
        );
    }

    
    default HistoricoEtapaProcessoEntity toEntity(HistoricoEtapaProcesso domain,
                                                  @Context ProcessoSeletivoEntity processo,
                                                  @Context EtapaProcessoEntity etapaAnterior,
                                                  @Context EtapaProcessoEntity etapaNova,
                                                  @Context UsuarioEntity usuario) {
        if (domain == null) {
            return null;
        }

        HistoricoEtapaProcessoEntity entity = new HistoricoEtapaProcessoEntity();
        entity.setId(domain.getId());
        entity.setProcesso(processo);
        entity.setEtapaAnterior(etapaAnterior);
        entity.setEtapaNova(etapaNova);
        entity.setUsuario(usuario);
        entity.setFeedback(domain.getFeedback());
        entity.setDataMudanca(domain.getDataMudanca());
        entity.setCriadoEm(domain.getDataMudanca());

        return entity;
    }
}
