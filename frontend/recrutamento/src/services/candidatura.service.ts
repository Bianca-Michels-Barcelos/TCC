import api from '@/lib/api';

export type StatusCandidatura = 'PENDENTE' | 'ACEITA' | 'REJEITADA' | 'DESISTENTE' | 'EM_PROCESSO' | 'FINALIZADA';

export interface Candidatura {
  id: string;
  vagaId: string;
  candidatoUsuarioId: string;
  status: StatusCandidatura;
  dataCandidatura: string;
  arquivoCurriculo?: string;
  compatibilidade?: number;
}

export interface CandidaturaComDetalhes {
  id: string;
  vagaId: string;
  vaga: {
    titulo: string;
    descricao: string;
    modalidade: string;
    salario?: number;
    organizacao: {
      nome: string;
    };
    endereco?: {
      cidade: string;
      uf: string;
    };
  };
  status: StatusCandidatura;
  dataCandidatura: string;
  etapaAtual: string | null;
  compatibilidade?: number;
}

export const candidaturaService = {
  async listarMinhasCandidaturas(): Promise<CandidaturaComDetalhes[]> {
    try {
      const response = await api.get('/candidaturas/minhas');
      return response.data;
    } catch (error) {
      console.error('Error fetching candidaturas:', error);
      throw error;
    }
  },

  async criar(vagaId: string, candidatoUsuarioId: string): Promise<Candidatura> {
    const response = await api.post(`/vagas/${vagaId}/candidaturas`, {
      candidatoUsuarioId,
    });
    return response.data;
  },

  async aceitar(candidaturaId: string): Promise<Candidatura> {
    const response = await api.patch(`/candidaturas/${candidaturaId}/aceitar`);
    return response.data;
  },

  async rejeitar(candidaturaId: string): Promise<Candidatura> {
    const response = await api.patch(`/candidaturas/${candidaturaId}/rejeitar`);
    return response.data;
  },
};
