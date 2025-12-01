package com.barcelos.recrutamento.data.mapper;

import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.model.vo.EnderecoSimples;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import com.barcelos.recrutamento.data.entity.EnderecoSimplesEmbeddable;
import com.barcelos.recrutamento.data.entity.NivelExperienciaEntity;
import com.barcelos.recrutamento.data.entity.OrganizacaoEntity;
import com.barcelos.recrutamento.data.entity.UsuarioEntity;
import com.barcelos.recrutamento.data.entity.VagaEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VagaMapper {

    
    default Vaga toDomain(VagaEntity entity) {
        if (entity == null) {
            return null;
        }

        EnderecoSimples endereco = null;
        if (entity.getEndereco() != null) {
            var en = entity.getEndereco();
            endereco = new EnderecoSimples(
                    en.getCidade(),
                    new Sigla(en.getUf())
            );
        }

        var nivelId = entity.getNivelExperiencia() != null ? entity.getNivelExperiencia().getId() : null;

        return Vaga.rehydrate(
                entity.getId(),
                entity.getOrganizacao().getId(),
                entity.getRecrutador().getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getRequisitos(),
                entity.getSalario(),
                entity.getDataPublicacao(),
                entity.getStatus(),
                entity.getTipoContrato(),
                entity.getModalidade(),
                entity.getHorarioTrabalho(),
                nivelId,
                endereco,
                entity.isAtivo(),
                entity.getMotivoCancelamento()
        );
    }

    
    @Mapping(target = "organizacao", source = "organizacao")
    @Mapping(target = "recrutador", source = "recrutador")
    @Mapping(target = "nivelExperiencia", source = "nivelExperiencia")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    default VagaEntity toEntity(Vaga domain,
                                @Context OrganizacaoEntity organizacao,
                                @Context UsuarioEntity recrutador,
                                @Context NivelExperienciaEntity nivelExperiencia) {
        if (domain == null) {
            return null;
        }

        VagaEntity entity = new VagaEntity();
        entity.setId(domain.getId());
        entity.setOrganizacao(organizacao);
        entity.setRecrutador(recrutador);
        entity.setTitulo(domain.getTitulo());
        entity.setDescricao(domain.getDescricao());
        entity.setRequisitos(domain.getRequisitos());
        entity.setSalario(domain.getSalario());
        entity.setDataPublicacao(domain.getDataPublicacao());
        entity.setStatus(domain.getStatus());
        entity.setTipoContrato(domain.getTipoContrato());
        entity.setModalidade(domain.getModalidade());
        entity.setHorarioTrabalho(domain.getHorarioTrabalho());
        entity.setNivelExperiencia(nivelExperiencia);

        if (domain.getEndereco() != null) {
            var en = new EnderecoSimplesEmbeddable();
            en.setCidade(domain.getEndereco().cidade());
            en.setUf(domain.getEndereco().uf().value());
            entity.setEndereco(en);
        } else {
            entity.setEndereco(null);
        }

        entity.setAtivo(domain.isAtivo());
        entity.setMotivoCancelamento(domain.getMotivoCancelamento());
        return entity;
    }
}
