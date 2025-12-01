package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.Organizacao;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.data.mapper.OrganizacaoMapper;
import com.barcelos.recrutamento.data.spring.OrganizacaoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrganizacaoRepositoryImpl implements OrganizacaoRepository {
    private final OrganizacaoJpaRepository jpa;
    private final OrganizacaoMapper mapper;

    public OrganizacaoRepositoryImpl(OrganizacaoJpaRepository jpa, OrganizacaoMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Organizacao> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Organizacao save(Organizacao organizacao) {
        var saved = jpa.save(mapper.toEntity(organizacao));
        return mapper.toDomain(saved);
    }
}
