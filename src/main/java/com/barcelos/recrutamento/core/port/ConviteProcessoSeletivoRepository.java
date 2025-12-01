package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.ConviteProcessoSeletivo;
import com.barcelos.recrutamento.core.model.StatusConviteProcesso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConviteProcessoSeletivoRepository {
    ConviteProcessoSeletivo save(ConviteProcessoSeletivo convite);

    Optional<ConviteProcessoSeletivo> findById(UUID id);

    List<ConviteProcessoSeletivo> findByCandidatoUsuarioId(UUID candidatoUsuarioId);

    List<ConviteProcessoSeletivo> findByVagaId(UUID vagaId);

    List<ConviteProcessoSeletivo> findByCandidatoUsuarioIdAndStatus(
            UUID candidatoUsuarioId,
            StatusConviteProcesso status
    );

    List<ConviteProcessoSeletivo> findByVagaIdAndCandidatoUsuarioId(
            UUID vagaId,
            UUID candidatoUsuarioId
    );
}
