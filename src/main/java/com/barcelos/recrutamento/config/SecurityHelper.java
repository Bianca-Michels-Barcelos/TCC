package com.barcelos.recrutamento.config;

import com.barcelos.recrutamento.core.port.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityHelper {

    private final UsuarioRepository usuarioRepository;

    public SecurityHelper(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UUID getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return usuario.getId();
    }

    public boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean hasAnyRole(Authentication authentication, String... roles) {
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }

    public UUID getOrganizacaoIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof OrganizacaoAuthentication organizacaoAuth) {
            return organizacaoAuth.getOrganizacaoId();
        }
        return null;
    }
}
