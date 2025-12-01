package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EnderecoSimplesEmbeddable {

    @Column(name = "end_cidade", length = 50)
    private String cidade;

    @Column(name = "end_uf", length = 2)
    private String uf;

    public EnderecoSimplesEmbeddable() {
    }

    public EnderecoSimplesEmbeddable(String cidade, String uf) {
        this.cidade = cidade;
        this.uf = uf;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}
