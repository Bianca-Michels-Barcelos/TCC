package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.MembroOrganizacao;
import com.barcelos.recrutamento.data.entity.PapelOrganizacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembroOrganizacaoRepository {
    MembroOrganizacao save(MembroOrganizacao membro);
    Optional<MembroOrganizacao> findByIds(UUID organizacaoId, UUID usuarioId);
    List<MembroOrganizacao> listByOrganizacao(UUID organizacaoId);
    List<MembroOrganizacao> listByUsuario(UUID usuarioId);
    boolean exists(UUID organizacaoId, UUID usuarioId);

    
    default Optional<PapelOrganizacao> getPapel(UUID organizacaoId, UUID usuarioId) {
        return findByIds(organizacaoId, usuarioId).map(MembroOrganizacao::getPapel);
    }

    default void addAdmin(UUID organizacaoId, UUID usuarioId) {
        save(MembroOrganizacao.novo(organizacaoId, usuarioId, PapelOrganizacao.ADMIN));
    }

    default void addMembro(UUID organizacaoId, UUID usuarioId, PapelOrganizacao papel) {
        save(MembroOrganizacao.novo(organizacaoId, usuarioId, papel));
    }

    void deleteByIds(UUID organizacaoId, UUID usuarioId);
}