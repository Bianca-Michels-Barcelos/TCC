package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EnderecoEmbeddable {
    @Column(name = "end_logradouro", length = 80)
    private String logradouro;

    @Column(name = "end_numero", length = 20)
    private String numero;

    @Column(name = "end_complemento", length = 50)
    private String complemento;

    @Column(name = "end_cep", length = 10)
    private String cep;

    @Column(name = "end_cidade", length = 50)
    private String cidade;

    @Column(name = "end_uf", length = 2)
    private String uf;

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
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
