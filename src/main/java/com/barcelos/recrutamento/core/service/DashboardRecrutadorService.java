package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.api.dto.dashboard.*;
import com.barcelos.recrutamento.data.spring.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardRecrutadorService {

    private final VagaJpaRepository vagaRepository;
    private final CandidaturaJpaRepository candidaturaRepository;
    private final HistoricoEtapaProcessoJpaRepository historicoRepository;
    private final ConviteProcessoSeletivoJpaRepository conviteRepository;
    private final ProcessoSeletivoJpaRepository processoRepository;

    public DashboardRecrutadorService(
            VagaJpaRepository vagaRepository,
            CandidaturaJpaRepository candidaturaRepository,
            HistoricoEtapaProcessoJpaRepository historicoRepository,
            ConviteProcessoSeletivoJpaRepository conviteRepository,
            ProcessoSeletivoJpaRepository processoRepository
    ) {
        this.vagaRepository = vagaRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.historicoRepository = historicoRepository;
        this.conviteRepository = conviteRepository;
        this.processoRepository = processoRepository;
    }

    
    public DashboardRecrutadorResponse gerarDashboard(UUID recrutadorUsuarioId) {

        long vagasAtivasCount = vagaRepository.countByRecrutadorAndStatus(recrutadorUsuarioId, "ABERTA");
        long candidaturasPendentesCount = candidaturaRepository.countPendingByRecrutador(recrutadorUsuarioId);

        var todasVagas = vagaRepository.findByRecrutador_Id(recrutadorUsuarioId);
        long totalCandidatosCount = todasVagas.stream()
                .mapToLong(vaga -> candidaturaRepository.countByVaga_Id(vaga.getId()))
                .sum();

        long totalAceitos = todasVagas.stream()
                .mapToLong(vaga -> candidaturaRepository.countByVaga_IdAndStatus(vaga.getId(), "ACEITA"))
                .sum();
        long totalRejeitados = todasVagas.stream()
                .mapToLong(vaga -> candidaturaRepository.countByVaga_IdAndStatus(vaga.getId(), "REJEITADA"))
                .sum();
        long totalFinalizados = totalAceitos + totalRejeitados;
        double taxaConversao = totalFinalizados > 0 ? (totalAceitos * 100.0 / totalFinalizados) : 0.0;

        var atividadesRecentes = buscarAtividadesRecentes(recrutadorUsuarioId, 10);
        var vagasAtencao = buscarVagasAtencao(recrutadorUsuarioId);
        var entrevistasProximas = buscarEntrevistasProximas(recrutadorUsuarioId);

        return new DashboardRecrutadorResponse(
                vagasAtivasCount,
                totalCandidatosCount,
                candidaturasPendentesCount,
                taxaConversao,
                atividadesRecentes,
                vagasAtencao,
                entrevistasProximas
        );
    }

    
    public List<AtividadeRecenteResponse> buscarAtividadesRecentes(UUID recrutadorUsuarioId, int limite) {
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDateTime dataInicioTime = dataInicio.atStartOfDay();

        List<AtividadeRecenteResponse> atividades = new ArrayList<>();

        var candidaturasRecentes = candidaturaRepository.findRecentByRecrutador(recrutadorUsuarioId, dataInicio);
        for (var candidatura : candidaturasRecentes) {
            if (atividades.size() >= limite) break;

            var processoOpt = processoRepository.findByCandidaturaId(candidatura.getId());
            UUID processoId = processoOpt.map(p -> p.getId()).orElse(null);

            atividades.add(new AtividadeRecenteResponse(
                    "NOVA_CANDIDATURA",
                    candidatura.getId(),
                    processoId,
                    candidatura.getVaga().getId(),
                    candidatura.getVaga().getTitulo(),
                    candidatura.getCandidato().getNome(),
                    "Nova candidatura recebida",
                    candidatura.getDataCandidatura().atStartOfDay()
            ));
        }

        var mudancasEtapa = historicoRepository.findRecentByRecrutador(
                recrutadorUsuarioId,
                dataInicioTime,
                PageRequest.of(0, limite)
        );
        for (var historico : mudancasEtapa) {
            if (atividades.size() >= limite) break;

            var candidatura = historico.getProcesso().getCandidatura();
            atividades.add(new AtividadeRecenteResponse(
                    "MUDANCA_ETAPA",
                    candidatura.getId(),
                    historico.getProcesso().getId(),
                    candidatura.getVaga().getId(),
                    candidatura.getVaga().getTitulo(),
                    candidatura.getCandidato().getNome(),
                    "Mudou para: " + historico.getEtapaNova().getNome(),
                    historico.getDataMudanca()
            ));
        }

        return atividades.stream()
                .sorted((a, b) -> b.dataHora().compareTo(a.dataHora()))
                .limit(limite)
                .toList();
    }

    
    public List<VagaAtencaoResponse> buscarVagasAtencao(UUID recrutadorUsuarioId) {
        List<VagaAtencaoResponse> alertas = new ArrayList<>();

        var vagasAbertas = vagaRepository.findByRecrutador_Id(recrutadorUsuarioId).stream()
                .filter(vaga -> vaga.getStatus().name().equals("ABERTA"))
                .toList();

        for (var vaga : vagasAbertas) {
            long candidaturasPendentes = candidaturaRepository.countByVaga_IdAndStatus(vaga.getId(), "PENDENTE");

            if (candidaturasPendentes > 5) {
                alertas.add(new VagaAtencaoResponse(
                        vaga.getId(),
                        vaga.getTitulo(),
                        "CANDIDATURAS_PENDENTES",
                        (int) candidaturasPendentes,
                        null
                ));
            }
        }

        return alertas;
    }

    
    public List<EntrevistaProximaResponse> buscarEntrevistasProximas(UUID recrutadorUsuarioId) {
        var convites = conviteRepository.findUpcomingByRecrutador(recrutadorUsuarioId);

        return convites.stream()
                .map(convite -> new EntrevistaProximaResponse(
                        convite.getId(),
                        convite.getVaga().getId(),
                        convite.getVaga().getTitulo(),
                        convite.getCandidato().getId(),
                        convite.getCandidato().getNome(),
                        "ENTREVISTA",
                        convite.getDataEnvio(),
                        convite.getDataExpiracao(),
                        convite.getStatus().name()
                ))
                .limit(10)
                .toList();
    }
}
