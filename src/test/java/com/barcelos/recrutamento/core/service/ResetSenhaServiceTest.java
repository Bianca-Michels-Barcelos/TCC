package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.ResetSenha;
import com.barcelos.recrutamento.core.model.StatusResetSenha;
import com.barcelos.recrutamento.core.model.Usuario;
import com.barcelos.recrutamento.core.model.vo.Cpf;
import com.barcelos.recrutamento.core.model.vo.Email;
import com.barcelos.recrutamento.core.port.ResetSenhaRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetSenhaServiceTest {

    @Mock
    private ResetSenhaRepository resetSenhaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetSenhaService service;

    private String email;
    private Usuario usuario;
    private UUID usuarioId;
    private ResetSenha resetSenha;
    private String token;

    @BeforeEach
    void setUp() {
        email = "joao@example.com";
        usuarioId = UUID.randomUUID();
        token = "valid-token-12345";

        usuario = Usuario.rehydrate(
            usuarioId, "João Silva", new Email(email),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

        resetSenha = ResetSenha.reconstruir(
            UUID.randomUUID(), usuarioId, token,
            StatusResetSenha.PENDENTE, LocalDateTime.now(), LocalDateTime.now().plusHours(24), null
        );
    }

    @Test
    void deveSolicitarResetComSucesso() {
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(resetSenhaRepository.findByUsuarioIdAndStatus(usuarioId, StatusResetSenha.PENDENTE))
            .thenReturn(Optional.empty());
        when(resetSenhaRepository.save(any(ResetSenha.class))).thenReturn(resetSenha);
        when(emailTemplateService.renderResetSenha(any(), any())).thenReturn("<html>Email</html>");

        service.solicitarReset(email);

        verify(resetSenhaRepository).save(any(ResetSenha.class));
        verify(emailService).sendHtmlEmailAsync(eq(email), any(), any());
    }

    @Test
    void naoDeveSolicitarResetQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.solicitarReset(email))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Usuário não encontrado");

        verify(resetSenhaRepository, never()).save(any());
    }

    @Test
    void deveEstenderValidadeQuandoJaExisteTokenPendente() {
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(resetSenhaRepository.findByUsuarioIdAndStatus(usuarioId, StatusResetSenha.PENDENTE))
            .thenReturn(Optional.of(resetSenha));
        when(resetSenhaRepository.save(any(ResetSenha.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailTemplateService.renderResetSenha(any(), any())).thenReturn("<html>Email</html>");

        service.solicitarReset(email);

        verify(resetSenhaRepository).save(any(ResetSenha.class));
    }

    @Test
    void deveValidarTokenComSucesso() {
        when(resetSenhaRepository.findByToken(token)).thenReturn(Optional.of(resetSenha));

        ResetSenha resultado = service.validarToken(token);

        assertThat(resultado).isNotNull();
        verify(resetSenhaRepository).findByToken(token);
    }

    @Test
    void naoDeveValidarTokenInvalido() {
        when(resetSenhaRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validarToken(token))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Token inválido");
    }

    @Test
    void naoDeveValidarTokenExpirado() {
        ResetSenha tokenExpirado = ResetSenha.reconstruir(
            UUID.randomUUID(), usuarioId, token, StatusResetSenha.PENDENTE,
            LocalDateTime.now().minusDays(2), LocalDateTime.now().minusHours(1), null
        );

        when(resetSenhaRepository.findByToken(token)).thenReturn(Optional.of(tokenExpirado));

        assertThatThrownBy(() -> service.validarToken(token))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Token expirado");
    }

    @Test
    void deveResetarSenhaComSucesso() {
        when(resetSenhaRepository.findByToken(token)).thenReturn(Optional.of(resetSenha));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$newHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(resetSenhaRepository.save(any(ResetSenha.class))).thenAnswer(inv -> inv.getArgument(0));

        service.resetarSenha(token, "novaSenha123");

        verify(passwordEncoder).encode("novaSenha123");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(resetSenhaRepository).save(any(ResetSenha.class));
    }
}

