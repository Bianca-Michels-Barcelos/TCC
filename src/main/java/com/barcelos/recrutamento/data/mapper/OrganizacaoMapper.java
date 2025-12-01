package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.Organizacao;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.data.entity.EnderecoEmbeddable;
import com.barcelos.recrutamento.data.entity.OrganizacaoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizacaoMapper {

    default Organizacao toDomain(OrganizacaoEntity entity) {
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

        return Organizacao.rehydrate(
            entity.getId(),
            new Cnpj(entity.getCnpj()),
            entity.getNome(),
            endereco,
            entity.isAtivo()
        );
    }

    default OrganizacaoEntity toEntity(Organizacao domain) {
        if (domain == null) return null;

        var entity = new OrganizacaoEntity();
        entity.setId(domain.getId());
        entity.setCnpj(domain.getCnpj().value());
        entity.setNome(domain.getNome());
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
