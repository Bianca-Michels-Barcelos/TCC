package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.Candidatura;
import com.barcelos.recrutamento.data.entity.CandidaturaEntity;
import com.barcelos.recrutamento.data.entity.UsuarioEntity;
import com.barcelos.recrutamento.data.entity.VagaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CandidaturaMapper {

    
    @Mapping(target = "vagaId", source = "vaga.id")
    @Mapping(target = "candidatoUsuarioId", source = "candidato.id")
    default Candidatura toDomain(CandidaturaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Candidatura.rehydrate(
            entity.getId(),
            entity.getVaga().getId(),
            entity.getCandidato().getId(),
            entity.getStatus(),
            entity.getDataCandidatura(),
            entity.getArquivoCurriculo(),
            entity.getCompatibilidade()
        );
    }

    
    @Mapping(target = "vaga", source = "vaga")
    @Mapping(target = "candidato", source = "candidato")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    default CandidaturaEntity toEntity(Candidatura domain,
                                       @Context VagaEntity vaga,
                                       @Context UsuarioEntity candidato) {
        if (domain == null) {
            return null;
        }

        CandidaturaEntity entity = new CandidaturaEntity();
        entity.setId(domain.getId());
        entity.setVaga(vaga);
        entity.setCandidato(candidato);
        entity.setStatus(domain.getStatus());
        entity.setDataCandidatura(domain.getDataCandidatura());
        entity.setArquivoCurriculo(domain.getArquivoCurriculo());
        entity.setCompatibilidade(domain.getCompatibilidade());

        return entity;
    }
}
