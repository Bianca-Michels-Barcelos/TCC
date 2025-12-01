package com.barcelos.recrutamento.core.model.vo;

import java.util.Objects;

public record Username(String value) {
    public Username {
        Objects.requireNonNull(value, "Username nāo pode ser nulo");
        if (value.isBlank() || value.length() > 50) {
            throw new IllegalArgumentException("Username inválido");
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
