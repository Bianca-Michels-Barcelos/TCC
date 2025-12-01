package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.model.vo.*;
import com.barcelos.recrutamento.core.port.EtapaProcessoRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtapaProcessoServiceTest {

    @Mock
    private EtapaProcessoRepository repository;

    @Mock
    private VagaRepository vagaRepository;

    @InjectMocks
    private EtapaProcessoService service;

    private UUID vagaId;
    private UUID etapaId;
    private Vaga vaga;
    private EtapaProcesso etapa;

    @BeforeEach
    void setUp() {
        vagaId = UUID.randomUUID();
        etapaId = UUID.randomUUID();

        vaga = Vaga.rehydrate(
            vagaId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Desenvolvedor Java",
            "Descrição",
            "Requisitos",
            new BigDecimal("5000.00"),
            LocalDate.now(),
            StatusVaga.ABERTA,
            TipoContrato.CLT,
            ModalidadeTrabalho.REMOTO,
            "9h às 18h",
            null,
            new EnderecoSimples("São Paulo", new Sigla("SP")),
            true,
            null
        );

        etapa = EtapaProcesso.rehydrate(
            etapaId,
            vagaId,
            "Triagem",
            "Primeira etapa",
            TipoEtapa.TRIAGEM_CURRICULO,
            1,
            StatusEtapa.EM_ANDAMENTO,
            null,
            null,
            LocalDateTime.now()
        );
    }

    @Test
    void deveCriarEtapaComSucesso() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(repository.save(any(EtapaProcesso.class))).thenReturn(etapa);

        EtapaProcesso resultado = service.criar(
            vagaId,
            "Triagem",
            "Primeira etapa",
            TipoEtapa.TRIAGEM_CURRICULO,
            1,
            null,
            null
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Triagem");
        verify(vagaRepository).findById(vagaId);
        verify(repository).save(any(EtapaProcesso.class));
    }

    @Test
    void naoDeveCriarEtapaQuandoVagaNaoExiste() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(
            vagaId, "Triagem", "Desc", TipoEtapa.TRIAGEM_CURRICULO, 1, null, null
        ))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Vaga não encontrada");

        verify(repository, never()).save(any());
    }

    @Test
    void deveCriarEtapaComOrdemAutomaticaQuandoNaoInformada() {
        EtapaProcesso etapa1 = EtapaProcesso.rehydrate(
            UUID.randomUUID(), vagaId, "Etapa 1", "Desc", TipoEtapa.TRIAGEM_CURRICULO, 1,
            StatusEtapa.EM_ANDAMENTO, null, null, LocalDateTime.now()
        );

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(repository.findByVagaId(vagaId)).thenReturn(List.of(etapa1));
        when(repository.save(any(EtapaProcesso.class))).thenReturn(etapa);

        EtapaProcesso resultado = service.criar(
            vagaId, "Etapa 2", "Segunda etapa", TipoEtapa.ENTREVISTA_PRESENCIAL, null, null, null
        );

        assertThat(resultado).isNotNull();
        verify(repository).save(argThat(e -> e.getOrdem() == 2));
    }

    @Test
    void deveListarEtapasPorVaga() {
        when(repository.findByVagaId(vagaId)).thenReturn(List.of(etapa));

        List<EtapaProcesso> resultado = service.listarPorVaga(vagaId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(etapaId);
        verify(repository).findByVagaId(vagaId);
    }

    @Test
    void deveRemoverEtapa() {
        when(repository.findById(etapaId)).thenReturn(Optional.of(etapa));

        service.remover(etapaId);

        verify(repository).deleteById(etapaId);
    }

    @Test
    void naoDeveRemoverEtapaInexistente() {
        when(repository.findById(etapaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(etapaId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Etapa não encontrada");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void deveCriarPrimeiraEtapaComOrdem1() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(repository.findByVagaId(vagaId)).thenReturn(List.of());
        when(repository.save(any(EtapaProcesso.class))).thenReturn(etapa);

        EtapaProcesso resultado = service.criar(
            vagaId, "Primeira Etapa", "Desc", TipoEtapa.TRIAGEM_CURRICULO, null, null, null
        );

        assertThat(resultado).isNotNull();
        verify(repository).save(argThat(e -> e.getOrdem() == 1));
    }

    @Test
    void deveCriarEtapaComDataInicioEFim() {
        LocalDateTime dataInicio = LocalDateTime.now();
        LocalDateTime dataFim = LocalDateTime.now().plusDays(7);

        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        when(repository.save(any(EtapaProcesso.class))).thenReturn(etapa);

        EtapaProcesso resultado = service.criar(
            vagaId, "Etapa com prazo", "Desc", TipoEtapa.ENTREVISTA_PRESENCIAL, 1, dataInicio, dataFim
        );

        assertThat(resultado).isNotNull();
        verify(repository).save(any(EtapaProcesso.class));
    }
}

