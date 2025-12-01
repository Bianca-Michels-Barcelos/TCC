import api from "@/lib/api";

export interface Beneficio {
  id: string;
  nome: string;
  descricao: string;
  organizacaoId: string;
}

export interface CriarBeneficioRequest {
  nome: string;
  descricao: string;
}

export interface AtualizarBeneficioRequest {
  nome: string;
  descricao: string;
}

export const beneficioService = {
  async criar(
    organizacaoId: string,
    data: CriarBeneficioRequest
  ): Promise<Beneficio> {
    const response = await api.post(
      `/organizacoes/${organizacaoId}/beneficios`,
      data
    );
    return response.data;
  },

  async listarPorOrganizacao(organizacaoId: string): Promise<Beneficio[]> {
    const response = await api.get(`/organizacoes/${organizacaoId}/beneficios`);
    return response.data;
  },

  async buscar(organizacaoId: string, beneficioId: string): Promise<Beneficio> {
    const response = await api.get(
      `/organizacoes/${organizacaoId}/beneficios/${beneficioId}`
    );
    return response.data;
  },

  async atualizar(
    organizacaoId: string,
    beneficioId: string,
    data: AtualizarBeneficioRequest
  ): Promise<Beneficio> {
    const response = await api.put(
      `/organizacoes/${organizacaoId}/beneficios/${beneficioId}`,
      data
    );
    return response.data;
  },

  async deletar(organizacaoId: string, beneficioId: string): Promise<void> {
    await api.delete(
      `/organizacoes/${organizacaoId}/beneficios/${beneficioId}`
    );
  },

  async listarBeneficiosDaVaga(vagaId: string): Promise<string[]> {
    const response = await api.get(`/vagas/${vagaId}/beneficios`);
    return response.data;
  },
};
