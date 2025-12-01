package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.BeneficioOrg;
import com.barcelos.recrutamento.core.port.BeneficioOrgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock
    private BeneficioOrgRepository repository;

    @InjectMocks
    private BeneficioService service;

    private UUID organizacaoId;
    private UUID beneficioId;
    private BeneficioOrg beneficio;

    @BeforeEach
    void setUp() {
        organizacaoId = UUID.randomUUID();
        beneficioId = UUID.randomUUID();
        beneficio = BeneficioOrg.rehydrate(beneficioId, organizacaoId, "Vale Alimentação", "R$ 500/mês");
    }

    @Test
    void deveCriarBeneficioComSucesso() {
        when(repository.save(any(BeneficioOrg.class))).thenReturn(beneficio);

        BeneficioOrg resultado = service.criar(organizacaoId, "Vale Alimentação", "R$ 500/mês");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Vale Alimentação");
        verify(repository).save(any(BeneficioOrg.class));
    }

    @Test
    void deveBuscarBeneficioPorId() {
        when(repository.findById(beneficioId)).thenReturn(Optional.of(beneficio));

        BeneficioOrg resultado = service.buscar(beneficioId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(beneficioId);
        verify(repository).findById(beneficioId);
    }

    @Test
    void naoDeveBuscarBeneficioInexistente() {
        when(repository.findById(beneficioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(beneficioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Benefício");
    }

    @Test
    void deveListarBeneficiosPorOrganizacao() {
        when(repository.listByOrganizacao(organizacaoId)).thenReturn(List.of(beneficio));

        List<BeneficioOrg> resultado = service.listarPorOrganizacao(organizacaoId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(beneficioId);
        verify(repository).listByOrganizacao(organizacaoId);
    }

    @Test
    void deveAtualizarBeneficioComSucesso() {
        when(repository.save(any(BeneficioOrg.class))).thenAnswer(inv -> inv.getArgument(0));

        BeneficioOrg resultado = service.atualizar(beneficio, "Vale Refeição", "R$ 600/mês");

        assertThat(resultado).isNotNull();
        verify(repository).save(any(BeneficioOrg.class));
    }

    @Test
    void deveDeletarBeneficioComSucesso() {
        when(repository.findById(beneficioId)).thenReturn(Optional.of(beneficio));

        service.deletar(beneficioId);

        verify(repository).deleteById(beneficioId);
    }
}

