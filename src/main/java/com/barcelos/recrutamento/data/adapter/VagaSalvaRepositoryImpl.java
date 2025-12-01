package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.VagaSalva;
import com.barcelos.recrutamento.core.port.VagaSalvaRepository;
import com.barcelos.recrutamento.data.mapper.VagaSalvaMapper;
import com.barcelos.recrutamento.data.spring.UsuarioJpaRepository;
import com.barcelos.recrutamento.data.spring.VagaJpaRepository;
import com.barcelos.recrutamento.data.spring.VagaSalvaJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VagaSalvaRepositoryImpl implements VagaSalvaRepository {

    private final VagaSalvaJpaRepository jpaRepository;
    private final VagaJpaRepository vagaJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final VagaSalvaMapper mapper;

    public VagaSalvaRepositoryImpl(VagaSalvaJpaRepository jpaRepository,
                                   VagaJpaRepository vagaJpaRepository,
                                   UsuarioJpaRepository usuarioJpaRepository,
                                   VagaSalvaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.vagaJpaRepository = vagaJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public VagaSalva save(VagaSalva vagaSalva) {
        var vaga = vagaJpaRepository.getReferenceById(vagaSalva.getVagaId());
        var usuario = usuarioJpaRepository.getReferenceById(vagaSalva.getUsuarioId());

        var entity = mapper.toEntity(vagaSalva, vaga, usuario);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VagaSalva> findByVagaIdAndUsuarioId(UUID vagaId, UUID usuarioId) {
        return jpaRepository.findByVaga_IdAndUsuario_Id(vagaId, usuarioId)
                .map(mapper::toDomain);
    }

    @Override
    public List<VagaSalva> findByUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuario_Id(usuarioId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUsuarioId(UUID usuarioId) {
        return jpaRepository.countByUsuario_Id(usuarioId);
    }

    @Override
    public void delete(VagaSalva vagaSalva) {
        jpaRepository.deleteById(vagaSalva.getId());
    }

    @Override
    public boolean existsByVagaIdAndUsuarioId(UUID vagaId, UUID usuarioId) {
        return jpaRepository.existsByVaga_IdAndUsuario_Id(vagaId, usuarioId);
    }
}
