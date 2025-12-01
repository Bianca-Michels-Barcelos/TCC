import api from '@/lib/api';

export interface AvaliacaoOrganizacao {
  id: string;
  processoId: string;
  candidatoUsuarioId: string;
  organizacaoId: string;
  nota: number;
  comentario: string;
  criadoEm: string;
  atualizadoEm: string;
}

export interface CriarAvaliacaoRequest {
  processoId: string;
  nota: number;
  comentario: string;
}

export interface EstatisticasOrganizacao {
  notaMedia: number;
  totalAvaliacoes: number;
}

export const avaliacaoOrganizacaoService = {
  async criar(data: CriarAvaliacaoRequest): Promise<AvaliacaoOrganizacao> {
    const response = await api.post('/avaliacoes-organizacao', data);
    return response.data;
  },

  async buscarPorProcesso(processoId: string): Promise<AvaliacaoOrganizacao | null> {
    try {
      const response = await api.get(`/avaliacoes-organizacao/processo/${processoId}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async listarPorOrganizacao(organizacaoId: string): Promise<AvaliacaoOrganizacao[]> {
    const response = await api.get(`/avaliacoes-organizacao/organizacao/${organizacaoId}`);
    return response.data;
  },

  async buscarEstatisticas(organizacaoId: string): Promise<EstatisticasOrganizacao> {
    const response = await api.get(`/avaliacoes-organizacao/organizacao/${organizacaoId}/estatisticas`);
    return response.data;
  },
};

