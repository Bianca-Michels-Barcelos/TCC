package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.Portfolio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository {
    Portfolio save(Portfolio portfolio);
    Optional<Portfolio> findById(UUID id);
    List<Portfolio> listByPerfilCandidato(UUID perfilCandidatoId);
    void delete(UUID id);
}
