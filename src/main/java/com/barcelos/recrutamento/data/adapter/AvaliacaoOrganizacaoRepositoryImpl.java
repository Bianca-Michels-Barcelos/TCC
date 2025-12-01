package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.AvaliacaoOrganizacao;
import com.barcelos.recrutamento.core.port.AvaliacaoOrganizacaoRepository;
import com.barcelos.recrutamento.data.mapper.AvaliacaoOrganizacaoMapper;
import com.barcelos.recrutamento.data.spring.AvaliacaoOrganizacaoJpaRepository;
import com.barcelos.recrutamento.data.spring.OrganizacaoJpaRepository;
import com.barcelos.recrutamento.data.spring.ProcessoSeletivoJpaRepository;
import com.barcelos.recrutamento.data.spring.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AvaliacaoOrganizacaoRepositoryImpl implements AvaliacaoOrganizacaoRepository {

    private final AvaliacaoOrganizacaoJpaRepository jpaRepository;
    private final ProcessoSeletivoJpaRepository processoJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final AvaliacaoOrganizacaoMapper mapper;

    public AvaliacaoOrganizacaoRepositoryImpl(AvaliacaoOrganizacaoJpaRepository jpaRepository,
                                              ProcessoSeletivoJpaRepository processoJpaRepository,
                                              UsuarioJpaRepository usuarioJpaRepository,
                                              OrganizacaoJpaRepository organizacaoJpaRepository,
                                              AvaliacaoOrganizacaoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.processoJpaRepository = processoJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AvaliacaoOrganizacao save(AvaliacaoOrganizacao avaliacao) {
        var processo = processoJpaRepository.getReferenceById(avaliacao.getProcessoId());
        var candidato = usuarioJpaRepository.getReferenceById(avaliacao.getCandidatoUsuarioId());
        var organizacao = organizacaoJpaRepository.getReferenceById(avaliacao.getOrganizacaoId());

        var entity = mapper.toEntity(avaliacao, processo, candidato, organizacao);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AvaliacaoOrganizacao> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<AvaliacaoOrganizacao> findByProcessoId(UUID processoId) {
        return jpaRepository.findByProcesso_Id(processoId)
                .map(mapper::toDomain);
    }

    @Override
    public List<AvaliacaoOrganizacao> findByOrganizacaoId(UUID organizacaoId) {
        return jpaRepository.findByOrganizacao_IdOrderByCriadoEmDesc(organizacaoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByOrganizacaoId(UUID organizacaoId) {
        return jpaRepository.countByOrganizacao_Id(organizacaoId);
    }

    @Override
    public Double findAverageNotaByOrganizacaoId(UUID organizacaoId) {
        return jpaRepository.findAverageNotaByOrganizacaoId(organizacaoId);
    }

    @Override
    public boolean existsByProcessoId(UUID processoId) {
        return jpaRepository.existsByProcesso_Id(processoId);
    }
}
