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
class BuscaCandidatoServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private ExperienciaProfissionalRepository experienciaRepository;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private CompatibilidadeCacheService compatibilidadeCacheService;

    @Mock
    private CompatibilidadeCacheRepository compatibilidadeCacheRepository;

    @InjectMocks
    private BuscaCandidatoService service;

    private UUID vagaId;
    private UUID candidatoId;
    private Vaga vaga;
    private Usuario candidato;
    private PerfilCandidato perfil;
    private CompatibilidadeCache cache;

    @BeforeEach
    void setUp() {
        vagaId = UUID.randomUUID();
        candidatoId = UUID.randomUUID();

        vaga = Vaga.rehydrate(
            vagaId, UUID.randomUUID(), UUID.randomUUID(), "Desenvolvedor Java", "Descrição", "Requisitos",
            new BigDecimal("5000.00"), LocalDate.now(), StatusVaga.ABERTA, TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO, "9h às 18h", null, null, true, null
        );

        candidato = Usuario.rehydrate(
            candidatoId, "João Silva", new Email("joao@example.com"),
            new Cpf("12345678901"), "$2a$10$hash", true, true
        );

        Endereco endereco = new Endereco("Rua Teste", "100", null, new Cep("01310100"), "São Paulo", new Sigla("SP"));
        perfil = PerfilCandidato.rehydrate(
            UUID.randomUUID(), candidatoId, LocalDate.of(1990, 1, 1), endereco, true
        );

        cache = CompatibilidadeCache.rehydrate(
            UUID.randomUUID(), candidatoId, vagaId, new BigDecimal("85.5"),
            "Alta compatibilidade", LocalDate.now().atStartOfDay(), null
        );
    }

    @Test
    void deveBuscarCandidatosPorNome() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(perfilCandidatoRepository.findAll()).thenReturn(List.of(perfil));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of());
        when(experienciaRepository.listByUsuario(any())).thenReturn(List.of());
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));

        BuscaCandidatoService.ResultadoPaginado resultado = service.buscarComPaginacao(
            vagaId, "João", 0, 10
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).isNotEmpty();
        verify(vagaRepository).findById(vagaId);
    }

    @Test
    void deveBuscarTodosCandidatosQuandoConsultaVazia() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(compatibilidadeCacheRepository.findByVaga(vagaId)).thenReturn(List.of(cache));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));

        BuscaCandidatoService.ResultadoPaginado resultado = service.buscarComPaginacao(
            vagaId, null, 0, 10
        );

        assertThat(resultado).isNotNull();
        verify(compatibilidadeCacheRepository).findByVaga(vagaId);
    }

    @Test
    void naoDeveBuscarQuandoVagaNaoExiste() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarComPaginacao(vagaId, "termo", 0, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Vaga não encontrada");
    }

    @Test
    void devePaginarResultadosCorretamente() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(compatibilidadeCacheRepository.findByVaga(vagaId)).thenReturn(List.of(cache));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));

        BuscaCandidatoService.ResultadoPaginado resultado = service.buscarComPaginacao(
            vagaId, null, 0, 5
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.size()).isEqualTo(5);
        assertThat(resultado.currentPage()).isEqualTo(0);
    }

    @Test
    void deveFiltrarPorCompetencias() {
        Competencia competencia = Competencia.rehydrate(
            UUID.randomUUID(), perfil.getUsuarioId(), "Java", "Linguagem", NivelCompetencia.AVANCADO, true
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(perfilCandidatoRepository.findAll()).thenReturn(List.of(perfil));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of(competencia));
        when(experienciaRepository.listByUsuario(any())).thenReturn(List.of());
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));

        BuscaCandidatoService.ResultadoPaginado resultado = service.buscarComPaginacao(
            vagaId, "Java", 0, 10
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).isNotEmpty();
    }

    @Test
    void deveFiltrarPorExperiencia() {
        ExperienciaProfissional experiencia = ExperienciaProfissional.rehydrate(
            UUID.randomUUID(), candidatoId, "Desenvolvedor Java", "Empresa", "Descrição",
            LocalDate.now(), null, true
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(perfilCandidatoRepository.findAll()).thenReturn(List.of(perfil));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(competenciaRepository.listByPerfilCandidato(any())).thenReturn(List.of());
        when(experienciaRepository.listByUsuario(any())).thenReturn(List.of(experiencia));
        when(compatibilidadeCacheService.obterDoCache(candidatoId, vagaId))
            .thenReturn(Optional.of(cache));

        BuscaCandidatoService.ResultadoPaginado resultado = service.buscarComPaginacao(
            vagaId, "Java", 0, 10
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).isNotEmpty();
    }

    @Test
    void deveRetornarListaVaziaQuandoNenhumCandidatoCorresponde() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(perfilCandidatoRepository.findAll()).thenReturn(List.of());

        BuscaCandidatoService.ResultadoPaginado resultado = service.buscarComPaginacao(
            vagaId, "NonExistentKeyword", 0, 10
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).isEmpty();
    }

    @Test
    void deveOrdenarResultadosPorScore() {
        Usuario candidato2 = Usuario.rehydrate(
            UUID.randomUUID(), "Maria Santos", new Email("maria@example.com"),
            new Cpf("98765432109"), "$2a$10$hash", true, true
        );
        
        PerfilCandidato perfil2 = PerfilCandidato.rehydrate(
            UUID.randomUUID(), candidato2.getId(), LocalDate.of(1992, 1, 1),
            perfil.getEndereco(), true
        );

        CompatibilidadeCache cache2 = CompatibilidadeCache.rehydrate(
            UUID.randomUUID(), candidato2.getId(), vagaId, new BigDecimal("90.0"),
            "Altíssima compatibilidade", LocalDate.now().atStartOfDay(), null
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(compatibilidadeCacheRepository.findByVaga(vagaId)).thenReturn(List.of(cache, cache2));
        when(usuarioRepository.findById(candidatoId)).thenReturn(Optional.of(candidato));
        when(usuarioRepository.findById(candidato2.getId())).thenReturn(Optional.of(candidato2));
        when(perfilCandidatoRepository.findByUsuarioId(candidatoId)).thenReturn(Optional.of(perfil));
        when(perfilCandidatoRepository.findByUsuarioId(candidato2.getId())).thenReturn(Optional.of(perfil2));

        BuscaCandidatoService.ResultadoPaginado resultado = service.buscarComPaginacao(
            vagaId, null, 0, 10
        );

        assertThat(resultado.content()).hasSize(2);
        assertThat(resultado.content().get(0).score()).isGreaterThanOrEqualTo(resultado.content().get(1).score());
    }
}

