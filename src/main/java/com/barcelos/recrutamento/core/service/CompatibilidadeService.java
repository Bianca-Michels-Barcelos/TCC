package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.api.dto.CompatibilidadeResponse;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CompatibilidadeService {

    private final CompatibilidadeCacheService cacheService;
    private final VagaRepository vagaRepository;
    private final UsuarioRepository usuarioRepository;

    public CompatibilidadeService(
            CompatibilidadeCacheService cacheService,
            VagaRepository vagaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.cacheService = cacheService;
        this.vagaRepository = vagaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    
    @Transactional(readOnly = true)
    public CompatibilidadeResponse calcularCompatibilidade(UUID candidatoUsuarioId, UUID vagaId) {

        usuarioRepository.findById(candidatoUsuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidato", candidatoUsuarioId));

        vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", vagaId));

        var cache = cacheService.obterOuCalcular(candidatoUsuarioId, vagaId);

        return new CompatibilidadeResponse(
                candidatoUsuarioId,
                vagaId,
                cache.getPercentualCompatibilidade().intValue(),
                cache.getJustificativa(),
                true
        );
    }
}
