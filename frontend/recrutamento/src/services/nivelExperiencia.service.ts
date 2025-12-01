import api from "@/lib/api";

export interface NivelExperiencia {
  id: string;
  organizacaoId: string;
  descricao: string;
  ativo: boolean;
}

export const nivelExperienciaService = {
  async listarPorOrganizacao(organizacaoId: string): Promise<NivelExperiencia[]> {
    const response = await api.get(`/organizacoes/${organizacaoId}/niveis-experiencia`);
    return response.data;
  },

  async buscar(organizacaoId: string, nivelExperienciaId: string): Promise<NivelExperiencia> {
    const response = await api.get(`/organizacoes/${organizacaoId}/niveis-experiencia/${nivelExperienciaId}`);
    return response.data;
  },

  async criar(organizacaoId: string, descricao: string): Promise<NivelExperiencia> {
    const response = await api.post(`/organizacoes/${organizacaoId}/niveis-experiencia`, { descricao });
    return response.data;
  },

  async atualizar(organizacaoId: string, nivelExperienciaId: string, descricao: string): Promise<NivelExperiencia> {
    const response = await api.put(`/organizacoes/${organizacaoId}/niveis-experiencia/${nivelExperienciaId}`, { descricao });
    return response.data;
  },

  async deletar(organizacaoId: string, nivelExperienciaId: string): Promise<void> {
    await api.delete(`/organizacoes/${organizacaoId}/niveis-experiencia/${nivelExperienciaId}`);
  },
};
