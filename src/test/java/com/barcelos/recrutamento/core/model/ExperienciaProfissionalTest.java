package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ExperienciaProfissionalTest {

    @Test
    void deveCriarNovaExperienciaComAtributosPadrao() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String cargo = "Desenvolvedor Java";
        String empresa = "Tech Company";
        String descricao = "Desenvolvimento de sistemas";
        LocalDate dataInicio = LocalDate.of(2020, 1, 1);
        LocalDate dataFim = LocalDate.of(2023, 12, 31);

        ExperienciaProfissional experiencia = ExperienciaProfissional.novo(id, usuarioId, cargo, 
                empresa, descricao, dataInicio, dataFim);

        assertThat(experiencia).isNotNull();
        assertThat(experiencia.getId()).isEqualTo(id);
        assertThat(experiencia.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(experiencia.getCargo()).isEqualTo(cargo);
        assertThat(experiencia.getEmpresa()).isEqualTo(empresa);
        assertThat(experiencia.getDescricao()).isEqualTo(descricao);
        assertThat(experiencia.getDataInicio()).isEqualTo(dataInicio);
        assertThat(experiencia.getDataFim()).isEqualTo(dataFim);
        assertThat(experiencia.isAtivo()).isTrue();
    }

    @Test
    void deveCriarExperienciaSemDataFim() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2023, 1, 1);

        ExperienciaProfissional experiencia = ExperienciaProfissional.novo(id, usuarioId, 
                "Desenvolvedor", "Empresa", "Descrição", dataInicio, null);

        assertThat(experiencia.getDataFim()).isNull();
    }

    @Test
    void deveRehydratarExperienciaExistente() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2020, 1, 1);
        LocalDate dataFim = LocalDate.of(2023, 12, 31);

        ExperienciaProfissional experiencia = ExperienciaProfissional.rehydrate(id, usuarioId, 
                "Analista", "Empresa X", "Análise de dados", dataInicio, dataFim, false);

        assertThat(experiencia.getId()).isEqualTo(id);
        assertThat(experiencia.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarCargoMantendoImutabilidade() {
        ExperienciaProfissional original = criarExperienciaPadrao();
        
        ExperienciaProfissional atualizada = original.comCargo("Desenvolvedor Sênior");

        assertThat(original.getCargo()).isEqualTo("Desenvolvedor Java");
        assertThat(atualizada.getCargo()).isEqualTo("Desenvolvedor Sênior");
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarEmpresaMantendoImutabilidade() {
        ExperienciaProfissional original = criarExperienciaPadrao();
        
        ExperienciaProfissional atualizada = original.comEmpresa("Nova Empresa");

        assertThat(original.getEmpresa()).isEqualTo("Tech Company");
        assertThat(atualizada.getEmpresa()).isEqualTo("Nova Empresa");
    }

    @Test
    void deveAtualizarDescricaoMantendoImutabilidade() {
        ExperienciaProfissional original = criarExperienciaPadrao();
        
        ExperienciaProfissional atualizada = original.comDescricao("Nova descrição");

        assertThat(original.getDescricao()).isEqualTo("Desenvolvimento de sistemas");
        assertThat(atualizada.getDescricao()).isEqualTo("Nova descrição");
    }

    @Test
    void deveAtualizarDataInicioMantendoImutabilidade() {
        ExperienciaProfissional original = criarExperienciaPadrao();
        LocalDate novaData = LocalDate.of(2021, 1, 1);
        
        ExperienciaProfissional atualizada = original.comDataInicio(novaData);

        assertThat(original.getDataInicio()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(atualizada.getDataInicio()).isEqualTo(novaData);
    }

    @Test
    void deveAtualizarDataFimMantendoImutabilidade() {
        ExperienciaProfissional original = criarExperienciaPadrao();
        LocalDate novaData = LocalDate.of(2024, 6, 30);
        
        ExperienciaProfissional atualizada = original.comDataFim(novaData);

        assertThat(original.getDataFim()).isEqualTo(LocalDate.of(2023, 12, 31));
        assertThat(atualizada.getDataFim()).isEqualTo(novaData);
    }

    @Test
    void deveAtivarExperiencia() {
        ExperienciaProfissional experiencia = criarExperienciaPadrao().desativar();
        
        ExperienciaProfissional ativada = experiencia.ativar();

        assertThat(experiencia.isAtivo()).isFalse();
        assertThat(ativada.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeExperienciaJaAtiva() {
        ExperienciaProfissional experiencia = criarExperienciaPadrao();
        
        ExperienciaProfissional resultado = experiencia.ativar();

        assertThat(resultado).isSameAs(experiencia);
    }

    @Test
    void deveDesativarExperiencia() {
        ExperienciaProfissional experiencia = criarExperienciaPadrao();
        
        ExperienciaProfissional desativada = experiencia.desativar();

        assertThat(experiencia.isAtivo()).isTrue();
        assertThat(desativada.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeExperienciaJaInativa() {
        ExperienciaProfissional experiencia = criarExperienciaPadrao().desativar();
        
        ExperienciaProfissional resultado = experiencia.desativar();

        assertThat(resultado).isSameAs(experiencia);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2020, 1, 1);

        assertThatThrownBy(() -> ExperienciaProfissional.novo(null, usuarioId, "Cargo", 
                "Empresa", "Descrição", dataInicio, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, null, "Cargo", 
                "Empresa", "Descrição", dataInicio, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, null, 
                "Empresa", "Descrição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cargo must not be blank");

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "   ", 
                "Empresa", "Descrição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cargo must not be blank");

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "Cargo", 
                null, "Descrição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empresa must not be blank");

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "Cargo", 
                "   ", "Descrição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empresa must not be blank");

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "Cargo", 
                "Empresa", null, dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "Cargo", 
                "Empresa", "   ", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "Cargo", 
                "Empresa", "Descrição", null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveValidarTamanhoMaximoDoCargo() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String cargoLongo = "a".repeat(81);
        LocalDate dataInicio = LocalDate.of(2020, 1, 1);

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, cargoLongo, 
                "Empresa", "Descrição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cargo max 80 chars");
    }

    @Test
    void deveValidarTamanhoMaximoDaEmpresa() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String empresaLonga = "a".repeat(81);
        LocalDate dataInicio = LocalDate.of(2020, 1, 1);

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "Cargo", 
                empresaLonga, "Descrição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empresa max 80 chars");
    }

    @Test
    void deveValidarDataFimPosteriorADataInicio() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2023, 1, 1);
        LocalDate dataFim = LocalDate.of(2022, 1, 1);

        assertThatThrownBy(() -> ExperienciaProfissional.novo(id, usuarioId, "Cargo", 
                "Empresa", "Descrição", dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dataFim must be after dataInicio");
    }

    private ExperienciaProfissional criarExperienciaPadrao() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2020, 1, 1);
        LocalDate dataFim = LocalDate.of(2023, 12, 31);
        return ExperienciaProfissional.novo(id, usuarioId, "Desenvolvedor Java", 
                "Tech Company", "Desenvolvimento de sistemas", dataInicio, dataFim);
    }
}

