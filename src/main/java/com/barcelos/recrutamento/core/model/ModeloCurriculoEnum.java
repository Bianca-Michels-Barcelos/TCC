package com.barcelos.recrutamento.core.model;

public enum ModeloCurriculoEnum {
    PROFISSIONAL("Profissional", "Formato tradicional e objetivo, ideal para empresas formais e vagas corporativas"),
    CRIATIVO("Criativo", "Formato mais visual e diferenciado, ideal para áreas criativas e design"),
    TECNICO("Técnico", "Focado em habilidades técnicas e projetos, ideal para tecnologia"),
    EXECUTIVO("Executivo", "Destaca liderança e conquistas, ideal para cargos de gestão e alta senioridade"),
    ACADEMICO("Acadêmico", "Enfatiza formação, pesquisas e publicações, adequado para posições acadêmicas");

    private final String nome;
    private final String descricao;

    ModeloCurriculoEnum(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
