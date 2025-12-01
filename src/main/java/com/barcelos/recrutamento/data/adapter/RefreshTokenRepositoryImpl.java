package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.RefreshToken;
import com.barcelos.recrutamento.core.port.RefreshTokenRepository;
import com.barcelos.recrutamento.data.mapper.RefreshTokenMapper;
import com.barcelos.recrutamento.data.spring.RefreshTokenJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenMapper mapper;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository jpaRepository, RefreshTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        var entity = mapper.toEntity(refreshToken);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findByUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByUsuarioId(UUID usuarioId) {
        jpaRepository.deleteByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public void deleteExpired() {
        jpaRepository.deleteByExpiraEmBefore(Instant.now());
    }
}
