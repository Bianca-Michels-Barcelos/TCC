package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import com.barcelos.recrutamento.data.entity.PapelOrganizacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecrutadorServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private MembroOrganizacaoRepository membroOrganizacaoRepository;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private ConviteRecrutadorRepository conviteRecrutadorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private RecrutadorService service;

    private UUID organizacaoId;
    private UUID usuarioId;
    private UUID usuarioLogadoId;
    private Usuario usuario;
    private Organizacao organizacao;
    private MembroOrganizacao membro;

    @BeforeEach
    void setUp() {
        organizacaoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        usuarioLogadoId = UUID.randomUUID();

        usuario = Usuario.rehydrate(
            usuarioId,
            "João Silva",
            new Email("joao@example.com"),
            new Cpf("12345678901"),
            "$2a$10$hashedPassword",
            true,
            true
        );

        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        organizacao = Organizacao.rehydrate(
            organizacaoId,
            new Cnpj("12345678000190"),
            "Empresa XYZ",
            endereco,
            true
        );

        membro = MembroOrganizacao.rehydrate(
            organizacaoId,
            usuarioId,
            PapelOrganizacao.RECRUTADOR,
            true
        );
    }

    @Test
    void deveListarRecrutadoresPorOrganizacao() {
        when(membroOrganizacaoRepository.listByOrganizacao(organizacaoId)).thenReturn(List.of(membro));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        List<Usuario> resultado = service.listarPorOrganizacao(organizacaoId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(usuarioId);
        verify(membroOrganizacaoRepository).listByOrganizacao(organizacaoId);
    }

    @Test
    void deveRemoverRecrutadorComSucesso() {
        when(membroOrganizacaoRepository.exists(organizacaoId, usuarioId)).thenReturn(true);
        when(membroOrganizacaoRepository.getPapel(organizacaoId, usuarioId))
            .thenReturn(Optional.of(PapelOrganizacao.RECRUTADOR));

        service.remover(organizacaoId, usuarioId, usuarioLogadoId);

        verify(membroOrganizacaoRepository).deleteByIds(organizacaoId, usuarioId);
    }

    @Test
    void naoDeveRemoverRecrutadorInexistente() {
        when(membroOrganizacaoRepository.exists(organizacaoId, usuarioId)).thenReturn(false);

        assertThatThrownBy(() -> service.remover(organizacaoId, usuarioId, usuarioLogadoId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Recrutador não encontrado");

        verify(membroOrganizacaoRepository, never()).deleteByIds(any(), any());
    }

    @Test
    void naoDevePermitirRemoverASiMesmo() {
        when(membroOrganizacaoRepository.exists(organizacaoId, usuarioId)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(organizacaoId, usuarioId, usuarioId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Não é possível remover a si mesmo");

        verify(membroOrganizacaoRepository, never()).deleteByIds(any(), any());
    }

    @Test
    void naoDeveRemoverUltimoAdmin() {
        MembroOrganizacao membroAdmin = MembroOrganizacao.rehydrate(
            organizacaoId,
            usuarioId,
            PapelOrganizacao.ADMIN,
            true
        );

        when(membroOrganizacaoRepository.exists(organizacaoId, usuarioId)).thenReturn(true);
        when(membroOrganizacaoRepository.getPapel(organizacaoId, usuarioId))
            .thenReturn(Optional.of(PapelOrganizacao.ADMIN));
        when(membroOrganizacaoRepository.listByOrganizacao(organizacaoId))
            .thenReturn(List.of(membroAdmin));

        assertThatThrownBy(() -> service.remover(organizacaoId, usuarioId, usuarioLogadoId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Não é possível remover o último administrador");

        verify(membroOrganizacaoRepository, never()).deleteByIds(any(), any());
    }

    @Test
    void deveAlterarPapelDeRecrutadorParaAdmin() {
        when(membroOrganizacaoRepository.findByIds(organizacaoId, usuarioId))
            .thenReturn(Optional.of(membro));
        when(membroOrganizacaoRepository.save(any(MembroOrganizacao.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(emailTemplateService.renderAlteracaoPapel(any(), any(), any(), any()))
            .thenReturn("<html>Email</html>");

        Usuario resultado = service.alterarPapel(organizacaoId, usuarioId, PapelOrganizacao.ADMIN, usuarioLogadoId);

        assertThat(resultado).isNotNull();
        verify(membroOrganizacaoRepository).save(any(MembroOrganizacao.class));
        verify(emailService).sendHtmlEmailAsync(eq("joao@example.com"), any(), any());
    }

    @Test
    void naoDeveAlterarProprioPapel() {
        assertThatThrownBy(() -> service.alterarPapel(organizacaoId, usuarioId, PapelOrganizacao.ADMIN, usuarioId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Não é possível alterar seu próprio papel");

        verify(membroOrganizacaoRepository, never()).save(any());
    }

    @Test
    void deveTransferirVagasEntreRecrutadores() {
        UUID usuarioOrigemId = UUID.randomUUID();
        UUID usuarioDestinoId = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();

        Usuario usuarioOrigem = Usuario.rehydrate(
            usuarioOrigemId, "Origem", new Email("origem@example.com"),
            new Cpf("11111111111"), "$2a$10$hash", true, true
        );

        Usuario usuarioDestino = Usuario.rehydrate(
            usuarioDestinoId, "Destino", new Email("destino@example.com"),
            new Cpf("22222222222"), "$2a$10$hash", true, true
        );

        Vaga vaga = Vaga.rehydrate(
            vagaId, organizacaoId, usuarioOrigemId, "Vaga Teste", "Desc", "Req",
            null, java.time.LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT, ModalidadeTrabalho.REMOTO,
            "9h às 18h", null, null, true, null
        );

        when(membroOrganizacaoRepository.exists(organizacaoId, usuarioOrigemId)).thenReturn(true);
        when(membroOrganizacaoRepository.exists(organizacaoId, usuarioDestinoId)).thenReturn(true);
        when(usuarioRepository.findById(usuarioOrigemId)).thenReturn(Optional.of(usuarioOrigem));
        when(usuarioRepository.findById(usuarioDestinoId)).thenReturn(Optional.of(usuarioDestino));
        when(vagaRepository.listByOrganizacao(organizacaoId)).thenReturn(List.of(vaga));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));
        when(candidaturaRepository.listByVaga(vagaId)).thenReturn(List.of());
        when(emailTemplateService.renderTransferenciaVagas(any(), any(), anyInt(), any()))
            .thenReturn("<html>Email</html>");

        int resultado = service.transferirVagas(organizacaoId, usuarioOrigemId, usuarioDestinoId);

        assertThat(resultado).isEqualTo(1);
        verify(vagaRepository).save(any(Vaga.class));
        verify(emailService).sendHtmlEmailAsync(eq("destino@example.com"), any(), any());
    }

    @Test
    void deveCadastrarViaConviteComSucesso() {
        String token = "valid-token";
        ConviteRecrutador convite = ConviteRecrutador.reconstruir(
            UUID.randomUUID(),
            organizacaoId,
            "joao@example.com",
            token,
            StatusConvite.PENDENTE,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now().plusDays(7),
            null
        );

        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(conviteRecrutadorRepository.findByToken(token)).thenReturn(Optional.of(convite));
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        RecrutadorService.CadastrarViaConviteResult resultado = service.cadastrarViaConvite(
            "João Silva",
            "12345678901",
            "joao@example.com",
            "senha123",
            organizacaoId,
            token
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.usuarioId()).isEqualTo(usuario.getId());
        assertThat(resultado.nome()).isEqualTo("João Silva");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(membroOrganizacaoRepository).addMembro(organizacaoId, usuario.getId(), PapelOrganizacao.RECRUTADOR);
    }

    @Test
    void naoDeveCadastrarViaConviteComTokenInvalido() {
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(conviteRecrutadorRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrarViaConvite(
            "João Silva", "12345678901", "joao@example.com", "senha123", organizacaoId, "invalid-token"
        ))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Token de convite inválido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveContarAdminsCorretamente() {
        MembroOrganizacao admin1 = MembroOrganizacao.rehydrate(
            organizacaoId, UUID.randomUUID(), PapelOrganizacao.ADMIN, true
        );
        MembroOrganizacao admin2 = MembroOrganizacao.rehydrate(
            organizacaoId, UUID.randomUUID(), PapelOrganizacao.ADMIN, true
        );
        MembroOrganizacao recrutador = MembroOrganizacao.rehydrate(
            organizacaoId, UUID.randomUUID(), PapelOrganizacao.RECRUTADOR, true
        );

        when(membroOrganizacaoRepository.listByOrganizacao(organizacaoId))
            .thenReturn(List.of(admin1, admin2, recrutador));

        long resultado = service.contarAdmins(organizacaoId);

        assertThat(resultado).isEqualTo(2);
    }
}

