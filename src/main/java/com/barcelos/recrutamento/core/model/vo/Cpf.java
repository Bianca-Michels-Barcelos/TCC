package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;

import java.util.Objects;

public record Cpf(String value) {

    public Cpf {
        Objects.requireNonNull(value, "Cpf não pode ser nulo");
        var digits = value.replaceAll("\\D", "");
        if (digits.length() != 11) throw new InvalidInputException("Cpf inválido");
        value = digits;
    }

    @Override
    public String toString() {
        return value;
    }
}
