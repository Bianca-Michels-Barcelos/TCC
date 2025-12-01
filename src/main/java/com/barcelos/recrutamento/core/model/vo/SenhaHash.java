package com.barcelos.recrutamento.core.model.vo;

import java.util.Objects;

public record SenhaHash(String value) {
    public SenhaHash {
        Objects.requireNonNull(value, "Senha hash não pode ser nula");
        if (value.isBlank()) throw new IllegalArgumentException("Senha hash inválida");
    }

    @Override
    public String toString() {
        return value;
    }
}
