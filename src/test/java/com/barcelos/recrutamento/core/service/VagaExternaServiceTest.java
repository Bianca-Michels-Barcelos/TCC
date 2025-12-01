package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.ModeloCurriculoEnum;
import com.barcelos.recrutamento.core.model.VagaExterna;
import com.barcelos.recrutamento.core.port.VagaExternaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VagaExternaServiceTest {

    @Mock
    private VagaExternaRepository repository;

    @Mock
    private VagaExternaCurriculoService curriculoService;

    @Mock
    private CurriculoAIService curriculoAIService;

    @Mock
    private CurriculoPDFService curriculoPDFService;

    @InjectMocks
    private VagaExternaService service;

    private UUID vagaExternaId;
    private UUID candidatoId;
    private VagaExterna vagaExterna;

    @BeforeEach
    void setUp() {
        vagaExternaId = UUID.randomUUID();
        candidatoId = UUID.randomUUID();
        vagaExterna = VagaExterna.rehydrate(
            vagaExternaId, "Desenvolvedor Python", "Descrição", "Requisitos",
            null, null, null, candidatoId, true, java.time.LocalDateTime.now()
        );
        ReflectionTestUtils.setField(service, "storagePathCurriculosExternos", System.getProperty("java.io.tmpdir"));
    }

    @Test
    void deveCriarVagaExternaComSucesso() {
        when(repository.save(any(VagaExterna.class))).thenReturn(vagaExterna);

        VagaExterna resultado = service.criar("Desenvolvedor", "Descrição", "Requisitos", candidatoId);

        assertThat(resultado).isNotNull();
        verify(repository).save(any(VagaExterna.class));
    }

    @Test
    void deveBuscarVagaExternaPorId() {
        when(repository.findById(vagaExternaId)).thenReturn(Optional.of(vagaExterna));

        VagaExterna resultado = service.buscar(vagaExternaId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(vagaExternaId);
        verify(repository).findById(vagaExternaId);
    }

    @Test
    void naoDeveBuscarVagaExternaInexistente() {
        when(repository.findById(vagaExternaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(vagaExternaId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vaga externa");
    }

    @Test
    void deveListarVagasPorUsuario() {
        when(repository.listByUsuario(candidatoId)).thenReturn(List.of(vagaExterna));

        List<VagaExterna> resultado = service.listarPorUsuario(candidatoId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(vagaExternaId);
        verify(repository).listByUsuario(candidatoId);
    }

    @Test
    void deveAtualizarVagaExternaComSucesso() {
        when(repository.findById(vagaExternaId)).thenReturn(Optional.of(vagaExterna));
        when(repository.save(any(VagaExterna.class))).thenAnswer(inv -> inv.getArgument(0));

        VagaExterna resultado = service.atualizar(vagaExternaId, "Novo Título", "Nova Desc", "Novos Req");

        assertThat(resultado).isNotNull();
        verify(repository).save(any(VagaExterna.class));
    }

    @Test
    void deveDesativarVagaExterna() {
        when(repository.findById(vagaExternaId)).thenReturn(Optional.of(vagaExterna));
        when(repository.save(any(VagaExterna.class))).thenAnswer(inv -> inv.getArgument(0));

        VagaExterna resultado = service.desativar(vagaExternaId);

        assertThat(resultado).isNotNull();
        verify(repository).save(any(VagaExterna.class));
    }

    @Test
    void deveDeletarVagaExterna() {
        when(repository.findById(vagaExternaId)).thenReturn(Optional.of(vagaExterna));

        service.deletar(vagaExternaId);

        verify(repository).delete(vagaExterna);
    }
}

