package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.Cep;
import com.barcelos.recrutamento.core.model.vo.Cnpj;
import com.barcelos.recrutamento.core.model.vo.Endereco;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.port.VagaSalvaRepository;
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
class VagaSalvaServiceTest {

    @Mock
    private VagaSalvaRepository vagaSalvaRepository;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @InjectMocks
    private VagaSalvaService service;

    private UUID vagaId;
    private UUID usuarioId;
    private Vaga vaga;
    private VagaSalva vagaSalva;

    @BeforeEach
    void setUp() {
        vagaId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        vaga = Vaga.rehydrate(
            vagaId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Desenvolvedor Java",
            "Descrição da vaga",
            "Requisitos",
            new BigDecimal("5000.00"),
            LocalDate.now(),
            StatusVaga.ABERTA,
            TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO,
            "9h às 18h",
            null,
            null,
            true,
            null
        );

        vagaSalva = VagaSalva.rehydrate(
            UUID.randomUUID(),
            vagaId,
            usuarioId,
            LocalDate.now().atStartOfDay()
        );
    }

    @Test
    void deveSalvarVagaComSucesso() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId)).thenReturn(false);
        when(vagaSalvaRepository.countByUsuarioId(usuarioId)).thenReturn(5L);
        when(vagaSalvaRepository.save(any(VagaSalva.class))).thenReturn(vagaSalva);

        VagaSalva resultado = service.salvar(vagaId, usuarioId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getVagaId()).isEqualTo(vagaId);
        assertThat(resultado.getUsuarioId()).isEqualTo(usuarioId);

        verify(vagaRepository).findById(vagaId);
        verify(vagaSalvaRepository).existsByVagaIdAndUsuarioId(vagaId, usuarioId);
        verify(vagaSalvaRepository).countByUsuarioId(usuarioId);
        verify(vagaSalvaRepository).save(any(VagaSalva.class));
    }

    @Test
    void naoDeveSalvarVagaQueNaoExiste() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(vagaId, usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vaga");

        verify(vagaSalvaRepository, never()).save(any());
    }

    @Test
    void naoDeveSalvarVagaJaSalva() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId)).thenReturn(true);

        assertThatThrownBy(() -> service.salvar(vagaId, usuarioId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Vaga já está salva");

        verify(vagaSalvaRepository, never()).save(any());
    }

    @Test
    void naoDeveSalvarQuandoAtingirLimiteDe20Vagas() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId)).thenReturn(false);
        when(vagaSalvaRepository.countByUsuarioId(usuarioId)).thenReturn(20L);

        assertThatThrownBy(() -> service.salvar(vagaId, usuarioId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Limite de 20 vagas salvas atingido");

        verify(vagaSalvaRepository, never()).save(any());
    }

    @Test
    void devePermitirSalvarNa19aVaga() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId)).thenReturn(false);
        when(vagaSalvaRepository.countByUsuarioId(usuarioId)).thenReturn(19L);
        when(vagaSalvaRepository.save(any(VagaSalva.class))).thenReturn(vagaSalva);

        VagaSalva resultado = service.salvar(vagaId, usuarioId);

        assertThat(resultado).isNotNull();
        verify(vagaSalvaRepository).save(any(VagaSalva.class));
    }

    @Test
    void deveRemoverVagaSalva() {
        when(vagaSalvaRepository.findByVagaIdAndUsuarioId(vagaId, usuarioId))
            .thenReturn(Optional.of(vagaSalva));

        service.remover(vagaId, usuarioId);

        verify(vagaSalvaRepository).findByVagaIdAndUsuarioId(vagaId, usuarioId);
        verify(vagaSalvaRepository).delete(vagaSalva);
    }

    @Test
    void naoDeveRemoverVagaNaoSalva() {
        when(vagaSalvaRepository.findByVagaIdAndUsuarioId(vagaId, usuarioId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(vagaId, usuarioId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vaga salva não encontrada");

        verify(vagaSalvaRepository, never()).delete(any());
    }

    @Test
    void deveVerificarSeVagaEstaSalva() {
        when(vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId)).thenReturn(true);

        boolean resultado = service.estaSalva(vagaId, usuarioId);

        assertThat(resultado).isTrue();
        verify(vagaSalvaRepository).existsByVagaIdAndUsuarioId(vagaId, usuarioId);
    }

    @Test
    void deveRetornarFalseQuandoVagaNaoEstaSalva() {
        when(vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId)).thenReturn(false);

        boolean resultado = service.estaSalva(vagaId, usuarioId);

        assertThat(resultado).isFalse();
        verify(vagaSalvaRepository).existsByVagaIdAndUsuarioId(vagaId, usuarioId);
    }

    @Test
    void deveListarVagasSalvasComDetalhes() {
        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        Organizacao organizacao = Organizacao.rehydrate(
            UUID.randomUUID(),
            new Cnpj("12345678000190"),
            "Empresa XYZ",
            endereco,
            true
        );

        when(vagaSalvaRepository.findByUsuarioId(usuarioId)).thenReturn(List.of(vagaSalva));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(organizacaoRepository.findById(vaga.getOrganizacaoId())).thenReturn(Optional.of(organizacao));

        List<VagaSalvaService.VagaSalvaDetalhada> resultado = service.listarComDetalhesPorUsuario(usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).vagaSalva()).isEqualTo(vagaSalva);
        assertThat(resultado.get(0).vaga()).isEqualTo(vaga);
        assertThat(resultado.get(0).organizacao()).isEqualTo(organizacao);
    }

    @Test
    void deveListarVaziasQuandoNaoHaVagasSalvas() {
        when(vagaSalvaRepository.findByUsuarioId(usuarioId)).thenReturn(List.of());

        List<VagaSalvaService.VagaSalvaDetalhada> resultado = service.listarComDetalhesPorUsuario(usuarioId);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveFiltrarVagasInexistentesAoListar() {
        when(vagaSalvaRepository.findByUsuarioId(usuarioId)).thenReturn(List.of(vagaSalva));
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        List<VagaSalvaService.VagaSalvaDetalhada> resultado = service.listarComDetalhesPorUsuario(usuarioId);

        assertThat(resultado).isEmpty();
    }
}

