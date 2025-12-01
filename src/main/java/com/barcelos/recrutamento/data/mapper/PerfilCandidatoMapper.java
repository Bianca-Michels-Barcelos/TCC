package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.PerfilCandidato;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.data.entity.EnderecoEmbeddable;
import com.barcelos.recrutamento.data.entity.PerfilCandidatoEntity;
import com.barcelos.recrutamento.data.entity.UsuarioEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PerfilCandidatoMapper {

    default PerfilCandidato toDomain(PerfilCandidatoEntity entity) {
        if (entity == null) return null;

        var endEmbeddable = entity.getEndereco();
        var endereco = new Endereco(
            endEmbeddable.getLogradouro(),
            endEmbeddable.getComplemento(),
            endEmbeddable.getNumero(),
            new Cep(endEmbeddable.getCep()),
            endEmbeddable.getCidade(),
            new Sigla(endEmbeddable.getUf())
        );

        return PerfilCandidato.rehydrate(
            entity.getId(),
            entity.getUsuario().getId(),
            entity.getDataNascimento(),
            endereco,
            entity.isAtivo()
        );
    }

    default PerfilCandidatoEntity toEntity(PerfilCandidato domain, @Context UsuarioEntity usuario) {
        if (domain == null) return null;

        var entity = new PerfilCandidatoEntity();
        entity.setUsuario(usuario);
        entity.setDataNascimento(domain.getDataNascimento());
        entity.setAtivo(domain.isAtivo());

        var endEmbeddable = new EnderecoEmbeddable();
        endEmbeddable.setLogradouro(domain.getEndereco().logradouro());
        endEmbeddable.setComplemento(domain.getEndereco().complemento());
        endEmbeddable.setNumero(domain.getEndereco().numero());
        endEmbeddable.setCep(domain.getEndereco().cep().value());
        endEmbeddable.setCidade(domain.getEndereco().cidade());
        endEmbeddable.setUf(domain.getEndereco().uf().value());
        entity.setEndereco(endEmbeddable);

        return entity;
    }
}
