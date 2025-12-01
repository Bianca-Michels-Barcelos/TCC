package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.Portfolio;
import com.barcelos.recrutamento.core.port.PortfolioRepository;
import com.barcelos.recrutamento.data.mapper.PortfolioMapper;
import com.barcelos.recrutamento.data.spring.PerfilCandidatoJpaRepository;
import com.barcelos.recrutamento.data.spring.PortfolioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PortfolioRepositoryImpl implements PortfolioRepository {

    private final PortfolioJpaRepository jpa;
    private final PerfilCandidatoJpaRepository perfilCandidatoJpa;
    private final PortfolioMapper mapper;

    public PortfolioRepositoryImpl(
            PortfolioJpaRepository jpa,
            PerfilCandidatoJpaRepository perfilCandidatoJpa,
            PortfolioMapper mapper) {
        this.jpa = jpa;
        this.perfilCandidatoJpa = perfilCandidatoJpa;
        this.mapper = mapper;
    }

    @Override
    public Portfolio save(Portfolio portfolio) {
        var perfilCandidato = perfilCandidatoJpa.getReferenceById(portfolio.getUsuarioId());
        var entity = mapper.toEntity(portfolio, perfilCandidato);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Portfolio> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Portfolio> listByPerfilCandidato(UUID perfilCandidatoId) {
        return jpa.findByPerfilCandidatoId(perfilCandidatoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }
}
