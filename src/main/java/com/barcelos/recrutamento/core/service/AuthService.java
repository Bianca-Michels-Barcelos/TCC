package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.config.JwtProperties;
import com.barcelos.recrutamento.config.JwtTokenProvider;
import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.MembroOrganizacao;
import com.barcelos.recrutamento.core.model.RefreshToken;
import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.RefreshTokenRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    public record LoginResult(
            String accessToken,
            String refreshToken,
            UUID usuarioId,
            String nome,
            String email,
            List<String> roles
    ) {
    }

    public record RefreshResult(
            String accessToken,
            String refreshToken
    ) {
    }

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UsuarioRepository usuarioRepository;
    private final MembroOrganizacaoRepository membroOrganizacaoRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties,
                       UsuarioRepository usuarioRepository,
                       MembroOrganizacaoRepository membroOrganizacaoRepository,
                       OrganizacaoRepository organizacaoRepository,
                       RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.usuarioRepository = usuarioRepository;
        this.membroOrganizacaoRepository = membroOrganizacaoRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public LoginResult login(String email, String senha) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, senha)
        );

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UUID organizacaoId = null;
        String organizacaoNome = null;
        var membros = membroOrganizacaoRepository.listByUsuario(usuario.getId());
        if (!membros.isEmpty()) {

            var membroAtivo = membros.stream()
                    .filter(MembroOrganizacao::isAtivo)
                    .findFirst();

            if (membroAtivo.isPresent()) {
                organizacaoId = membroAtivo.get().getOrganizacaoId();

                var organizacao = organizacaoRepository.findById(organizacaoId);
                organizacaoNome = organizacao.map(org -> org.getNome()).orElse(null);
            }
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                usuario.getEmail().value(),
                roles,
                organizacaoId,
                organizacaoNome,
                usuario.getId(),
                usuario.getNome()
        );
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        Instant expiraEm = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs());
        RefreshToken refreshToken = RefreshToken.novo(refreshTokenValue, usuario.getId(), expiraEm);
        refreshTokenRepository.save(refreshToken);

        return new LoginResult(
                accessToken,
                refreshTokenValue,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail().value(),
                roles
        );
    }

    @Transactional
    public RefreshResult refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token inválido"));

        if (!refreshToken.isValido()) {
            throw new BusinessRuleViolationException("Refresh token expirado ou revogado");
        }

        var usuario = usuarioRepository.findById(refreshToken.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        var membros = membroOrganizacaoRepository.listByUsuario(usuario.getId());
        List<String> roles;
        UUID organizacaoId = null;
        String organizacaoNome = null;
        if (membros.isEmpty()) {
            roles = List.of("ROLE_CANDIDATO");
        } else {
            roles = membros.stream()
                    .map(m -> "ROLE_" + m.getPapel().name())
                    .distinct()
                    .toList();

            var membroAtivo = membros.stream()
                    .filter(MembroOrganizacao::isAtivo)
                    .findFirst();

            if (membroAtivo.isPresent()) {
                organizacaoId = membroAtivo.get().getOrganizacaoId();

                var organizacao = organizacaoRepository.findById(organizacaoId);
                organizacaoNome = organizacao.map(org -> org.getNome()).orElse(null);
            }
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                usuario.getEmail().value(),
                roles,
                organizacaoId,
                organizacaoNome,
                usuario.getId(),
                usuario.getNome()
        );

        String newRefreshTokenValue = jwtTokenProvider.generateRefreshToken();
        Instant expiraEm = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs());
        RefreshToken newRefreshToken = RefreshToken.novo(newRefreshTokenValue, usuario.getId(), expiraEm);

        refreshTokenRepository.save(refreshToken.revogar());

        refreshTokenRepository.save(newRefreshToken);

        return new RefreshResult(newAccessToken, newRefreshTokenValue);
    }
}
