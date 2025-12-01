package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.CompatibilidadeCache;
import com.barcelos.recrutamento.core.port.CompatibilidadeCacheRepository;
import com.barcelos.recrutamento.data.mapper.CompatibilidadeCacheMapper;
import com.barcelos.recrutamento.data.spring.CompatibilidadeCacheJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CompatibilidadeCacheRepositoryImpl implements CompatibilidadeCacheRepository {

    private final CompatibilidadeCacheJpaRepository jpaRepository;
    private final CompatibilidadeCacheMapper mapper;

    public CompatibilidadeCacheRepositoryImpl(CompatibilidadeCacheJpaRepository jpaRepository,
                                             CompatibilidadeCacheMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CompatibilidadeCache save(CompatibilidadeCache cache) {
        var entity = mapper.toEntity(cache);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompatibilidadeCache> findByCandidatoAndVaga(UUID candidatoUsuarioId, UUID vagaId) {
        return jpaRepository.findByCandidatoAndVaga(candidatoUsuarioId, vagaId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompatibilidadeCache> findByCandidato(UUID candidatoUsuarioId) {
        return jpaRepository.findByCandidato(candidatoUsuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompatibilidadeCache> findByVaga(UUID vagaId) {
        return jpaRepository.findByVaga(vagaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCandidatoAndVaga(UUID candidatoUsuarioId, UUID vagaId) {
        return jpaRepository.existsByCandidatoAndVaga(candidatoUsuarioId, vagaId);
    }

    @Override
    @Transactional
    public void deleteByVaga(UUID vagaId) {
        jpaRepository.deleteByVaga(vagaId);
    }

    @Override
    @Transactional
    public void deleteByCandidato(UUID candidatoUsuarioId) {
        jpaRepository.deleteByCandidato(candidatoUsuarioId);
    }
}
