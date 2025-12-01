package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.ConviteRecrutador;
import com.barcelos.recrutamento.core.model.StatusConvite;
import com.barcelos.recrutamento.core.port.ConviteRecrutadorRepository;
import com.barcelos.recrutamento.data.mapper.ConviteRecrutadorMapper;
import com.barcelos.recrutamento.data.spring.ConviteRecrutadorJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConviteRecrutadorRepositoryImpl implements ConviteRecrutadorRepository {

    private final ConviteRecrutadorJpaRepository jpaRepository;
    private final ConviteRecrutadorMapper mapper;

    public ConviteRecrutadorRepositoryImpl(ConviteRecrutadorJpaRepository jpaRepository,
                                          ConviteRecrutadorMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ConviteRecrutador save(ConviteRecrutador convite) {
        var entity = mapper.toEntity(convite);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ConviteRecrutador> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ConviteRecrutador> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmailAndOrganizacaoAndStatus(String email, UUID organizacaoId, StatusConvite status) {
        return jpaRepository.existsByEmailAndOrganizacaoIdAndStatus(email, organizacaoId, status.name());
    }
}
