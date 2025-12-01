package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;

import java.util.Objects;

public record Sigla(String value) {
    public Sigla {
        Objects.requireNonNull(value, "Sigla não pode ser nula");
        if (!value.matches("^[A-Za-z]{2}$")) {
            throw new InvalidInputException("Sigla precisa ter dois caracteres alfabéticos");
        }
        value = value.toUpperCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
