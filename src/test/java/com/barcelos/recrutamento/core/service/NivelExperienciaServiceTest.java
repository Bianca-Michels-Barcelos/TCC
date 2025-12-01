package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.NivelExperiencia;
import com.barcelos.recrutamento.core.port.NivelExperienciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NivelExperienciaServiceTest {

    @Mock
    private NivelExperienciaRepository repository;

    @InjectMocks
    private NivelExperienciaService service;

    private UUID organizacaoId;
    private UUID nivelId;
    private NivelExperiencia nivel;

    @BeforeEach
    void setUp() {
        organizacaoId = UUID.randomUUID();
        nivelId = UUID.randomUUID();
        nivel = NivelExperiencia.rehydrate(nivelId, organizacaoId, "Senior", true);
    }

    @Test
    void deveCriarNivelExperienciaComSucesso() {
        when(repository.save(any(NivelExperiencia.class))).thenReturn(nivel);

        NivelExperiencia resultado = service.criar(organizacaoId, "Senior");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDescricao()).isEqualTo("Senior");
        verify(repository).save(any(NivelExperiencia.class));
    }

    @Test
    void deveBuscarNivelPorId() {
        when(repository.findById(nivelId)).thenReturn(Optional.of(nivel));

        NivelExperiencia resultado = service.buscar(nivelId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(nivelId);
        verify(repository).findById(nivelId);
    }

    @Test
    void naoDeveBuscarNivelInexistente() {
        when(repository.findById(nivelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(nivelId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Nível de experiência");
    }

    @Test
    void deveListarNiveisPorOrganizacao() {
        when(repository.listByOrganizacao(organizacaoId)).thenReturn(List.of(nivel));

        List<NivelExperiencia> resultado = service.listarPorOrganizacao(organizacaoId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(nivelId);
        verify(repository).listByOrganizacao(organizacaoId);
    }

    @Test
    void deveAtualizarNivelComSucesso() {
        when(repository.save(any(NivelExperiencia.class))).thenAnswer(inv -> inv.getArgument(0));

        NivelExperiencia resultado = service.atualizar(nivel, "Pleno");

        assertThat(resultado).isNotNull();
        verify(repository).save(any(NivelExperiencia.class));
    }

    @Test
    void deveDeletarNivelComSucesso() {
        when(repository.findById(nivelId)).thenReturn(Optional.of(nivel));

        service.deletar(nivelId);

        verify(repository).deleteById(nivelId);
    }
}

