package com.barcelos.recrutamento.api.dto.dashboard;

import java.util.List;

public record DashboardRecrutadorResponse(
    Long vagasAtivasCount,
    Long totalCandidatosCount,
    Long candidaturasPendentesCount,
    Double taxaConversao,
    List<AtividadeRecenteResponse> atividadesRecentes,
    List<VagaAtencaoResponse> vagasAtencao,
    List<EntrevistaProximaResponse> entrevistasProximas
) {
}
