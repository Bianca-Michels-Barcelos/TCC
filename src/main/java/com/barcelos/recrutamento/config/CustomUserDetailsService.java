package com.barcelos.recrutamento.config;

import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final MembroOrganizacaoRepository membroOrganizacaoRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository,
                                    MembroOrganizacaoRepository membroOrganizacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.membroOrganizacaoRepository = membroOrganizacaoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!usuario.isAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        var membros = membroOrganizacaoRepository.listByUsuario(usuario.getId());

        if (membros.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CANDIDATO"));
        } else {
            for (var membro : membros) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + membro.getPapel().name()));
            }
        }

        return User.builder()
                .username(usuario.getEmail().value())
                .password(usuario.getSenhaHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.isAtivo())
                .build();
    }
}
