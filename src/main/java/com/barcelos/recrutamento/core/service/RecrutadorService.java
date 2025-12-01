package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.MembroOrganizacao;
import com.barcelos.recrutamento.core.model.StatusConvite;
import com.barcelos.recrutamento.core.model.Usuario;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.model.vo.Cpf;
import com.barcelos.recrutamento.core.model.vo.Email;
import com.barcelos.recrutamento.data.entity.PapelOrganizacao;
import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.port.ConviteRecrutadorRepository;
import com.barcelos.recrutamento.core.port.CandidaturaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RecrutadorService {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final MembroOrganizacaoRepository membroOrganizacaoRepository;
    private final VagaRepository vagaRepository;
    private final ConviteRecrutadorRepository conviteRecrutadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final CandidaturaRepository candidaturaRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    public RecrutadorService(UsuarioRepository usuarioRepository,
                            OrganizacaoRepository organizacaoRepository,
                            MembroOrganizacaoRepository membroOrganizacaoRepository,
                            VagaRepository vagaRepository,
                            ConviteRecrutadorRepository conviteRecrutadorRepository,
                            PasswordEncoder passwordEncoder,
                            CandidaturaRepository candidaturaRepository,
                            EmailService emailService,
                            EmailTemplateService emailTemplateService) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.membroOrganizacaoRepository = membroOrganizacaoRepository;
        this.vagaRepository = vagaRepository;
        this.conviteRecrutadorRepository = conviteRecrutadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.candidaturaRepository = candidaturaRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarPorOrganizacao(UUID organizacaoId) {
        return membroOrganizacaoRepository.listByOrganizacao(organizacaoId).stream()
                .filter(m -> m.getPapel() == PapelOrganizacao.RECRUTADOR || m.getPapel() == PapelOrganizacao.ADMIN)
                .map(MembroOrganizacao::getUsuarioId)
                .map(usuarioId -> usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId)))
                .toList();
    }

    @Transactional
    public void remover(UUID organizacaoId, UUID usuarioId, UUID usuarioLogadoId) {
        if (!membroOrganizacaoRepository.exists(organizacaoId, usuarioId)) {
            throw new ResourceNotFoundException("Recrutador não encontrado na organização");
        }

        if (usuarioId.equals(usuarioLogadoId)) {
            throw new BusinessRuleViolationException("Não é possível remover a si mesmo");
        }

        var papel = membroOrganizacaoRepository.getPapel(organizacaoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Papel não encontrado"));

        if (papel == PapelOrganizacao.ADMIN) {
            long totalAdmins = contarAdmins(organizacaoId);
            if (totalAdmins <= 1) {
                throw new BusinessRuleViolationException("Não é possível remover o último administrador da organização");
            }
        }

        membroOrganizacaoRepository.deleteByIds(organizacaoId, usuarioId);
    }

    @Transactional(readOnly = true)
    public long contarAdmins(UUID organizacaoId) {
        return membroOrganizacaoRepository.listByOrganizacao(organizacaoId).stream()
                .filter(MembroOrganizacao::isAdmin)
                .filter(MembroOrganizacao::isAtivo)
                .count();
    }

    @Transactional
    public Usuario alterarPapel(UUID organizacaoId, UUID usuarioId, PapelOrganizacao novoPapel, UUID usuarioLogadoId) {

        if (usuarioId.equals(usuarioLogadoId)) {
            throw new BusinessRuleViolationException("Não é possível alterar seu próprio papel");
        }

        var membro = membroOrganizacaoRepository.findByIds(organizacaoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado na organização"));

        if (membro.getPapel() == PapelOrganizacao.ADMIN && novoPapel == PapelOrganizacao.RECRUTADOR) {
            long totalAdmins = contarAdmins(organizacaoId);
            if (totalAdmins <= 1) {
                throw new BusinessRuleViolationException("Não é possível remover o último administrador da organização");
            }
        }

        PapelOrganizacao papelAnterior = membro.getPapel();

        var membroAtualizado = membro.comPapel(novoPapel);
        membroOrganizacaoRepository.save(membroAtualizado);

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        try {
            var organizacao = organizacaoRepository.findById(organizacaoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Organização", organizacaoId));

            String htmlContent = emailTemplateService.renderAlteracaoPapel(
                    usuario.getNome(),
                    papelAnterior.name(),
                    novoPapel.name(),
                    organizacao.getNome()
            );

            String subject = novoPapel == PapelOrganizacao.ADMIN 
                    ? "Parabéns! Você foi promovido a Administrador" 
                    : "Seu papel foi alterado - " + organizacao.getNome();

            emailService.sendHtmlEmailAsync(
                    usuario.getEmail().value(),
                    subject,
                    htmlContent
            );
        } catch (Exception e) {

            org.slf4j.LoggerFactory.getLogger(RecrutadorService.class)
                    .error("Erro ao enviar email de alteração de papel para {}: {}", 
                           usuario.getEmail().value(), e.getMessage());
        }

        return usuario;
    }

    @Transactional
    public int transferirVagas(UUID organizacaoId, UUID usuarioIdOrigem, UUID usuarioIdDestino) {

        if (!membroOrganizacaoRepository.exists(organizacaoId, usuarioIdOrigem)) {
            throw new ResourceNotFoundException("Usuário de origem não encontrado na organização");
        }
        if (!membroOrganizacaoRepository.exists(organizacaoId, usuarioIdDestino)) {
            throw new ResourceNotFoundException("Usuário de destino não encontrado na organização");
        }

        var usuarioOrigem = usuarioRepository.findById(usuarioIdOrigem)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de origem não encontrado"));
        var usuarioDestino = usuarioRepository.findById(usuarioIdDestino)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de destino não encontrado"));

        var vagas = vagaRepository.listByOrganizacao(organizacaoId).stream()
                .filter(vaga -> vaga.getRecrutadorUsuarioId().equals(usuarioIdOrigem))
                .toList();

        var vagasTransferidas = new java.util.ArrayList<EmailTemplateService.VagaTransferida>();

        for (Vaga vaga : vagas) {
            var vagaAtualizada = vaga.comRecrutador(usuarioIdDestino);
            vagaRepository.save(vagaAtualizada);

            int candidatosAtivos = (int) candidaturaRepository.listByVaga(vaga.getId()).stream()
                    .filter(c -> c.getStatus() != com.barcelos.recrutamento.core.model.StatusCandidatura.REJEITADA)
                    .count();

            vagasTransferidas.add(new EmailTemplateService.VagaTransferida(
                    vaga.getId().toString(),
                    vaga.getTitulo(),
                    vaga.getStatus().name(),
                    candidatosAtivos
            ));
        }

        if (!vagas.isEmpty()) {
            try {
                String htmlContent = emailTemplateService.renderTransferenciaVagas(
                        usuarioDestino.getNome(),
                        usuarioOrigem.getNome(),
                        vagas.size(),
                        vagasTransferidas
                );

                emailService.sendHtmlEmailAsync(
                        usuarioDestino.getEmail().value(),
                        "Novas vagas transferidas para você",
                        htmlContent
                );
            } catch (Exception e) {

                org.slf4j.LoggerFactory.getLogger(RecrutadorService.class)
                        .error("Erro ao enviar email de transferência de vagas para {}: {}", 
                               usuarioDestino.getEmail().value(), e.getMessage());
            }
        }

        return vagas.size();
    }

    
    @Transactional
    public CadastrarViaConviteResult cadastrarViaConvite(
            String nome,
            String cpf,
            String email,
            String senha,
            UUID organizacaoId,
            String tokenConvite
    ) {

        organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", organizacaoId));

        var convite = conviteRecrutadorRepository.findByToken(tokenConvite)
                .orElseThrow(() -> new BusinessRuleViolationException("Token de convite inválido"));

        if (!convite.getOrganizacaoId().equals(organizacaoId)) {
            throw new BusinessRuleViolationException("Convite não pertence a esta organização");
        }

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new BusinessRuleViolationException("Convite já foi utilizado ou está expirado");
        }

        if (!convite.getEmail().equalsIgnoreCase(email)) {
            throw new BusinessRuleViolationException("Email não corresponde ao convite");
        }

        if (!convite.isValido()) {
            throw new BusinessRuleViolationException("Convite expirado");
        }

        var usuarioExistente = usuarioRepository.findByEmail(email);
        if (usuarioExistente.isPresent()) {
            throw new BusinessRuleViolationException("Já existe um usuário cadastrado com este email");
        }

        var senhaHash = passwordEncoder.encode(senha);
        var usuario = Usuario.novo(nome, new Email(email), new Cpf(cpf), senhaHash);
        usuario = usuarioRepository.save(usuario);

        membroOrganizacaoRepository.addMembro(organizacaoId, usuario.getId(), PapelOrganizacao.RECRUTADOR);

        return new CadastrarViaConviteResult(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail().value(),
                organizacaoId
        );
    }

    public record CadastrarViaConviteResult(
            UUID usuarioId,
            String nome,
            String email,
            UUID organizacaoId
    ) {}
}
