package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.Organizacao;
import com.barcelos.recrutamento.core.model.Usuario;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.data.entity.PapelOrganizacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizacaoServiceTest {

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MembroOrganizacaoRepository membroOrganizacaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OrganizacaoService service;

    private OrganizacaoService.RegistrarCommand command;
    private Organizacao organizacao;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        command = new OrganizacaoService.RegistrarCommand(
            "12345678000190",
            "Empresa XYZ",
            "Rua Teste",
            "Apto 100",
            "123",
            "01310100",
            "São Paulo",
            "SP",
            "João Silva",
            "12345678901",
            "joao@example.com",
            "senha123"
        );

        Endereco endereco = new Endereco(
            "Rua Teste", "Apto 100", "123",
            new Cep("01310100"), "São Paulo", new Sigla("SP")
        );

        organizacao = Organizacao.rehydrate(
            UUID.randomUUID(),
            new Cnpj("12345678000190"),
            "Empresa XYZ",
            endereco,
            true
        );

        usuario = Usuario.rehydrate(
            UUID.randomUUID(),
            "João Silva",
            new Email("joao@example.com"),
            new Cpf("12345678901"),
            "$2a$10$hashedPassword",
            true,
            true
        );
    }

    @Test
    void deveRegistrarOrganizacaoComSucesso() {
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");
        when(organizacaoRepository.save(any(Organizacao.class))).thenReturn(organizacao);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        OrganizacaoService.RegistrarResult resultado = service.registrar(command);

        assertThat(resultado).isNotNull();
        assertThat(resultado.organizacaoId()).isEqualTo(organizacao.getId());
        assertThat(resultado.cnpj()).isEqualTo("12345678000190");
        assertThat(resultado.nome()).isEqualTo("Empresa XYZ");
        assertThat(resultado.adminId()).isEqualTo(usuario.getId());
        assertThat(resultado.adminNome()).isEqualTo("João Silva");
        assertThat(resultado.adminEmail()).isEqualTo("joao@example.com");

        verify(organizacaoRepository).save(any(Organizacao.class));
        verify(usuarioRepository).save(any(Usuario.class));
        verify(membroOrganizacaoRepository).addAdmin(organizacao.getId(), usuario.getId());
    }

    @Test
    void deveEncodeSenhaAoRegistrar() {
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedPassword");
        when(organizacaoRepository.save(any(Organizacao.class))).thenReturn(organizacao);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        service.registrar(command);

        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveCriarOrganizacaoEUsuarioComDadosCorretos() {
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashedPassword");
        when(organizacaoRepository.save(any(Organizacao.class))).thenReturn(organizacao);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        OrganizacaoService.RegistrarResult resultado = service.registrar(command);

        verify(organizacaoRepository).save(argThat(org ->
            org.getNome().equals("Empresa XYZ") &&
            org.getCnpj().value().equals("12345678000190") &&
            org.getEndereco().cidade().equals("São Paulo")
        ));

        verify(usuarioRepository).save(argThat(user ->
            user.getNome().equals("João Silva") &&
            user.getEmail().value().equals("joao@example.com") &&
            user.getCpf().value().equals("12345678901")
        ));
    }

    @Test
    void deveAdicionarUsuarioComoAdminDaOrganizacao() {
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashedPassword");
        when(organizacaoRepository.save(any(Organizacao.class))).thenReturn(organizacao);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        service.registrar(command);

        verify(membroOrganizacaoRepository).addAdmin(organizacao.getId(), usuario.getId());
    }
}

