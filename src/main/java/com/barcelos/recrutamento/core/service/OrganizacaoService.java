package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizacaoService {

    private final OrganizacaoRepository organizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MembroOrganizacaoRepository membroOrganizacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganizacaoService(OrganizacaoRepository organizacaoRepository, UsuarioRepository usuarioRepository, MembroOrganizacaoRepository membroOrganizacaoRepository, PasswordEncoder passwordEncoder) {
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.membroOrganizacaoRepository = membroOrganizacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrarResult registrar(RegistrarCommand cmd) {
        var endereco = new Endereco(
                cmd.logradouro(), cmd.complemento(), cmd.numero(),
                new Cep(cmd.cep()), cmd.cidade(), new Sigla(cmd.uf())
        );
        var organizacao = Organizacao.novo(new Cnpj(cmd.cnpj()), cmd.nome(), endereco);
        organizacao = organizacaoRepository.save(organizacao);

        var senhaHash = passwordEncoder.encode(cmd.adminSenha());
        var usuario = Usuario.novo(
                cmd.adminNome(),
                new Email(cmd.adminEmail()),
                new Cpf(cmd.adminCpf()),
                senhaHash
        );
        usuario = usuarioRepository.save(usuario);

        membroOrganizacaoRepository.addAdmin(organizacao.getId(), usuario.getId());

        return new RegistrarResult(
                organizacao.getId(), organizacao.getCnpj().value(), organizacao.getNome(),
                organizacao.getEndereco().cidade(), organizacao.getEndereco().uf().value(),
                usuario.getId(), usuario.getNome(), usuario.getEmail().value()
        );
    }

    public record RegistrarCommand(
            String cnpj, String nome,
            String logradouro, String complemento, String numero,
            String cep, String cidade, String uf,
            String adminNome, String adminCpf, String adminEmail, String adminSenha
    ) {
    }

    public record RegistrarResult(
            UUID organizacaoId, String cnpj, String nome, String cidade, String uf,
            UUID adminId, String adminNome, String adminEmail
    ) {
    }
}
