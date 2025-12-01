package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.EtapaProcesso;
import com.barcelos.recrutamento.core.port.EtapaProcessoRepository;
import com.barcelos.recrutamento.data.mapper.EtapaProcessoMapper;
import com.barcelos.recrutamento.data.spring.EtapaProcessoJpaRepository;
import com.barcelos.recrutamento.data.spring.VagaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EtapaProcessoRepositoryImpl implements EtapaProcessoRepository {

    private final EtapaProcessoJpaRepository jpa;
    private final VagaJpaRepository vagaJpa;
    private final EtapaProcessoMapper mapper;

    public EtapaProcessoRepositoryImpl(
            EtapaProcessoJpaRepository jpa,
            VagaJpaRepository vagaJpa,
            EtapaProcessoMapper mapper
    ) {
        this.jpa = jpa;
        this.vagaJpa = vagaJpa;
        this.mapper = mapper;
    }

    @Override
    public EtapaProcesso save(EtapaProcesso etapa) {
        var vaga = vagaJpa.getReferenceById(etapa.getVagaId());
        var entity = mapper.toEntity(etapa, vaga);
        var saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EtapaProcesso> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<EtapaProcesso> findByVagaId(UUID vagaId) {
        return jpa.findByVagaIdOrderByOrdem(vagaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
