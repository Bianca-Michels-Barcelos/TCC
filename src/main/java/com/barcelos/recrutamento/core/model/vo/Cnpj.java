package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;

import java.util.Objects;

public record Cnpj(String value) {

    public Cnpj {
        Objects.requireNonNull(value, "CNPJ não pode ser nulo");
        var digits = value.replaceAll("\\D", "");
        if (digits.length() != 14) throw new InvalidInputException("CNPJ inválido");
        value = digits;
    }

    @Override
    public String toString() {
        return value;
    }
}
