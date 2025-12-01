package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;

import java.util.Objects;

public record Cep(String value) {
    public Cep {
        Objects.requireNonNull(value, "CEP não pode ser nulo");
        var digits = value.replaceAll("\\D", "");
        if (digits.length() != 8) throw new InvalidInputException("CEP inválido");
        value = digits;
    }

    @Override
    public String toString() {
        return value;
    }
}
