package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.MembroOrganizacao;
import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.data.mapper.MembroOrganizacaoMapper;
import com.barcelos.recrutamento.data.spring.MembroOrganizacaoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MembroOrganizacaoRepositoryImpl implements MembroOrganizacaoRepository {

    private final MembroOrganizacaoJpaRepository jpa;
    private final MembroOrganizacaoMapper mapper;

    public MembroOrganizacaoRepositoryImpl(MembroOrganizacaoJpaRepository jpa, MembroOrganizacaoMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public MembroOrganizacao save(MembroOrganizacao membro) {
        var entity = mapper.toEntity(membro);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<MembroOrganizacao> findByIds(UUID organizacaoId, UUID usuarioId) {
        return jpa.findByOrganizacaoIdAndUsuarioId(organizacaoId, usuarioId)
                .map(mapper::toDomain);
    }

    @Override
    public List<MembroOrganizacao> listByOrganizacao(UUID organizacaoId) {
        return jpa.findByOrganizacaoId(organizacaoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MembroOrganizacao> listByUsuario(UUID usuarioId) {
        return jpa.findByUsuarioId(usuarioId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean exists(UUID organizacaoId, UUID usuarioId) {
        return jpa.existsByOrganizacaoIdAndUsuarioId(organizacaoId, usuarioId);
    }

    @Override
    public void deleteByIds(UUID organizacaoId, UUID usuarioId) {
        jpa.deleteByOrganizacaoIdAndUsuarioId(organizacaoId, usuarioId);
    }
}