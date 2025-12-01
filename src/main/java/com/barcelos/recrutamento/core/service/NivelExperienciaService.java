package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.NivelExperiencia;
import com.barcelos.recrutamento.core.port.NivelExperienciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NivelExperienciaService {

    private final NivelExperienciaRepository nivelExperienciaRepository;

    public NivelExperienciaService(NivelExperienciaRepository nivelExperienciaRepository) {
        this.nivelExperienciaRepository = nivelExperienciaRepository;
    }

    @Transactional
    public NivelExperiencia criar(UUID organizacaoId, String descricao) {
        var nivel = NivelExperiencia.novo(organizacaoId, descricao);
        return nivelExperienciaRepository.save(nivel);
    }

    @Transactional(readOnly = true)
    public NivelExperiencia buscar(UUID nivelId) {
        return nivelExperienciaRepository.findById(nivelId)
                .orElseThrow(() -> new ResourceNotFoundException("Nível de experiência", nivelId));
    }

    @Transactional(readOnly = true)
    public List<NivelExperiencia> listarPorOrganizacao(UUID organizacaoId) {
        return nivelExperienciaRepository.listByOrganizacao(organizacaoId);
    }

    @Transactional
    public NivelExperiencia atualizar(NivelExperiencia nivelExperiencia, String descricao) {
        var nivelAtualizado = NivelExperiencia.atualizar(nivelExperiencia.getId(), nivelExperiencia.getOrganizacaoId(), descricao);
        return nivelExperienciaRepository.save(nivelAtualizado);
    }

    @Transactional
    public void deletar(UUID nivelId) {
        var nivel = buscar(nivelId);
        nivelExperienciaRepository.deleteById(nivelId);
    }
}
