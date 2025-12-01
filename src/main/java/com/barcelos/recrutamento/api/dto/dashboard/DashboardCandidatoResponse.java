package com.barcelos.recrutamento.api.dto.dashboard;

import java.util.List;

public record DashboardCandidatoResponse(
    Long candidaturasAtivasCount,
    Long vagasSalvasCount,
    Double taxaResposta,
    Double compatibilidadeMedia,
    List<AtualizacaoRecenteResponse> atualizacoesRecentes,
    List<ProximaEtapaResponse> proximasEtapas
) {
}
