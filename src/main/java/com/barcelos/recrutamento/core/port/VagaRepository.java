package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.api.dto.VagaComEstatisticas;
import com.barcelos.recrutamento.core.model.Vaga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VagaRepository {
    Vaga save(Vaga vaga);

    List<Vaga> listByOrganizacao(UUID organizacaoId);

    List<Vaga> listByRecrutador(UUID recrutadorUsuarioId);

    Optional<Vaga> findById(UUID id);

    List<Vaga> listPublicas();

    void deleteById(UUID id);

    
    Page<VagaComEstatisticas> findWithStatsByOrganizacao(
        UUID organizacaoId,
        String status,
        String modalidade,
        String search,
        Pageable pageable
    );

    
    Page<VagaComEstatisticas> findWithStatsByRecrutador(
        UUID recrutadorUsuarioId,
        String status,
        String modalidade,
        String search,
        Pageable pageable
    );
}