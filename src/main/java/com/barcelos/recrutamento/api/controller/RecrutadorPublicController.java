package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.CadastrarRecrutadorViaConviteRequest;
import com.barcelos.recrutamento.api.dto.CadastrarRecrutadorViaConviteResponse;
import com.barcelos.recrutamento.core.service.RecrutadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recrutadores")
public class RecrutadorPublicController {

    private final RecrutadorService recrutadorService;

    public RecrutadorPublicController(RecrutadorService recrutadorService) {
        this.recrutadorService = recrutadorService;
    }

    
    @PostMapping("/cadastro-via-convite")
    public ResponseEntity<CadastrarRecrutadorViaConviteResponse> cadastrarViaConvite(
            @Valid @RequestBody CadastrarRecrutadorViaConviteRequest request
    ) {
        var result = recrutadorService.cadastrarViaConvite(
                request.nome(),
                request.cpf(),
                request.email(),
                request.senha(),
                request.organizacaoId(),
                request.tokenConvite()
        );

        var response = new CadastrarRecrutadorViaConviteResponse(
                result.usuarioId(),
                result.nome(),
                result.email(),
                result.organizacaoId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

