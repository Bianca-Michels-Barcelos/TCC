package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "vaga_salva", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vaga_salva_vaga_usuario", columnNames = {"vaga_id", "usuario_id"})
})
public class VagaSalvaEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private VagaEntity vaga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "salva_em", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime salvaEm;

    @PrePersist
    protected void onCreate() {
        if (salvaEm == null) {
            salvaEm = LocalDateTime.now();
        }
    }

    public VagaSalvaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public VagaEntity getVaga() {
        return vaga;
    }

    public void setVaga(VagaEntity vaga) {
        this.vaga = vaga;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getSalvaEm() {
        return salvaEm;
    }

    public void setSalvaEm(LocalDateTime salvaEm) {
        this.salvaEm = salvaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VagaSalvaEntity that = (VagaSalvaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VagaSalvaEntity{" +
                "id=" + id +
                ", vaga=" + (vaga != null ? vaga.getId() : null) +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", salvaEm=" + salvaEm +
                '}';
    }
}
