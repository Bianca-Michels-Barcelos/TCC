package com.barcelos.recrutamento.data.repository;

import com.barcelos.recrutamento.core.model.ResetSenha;
import com.barcelos.recrutamento.core.model.StatusResetSenha;
import com.barcelos.recrutamento.core.port.ResetSenhaRepository;
import com.barcelos.recrutamento.data.mapper.ResetSenhaMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ResetSenhaRepositoryImpl implements ResetSenhaRepository {

    private final ResetSenhaJpaRepository jpaRepository;
    private final ResetSenhaMapper mapper;

    public ResetSenhaRepositoryImpl(ResetSenhaJpaRepository jpaRepository,
                                    ResetSenhaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ResetSenha save(ResetSenha resetSenha) {
        var entity = mapper.toEntity(resetSenha);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ResetSenha> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ResetSenha> findByUsuarioIdAndStatus(UUID usuarioId, StatusResetSenha status) {
        return jpaRepository.findByUsuarioIdAndStatus(usuarioId, status.name())
                .map(mapper::toDomain);
    }
}

