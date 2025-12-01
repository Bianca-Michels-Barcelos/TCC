import api from '@/lib/api';

export interface Endereco {
  logradouro: string | null;
  numero: string | null;
  complemento: string | null;
  bairro: string | null;
  cidade: string;
  uf: string;
  cep: string | null;
}

export interface VagaSalva {
  id: string;
  vagaId: string;
  salvaEm: string;
  vaga: {
    id: string;
    titulo: string;
    descricao: string;
    requisitos: string;
    salario: number;
    modalidade: string;
    tipoContrato: string;
    status: string;
    dataPublicacao: string;
    endereco: Endereco;
  };
  organizacao: {
    id: string;
    nome: string;
  };
}

export const vagaSalvaService = {
  async listar(): Promise<VagaSalva[]> {
    try {
      const response = await api.get('/vagas-salvas');
      return response.data;
    } catch (error) {
      console.error('Error fetching vagas salvas:', error);
      throw error;
    }
  },

  async salvar(vagaId: string): Promise<void> {
    try {
      await api.post(`/vagas-salvas/vaga/${vagaId}`);
    } catch (error) {
      console.error('Error saving vaga:', error);
      throw error;
    }
  },

  async remover(vagaId: string): Promise<void> {
    try {
      await api.delete(`/vagas-salvas/vaga/${vagaId}`);
    } catch (error) {
      console.error('Error removing vaga salva:', error);
      throw error;
    }
  },

  async verificarSalva(vagaId: string): Promise<boolean> {
    try {
      const response = await api.get(`/vagas-salvas/vaga/${vagaId}/esta-salva`);
      return response.data.estaSalva;
    } catch (error) {
      console.error('Error checking if vaga is saved:', error);
      return false;
    }
  },
};
