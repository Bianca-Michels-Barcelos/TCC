package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.ProjetoExperiencia;

import java.util.List;
import java.util.UUID;

public interface ProjetoExperienciaRepository {
    ProjetoExperiencia save(ProjetoExperiencia projeto);

    void deleteById(UUID id);
}
