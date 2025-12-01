package com.barcelos.recrutamento.core.model;

public enum TipoEtapa {
    TRIAGEM_CURRICULO("Triagem de Currículo"),
    ENTREVISTA_TELEFONICA("Entrevista Telefônica"),
    TESTE_TECNICO("Teste Técnico"),
    ENTREVISTA_PRESENCIAL("Entrevista Presencial"),
    ENTREVISTA_ONLINE("Entrevista Online"),
    DINAMICA_GRUPO("Dinâmica de Grupo"),
    AVALIACAO_PSICOLOGICA("Avaliação Psicológica"),
    CASE_NEGOCIO("Case de Negócio"),
    PROPOSTA_SALARIAL("Proposta Salarial"),
    OUTRA("Outra");

    private final String descricao;

    TipoEtapa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
