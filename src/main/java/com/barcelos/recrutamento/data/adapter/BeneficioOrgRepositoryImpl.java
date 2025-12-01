package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.model.BeneficioOrg;
import com.barcelos.recrutamento.core.port.BeneficioOrgRepository;
import com.barcelos.recrutamento.data.mapper.BeneficioOrgMapper;
import com.barcelos.recrutamento.data.spring.BeneficioOrgJpaRepository;
import com.barcelos.recrutamento.data.spring.OrganizacaoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BeneficioOrgRepositoryImpl implements BeneficioOrgRepository {

    private final BeneficioOrgJpaRepository jpaRepository;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final BeneficioOrgMapper mapper;

    public BeneficioOrgRepositoryImpl(BeneficioOrgJpaRepository jpaRepository,
                                      OrganizacaoJpaRepository organizacaoJpaRepository,
                                      BeneficioOrgMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public BeneficioOrg save(BeneficioOrg beneficio) {
        var organizacao = organizacaoJpaRepository.getReferenceById(beneficio.getOrganizacaoId());
        var entity = mapper.toEntity(beneficio, organizacao);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<BeneficioOrg> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<BeneficioOrg> listByOrganizacao(UUID organizacaoId) {
        return jpaRepository.findByOrganizacao_Id(organizacaoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}