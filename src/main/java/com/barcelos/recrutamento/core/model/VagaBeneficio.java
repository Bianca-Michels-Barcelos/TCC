package com.barcelos.recrutamento.core.model;

import java.util.Objects;
import java.util.UUID;

public final class VagaBeneficio {
    private final UUID vagaId;
    private final UUID beneficioId;

    private VagaBeneficio(UUID vagaId, UUID beneficioId) {
        this.vagaId = Objects.requireNonNull(vagaId, "vagaId must not be null");
        this.beneficioId = Objects.requireNonNull(beneficioId, "beneficioId must not be null");
    }

    
    public static VagaBeneficio novo(UUID vagaId, UUID beneficioId) {
        return new VagaBeneficio(vagaId, beneficioId);
    }

    
    public static VagaBeneficio rehydrate(UUID vagaId, UUID beneficioId) {
        return new VagaBeneficio(vagaId, beneficioId);
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public UUID getBeneficioId() {
        return beneficioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VagaBeneficio that = (VagaBeneficio) o;
        return Objects.equals(vagaId, that.vagaId) && Objects.equals(beneficioId, that.beneficioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vagaId, beneficioId);
    }
}
