package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.ConviteRecrutador;
import com.barcelos.recrutamento.core.model.Organizacao;
import com.barcelos.recrutamento.core.model.StatusConvite;
import com.barcelos.recrutamento.core.model.vo.Cep;
import com.barcelos.recrutamento.core.model.vo.Cnpj;
import com.barcelos.recrutamento.core.model.vo.Endereco;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import com.barcelos.recrutamento.core.port.ConviteRecrutadorRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConviteRecrutadorServiceTest {

    @Mock
    private ConviteRecrutadorRepository conviteRepository;

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private ConviteRecrutadorService service;

    private UUID organizacaoId;
    private Organizacao organizacao;
    private ConviteRecrutador convite;
    private String email;
    private String token;

    @BeforeEach
    void setUp() {
        organizacaoId = UUID.randomUUID();
        email = "joao@example.com";
        token = "valid-token-12345";

        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        organizacao = Organizacao.rehydrate(
            organizacaoId,
            new Cnpj("12345678000190"),
            "Empresa XYZ",
            endereco,
            true
        );

        convite = ConviteRecrutador.reconstruir(
            UUID.randomUUID(),
            organizacaoId,
            email,
            token,
            StatusConvite.PENDENTE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(7),
            null
        );
    }

    @Test
    void deveCriarConviteComSucesso() {
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(conviteRepository.existsByEmailAndOrganizacaoAndStatus(email, organizacaoId, StatusConvite.PENDENTE))
            .thenReturn(false);
        when(conviteRepository.save(any(ConviteRecrutador.class))).thenReturn(convite);
        when(emailTemplateService.renderConviteRecrutador(any(), any(), any()))
            .thenReturn("<html>Email</html>");

        ConviteRecrutador resultado = service.criarConvite(organizacaoId, email);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo(email);
        assertThat(resultado.getStatus()).isEqualTo(StatusConvite.PENDENTE);
        verify(conviteRepository).save(any(ConviteRecrutador.class));
        verify(emailService).sendHtmlEmailAsync(eq(email), any(), any());
    }

    @Test
    void naoDeveCriarConviteQuandoOrganizacaoNaoExiste() {
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarConvite(organizacaoId, email))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Empresa");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarConviteDuplicado() {
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(conviteRepository.existsByEmailAndOrganizacaoAndStatus(email, organizacaoId, StatusConvite.PENDENTE))
            .thenReturn(true);

        assertThatThrownBy(() -> service.criarConvite(organizacaoId, email))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Já existe um convite pendente");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void deveBuscarConvitePorToken() {
        when(conviteRepository.findByToken(token)).thenReturn(Optional.of(convite));

        ConviteRecrutador resultado = service.buscarPorToken(token);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getToken()).isEqualTo(token);
        verify(conviteRepository).findByToken(token);
    }

    @Test
    void naoDeveBuscarConviteComTokenInvalido() {
        when(conviteRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorToken(token))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Convite");
    }

    @Test
    void deveAceitarConviteValido() {
        when(conviteRepository.findByToken(token)).thenReturn(Optional.of(convite));
        when(conviteRepository.save(any(ConviteRecrutador.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ConviteRecrutador resultado = service.aceitarConvite(token);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusConvite.ACEITO);
        verify(conviteRepository).save(any(ConviteRecrutador.class));
    }

    @Test
    void naoDeveAceitarConviteExpirado() {
        ConviteRecrutador conviteExpirado = ConviteRecrutador.reconstruir(
            UUID.randomUUID(),
            organizacaoId,
            email,
            token,
            StatusConvite.PENDENTE,
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(3),
            null
        );

        when(conviteRepository.findByToken(token)).thenReturn(Optional.of(conviteExpirado));

        assertThatThrownBy(() -> service.aceitarConvite(token))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Convite inválido ou expirado");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void deveRecusarConvitePendente() {
        when(conviteRepository.findByToken(token)).thenReturn(Optional.of(convite));
        when(conviteRepository.save(any(ConviteRecrutador.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ConviteRecrutador resultado = service.recusarConvite(token);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusConvite.RECUSADO);
        verify(conviteRepository).save(any(ConviteRecrutador.class));
    }

    @Test
    void naoDeveRecusarConviteJaAceito() {
        ConviteRecrutador conviteAceito = ConviteRecrutador.reconstruir(
            UUID.randomUUID(),
            organizacaoId,
            email,
            token,
            StatusConvite.ACEITO,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now()
        );

        when(conviteRepository.findByToken(token)).thenReturn(Optional.of(conviteAceito));

        assertThatThrownBy(() -> service.recusarConvite(token))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Convite não está pendente");

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void deveEnviarEmailAoCriarConvite() {
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(conviteRepository.existsByEmailAndOrganizacaoAndStatus(email, organizacaoId, StatusConvite.PENDENTE))
            .thenReturn(false);
        when(conviteRepository.save(any(ConviteRecrutador.class))).thenReturn(convite);
        when(emailTemplateService.renderConviteRecrutador(eq(email), eq("Empresa XYZ"), anyString()))
            .thenReturn("<html>Email Content</html>");

        service.criarConvite(organizacaoId, email);

        verify(emailTemplateService).renderConviteRecrutador(eq(email), eq("Empresa XYZ"), any());
        verify(emailService).sendHtmlEmailAsync(
            eq(email),
            contains("Empresa XYZ"),
            anyString()
        );
    }
}

