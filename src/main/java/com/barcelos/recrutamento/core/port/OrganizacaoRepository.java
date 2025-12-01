package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.Organizacao;

import java.util.Optional;
import java.util.UUID;

public interface OrganizacaoRepository {
    Optional<Organizacao> findById(UUID id);

    Organizacao save(Organizacao organizacao);
}
