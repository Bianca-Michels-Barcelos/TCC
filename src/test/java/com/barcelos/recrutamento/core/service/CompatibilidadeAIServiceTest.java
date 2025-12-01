package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import com.barcelos.recrutamento.data.entity.NivelCompetencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompatibilidadeAIServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private ExperienciaProfissionalRepository experienciaRepository;

    @Mock
    private HistoricoAcademicoRepository historicoAcademicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CompatibilidadeAIService service;

    private UUID candidatoId;
    private UUID vagaId;
    private Usuario candidato;
    private PerfilCandidato perfil;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        candidatoId = UUID.randomUUID();
        vagaId = UUID.randomUUID();

        candidato = Usuario.rehydrate(
            candidatoId, "João Silva", new Email("joao@example.com"),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

        Endereco endereco = new Endereco("Rua", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        perfil = PerfilCandidato.rehydrate(
            UUID.randomUUID(), candidatoId, LocalDate.of(1990, 1, 1), endereco, true
        );

        vaga = Vaga.rehydrate(
            vagaId, UUID.randomUUID(), UUID.randomUUID(), "Desenvolvedor Java", "Descrição vaga", "Requisitos vaga",
            new BigDecimal("5000.00"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );
    }

    @Test
    void deveCalcularCompatibilidadeComSucesso() {
        Competencia competencia = Competencia.rehydrate(
            UUID.randomUUID(), perfil.getUsuarioId(), "Java", "Linguagem", NivelCompetencia.AVANCADO, true
        );

        ExperienciaProfissional experiencia = ExperienciaProfissional.rehydrate(
            UUID.randomUUID(), candidatoId, "Desenvolvedor", "Empresa", "Descrição",
            LocalDate.now(), null, true
        );

        HistoricoAcademico historico = HistoricoAcademico.rehydrate(
            UUID.randomUUID(), candidatoId, "Ciência da Computação", "Bacharelado", "UFMG",
            LocalDate.now(), null, true
        );

        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of(competencia));
        when(experienciaRepository.listByUsuario(candidatoId)).thenReturn(List.of(experiencia));
        when(historicoAcademicoRepository.listByUsuario(candidatoId)).thenReturn(List.of(historico));

        CompatibilidadeAIService.ResultadoCompatibilidade resultado = service.calcularCompatibilidade(candidatoId, vaga);

        assertThat(resultado).isNotNull();
        assertThat(resultado.score()).isGreaterThanOrEqualTo(0);
        assertThat(resultado.score()).isLessThanOrEqualTo(100);
    }

    @Test
    void deveRetornar50QuandoErroNaIA() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of());
        when(experienciaRepository.listByUsuario(candidatoId)).thenReturn(List.of());
        when(historicoAcademicoRepository.listByUsuario(candidatoId)).thenReturn(List.of());

        when(chatClient.prompt()).thenThrow(new RuntimeException("API error"));

        CompatibilidadeAIService.ResultadoCompatibilidade resultado = service.calcularCompatibilidade(candidatoId, vaga);

        assertThat(resultado).isNotNull();
        assertThat(resultado.score()).isEqualTo(50);
        assertThat(resultado.justificativa()).contains("Erro ao processar análise com IA");
    }
}

