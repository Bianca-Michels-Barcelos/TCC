package com.barcelos.recrutamento.config;

import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.data.entity.PapelOrganizacao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizacaoSecurityService {

    private final MembroOrganizacaoRepository membroOrganizacaoRepository;
    private final SecurityHelper securityHelper;

    public OrganizacaoSecurityService(MembroOrganizacaoRepository membroOrganizacaoRepository,
                                      SecurityHelper securityHelper) {
        this.membroOrganizacaoRepository = membroOrganizacaoRepository;
        this.securityHelper = securityHelper;
    }

    public void validateUserBelongsToOrganization(UUID organizacaoId, Authentication authentication) {
        UUID tokenOrganizacaoId = securityHelper.getOrganizacaoIdFromAuthentication(authentication);
        if (tokenOrganizacaoId == null) {
            throw new AccessDeniedException("Token não contém organizacaoId");
        }
        if (!tokenOrganizacaoId.equals(organizacaoId)) {
            throw new AccessDeniedException("OrganizacaoId do token não corresponde ao organizacaoId solicitado");
        }

        UUID usuarioId = securityHelper.getUserIdFromAuthentication(authentication);

        if (!membroOrganizacaoRepository.exists(organizacaoId, usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta organização");
        }
    }

    public void validateUserIsAdminOfOrganization(UUID organizacaoId, Authentication authentication) {
        UUID tokenOrganizacaoId = securityHelper.getOrganizacaoIdFromAuthentication(authentication);
        if (tokenOrganizacaoId == null) {
            throw new AccessDeniedException("Token não contém organizacaoId");
        }
        if (!tokenOrganizacaoId.equals(organizacaoId)) {
            throw new AccessDeniedException("OrganizacaoId do token não corresponde ao organizacaoId solicitado");
        }

        UUID usuarioId = securityHelper.getUserIdFromAuthentication(authentication);

        var papel = membroOrganizacaoRepository.getPapel(organizacaoId, usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Você não pertence a esta organização"));

        if (papel != PapelOrganizacao.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem realizar esta ação");
        }
    }
}
