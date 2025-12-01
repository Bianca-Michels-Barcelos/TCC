package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.Certificado;
import com.barcelos.recrutamento.data.entity.CertificadoEntity;
import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CertificadoMapper {

    default Certificado toDomain(CertificadoEntity entity) {
        if (entity == null) return null;

        return Certificado.rehydrate(
            entity.getId(),
            entity.getPerfilCandidato().getId(),
            entity.getTitulo(),
            entity.getInstituicao(),
            entity.getDataEmissao(),
            entity.getDataValidade(),
            entity.getDescricao(),
            entity.isAtivo()
        );
    }

    default CertificadoEntity toEntity(Certificado domain, @Context PerfilCandidatoEntity perfilCandidato) {
        if (domain == null) return null;

        var entity = new CertificadoEntity();
        entity.setId(domain.getId());
        entity.setPerfilCandidato(perfilCandidato);
        entity.setTitulo(domain.getTitulo());
        entity.setInstituicao(domain.getInstituicao());
        entity.setDataEmissao(domain.getDataEmissao());
        entity.setDataValidade(domain.getDataValidade());
        entity.setDescricao(domain.getDescricao());
        entity.setAtivo(domain.isAtivo());

        return entity;
    }
}
