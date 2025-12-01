package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.VagaSalvaComDetalhesResponse;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.VagaSalva;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.service.VagaSalvaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vagas-salvas")
public class VagaSalvaController {

    private final VagaSalvaService service;
    private final UsuarioRepository usuarioRepository;

    public VagaSalvaController(VagaSalvaService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/vaga/{vagaId}")
    public ResponseEntity<VagaSalva> salvar(
            @PathVariable UUID vagaId,
            Authentication authentication
    ) {
        UUID usuarioId = getUsuarioId(authentication);
        var vagaSalva = service.salvar(vagaId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(vagaSalva);
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @DeleteMapping("/vaga/{vagaId}")
    public ResponseEntity<Void> remover(
            @PathVariable UUID vagaId,
            Authentication authentication
    ) {
        UUID usuarioId = getUsuarioId(authentication);
        service.remover(vagaId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping
    public ResponseEntity<List<VagaSalvaComDetalhesResponse>> listar(Authentication authentication) {
        UUID usuarioId = getUsuarioId(authentication);
        var vagasSalvasDetalhadas = service.listarComDetalhesPorUsuario(usuarioId);

        var response = vagasSalvasDetalhadas.stream()
                .map(vsd -> {
                    var endereco = vsd.vaga().getEndereco();
                    var enderecoInfo = endereco != null ? new VagaSalvaComDetalhesResponse.EnderecoInfo(
                            endereco.cidade(),
                            endereco.uf() != null ? endereco.uf().value() : null
                    ) : null;

                    return new VagaSalvaComDetalhesResponse(
                            vsd.vagaSalva().getId(),
                            vsd.vagaSalva().getVagaId(),
                            vsd.vagaSalva().getSalvaEm(),
                            new VagaSalvaComDetalhesResponse.VagaInfo(
                                    vsd.vaga().getId(),
                                    vsd.vaga().getTitulo(),
                                    vsd.vaga().getDescricao(),
                                    vsd.vaga().getRequisitos(),
                                    vsd.vaga().getSalario(),
                                    vsd.vaga().getModalidade().name(),
                                    vsd.vaga().getTipoContrato().name(),
                                    vsd.vaga().getStatus().name(),
                                    vsd.vaga().getDataPublicacao(),
                                    enderecoInfo
                            ),
                            new VagaSalvaComDetalhesResponse.OrganizacaoInfo(
                                    vsd.organizacao().getId(),
                                    vsd.organizacao().getNome()
                            )
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/vaga/{vagaId}/esta-salva")
    public ResponseEntity<Map<String, Boolean>> verificarSalva(
            @PathVariable UUID vagaId,
            Authentication authentication
    ) {
        UUID usuarioId = getUsuarioId(authentication);
        boolean estaSalva = service.estaSalva(vagaId, usuarioId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("estaSalva", estaSalva);

        return ResponseEntity.ok(response);
    }

    private UUID getUsuarioId(Authentication authentication) {
        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));
        return usuario.getId();
    }
}
