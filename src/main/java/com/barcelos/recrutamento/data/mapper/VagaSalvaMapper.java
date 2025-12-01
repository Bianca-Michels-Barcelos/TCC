package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.VagaSalva;
import com.barcelos.recrutamento.data.entity.UsuarioEntity;
import com.barcelos.recrutamento.data.entity.VagaEntity;
import com.barcelos.recrutamento.data.entity.VagaSalvaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VagaSalvaMapper {

    
    default VagaSalva toDomain(VagaSalvaEntity entity) {
        if (entity == null) {
            return null;
        }
        return VagaSalva.rehydrate(
            entity.getId(),
            entity.getVaga().getId(),
            entity.getUsuario().getId(),
            entity.getSalvaEm()
        );
    }

    
    default VagaSalvaEntity toEntity(VagaSalva domain,
                                     @Context VagaEntity vaga,
                                     @Context UsuarioEntity usuario) {
        if (domain == null) {
            return null;
        }

        VagaSalvaEntity entity = new VagaSalvaEntity();
        entity.setId(domain.getId());
        entity.setVaga(vaga);
        entity.setUsuario(usuario);
        entity.setSalvaEm(domain.getSalvaEm());

        return entity;
    }
}
