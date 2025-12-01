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
class CurriculoAIServiceTest {

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
    private PortfolioRepository portfolioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CurriculoAIService service;

    private UUID candidatoId;
    private Usuario candidato;
    private PerfilCandidato perfil;
    private Vaga vaga;
    private VagaExterna vagaExterna;

    @BeforeEach
    void setUp() {
        candidatoId = UUID.randomUUID();

        candidato = Usuario.rehydrate(
            candidatoId, "João Silva", new Email("joao@example.com"),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

        Endereco endereco = new Endereco("Rua", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        perfil = PerfilCandidato.rehydrate(
            UUID.randomUUID(), candidatoId, LocalDate.of(1990, 1, 1), endereco, true
        );

        vaga = Vaga.rehydrate(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Desenvolvedor Java", "Descrição", "Requisitos",
            new BigDecimal("5000.00"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        vagaExterna = VagaExterna.rehydrate(
            UUID.randomUUID(), "Desenvolvedor Python", "Descrição vaga", "Requisitos vaga",
            null, null, null, candidatoId, true, java.time.LocalDateTime.now()
        );
    }

    @Test
    void deveRetornarTextoGenericoQuandoErroNaIA() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of());
        when(experienciaRepository.listByUsuario(candidatoId)).thenReturn(List.of());
        when(historicoAcademicoRepository.listByUsuario(candidatoId)).thenReturn(List.of());

        when(chatClient.prompt()).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> service.gerarCurriculoOtimizado(candidatoId, vaga, ModeloCurriculoEnum.PROFISSIONAL, null))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deveLancarExcecaoQuandoIAFalha() {
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of());
        when(experienciaRepository.listByUsuario(candidatoId)).thenReturn(List.of());
        when(historicoAcademicoRepository.listByUsuario(candidatoId)).thenReturn(List.of());
        when(portfolioRepository.listByPerfilCandidato(any())).thenReturn(List.of());

        when(chatClient.prompt()).thenThrow(new RuntimeException("API Error"));

        assertThatThrownBy(() -> service.gerarCurriculoOtimizado(
            candidatoId, vaga, ModeloCurriculoEnum.PROFISSIONAL, null
        ))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Erro ao gerar currículo com IA");
    }
}

