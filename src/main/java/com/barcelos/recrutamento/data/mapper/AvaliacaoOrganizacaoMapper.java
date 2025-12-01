package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.AvaliacaoOrganizacao;
import com.barcelos.recrutamento.data.entity.AvaliacaoOrganizacaoEntity;
import com.barcelos.recrutamento.data.entity.OrganizacaoEntity;
import com.barcelos.recrutamento.data.entity.ProcessoSeletivoEntity;
import com.barcelos.recrutamento.data.entity.UsuarioEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AvaliacaoOrganizacaoMapper {

    
    default AvaliacaoOrganizacao toDomain(AvaliacaoOrganizacaoEntity entity) {
        if (entity == null) {
            return null;
        }
        return AvaliacaoOrganizacao.rehydrate(
            entity.getId(),
            entity.getProcesso().getId(),
            entity.getCandidato().getId(),
            entity.getOrganizacao().getId(),
            entity.getNota(),
            entity.getComentario(),
            entity.getCriadoEm().toLocalDateTime(),
            entity.getAtualizadoEm().toLocalDateTime()
        );
    }

    
    default AvaliacaoOrganizacaoEntity toEntity(AvaliacaoOrganizacao domain,
                                                @Context ProcessoSeletivoEntity processo,
                                                @Context UsuarioEntity candidato,
                                                @Context OrganizacaoEntity organizacao) {
        if (domain == null) {
            return null;
        }

        AvaliacaoOrganizacaoEntity entity = new AvaliacaoOrganizacaoEntity();
        entity.setId(domain.getId());
        entity.setProcesso(processo);
        entity.setCandidato(candidato);
        entity.setOrganizacao(organizacao);
        entity.setNota(domain.getNota());
        entity.setComentario(domain.getComentario());

        return entity;
    }
}
