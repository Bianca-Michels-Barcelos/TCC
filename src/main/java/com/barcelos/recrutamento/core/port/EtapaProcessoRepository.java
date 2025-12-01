package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.EtapaProcesso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EtapaProcessoRepository {

    EtapaProcesso save(EtapaProcesso etapa);

    Optional<EtapaProcesso> findById(UUID id);

    List<EtapaProcesso> findByVagaId(UUID vagaId);

    void deleteById(UUID id);
}
