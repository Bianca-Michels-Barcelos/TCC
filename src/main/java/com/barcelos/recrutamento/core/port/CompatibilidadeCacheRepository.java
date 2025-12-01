package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.CompatibilidadeCache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompatibilidadeCacheRepository {
    
    
    CompatibilidadeCache save(CompatibilidadeCache cache);

    
    Optional<CompatibilidadeCache> findByCandidatoAndVaga(UUID candidatoUsuarioId, UUID vagaId);

    
    List<CompatibilidadeCache> findByCandidato(UUID candidatoUsuarioId);

    
    List<CompatibilidadeCache> findByVaga(UUID vagaId);

    
    boolean existsByCandidatoAndVaga(UUID candidatoUsuarioId, UUID vagaId);

    
    void deleteByVaga(UUID vagaId);

    
    void deleteByCandidato(UUID candidatoUsuarioId);
}
