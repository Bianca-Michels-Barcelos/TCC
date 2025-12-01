package com.barcelos.recrutamento.core.model.vo;

public record Endereco(
        String logradouro,
        String complemento,
        String numero,
        Cep cep,
        String cidade,
        Sigla uf
) {
}