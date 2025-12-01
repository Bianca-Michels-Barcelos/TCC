package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.PerfilCandidato;
import com.barcelos.recrutamento.core.port.PerfilCandidatoRepository;
import com.barcelos.recrutamento.data.mapper.PerfilCandidatoMapper;
import com.barcelos.recrutamento.data.spring.PerfilCandidatoJpaRepository;
import com.barcelos.recrutamento.data.spring.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PerfilCandidatoRepositoryImpl implements PerfilCandidatoRepository {

    private final PerfilCandidatoJpaRepository jpa;
    private final UsuarioJpaRepository usuarioJpa;
    private final PerfilCandidatoMapper mapper;

    public PerfilCandidatoRepositoryImpl(PerfilCandidatoJpaRepository jpa, UsuarioJpaRepository usuarioJpa, PerfilCandidatoMapper mapper) {
        this.jpa = jpa;
        this.usuarioJpa = usuarioJpa;
        this.mapper = mapper;
    }

    @Override
    public PerfilCandidato save(PerfilCandidato perfil) {
        var usuario = usuarioJpa.getReferenceById(perfil.getUsuarioId());
        var entity = mapper.toEntity(perfil, usuario);

        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<PerfilCandidato> findByUsuarioId(UUID usuarioId) {
        return jpa.findByUsuarioId(usuarioId).map(mapper::toDomain);
    }

    @Override
    public List<PerfilCandidato> findAll() {
        return jpa.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
