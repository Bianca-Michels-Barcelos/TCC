package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.config.JwtProperties;
import com.barcelos.recrutamento.config.JwtTokenProvider;
import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.Cep;
import com.barcelos.recrutamento.core.model.vo.Cnpj;
import com.barcelos.recrutamento.core.model.vo.Cpf;
import com.barcelos.recrutamento.core.model.vo.Email;
import com.barcelos.recrutamento.core.model.vo.Endereco;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import com.barcelos.recrutamento.data.entity.PapelOrganizacao;
import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.RefreshTokenRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MembroOrganizacaoRepository membroOrganizacaoRepository;

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService service;

    private String email;
    private String senha;
    private Usuario usuario;
    private Authentication authentication;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        email = "joao@example.com";
        senha = "senha123";
        usuarioId = UUID.randomUUID();

        usuario = Usuario.rehydrate(
            usuarioId,
            "João Silva",
            new Email(email),
            new Cpf("12345678901"),
            "$2a$10$hashedPassword",
            true,
            true
        );

        authentication = new UsernamePasswordAuthenticationToken(
            email,
            senha,
            List.of(new SimpleGrantedAuthority("ROLE_CANDIDATO"))
        );
    }

    @Test
    void deveRealizarLoginComSucesso() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(membroOrganizacaoRepository.listByUsuario(usuarioId)).thenReturn(List.of());
        when(jwtTokenProvider.generateAccessToken(anyString(), anyList(), any(), any(), any(), anyString()))
            .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh-token");
        when(jwtProperties.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AuthService.LoginResult result = service.login(email, senha);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.usuarioId()).isEqualTo(usuarioId);
        assertThat(result.nome()).isEqualTo("João Silva");
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.roles()).contains("ROLE_CANDIDATO");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByEmail(email);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void deveRealizarLoginDeRecrutadorComOrganizacao() {
        UUID organizacaoId = UUID.randomUUID();
        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        Organizacao organizacao = Organizacao.rehydrate(
            organizacaoId,
            new Cnpj("12345678000190"),
            "Empresa XYZ",
            endereco,
            true
        );

        MembroOrganizacao membro = MembroOrganizacao.rehydrate(
            organizacaoId,
            usuarioId,
            PapelOrganizacao.RECRUTADOR,
            true
        );

        Authentication authRecrutador = new UsernamePasswordAuthenticationToken(
            email,
            senha,
            List.of(new SimpleGrantedAuthority("ROLE_RECRUTADOR"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authRecrutador);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(membroOrganizacaoRepository.listByUsuario(usuarioId)).thenReturn(List.of(membro));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(jwtTokenProvider.generateAccessToken(anyString(), anyList(), any(), any(), any(), anyString()))
            .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh-token");
        when(jwtProperties.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AuthService.LoginResult result = service.login(email, senha);

        assertThat(result).isNotNull();
        assertThat(result.roles()).contains("ROLE_RECRUTADOR");
        verify(organizacaoRepository).findById(organizacaoId);
    }

    @Test
    void naoDeveRealizarLoginQuandoUsuarioNaoExiste() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(email, senha))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Usuário não encontrado");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void deveRefreshTokenComSucesso() {
        String refreshTokenValue = "refresh-token";
        Instant expiraEm = Instant.now().plusSeconds(3600);

        RefreshToken refreshToken = RefreshToken.reconstituir(
            UUID.randomUUID(),
            refreshTokenValue,
            usuarioId,
            expiraEm,
            Instant.now(),
            false
        );

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(membroOrganizacaoRepository.listByUsuario(usuarioId)).thenReturn(List.of());
        when(jwtTokenProvider.generateAccessToken(anyString(), anyList(), any(), any(), any(), anyString()))
            .thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh-token");
        when(jwtProperties.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AuthService.RefreshResult result = service.refresh(refreshTokenValue);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        verify(refreshTokenRepository).findByToken(refreshTokenValue);
    }

    @Test
    void naoDeveRefreshQuandoTokenNaoExiste() {
        String refreshTokenValue = "invalid-token";

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(refreshTokenValue))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Refresh token inválido");

        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void naoDeveRefreshQuandoTokenEstaExpirado() {
        String refreshTokenValue = "expired-token";
        Instant expiraEm = Instant.now().minusSeconds(3600);

        RefreshToken refreshToken = RefreshToken.reconstituir(
            UUID.randomUUID(),
            refreshTokenValue,
            usuarioId,
            expiraEm,
            Instant.now().minusSeconds(7200),
            false
        );

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> service.refresh(refreshTokenValue))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Refresh token expirado ou revogado");

        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void naoDeveRefreshQuandoTokenEstaRevogado() {
        String refreshTokenValue = "revoked-token";
        Instant expiraEm = Instant.now().plusSeconds(3600);

        RefreshToken refreshToken = RefreshToken.reconstituir(
            UUID.randomUUID(),
            refreshTokenValue,
            usuarioId,
            expiraEm,
            Instant.now(),
            true
        );

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> service.refresh(refreshTokenValue))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Refresh token expirado ou revogado");

        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void deveRefreshComRolesDeRecrutador() {
        String refreshTokenValue = "refresh-token";
        Instant expiraEm = Instant.now().plusSeconds(3600);
        UUID organizacaoId = UUID.randomUUID();

        RefreshToken refreshToken = RefreshToken.reconstituir(
            UUID.randomUUID(),
            refreshTokenValue,
            usuarioId,
            expiraEm,
            Instant.now(),
            false
        );

        MembroOrganizacao membro = MembroOrganizacao.rehydrate(
            organizacaoId,
            usuarioId,
            PapelOrganizacao.RECRUTADOR,
            true
        );

        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        Organizacao organizacao = Organizacao.rehydrate(
            organizacaoId,
            new Cnpj("12345678000190"),
            "Empresa XYZ",
            endereco,
            true
        );

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(membroOrganizacaoRepository.listByUsuario(usuarioId)).thenReturn(List.of(membro));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(jwtTokenProvider.generateAccessToken(anyString(), anyList(), any(), any(), any(), anyString()))
            .thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh-token");
        when(jwtProperties.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AuthService.RefreshResult result = service.refresh(refreshTokenValue);

        assertThat(result).isNotNull();
        verify(jwtTokenProvider).generateAccessToken(
            eq(email),
            argThat(roles -> roles.contains("ROLE_RECRUTADOR")),
            eq(organizacaoId),
            eq("Empresa XYZ"),
            eq(usuarioId),
            eq("João Silva")
        );
    }
}

