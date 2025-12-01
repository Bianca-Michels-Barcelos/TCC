package com.barcelos.recrutamento.core.model.vo;

import com.barcelos.recrutamento.core.exception.InvalidInputException;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern RX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        Objects.requireNonNull(value, "Email não pode ser nulo");
        if (!RX.matcher(value).matches()) {
            throw new InvalidInputException("Email inválido");
        }
        value = value.toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
