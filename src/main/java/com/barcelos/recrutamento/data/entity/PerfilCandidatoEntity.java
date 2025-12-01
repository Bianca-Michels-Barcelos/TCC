package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "perfil_candidato")
public class PerfilCandidatoEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "usuario_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "perfil_usuario_fk"))
    private UsuarioEntity usuario;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Embedded
    private EnderecoEmbeddable endereco;

    @Column(nullable = false)
    private boolean ativo = true;

    public PerfilCandidatoEntity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerfilCandidatoEntity that = (PerfilCandidatoEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PerfilCandidatoEntity{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", dataNascimento=" + dataNascimento +
                ", ativo=" + ativo +
                '}';
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public EnderecoEmbeddable getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoEmbeddable endereco) {
        this.endereco = endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}