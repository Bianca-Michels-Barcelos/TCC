import api from '../lib/api';

export interface AtividadeRecente {
  tipo: string;
  candidaturaId: string;
  processoId: string | null;
  vagaId: string;
  tituloVaga: string;
  nomeCandidato: string;
  descricao: string;
  dataHora: string;
}

export interface VagaAtencao {
  vagaId: string;
  titulo: string;
  motivoAlerta: string;
  quantidadePendente: number;
  diasSemAtualizacao: number | null;
}

export interface EntrevistaProxima {
  conviteId: string;
  vagaId: string;
  tituloVaga: string;
  candidatoId: string;
  nomeCandidato: string;
  tipoEntrevista: string;
  dataEnvio: string;
  dataExpiracao: string;
  status: string;
}

export interface DashboardRecrutador {
  vagasAtivasCount: number;
  totalCandidatosCount: number;
  candidaturasPendentesCount: number;
  taxaConversao: number;
  atividadesRecentes: AtividadeRecente[];
  vagasAtencao: VagaAtencao[];
  entrevistasProximas: EntrevistaProxima[];
}

export interface AtualizacaoRecente {
  tipo: string;
  candidaturaId: string;
  vagaId: string;
  tituloVaga: string;
  nomeOrganizacao: string;
  descricao: string;
  dataHora: string;
}

export interface ProximaEtapa {
  processoId: string | null;
  candidaturaId: string | null;
  vagaId: string;
  tituloVaga: string;
  nomeOrganizacao: string;
  etapaAtual: string;
  tipoEtapa: string;
  acao: string;
  prazo: string | null;
}

export interface DashboardCandidato {
  candidaturasAtivasCount: number;
  vagasSalvasCount: number;
  taxaResposta: number;
  compatibilidadeMedia: number | null;
  atualizacoesRecentes: AtualizacaoRecente[];
  proximasEtapas: ProximaEtapa[];
}

class DashboardService {

  async getDashboardRecrutador(organizacaoId: string): Promise<DashboardRecrutador> {
    const response = await api.get<DashboardRecrutador>(`/api/dashboard/recrutador/${organizacaoId}`);
    return response.data;
  }

  async getDashboardCandidato(): Promise<DashboardCandidato> {
    const response = await api.get<DashboardCandidato>('/api/dashboard/candidato');
    return response.data;
  }
}

export default new DashboardService();
