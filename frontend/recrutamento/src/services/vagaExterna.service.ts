import api from "@/lib/api";

export interface VagaExterna {
  id: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  arquivoCurriculo?: string;
  modeloCurriculo?: string;
  candidatoUsuarioId: string;
  ativo: boolean;
  criadoEm: string;
}

export interface CriarVagaExternaRequest {
  titulo: string;
  descricao: string;
  requisitos: string;
  candidatoUsuarioId: string;
}

export interface AtualizarVagaExternaRequest {
  titulo: string;
  descricao: string;
  requisitos: string;
}

export const vagaExternaService = {
  async listar(): Promise<VagaExterna[]> {
    const response = await api.get("/vagas-externas");
    return response.data;
  },

  async buscar(id: string): Promise<VagaExterna> {
    const response = await api.get(`/vagas-externas/${id}`);
    return response.data;
  },

  async criar(data: CriarVagaExternaRequest): Promise<VagaExterna> {
    const response = await api.post("/vagas-externas", data);
    return response.data;
  },

  async atualizar(
    id: string,
    data: AtualizarVagaExternaRequest
  ): Promise<VagaExterna> {
    const response = await api.put(`/vagas-externas/${id}`, data);
    return response.data;
  },

  async gerarCurriculo(id: string): Promise<void> {
    await api.post(`/vagas-externas/${id}/curriculo`);
  },

  async gerarCurriculoComIA(id: string, modelo: string): Promise<VagaExterna> {
    const response = await api.post(`/vagas-externas/${id}/curriculo`, { modelo });
    return response.data;
  },

  async desativar(id: string): Promise<VagaExterna> {
    const response = await api.patch(`/vagas-externas/${id}/desativar`);
    return response.data;
  },

  async deletar(id: string): Promise<void> {
    await api.delete(`/vagas-externas/${id}`);
  },
};
