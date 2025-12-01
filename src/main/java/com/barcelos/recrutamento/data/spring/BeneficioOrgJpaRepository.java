package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.BeneficioOrgEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BeneficioOrgJpaRepository extends JpaRepository<BeneficioOrgEntity, UUID> {
    List<BeneficioOrgEntity> findByOrganizacao_Id(UUID organizacaoId);
}
