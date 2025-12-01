package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "vaga_beneficio")
@IdClass(VagaBeneficioId.class)
public class VagaBeneficioEntity implements Serializable {
    @Id
    @Column(name = "vaga_id")
    private UUID vagaId;

    @Id
    @Column(name = "beneficio_id")
    private UUID beneficioId;

    public UUID getVagaId() {
        return vagaId;
    }

    public void setVagaId(UUID vagaId) {
        this.vagaId = vagaId;
    }

    public UUID getBeneficioId() {
        return beneficioId;
    }

    public void setBeneficioId(UUID beneficioId) {
        this.beneficioId = beneficioId;
    }
}

class VagaBeneficioId implements Serializable {
    private UUID vagaId;
    private UUID beneficioId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VagaBeneficioId that = (VagaBeneficioId) o;
        return Objects.equals(vagaId, that.vagaId) && Objects.equals(beneficioId, that.beneficioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vagaId, beneficioId);
    }
}
