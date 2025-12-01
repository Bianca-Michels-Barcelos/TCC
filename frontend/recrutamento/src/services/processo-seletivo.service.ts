import api from '@/lib/api';

export interface EtapaProcesso {
  id: string;
  vagaId: string;
  nome: string;
  descricao: string;
  tipo: string;
  ordem: number;
  dataInicio: string;
  dataFim: string;
  status: 'PENDENTE' | 'EM_ANDAMENTO' | 'CONCLUIDA' | 'CANCELADA';
}

export interface ProcessoSeletivo {
  id: string;
  candidaturaId: string;
  etapaProcessoAtualId: string;
  dataInicio: string;
  dataFim: string | null;
  dataUltimaMudanca: string;
}

export interface Candidatura {
  id: string;
  vagaId: string;
  candidatoUsuarioId: string;
  dataCandidatura: string;
  status: string;
}

export interface HistoricoEtapaProcesso {
  id: string;
  processoSeletivoId: string;
  etapaProcessoId: string;
  etapaProcessoNome: string;
  usuarioResponsavelId: string;
  dataMovimentacao: string;
  feedback: string | null;
  acao: string;
}

export interface AvaliacaoCandidato {
  id: string;
  processoSeletivoId: string;
  avaliadorUsuarioId: string;
  nota: number;
  comentario: string;
  dataAvaliacao: string;
}

export interface ProcessoSeletivoComCandidato {
  processoId: string;
  candidaturaId: string;
  dataInicio: string;
  dataFim: string | null;
  dataUltimaMudanca: string;

  etapaAtualId: string;
  etapaAtualNome: string;
  etapaAtualDescricao: string;
  etapaAtualOrdem: number;
  etapaAtualStatus: string;

  candidatoUsuarioId: string;
  candidatoNome: string;
  candidatoEmail: string;

  statusCandidatura: string;
  dataCandidatura: string;
  compatibilidade: number | null;
  arquivoCurriculo: string | null;

  vagaId: string;
  vagaTitulo: string;
}

export const processoSeletivoService = {
  async listarPorVaga(vagaId: string): Promise<ProcessoSeletivo[]> {
    const response = await api.get(`/vagas/${vagaId}/processos-seletivos`);
    return response.data;
  },

  async listarComCandidatosPorVaga(vagaId: string): Promise<ProcessoSeletivoComCandidato[]> {
    const response = await api.get(`/vagas/${vagaId}/processos-seletivos/com-candidatos`);
    return response.data;
  },

  async buscarComCandidatoPorId(processoId: string): Promise<ProcessoSeletivoComCandidato> {
    const response = await api.get(`/processos-seletivos/${processoId}/com-candidato`);
    return response.data;
  },

  async buscarPorCandidatura(candidaturaId: string): Promise<{ processo: ProcessoSeletivo; vagaId: string }> {
    const response = await api.get(`/processos-seletivos/candidatura/${candidaturaId}`);
    return response.data;
  },

  async buscarHistorico(processoId: string): Promise<HistoricoEtapaProcesso[]> {
    const response = await api.get(`/processos-seletivos/${processoId}/historico`);
    return response.data;
  },

  async avancarParaProximaEtapa(processoId: string, feedback?: string): Promise<ProcessoSeletivo> {
    const response = await api.post(`/processos-seletivos/${processoId}/avancar`, { feedback });
    return response.data;
  },

  async avancarParaEtapa(processoId: string, etapaId: string, feedback?: string): Promise<ProcessoSeletivo> {
    const response = await api.post(`/processos-seletivos/${processoId}/avancar-para-etapa`, {
      etapaId,
      feedback,
    });
    return response.data;
  },

  async retornarParaEtapa(processoId: string, etapaId: string, feedback?: string): Promise<ProcessoSeletivo> {
    const response = await api.post(`/processos-seletivos/${processoId}/retornar-para-etapa`, {
      etapaId,
      feedback,
    });
    return response.data;
  },

  async finalizar(processoId: string, feedback?: string): Promise<ProcessoSeletivo> {
    const response = await api.post(`/processos-seletivos/${processoId}/finalizar`, { feedback });
    return response.data;
  },

  async reprovar(processoId: string, feedback: string): Promise<ProcessoSeletivo> {
    const response = await api.post(`/processos-seletivos/${processoId}/reprovar`, { feedback });
    return response.data;
  },
};

export const etapaProcessoService = {
  async listarPorVaga(vagaId: string): Promise<EtapaProcesso[]> {
    const response = await api.get(`/vagas/${vagaId}/etapas`);
    return response.data;
  },
};

export const candidaturaService = {
  async buscarPorId(candidaturaId: string): Promise<Candidatura> {
    const response = await api.get(`/candidaturas/${candidaturaId}`);
    return response.data;
  },
};
