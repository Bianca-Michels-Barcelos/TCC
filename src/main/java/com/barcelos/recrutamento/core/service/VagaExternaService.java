package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.*;
import com.barcelos.recrutamento.core.port.VagaExternaRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class VagaExternaService {

    private final VagaExternaRepository repository;
    private final VagaExternaCurriculoService curriculoService;
    private final CurriculoAIService curriculoAIService;
    private final CurriculoPDFService curriculoPDFService;

    @Value("${app.curriculos-externos.diretorio}")
    private String storagePathCurriculosExternos;

    public VagaExternaService(VagaExternaRepository repository,
                              VagaExternaCurriculoService curriculoService,
                              CurriculoAIService curriculoAIService,
                              CurriculoPDFService curriculoPDFService) {
        this.repository = repository;
        this.curriculoService = curriculoService;
        this.curriculoAIService = curriculoAIService;
        this.curriculoPDFService = curriculoPDFService;
    }

    
    @Transactional
    public VagaExterna criar(String titulo, String descricao, String requisitos, UUID candidatoUsuarioId) {
        VagaExterna vagaExterna = VagaExterna.nova(titulo, descricao, requisitos, candidatoUsuarioId);
        return repository.save(vagaExterna);
    }

    
    public VagaExterna buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga externa", id));
    }

    
    public List<VagaExterna> listarPorUsuario(UUID usuarioId) {
        return repository.listByUsuario(usuarioId);
    }

    
    @Transactional
    public VagaExterna atualizar(UUID id, String titulo, String descricao, String requisitos) {
        VagaExterna vagaExterna = buscar(id);
        VagaExterna vagaAtualizada = vagaExterna.atualizar(titulo, descricao, requisitos);
        return repository.save(vagaAtualizada);
    }

    
    @Transactional
    public VagaExterna desativar(UUID id) {
        VagaExterna vagaExterna = buscar(id);
        VagaExterna vagaDesativada = vagaExterna.desativar();
        return repository.save(vagaDesativada);
    }

    
    @Transactional
    public void deletar(UUID id) {
        VagaExterna vagaExterna = buscar(id);
        repository.delete(vagaExterna);
    }

    
    @Transactional
    public VagaExterna gerarCurriculoComIA(UUID vagaExternaId, ModeloCurriculoEnum modelo) {
        VagaExterna vagaExterna = buscar(vagaExternaId);

        String conteudo = curriculoAIService.gerarCurriculoOtimizadoParaVagaExterna(
            vagaExterna,
            modelo,
            null
        );

        String nomeArquivo = gerarNomeArquivoPDF(vagaExternaId);
        String caminhoCompleto = storagePathCurriculosExternos + "/" + nomeArquivo;
        File arquivoPDF = curriculoPDFService.gerarPDF(conteudo, modelo, caminhoCompleto);

        String caminhoRelativo = "curriculos-externos/" + nomeArquivo;
        VagaExterna vagaAtualizada = vagaExterna.comCurriculo(conteudo, caminhoRelativo, modelo);
        return repository.save(vagaAtualizada);
    }

    
    @Transactional
    public VagaExterna atualizarCurriculo(UUID vagaExternaId, String novoConteudo) {
        VagaExterna vagaExterna = buscar(vagaExternaId);

        if (vagaExterna.getModeloCurriculo() == null) {
            throw new IllegalStateException("Vaga não possui um modelo de currículo definido");
        }

        String nomeArquivo = gerarNomeArquivoPDF(vagaExternaId);
        String caminhoCompleto = storagePathCurriculosExternos + "/" + nomeArquivo;
        File arquivoPDF = curriculoPDFService.gerarPDF(novoConteudo, vagaExterna.getModeloCurriculo(), caminhoCompleto);

        String caminhoRelativo = "curriculos-externos/" + nomeArquivo;
        VagaExterna vagaAtualizada = vagaExterna.comCurriculo(novoConteudo, caminhoRelativo, vagaExterna.getModeloCurriculo());
        return repository.save(vagaAtualizada);
    }

    
    @Transactional
    public VagaExterna regenerarCurriculoComModelo(UUID vagaExternaId, ModeloCurriculoEnum novoModelo) {
        VagaExterna vagaExterna = buscar(vagaExternaId);

        if (vagaExterna.getConteudoCurriculo() == null) {
            throw new IllegalStateException("Vaga não possui conteúdo de currículo");
        }

        String nomeArquivo = gerarNomeArquivoPDF(vagaExternaId);
        String caminhoCompleto = storagePathCurriculosExternos + "/" + nomeArquivo;
        File arquivoPDF = curriculoPDFService.gerarPDF(vagaExterna.getConteudoCurriculo(), novoModelo, caminhoCompleto);

        String caminhoRelativo = "curriculos-externos/" + nomeArquivo;
        VagaExterna vagaAtualizada = vagaExterna.comCurriculo(vagaExterna.getConteudoCurriculo(), caminhoRelativo, novoModelo);
        return repository.save(vagaAtualizada);
    }

    private String gerarNomeArquivoPDF(UUID vagaExternaId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "curriculo-externo-" + vagaExternaId + "-" + timestamp + ".pdf";
    }
}
