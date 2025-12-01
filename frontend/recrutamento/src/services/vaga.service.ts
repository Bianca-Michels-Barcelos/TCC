import api from "@/lib/api";

export interface CriarVagaRequest {
  organizacaoId: string;
  recrutadorUsuarioId: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  salario?: number;
  dataPublicacao: string;
  status: string;
  tipoContrato: string;
  modalidade: string;
  horarioTrabalho: string;
  nivelExperienciaId?: string;
  cidade?: string;
  uf?: string;
  beneficioIds?: string[];
}

export interface AtualizarVagaRequest {
  titulo: string;
  descricao: string;
  requisitos: string;
  salario?: number;
  status: string;
  tipoContrato: string;
  modalidade: string;
  horarioTrabalho: string;
  nivelExperienciaId?: string;
  cidade?: string;
  uf?: string;
  beneficioIds?: string[];
}

export interface Vaga {
  id: string;
  organizacaoId: string;
  recrutadorUsuarioId: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  salario?: number;
  dataPublicacao: string;
  status: string;
  tipoContrato: string;
  modalidade: string;
  horarioTrabalho: string;
  nivelExperienciaId?: string;
  endereco?: {
    cidade: string;
    uf: string | { value: string };
  };
}

export interface BuscaVagaRequest {
  consulta: string;
  limite?: number;
}

export interface BuscaVagaResponse {
  vagaId: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  nomeOrganizacao: string;
  salario?: number;
  modalidade?: string;
  cidade?: string;
  uf?: string;
  scoreRelevancia: number;
  percentualCompatibilidade?: number;
  justificativa?: string;
  usouIA?: boolean;
}

export interface VagaComEstatisticas {
  id: string;
  organizacaoId: string;
  recrutadorUsuarioId: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  salario?: number;
  dataPublicacao: string;
  status: string;
  tipoContrato: string;
  modalidade: string;
  horarioTrabalho: string;
  endereco?: {
    cidade: string;
    uf: { value: string } | string;
  };
  ativo: boolean;
  motivoCancelamento?: string;
  nivelExperienciaId?: string;
  nomeNivelExperiencia?: string;
  totalCandidatos: number;
  candidatosAtivos: number;
  candidatosAceitos: number;
  candidatosRejeitados: number;
  totalEtapas: number;
  totalBeneficios: number;
  ultimaAtualizacao: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export const vagaService = {
  async criar(data: CriarVagaRequest): Promise<Vaga> {
    const response = await api.post("/vagas", data);
    return response.data;
  },

  async listarPorOrganizacao(organizacaoId: string): Promise<Vaga[]> {
    const response = await api.get(`/vagas/organizacao/${organizacaoId}`);
    return response.data;
  },

  async buscar(vagaId: string): Promise<Vaga> {
    const response = await api.get(`/vagas/${vagaId}`);
    return response.data;
  },

  async buscarPorId(vagaId: string): Promise<Vaga> {
    return this.buscar(vagaId);
  },

  async atualizar(vagaId: string, data: AtualizarVagaRequest): Promise<Vaga> {
    const response = await api.put(`/vagas/${vagaId}`, data);
    return response.data;
  },

  async deletar(vagaId: string): Promise<void> {
    await api.delete(`/vagas/${vagaId}`);
  },

  async ativar(vagaId: string): Promise<void> {
    await api.patch(`/vagas/${vagaId}/ativar`);
  },

  async desativar(vagaId: string): Promise<void> {
    await api.patch(`/vagas/${vagaId}/desativar`);
  },

  async cancelar(vagaId: string, motivo: string): Promise<Vaga> {
    const response = await api.patch(`/vagas/${vagaId}/cancelar`, { motivo });
    return response.data;
  },

  async listarBeneficiosDaVaga(vagaId: string): Promise<string[]> {
    const response = await api.get(`/vagas/${vagaId}/beneficios`);
    return response.data;
  },

  async adicionarEtapa(
    vagaId: string,
    etapa: {
      nome: string;
      descricao: string;
      tipo: string;
      ordem: number;
      dataInicio?: string;
      dataFim?: string;
    }
  ): Promise<any> {
    const response = await api.post(`/vagas/${vagaId}/etapas`, etapa);
    return response.data;
  },

  async listarEtapas(vagaId: string): Promise<any[]> {
    const response = await api.get(`/vagas/${vagaId}/etapas`);
    return response.data;
  },

  async buscarInteligente(request: BuscaVagaRequest): Promise<BuscaVagaResponse[]> {
    const response = await api.post('/vagas/busca-inteligente', {
      consulta: request.consulta,
      limite: request.limite || 20
    });
    return response.data;
  },

  async listarComEstatisticas(
    organizacaoId: string,
    params?: {
      status?: string;
      modalidade?: string;
      search?: string;
      page?: number;
      size?: number;
    }
  ): Promise<PageResponse<VagaComEstatisticas>> {
    const response = await api.get(`/vagas/organizacao/${organizacaoId}/with-stats`, {
      params: {
        status: params?.status,
        modalidade: params?.modalidade,
        search: params?.search,
        page: params?.page ?? 0,
        size: params?.size ?? 10,
      },
    });
    return response.data;
  },

  async fecharVaga(vagaId: string): Promise<void> {
    await api.patch(`/vagas/${vagaId}/fechar`);
  },

  async cancelarVaga(vagaId: string, motivo: string): Promise<void> {
    await api.patch(`/vagas/${vagaId}/cancelar`, { motivo });
  },
};
