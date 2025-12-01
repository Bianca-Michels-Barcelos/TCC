package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "avaliacao_organizacao", uniqueConstraints = {
        @UniqueConstraint(name = "uk_avaliacao_processo_candidato", columnNames = {"processo_id", "candidato_usuario_id"})
})
public class AvaliacaoOrganizacaoEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false, unique = true)
    private ProcessoSeletivoEntity processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_usuario_id", nullable = false)
    private UsuarioEntity candidato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private OrganizacaoEntity organizacao;

    @Column(nullable = false)
    private Integer nota;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comentario;

    public AvaliacaoOrganizacaoEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProcessoSeletivoEntity getProcesso() {
        return processo;
    }

    public void setProcesso(ProcessoSeletivoEntity processo) {
        this.processo = processo;
    }

    public UsuarioEntity getCandidato() {
        return candidato;
    }

    public void setCandidato(UsuarioEntity candidato) {
        this.candidato = candidato;
    }

    public OrganizacaoEntity getOrganizacao() {
        return organizacao;
    }

    public void setOrganizacao(OrganizacaoEntity organizacao) {
        this.organizacao = organizacao;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvaliacaoOrganizacaoEntity that = (AvaliacaoOrganizacaoEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AvaliacaoOrganizacaoEntity{" +
                "id=" + id +
                ", processo=" + (processo != null ? processo.getId() : null) +
                ", candidato=" + (candidato != null ? candidato.getId() : null) +
                ", organizacao=" + (organizacao != null ? organizacao.getId() : null) +
                ", nota=" + nota +
                '}';
    }
}
