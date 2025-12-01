package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.BeneficioOrg;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficioOrgRepository {
    BeneficioOrg save(BeneficioOrg beneficio);

    Optional<BeneficioOrg> findById(UUID id);

    List<BeneficioOrg> listByOrganizacao(UUID organizacaoId);

    void deleteById(UUID id);
}