package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.exception.ResourceOwnershipException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VagaBeneficioServiceTest {

    @Mock
    private VagaBeneficioRepository vagaBeneficioRepository;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private BeneficioOrgRepository beneficioOrgRepository;

    @InjectMocks
    private VagaBeneficioService service;

    private UUID vagaId;
    private UUID beneficioId;
    private UUID organizacaoId;
    private Vaga vaga;
    private BeneficioOrg beneficio;

    @BeforeEach
    void setUp() {
        vagaId = UUID.randomUUID();
        beneficioId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();

        vaga = Vaga.rehydrate(
            vagaId, organizacaoId, UUID.randomUUID(), "Desenvolvedor", "Desc", "Req",
            new BigDecimal("5000"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        beneficio = BeneficioOrg.rehydrate(beneficioId, organizacaoId, "Vale Alimentação", "R$ 500");
    }

    @Test
    void deveAdicionarBeneficioAVagaComSucesso() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(beneficioOrgRepository.findById(beneficioId)).thenReturn(Optional.of(beneficio));

        service.adicionar(vagaId, beneficioId);

        verify(vagaBeneficioRepository).add(any(VagaBeneficio.class));
    }

    @Test
    void naoDeveAdicionarBeneficioQuandoOrganizacaoDiferente() {
        UUID outraOrganizacaoId = UUID.randomUUID();
        BeneficioOrg beneficioOutraOrg = BeneficioOrg.rehydrate(
            beneficioId, outraOrganizacaoId, "Benefício", "Desc"
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(beneficioOrgRepository.findById(beneficioId)).thenReturn(Optional.of(beneficioOutraOrg));

        assertThatThrownBy(() -> service.adicionar(vagaId, beneficioId))
            .isInstanceOf(ResourceOwnershipException.class)
            .hasMessageContaining("Benefício não pertence à mesma organização");

        verify(vagaBeneficioRepository, never()).add(any());
    }

    @Test
    void deveRemoverBeneficioDaVaga() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        service.remover(vagaId, beneficioId);

        verify(vagaBeneficioRepository).remove(vagaId, beneficioId);
    }

    @Test
    void deveListarBeneficiosDaVaga() {
        VagaBeneficio vagaBeneficio = VagaBeneficio.novo(vagaId, beneficioId);

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaBeneficioRepository.listByVaga(vagaId)).thenReturn(List.of(vagaBeneficio));

        List<UUID> resultado = service.listarBeneficiosDaVaga(vagaId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isEqualTo(beneficioId);
        verify(vagaBeneficioRepository).listByVaga(vagaId);
    }
}

