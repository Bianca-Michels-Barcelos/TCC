package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
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
class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private MembroOrganizacaoRepository membroOrganizacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VagaBeneficioService vagaBeneficioService;

    @Mock
    private CompatibilidadeCacheService compatibilidadeCacheService;

    @Mock
    private EtapaProcessoService etapaProcessoService;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private VagaService service;

    private UUID organizacaoId;
    private UUID recrutadorId;
    private UUID vagaId;
    private Organizacao organizacao;
    private Usuario recrutador;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        organizacaoId = UUID.randomUUID();
        recrutadorId = UUID.randomUUID();
        vagaId = UUID.randomUUID();

        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        organizacao = Organizacao.rehydrate(
            organizacaoId,
            new Cnpj("12345678000190"),
            "Empresa XYZ",
            endereco,
            true
        );

        recrutador = Usuario.rehydrate(
            recrutadorId,
            "João Silva",
            new Email("joao@example.com"),
            new Cpf("12345678901"),
            "$2a$10$hashedPassword",
            true,
            true
        );

        vaga = Vaga.rehydrate(
            vagaId,
            organizacaoId,
            recrutadorId,
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
            new EnderecoSimples("São Paulo", new Sigla("SP")),
            true,
            null
        );
    }

    @Test
    void deveCriarVagaComSucesso() {
        VagaService.CriarVagaCommand cmd = new VagaService.CriarVagaCommand(
            organizacaoId,
            recrutadorId,
            "Desenvolvedor Java",
            "Descrição",
            "Requisitos",
            new BigDecimal("5000.00"),
            LocalDate.now(),
            StatusVaga.ABERTA,
            TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO,
            "9h às 18h",
            null,
            "São Paulo",
            "SP",
            List.of()
        );

        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(usuarioRepository.findById(recrutadorId)).thenReturn(Optional.of(recrutador));
        when(membroOrganizacaoRepository.exists(organizacaoId, recrutadorId)).thenReturn(true);
        when(vagaRepository.save(any(Vaga.class))).thenReturn(vaga);

        Vaga resultado = service.criar(cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Desenvolvedor Java");
        verify(vagaRepository).save(any(Vaga.class));
        verify(etapaProcessoService).criar(any(), eq("Triagem"), any(), any(), eq(1), any(), any());
        verify(compatibilidadeCacheService).calcularParaTodosCandidatos(any());
    }

    @Test
    void naoDeveCriarVagaQuandoOrganizacaoNaoExiste() {
        VagaService.CriarVagaCommand cmd = new VagaService.CriarVagaCommand(
            organizacaoId, recrutadorId, "Desenvolvedor", "Desc", "Req",
            new BigDecimal("5000"), LocalDate.now(), StatusVaga.ABERTA,
            TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, null, List.of()
        );

        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(cmd))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Empresa não encontrada");

        verify(vagaRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarVagaQuandoRecrutadorNaoExiste() {
        VagaService.CriarVagaCommand cmd = new VagaService.CriarVagaCommand(
            organizacaoId, recrutadorId, "Desenvolvedor", "Desc", "Req",
            new BigDecimal("5000"), LocalDate.now(), StatusVaga.ABERTA,
            TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, null, List.of()
        );

        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(usuarioRepository.findById(recrutadorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(cmd))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Usuário recrutador não encontrado");

        verify(vagaRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarVagaQuandoRecrutadorNaoEMembroDaOrganizacao() {
        VagaService.CriarVagaCommand cmd = new VagaService.CriarVagaCommand(
            organizacaoId, recrutadorId, "Desenvolvedor", "Desc", "Req",
            new BigDecimal("5000"), LocalDate.now(), StatusVaga.ABERTA,
            TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, null, List.of()
        );

        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(usuarioRepository.findById(recrutadorId)).thenReturn(Optional.of(recrutador));
        when(membroOrganizacaoRepository.exists(organizacaoId, recrutadorId)).thenReturn(false);

        assertThatThrownBy(() -> service.criar(cmd))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Recrutador não é membro da organização");

        verify(vagaRepository, never()).save(any());
    }

    @Test
    void deveBuscarVagaPorId() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        Vaga resultado = service.buscar(vagaId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(vagaId);
        verify(vagaRepository).findById(vagaId);
    }

    @Test
    void naoDeveBuscarVagaInexistente() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(vagaId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vaga não encontrada");
    }

    @Test
    void deveAtualizarVagaComSucesso() {
        VagaService.AtualizarVagaCommand cmd = new VagaService.AtualizarVagaCommand(
            "Desenvolvedor Senior",
            "Nova descrição",
            "Novos requisitos",
            new BigDecimal("7000.00"),
            StatusVaga.ABERTA,
            TipoContrato.CLT,
            ModalidadeTrabalho.HIBRIDO,
            "9h às 18h",
            null,
            "Rio de Janeiro",
            "RJ",
            List.of()
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vagaBeneficioService.listarBeneficiosDaVaga(vagaId)).thenReturn(List.of());

        Vaga resultado = service.atualizar(vagaId, cmd);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Desenvolvedor Senior");
        verify(vagaRepository).save(any(Vaga.class));
        verify(compatibilidadeCacheService).recalcularVaga(vagaId);
    }

    @Test
    void deveDesativarVaga() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));

        service.desativar(vagaId);

        verify(vagaRepository).save(any(Vaga.class));
    }

    @Test
    void deveAtivarVaga() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ativar(vagaId);

        verify(vagaRepository).save(any(Vaga.class));
    }

    @Test
    void deveFecharVaga() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));

        Vaga resultado = service.fechar(vagaId);

        assertThat(resultado).isNotNull();
        verify(vagaRepository).save(any(Vaga.class));
    }

    @Test
    void deveCancelarVagaENotificarCandidatos() {
        Usuario candidato = Usuario.rehydrate(
            UUID.randomUUID(),
            "Maria Silva",
            new Email("maria@example.com"),
            new Cpf("98765432109"),
            "$2a$10$hashedPassword",
            true,
            true
        );

        Candidatura candidatura = Candidatura.rehydrate(
            UUID.randomUUID(),
            vagaId,
            candidato.getId(),
            StatusCandidatura.PENDENTE,
            LocalDate.now(),
            null,
            new BigDecimal("85.5")
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(candidaturaRepository.listByVaga(vagaId)).thenReturn(List.of(candidatura));
        when(usuarioRepository.findById(candidato.getId())).thenReturn(Optional.of(candidato));
        when(emailTemplateService.renderVagaCancelada(any(), any(), any())).thenReturn("<html>Email</html>");

        Vaga resultado = service.cancelar(vagaId, "Mudança de prioridades");

        assertThat(resultado).isNotNull();
        verify(vagaRepository).save(any(Vaga.class));
        verify(emailService).sendHtmlEmailAsync(eq("maria@example.com"), any(), any());
    }

    @Test
    void deveDeletarVaga() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        service.deletar(vagaId);

        verify(vagaRepository).deleteById(vagaId);
    }

    @Test
    void deveListarVagasPorRecrutador() {
        when(vagaRepository.listByRecrutador(recrutadorId)).thenReturn(List.of(vaga));

        List<Vaga> resultado = service.listarPorRecrutador(recrutadorId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(vagaId);
        verify(vagaRepository).listByRecrutador(recrutadorId);
    }
}

