package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.VagaExterna;
import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import com.barcelos.recrutamento.data.entity.VagaExternaEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VagaExternaMapper {

    
    @Mapping(target = "candidatoUsuarioId", source = "candidato.id")
    default VagaExterna toDomain(VagaExternaEntity entity) {
        if (entity == null) {
            return null;
        }

        var criadoEm = entity.getCriadoEm() != null
            ? entity.getCriadoEm().toLocalDateTime()
            : java.time.LocalDateTime.now();

        return VagaExterna.rehydrate(
            entity.getId(),
            entity.getTitulo(),
            entity.getDescricao(),
            entity.getRequisitos(),
            entity.getArquivoCurriculo(),
            entity.getConteudoCurriculo(),
            entity.getModeloCurriculo(),
            entity.getCandidato().getId(),
            entity.isAtivo(),
            criadoEm
        );
    }

    
    @Mapping(target = "candidato", source = "candidato")
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    default VagaExternaEntity toEntity(VagaExterna domain,
                                       @Context PerfilCandidatoEntity candidato) {
        if (domain == null) {
            return null;
        }

        VagaExternaEntity entity = new VagaExternaEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setDescricao(domain.getDescricao());
        entity.setRequisitos(domain.getRequisitos());
        entity.setArquivoCurriculo(domain.getArquivoCurriculo());
        entity.setConteudoCurriculo(domain.getConteudoCurriculo());
        entity.setModeloCurriculo(domain.getModeloCurriculo());
        entity.setCandidato(candidato);
        entity.setAtivo(domain.getAtivo());

        return entity;
    }
}
