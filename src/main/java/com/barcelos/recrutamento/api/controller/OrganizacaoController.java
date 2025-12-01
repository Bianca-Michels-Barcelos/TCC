package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.MembroOrganizacaoResponse;
import com.barcelos.recrutamento.api.dto.OrganizacaoPublicaResponse;
import com.barcelos.recrutamento.api.dto.RegistrarOrganizacaoRequest;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.port.MembroOrganizacaoRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.service.OrganizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizacoes")
public class OrganizacaoController {

    private final OrganizacaoService organizacaoService;
    private final OrganizacaoRepository organizacaoRepository;
    private final MembroOrganizacaoRepository membroOrganizacaoRepository;
    private final OrganizacaoSecurityService orgSecurityService;

    public OrganizacaoController(
            OrganizacaoService organizacaoService,
            OrganizacaoRepository organizacaoRepository,
            MembroOrganizacaoRepository membroOrganizacaoRepository,
            OrganizacaoSecurityService orgSecurityService) {
        this.organizacaoService = organizacaoService;
        this.organizacaoRepository = organizacaoRepository;
        this.membroOrganizacaoRepository = membroOrganizacaoRepository;
        this.orgSecurityService = orgSecurityService;
    }

    
    @PostMapping
    public ResponseEntity<OrganizacaoService.RegistrarResult> registrar(
            @Valid @RequestBody RegistrarOrganizacaoRequest request) {

        var cmd = new OrganizacaoService.RegistrarCommand(
                request.cnpj(),
                request.nome(),
                request.logradouro(),
                request.complemento(),
                request.numero(),
                request.cep(),
                request.cidade(),
                request.uf(),
                request.adminRecruiter().nome(),
                request.adminRecruiter().cpf(),
                request.adminRecruiter().email(),
                request.adminRecruiter().senha()
        );
        var result = organizacaoService.registrar(cmd);
        return ResponseEntity.ok(result);
    }

    
    @GetMapping("/publicas/{organizacaoId}")
    public ResponseEntity<OrganizacaoPublicaResponse> buscarOrganizacaoPublica(
            @PathVariable UUID organizacaoId
    ) {
        var organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada"));

        if (!organizacao.isAtivo()) {
            throw new ResourceNotFoundException("Organização não encontrada");
        }

        var endereco = organizacao.getEndereco();
        var enderecoInfo = new OrganizacaoPublicaResponse.EnderecoInfo(
                endereco.cidade(),
                endereco.uf().value()
        );

        var response = new OrganizacaoPublicaResponse(
                organizacao.getId(),
                organizacao.getNome(),
                enderecoInfo
        );

        return ResponseEntity.ok(response);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{organizacaoId}/membros")
    public ResponseEntity<List<MembroOrganizacaoResponse>> listarMembros(
            @PathVariable UUID organizacaoId,
            Authentication authentication
    ) {

        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var membros = membroOrganizacaoRepository.listByOrganizacao(organizacaoId);

        var response = membros.stream()
                .map(membro -> new MembroOrganizacaoResponse(
                        membro.getUsuarioId(),
                        membro.getPapel(),
                        membro.isAtivo()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}
