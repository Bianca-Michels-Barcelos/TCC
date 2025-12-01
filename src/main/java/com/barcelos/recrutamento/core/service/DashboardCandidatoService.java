package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.api.dto.dashboard.*;
import com.barcelos.recrutamento.core.port.VagaSalvaRepository;
import com.barcelos.recrutamento.data.spring.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardCandidatoService {

    private final CandidaturaJpaRepository candidaturaRepository;
    private final VagaSalvaRepository vagaSalvaRepository;
    private final HistoricoEtapaProcessoJpaRepository historicoRepository;
    private final ProcessoSeletivoJpaRepository processoRepository;
    private final ConviteProcessoSeletivoJpaRepository conviteRepository;
    private final VagaJpaRepository vagaRepository;

    public DashboardCandidatoService(
            CandidaturaJpaRepository candidaturaRepository,
            VagaSalvaRepository vagaSalvaRepository,
            HistoricoEtapaProcessoJpaRepository historicoRepository,
            ProcessoSeletivoJpaRepository processoRepository,
            ConviteProcessoSeletivoJpaRepository conviteRepository,
            VagaJpaRepository vagaRepository
    ) {
        this.candidaturaRepository = candidaturaRepository;
        this.vagaSalvaRepository = vagaSalvaRepository;
        this.historicoRepository = historicoRepository;
        this.processoRepository = processoRepository;
        this.conviteRepository = conviteRepository;
        this.vagaRepository = vagaRepository;
    }

    
    public DashboardCandidatoResponse gerarDashboard(UUID candidatoUsuarioId) {

        long candidaturasAtivasCount = candidaturaRepository.countActiveApplications(candidatoUsuarioId);
        long vagasSalvasCount = vagaSalvaRepository.countByUsuarioId(candidatoUsuarioId);

        long totalCandidaturas = candidaturaRepository.countByCandidato_Id(candidatoUsuarioId);
        long candidaturasPendentes = candidaturaRepository.countByCandidato_IdAndStatus(candidatoUsuarioId, "PENDENTE");
        long candidaturasRespondidas = totalCandidaturas - candidaturasPendentes;
        double taxaResposta = totalCandidaturas > 0 ? (candidaturasRespondidas * 100.0 / totalCandidaturas) : 0.0;

        Double compatibilidadeMedia = candidaturaRepository.findAverageCompatibility(candidatoUsuarioId);

        var atualizacoesRecentes = buscarAtualizacoesRecentes(candidatoUsuarioId, 10);
        var proximasEtapas = buscarProximasEtapas(candidatoUsuarioId);

        return new DashboardCandidatoResponse(
                candidaturasAtivasCount,
                vagasSalvasCount,
                taxaResposta,
                compatibilidadeMedia,
                atualizacoesRecentes,
                proximasEtapas
        );
    }

    
    public List<AtualizacaoRecenteResponse> buscarAtualizacoesRecentes(UUID candidatoUsuarioId, int limite) {
        LocalDateTime dataInicio = LocalDate.now().minusDays(7).atStartOfDay();
        List<AtualizacaoRecenteResponse> atualizacoes = new ArrayList<>();

        var mudancasEtapa = historicoRepository.findRecentByCandidato(
                candidatoUsuarioId,
                dataInicio,
                PageRequest.of(0, limite)
        );

        for (var historico : mudancasEtapa) {
            var candidatura = historico.getProcesso().getCandidatura();
            atualizacoes.add(new AtualizacaoRecenteResponse(
                    "NOVA_ETAPA",
                    candidatura.getId(),
                    candidatura.getVaga().getId(),
                    candidatura.getVaga().getTitulo(),
                    candidatura.getVaga().getOrganizacao().getNome(),
                    "Avançou para: " + historico.getEtapaNova().getNome(),
                    historico.getDataMudanca()
            ));
        }

        var candidaturasRecentes = candidaturaRepository.findByCandidato_Id(candidatoUsuarioId).stream()
                .filter(c -> c.getDataCandidatura().isAfter(dataInicio.toLocalDate().minusDays(7)))
                .sorted((a, b) -> b.getDataCandidatura().compareTo(a.getDataCandidatura()))
                .limit(5)
                .toList();

        for (var candidatura : candidaturasRecentes) {
            if (atualizacoes.size() >= limite) break;

            atualizacoes.add(new AtualizacaoRecenteResponse(
                    "MUDANCA_STATUS",
                    candidatura.getId(),
                    candidatura.getVaga().getId(),
                    candidatura.getVaga().getTitulo(),
                    candidatura.getVaga().getOrganizacao().getNome(),
                    "Candidatura enviada",
                    candidatura.getDataCandidatura().atStartOfDay()
            ));
        }

        return atualizacoes.stream()
                .sorted((a, b) -> b.dataHora().compareTo(a.dataHora()))
                .limit(limite)
                .toList();
    }

    
    public List<ProximaEtapaResponse> buscarProximasEtapas(UUID candidatoUsuarioId) {
        var processosAtivos = processoRepository.findActiveProcessesByCandidato(candidatoUsuarioId);
        var convitesPendentes = conviteRepository.findPendingByCandidato(candidatoUsuarioId);

        List<ProximaEtapaResponse> proximas = new ArrayList<>();

        for (var convite : convitesPendentes) {
            if (proximas.size() >= 10) break;

            proximas.add(new ProximaEtapaResponse(
                    null,
                    null,
                    convite.getVaga().getId(),
                    convite.getVaga().getTitulo(),
                    convite.getVaga().getOrganizacao().getNome(),
                    "Convite de Entrevista",
                    "ENTREVISTA",
                    convite.getStatus().name().equals("PENDENTE") ? "Convite pendente de resposta" : "Aguardando entrevista",
                    convite.getDataExpiracao()
            ));
        }

        for (var processo : processosAtivos) {
            if (proximas.size() >= 10) break;

            var candidatura = processo.getCandidatura();
            var etapaAtual = processo.getEtapaProcessoAtual();

            proximas.add(new ProximaEtapaResponse(
                    processo.getId(),
                    candidatura.getId(),
                    candidatura.getVaga().getId(),
                    candidatura.getVaga().getTitulo(),
                    candidatura.getVaga().getOrganizacao().getNome(),
                    etapaAtual.getNome(),
                    etapaAtual.getTipo().name(),
                    "Em andamento",
                    null
            ));
        }

        return proximas;
    }
}
