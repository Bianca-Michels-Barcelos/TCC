package com.barcelos.recrutamento.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrarOrganizacaoRequest(
        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos numéricos")
        String cnpj,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "Logradouro é obrigatório")
        String logradouro,

        @NotBlank(message = "Número é obrigatório")
        String numero,

        String complemento,

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos")
        String cep,

        @NotBlank(message = "Cidade é obrigatória")
        String cidade,

        @NotBlank(message = "UF é obrigatória")
        @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
        String uf,

        @Valid
        AdminRecruiter adminRecruiter
) {
        public record AdminRecruiter(
                @NotBlank(message = "Nome do recrutador é obrigatório")
                String nome,

                @NotBlank(message = "CPF é obrigatório")
                @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos")
                String cpf,

                @NotBlank(message = "E-mail é obrigatório")
                @Email(message = "E-mail inválido")
                String email,

                @NotBlank(message = "Senha é obrigatória")
                @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
                String senha
        ) { }
}