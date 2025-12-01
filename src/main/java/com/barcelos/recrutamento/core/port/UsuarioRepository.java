package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Optional<Usuario> findById(UUID id);

    Optional<Usuario> findByEmail(String email);

    Usuario save(Usuario usuario);
}
