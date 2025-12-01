package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.api.dto.VagaComEstatisticas;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.data.entity.VagaEntity;
import com.barcelos.recrutamento.data.mapper.VagaMapper;
import com.barcelos.recrutamento.data.spring.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class VagaRepositoryImpl implements VagaRepository {

    private final VagaJpaRepository jpa;
    private final OrganizacaoJpaRepository orgJpa;
    private final UsuarioJpaRepository usuarioJpa;
    private final NivelExperienciaJpaRepository nivelJpa;
    private final ProcessoSeletivoJpaRepository processoJpa;
    private final CandidaturaJpaRepository candidaturaJpa;
    private final EtapaProcessoJpaRepository etapaJpa;
    private final VagaBeneficioJpaRepository vagaBeneficioJpa;
    private final VagaMapper mapper;

    public VagaRepositoryImpl(VagaJpaRepository jpa,
                              OrganizacaoJpaRepository orgJpa,
                              UsuarioJpaRepository usuarioJpa,
                              NivelExperienciaJpaRepository nivelJpa,
                              ProcessoSeletivoJpaRepository processoJpa,
                              CandidaturaJpaRepository candidaturaJpa,
                              EtapaProcessoJpaRepository etapaJpa,
                              VagaBeneficioJpaRepository vagaBeneficioJpa,
                              VagaMapper mapper) {
        this.jpa = jpa;
        this.orgJpa = orgJpa;
        this.usuarioJpa = usuarioJpa;
        this.nivelJpa = nivelJpa;
        this.processoJpa = processoJpa;
        this.candidaturaJpa = candidaturaJpa;
        this.etapaJpa = etapaJpa;
        this.vagaBeneficioJpa = vagaBeneficioJpa;
        this.mapper = mapper;
    }

    @Override
    public Vaga save(Vaga v) {
        var org = orgJpa.getReferenceById(v.getOrganizacaoId());
        var recrutador = usuarioJpa.getReferenceById(v.getRecrutadorUsuarioId());
        var nivel = v.getNivelExperienciaId() != null ? nivelJpa.getReferenceById(v.getNivelExperienciaId()) : null;

        var entity = mapper.toEntity(v, org, recrutador, nivel);
        var saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Vaga> listByOrganizacao(UUID organizacaoId) {
        return jpa.findByOrganizacao_Id(organizacaoId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Vaga> listByRecrutador(UUID recrutadorUsuarioId) {
        return jpa.findByRecrutador_Id(recrutadorUsuarioId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Vaga> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Vaga> listPublicas() {
        return jpa.findByStatusAndAtivoTrue(com.barcelos.recrutamento.core.model.StatusVaga.ABERTA.name()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public Page<VagaComEstatisticas> findWithStatsByOrganizacao(
            UUID organizacaoId,
            String status,
            String modalidade,
            String search,
            Pageable pageable) {

        Page<VagaEntity> vagasPage = jpa.findWithStatsByOrganizacao(
            organizacaoId, status, modalidade, search, pageable
        );

        return buildVagasComEstatisticas(vagasPage, pageable);
    }

    @Override
    public Page<VagaComEstatisticas> findWithStatsByRecrutador(
            UUID recrutadorUsuarioId,
            String status,
            String modalidade,
            String search,
            Pageable pageable) {

        Page<VagaEntity> vagasPage = jpa.findWithStatsByRecrutador(
            recrutadorUsuarioId, status, modalidade, search, pageable
        );

        return buildVagasComEstatisticas(vagasPage, pageable);
    }

    private Page<VagaComEstatisticas> buildVagasComEstatisticas(Page<VagaEntity> vagasPage, Pageable pageable) {
        if (vagasPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> vagaIds = vagasPage.getContent().stream()
            .map(VagaEntity::getId)
            .collect(Collectors.toList());

        vagasPage.getContent().forEach(v -> {
            if (v.getNivelExperiencia() != null) {
                v.getNivelExperiencia().getDescricao();
            }
        });

        Map<UUID, Integer> totalCandidatos = toMap(processoJpa.countProcessosByVagaIds(vagaIds));
        Map<UUID, Integer> candidatosAtivos = toMap(processoJpa.countProcessosAtivosByVagaIds(vagaIds));
        Map<UUID, Integer> candidatosAceitos = toMap(candidaturaJpa.countCandidatosAceitosByVagaIds(vagaIds));
        Map<UUID, Integer> candidatosRejeitados = toMap(candidaturaJpa.countCandidatosRejeitadosByVagaIds(vagaIds));
        Map<UUID, Integer> totalEtapas = toMap(etapaJpa.countEtapasByVagaIds(vagaIds));
        Map<UUID, Integer> totalBeneficios = toMap(vagaBeneficioJpa.countBeneficiosByVagaIds(vagaIds));

        List<VagaComEstatisticas> content = vagasPage.getContent().stream()
            .map(v -> toVagaComEstatisticas(
                v,
                totalCandidatos.getOrDefault(v.getId(), 0),
                candidatosAtivos.getOrDefault(v.getId(), 0),
                candidatosAceitos.getOrDefault(v.getId(), 0),
                candidatosRejeitados.getOrDefault(v.getId(), 0),
                totalEtapas.getOrDefault(v.getId(), 0),
                totalBeneficios.getOrDefault(v.getId(), 0)
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, vagasPage.getTotalElements());
    }

    private Map<UUID, Integer> toMap(List<Object[]> results) {
        return results.stream()
            .collect(Collectors.toMap(
                r -> (UUID) r[0],
                r -> ((Number) r[1]).intValue()
            ));
    }

    private VagaComEstatisticas toVagaComEstatisticas(
            VagaEntity v,
            Integer totalCandidatos,
            Integer candidatosAtivos,
            Integer candidatosAceitos,
            Integer candidatosRejeitados,
            Integer totalEtapas,
            Integer totalBeneficios) {

        com.barcelos.recrutamento.core.model.vo.EnderecoSimples endereco = null;
        if (v.getEndereco() != null) {
            var e = v.getEndereco();
            endereco = new com.barcelos.recrutamento.core.model.vo.EnderecoSimples(
                e.getCidade(),
                new com.barcelos.recrutamento.core.model.vo.Sigla(e.getUf())
            );
        }

        String nomeNivelExperiencia = null;
        if (v.getNivelExperiencia() != null) {
            nomeNivelExperiencia = v.getNivelExperiencia().getDescricao();
        }

        return new VagaComEstatisticas(
            v.getId(),
            v.getOrganizacao().getId(),
            v.getRecrutador().getId(),
            v.getTitulo(),
            v.getDescricao(),
            v.getRequisitos(),
            v.getSalario(),
            v.getDataPublicacao(),
            v.getStatus() != null ? v.getStatus().name() : null,
            v.getTipoContrato() != null ? v.getTipoContrato().name() : null,
            v.getModalidade() != null ? v.getModalidade().name() : null,
            v.getHorarioTrabalho(),
            endereco,
            v.isAtivo(),
            v.getMotivoCancelamento(),
            v.getNivelExperiencia() != null ? v.getNivelExperiencia().getId() : null,
            nomeNivelExperiencia,
            totalCandidatos,
            candidatosAtivos,
            candidatosAceitos,
            candidatosRejeitados,
            totalEtapas,
            totalBeneficios,
            v.getAtualizadoEm() != null ? v.getAtualizadoEm().toLocalDate() : LocalDate.now()
        );
    }
}