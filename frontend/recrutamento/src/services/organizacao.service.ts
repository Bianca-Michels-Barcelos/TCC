import api from '@/lib/api';

export interface RegistrarOrganizacaoRequest {
  cnpj: string;
  nome: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  cep: string;
  cidade: string;
  uf: string;
  adminRecruiter: {
    nome: string;
    cpf: string;
    email: string;
    senha: string;
  };
}

export interface RegistrarOrganizacaoResponse {
  organizacaoId: string;
  usuarioId: string;
  nome: string;
  email: string;
}

export interface OrganizacaoPublica {
  id: string;
  nome: string;
  endereco: {
    cidade: string;
    uf: string;
  };
}

export const organizacaoService = {
  async registrar(data: RegistrarOrganizacaoRequest): Promise<RegistrarOrganizacaoResponse> {
    const response = await api.post('/organizacoes', data);
    return response.data;
  },

  async buscarPublica(organizacaoId: string): Promise<OrganizacaoPublica> {
    const response = await api.get(`/organizacoes/publicas/${organizacaoId}`);
    return response.data;
  },
};
