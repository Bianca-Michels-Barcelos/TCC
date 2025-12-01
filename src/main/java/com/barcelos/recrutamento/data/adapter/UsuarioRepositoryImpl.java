package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.Usuario;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.data.mapper.UsuarioMapper;
import com.barcelos.recrutamento.data.spring.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;
    private final UsuarioMapper mapper;

    public UsuarioRepositoryImpl(UsuarioJpaRepository jpa, UsuarioMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Usuario save(Usuario usuario) {
        var saved = jpa.save(mapper.toEntity(usuario));
        return mapper.toDomain(saved);
    }
}
