import api from '@/lib/api';

export interface BuscarCandidatoRequest {
  vagaId: string;
  consulta: string;
}

export interface BuscarCandidatoResponse {
  usuarioId: string;
  nome: string;
  email: string;
  cidade: string | null;
  uf: string | null;
  dataNascimento: string;
  competencias: string[];
  experiencias: string[];
  scoreRelevancia: number;
  resumo: string;
  jaConvidado: boolean;
}

export interface BuscarCandidatoPageResponse {
  content: BuscarCandidatoResponse[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
}

export interface EnviarConviteRequest {
  vagaId: string;
  candidatoUsuarioId: string;
  mensagem: string;
}

export interface ConviteProcessoResponse {
  id: string;
  vagaId: string;
  vagaTitulo: string;
  recrutadorNome: string;
  mensagem: string;
  status: string;
  dataEnvio: string;
  dataExpiracao: string;
  dataResposta: string | null;
}

export interface Vaga {
  id: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  salario: number | null;
  dataPublicacao: string;
  status: string;
  tipoContrato: string;
  modalidade: string;
  horarioTrabalho: string;
  ativo: boolean;
}

export interface AlterarPapelRequest {
  papel: 'ADMIN' | 'RECRUTADOR';
}

export interface TransferirVagasRequest {
  usuarioIdDestino: string;
}

export interface TransferirVagasResponse {
  quantidadeTransferida: number;
}

export const recrutadorService = {
  async buscarCandidatos(
    organizacaoId: string,
    request: BuscarCandidatoRequest,
    page: number = 0,
    size: number = 10
  ): Promise<BuscarCandidatoPageResponse> {
    const response = await api.post(
      `/organizacoes/${organizacaoId}/recrutadores/buscar-candidatos`,
      request,
      { params: { page, size } }
    );
    return response.data;
  },

  async enviarConvite(
    organizacaoId: string,
    request: EnviarConviteRequest
  ): Promise<ConviteProcessoResponse> {
    const response = await api.post(
      `/organizacoes/${organizacaoId}/recrutadores/convites`,
      request
    );
    return response.data;
  },

  async listarRecrutadores(organizacaoId: string) {
    const response = await api.get(`/organizacoes/${organizacaoId}/recrutadores`);
    return response.data;
  },

  async getVagasRecentes(
    organizacaoId: string,
    limite: number = 10
  ): Promise<Vaga[]> {
    const response = await api.get(
      `/vagas/organizacao/${organizacaoId}/recentes`,
      { params: { limite } }
    );
    return response.data;
  },

  async alterarPapel(
    organizacaoId: string,
    usuarioId: string,
    request: AlterarPapelRequest
  ) {
    const response = await api.patch(
      `/organizacoes/${organizacaoId}/recrutadores/${usuarioId}/papel`,
      request
    );
    return response.data;
  },

  async transferirVagas(
    organizacaoId: string,
    usuarioIdOrigem: string,
    request: TransferirVagasRequest
  ): Promise<TransferirVagasResponse> {
    const response = await api.post(
      `/organizacoes/${organizacaoId}/recrutadores/${usuarioIdOrigem}/transferir-vagas`,
      request
    );
    return response.data;
  },

  async removerRecrutador(
    organizacaoId: string,
    usuarioId: string
  ): Promise<void> {
    await api.delete(`/organizacoes/${organizacaoId}/recrutadores/${usuarioId}`);
  },
};
