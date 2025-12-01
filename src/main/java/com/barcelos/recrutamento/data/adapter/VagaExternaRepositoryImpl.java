package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.VagaExterna;
import com.barcelos.recrutamento.core.port.VagaExternaRepository;
import com.barcelos.recrutamento.data.mapper.VagaExternaMapper;
import com.barcelos.recrutamento.data.spring.PerfilCandidatoJpaRepository;
import com.barcelos.recrutamento.data.spring.VagaExternaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VagaExternaRepositoryImpl implements VagaExternaRepository {

    private final VagaExternaJpaRepository jpaRepository;
    private final PerfilCandidatoJpaRepository perfilCandidatoJpaRepository;
    private final VagaExternaMapper mapper;

    public VagaExternaRepositoryImpl(VagaExternaJpaRepository jpaRepository,
                                     PerfilCandidatoJpaRepository perfilCandidatoJpaRepository,
                                     VagaExternaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.perfilCandidatoJpaRepository = perfilCandidatoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public VagaExterna save(VagaExterna vagaExterna) {
        var candidato = perfilCandidatoJpaRepository.getReferenceById(vagaExterna.getCandidatoUsuarioId());

        var entityOptional = jpaRepository.findById(vagaExterna.getId());

        var entity = entityOptional.orElseGet(() -> {

            return mapper.toEntity(vagaExterna, candidato);
        });

        if (entityOptional.isPresent()) {
            entity.setTitulo(vagaExterna.getTitulo());
            entity.setDescricao(vagaExterna.getDescricao());
            entity.setRequisitos(vagaExterna.getRequisitos());
            entity.setArquivoCurriculo(vagaExterna.getArquivoCurriculo());
            entity.setConteudoCurriculo(vagaExterna.getConteudoCurriculo());
            entity.setModeloCurriculo(vagaExterna.getModeloCurriculo());
            entity.setCandidato(candidato);
            entity.setAtivo(vagaExterna.getAtivo());

        }

        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VagaExterna> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<VagaExterna> listByUsuario(UUID usuarioId) {
        return jpaRepository.findByCandidato_UsuarioId(usuarioId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(VagaExterna vagaExterna) {
        jpaRepository.deleteById(vagaExterna.getId());
    }
}
