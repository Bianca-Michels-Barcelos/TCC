package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.BeneficioOrg;
import com.barcelos.recrutamento.core.port.BeneficioOrgRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BeneficioService {

    private final BeneficioOrgRepository beneficioOrgRepository;

    public BeneficioService(BeneficioOrgRepository beneficioOrgRepository) {
        this.beneficioOrgRepository = beneficioOrgRepository;
    }

    @Transactional
    public BeneficioOrg criar(UUID organizacaoId, String nome, String descricao) {
        var beneficio = BeneficioOrg.novo(organizacaoId, nome, descricao);
        return beneficioOrgRepository.save(beneficio);
    }

    @Transactional(readOnly = true)
    public BeneficioOrg buscar(UUID beneficioId) {
        return beneficioOrgRepository.findById(beneficioId)
                .orElseThrow(() -> new ResourceNotFoundException("Benefício", beneficioId));
    }

    @Transactional(readOnly = true)
    public List<BeneficioOrg> listarPorOrganizacao(UUID organizacaoId) {
        return beneficioOrgRepository.listByOrganizacao(organizacaoId);
    }

    @Transactional
    public BeneficioOrg atualizar(BeneficioOrg beneficio, String nome, String descricao) {
        var beneficioAtualizado = BeneficioOrg.atualizar(beneficio.getId(), beneficio.getOrganizacaoId(), nome, descricao);
        return beneficioOrgRepository.save(beneficioAtualizado);
    }

    @Transactional
    public void deletar(UUID beneficioId) {
        var beneficio = buscar(beneficioId);
        beneficioOrgRepository.deleteById(beneficioId);
    }
}
