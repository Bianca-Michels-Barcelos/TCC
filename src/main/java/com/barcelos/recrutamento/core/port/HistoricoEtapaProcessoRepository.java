package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.HistoricoEtapaProcesso;

import java.util.List;
import java.util.UUID;

public interface HistoricoEtapaProcessoRepository {
    HistoricoEtapaProcesso save(HistoricoEtapaProcesso historico);

    List<HistoricoEtapaProcesso> findByProcessoIdOrderByDataMudancaDesc(UUID processoId);
}
