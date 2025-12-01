package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.AvaliacaoOrganizacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoOrganizacaoRepository {
    AvaliacaoOrganizacao save(AvaliacaoOrganizacao avaliacao);

    Optional<AvaliacaoOrganizacao> findById(UUID id);

    Optional<AvaliacaoOrganizacao> findByProcessoId(UUID processoId);

    List<AvaliacaoOrganizacao> findByOrganizacaoId(UUID organizacaoId);

    long countByOrganizacaoId(UUID organizacaoId);

    Double findAverageNotaByOrganizacaoId(UUID organizacaoId);

    boolean existsByProcessoId(UUID processoId);
}
