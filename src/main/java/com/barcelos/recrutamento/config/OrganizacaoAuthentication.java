package com.barcelos.recrutamento.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class OrganizacaoAuthentication extends UsernamePasswordAuthenticationToken {

    private final UUID organizacaoId;

    public OrganizacaoAuthentication(Object principal, Object credentials,
                                      Collection<? extends GrantedAuthority> authorities,
                                      UUID organizacaoId) {
        super(principal, credentials, authorities);
        this.organizacaoId = organizacaoId;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }
}
