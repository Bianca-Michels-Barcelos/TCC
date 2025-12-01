package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.Usuario;
import com.barcelos.recrutamento.core.model.vo.Cpf;
import com.barcelos.recrutamento.core.model.vo.Email;
import com.barcelos.recrutamento.data.entity.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    default Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;
        return Usuario.rehydrate(
            entity.getId(),
            entity.getNome(),
            new Email(entity.getEmail()),
            new Cpf(entity.getCpf()),
            entity.getSenhaHash(),
            entity.isEmailVerificado(),
            entity.isAtivo()
        );
    }

    default UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setEmail(domain.getEmail().value());
        entity.setCpf(domain.getCpf().value());
        entity.setSenhaHash(domain.getSenhaHash());
        entity.setEmailVerificado(domain.isEmailVerificado());
        entity.setAtivo(domain.isAtivo());
        return entity;
    }
}
