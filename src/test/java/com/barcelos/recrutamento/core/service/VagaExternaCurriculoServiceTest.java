package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VagaExternaCurriculoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @Mock
    private ExperienciaProfissionalRepository experienciaRepository;

    @Mock
    private HistoricoAcademicoRepository historicoRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private VagaExternaRepository vagaExternaRepository;

    @InjectMocks
    private VagaExternaCurriculoService service;

    private UUID vagaExternaId;
    private UUID candidatoId;
    private Usuario usuario;
    private VagaExterna vagaExterna;

    @BeforeEach
    void setUp() {
        vagaExternaId = UUID.randomUUID();
        candidatoId = UUID.randomUUID();

        usuario = Usuario.rehydrate(
            candidatoId, "João Silva", new Email("joao@example.com"),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

        vagaExterna = VagaExterna.rehydrate(
            vagaExternaId, "Desenvolvedor", "Descrição", "Requisitos",
            null, null, null, candidatoId, true, java.time.LocalDateTime.now()
        );

        ReflectionTestUtils.setField(service, "diretorioCurriculos", System.getProperty("java.io.tmpdir"));
    }

    @Test
    void deveGerarCurriculoParaVagaExterna() {
        Endereco endereco = new Endereco("Rua", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        PerfilCandidato perfil = PerfilCandidato.rehydrate(
            UUID.randomUUID(), candidatoId, LocalDate.of(1990, 1, 1), endereco, true
        );

        when(vagaExternaRepository.findById(vagaExternaId)).thenReturn(Optional.of(vagaExterna));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(usuario));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));
        when(experienciaRepository.listByUsuario(candidatoId)).thenReturn(List.of());
        when(historicoRepository.listByUsuario(candidatoId)).thenReturn(List.of());
        when(portfolioRepository.listByPerfilCandidato(any())).thenReturn(List.of());
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of());
        when(vagaExternaRepository.save(any(VagaExterna.class))).thenAnswer(inv -> inv.getArgument(0));

        service.gerarEAtualizarCurriculo(vagaExternaId);

        verify(vagaExternaRepository, timeout(2000)).save(any(VagaExterna.class));
    }
}

