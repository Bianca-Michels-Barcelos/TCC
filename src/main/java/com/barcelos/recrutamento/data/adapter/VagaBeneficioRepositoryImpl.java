package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.VagaBeneficio;
import com.barcelos.recrutamento.core.port.VagaBeneficioRepository;
import com.barcelos.recrutamento.data.entity.VagaBeneficioEntity;
import com.barcelos.recrutamento.data.spring.VagaBeneficioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class VagaBeneficioRepositoryImpl implements VagaBeneficioRepository {

    private final VagaBeneficioJpaRepository jpa;

    public VagaBeneficioRepositoryImpl(VagaBeneficioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void add(VagaBeneficio vagaBeneficio) {
        if (!jpa.existsByVagaIdAndBeneficioId(vagaBeneficio.getVagaId(), vagaBeneficio.getBeneficioId())) {
            var entity = toEntity(vagaBeneficio);
            jpa.save(entity);
        }
    }

    @Override
    public void remove(UUID vagaId, UUID beneficioId) {
        jpa.deleteByVagaIdAndBeneficioId(vagaId, beneficioId);
    }

    @Override
    public List<VagaBeneficio> listByVaga(UUID vagaId) {
        return jpa.findByVagaId(vagaId).stream()
                .map(this::toDomain)
                .toList();
    }

    private VagaBeneficioEntity toEntity(VagaBeneficio domain) {
        var entity = new VagaBeneficioEntity();
        entity.setVagaId(domain.getVagaId());
        entity.setBeneficioId(domain.getBeneficioId());
        return entity;
    }

    private VagaBeneficio toDomain(VagaBeneficioEntity entity) {
        return VagaBeneficio.rehydrate(entity.getVagaId(), entity.getBeneficioId());
    }
}