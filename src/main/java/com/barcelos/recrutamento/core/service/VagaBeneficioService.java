package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.exception.ResourceOwnershipException;
import com.barcelos.recrutamento.core.model.VagaBeneficio;
import com.barcelos.recrutamento.core.port.BeneficioOrgRepository;
import com.barcelos.recrutamento.core.port.VagaBeneficioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VagaBeneficioService {

    private final VagaBeneficioRepository vagaBeneficioRepository;
    private final VagaRepository vagaRepository;
    private final BeneficioOrgRepository beneficioOrgRepository;

    public VagaBeneficioService(VagaBeneficioRepository vagaBeneficioRepository,
                               VagaRepository vagaRepository,
                               BeneficioOrgRepository beneficioOrgRepository) {
        this.vagaBeneficioRepository = vagaBeneficioRepository;
        this.vagaRepository = vagaRepository;
        this.beneficioOrgRepository = beneficioOrgRepository;
    }

    @Transactional
    public void adicionar(UUID vagaId, UUID beneficioId) {
        var vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));

        var beneficio = beneficioOrgRepository.findById(beneficioId)
                .orElseThrow(() -> new ResourceNotFoundException("Benefício não encontrado"));

        if (!beneficio.getOrganizacaoId().equals(vaga.getOrganizacaoId())) {
            throw new ResourceOwnershipException("Benefício não pertence à mesma organização da vaga");
        }

        var vagaBeneficio = VagaBeneficio.novo(vagaId, beneficioId);
        vagaBeneficioRepository.add(vagaBeneficio);
    }

    @Transactional
    public void remover(UUID vagaId, UUID beneficioId) {

        vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));

        vagaBeneficioRepository.remove(vagaId, beneficioId);
    }

    @Transactional(readOnly = true)
    public List<UUID> listarBeneficiosDaVaga(UUID vagaId) {

        vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));

        return vagaBeneficioRepository.listByVaga(vagaId).stream()
                .map(VagaBeneficio::getBeneficioId)
                .toList();
    }
}
