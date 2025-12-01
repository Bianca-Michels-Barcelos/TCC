package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.VagaBeneficio;

import java.util.List;
import java.util.UUID;

public interface VagaBeneficioRepository {
    void add(VagaBeneficio vagaBeneficio);

    void remove(UUID vagaId, UUID beneficioId);

    List<VagaBeneficio> listByVaga(UUID vagaId);
}