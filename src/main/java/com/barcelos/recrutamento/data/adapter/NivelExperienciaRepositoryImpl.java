package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.model.NivelExperiencia;
import com.barcelos.recrutamento.core.port.NivelExperienciaRepository;
import com.barcelos.recrutamento.data.mapper.NivelExperienciaMapper;
import com.barcelos.recrutamento.data.spring.NivelExperienciaJpaRepository;
import com.barcelos.recrutamento.data.spring.OrganizacaoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NivelExperienciaRepositoryImpl implements NivelExperienciaRepository {

    private final NivelExperienciaJpaRepository jpaRepository;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final NivelExperienciaMapper mapper;

    public NivelExperienciaRepositoryImpl(NivelExperienciaJpaRepository jpaRepository,
                                          OrganizacaoJpaRepository organizacaoJpaRepository,
                                          NivelExperienciaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public NivelExperiencia save(NivelExperiencia nivel) {
        var organizacao = organizacaoJpaRepository.getReferenceById(nivel.getOrganizacaoId());

        if (jpaRepository.existsByOrganizacao_IdAndDescricaoIgnoreCase(
                organizacao.getId(), nivel.getDescricao())) {
            throw new BusinessRuleViolationException("Nível de experiência já existe para esta organização");
        }

        var entity = mapper.toEntity(nivel, organizacao);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<NivelExperiencia> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<NivelExperiencia> listByOrganizacao(UUID organizacaoId) {
        return jpaRepository.findByOrganizacao_Id(organizacaoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
