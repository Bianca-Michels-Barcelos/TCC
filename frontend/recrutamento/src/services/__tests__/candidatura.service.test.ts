import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { candidaturaService } from '../candidatura.service';
import api from '@/lib/api';

vi.mock('@/lib/api');

describe('Serviço de Candidatura', () => {
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    vi.clearAllMocks();
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  describe('listarMinhasCandidaturas', () => {
    it('deve listar candidaturas do usuário com sucesso', async () => {
      const mockCandidaturas = [
        {
          id: 'cand-1',
          vagaId: 'vaga-1',
          vaga: {
            titulo: 'Desenvolvedor Java',
            descricao: 'Vaga para dev',
            modalidade: 'REMOTO',
            salario: 5000,
            organizacao: {
              nome: 'Empresa X',
            },
            endereco: {
              cidade: 'São Paulo',
              uf: 'SP',
            },
          },
          status: 'PENDENTE' as const,
          dataCandidatura: '2024-01-01',
          etapaAtual: 'Triagem',
          compatibilidade: 85,
        },
        {
          id: 'cand-2',
          vagaId: 'vaga-2',
          vaga: {
            titulo: 'Desenvolvedor Frontend',
            descricao: 'Vaga para dev',
            modalidade: 'HIBRIDO',
            organizacao: {
              nome: 'Empresa Y',
            },
          },
          status: 'EM_PROCESSO' as const,
          dataCandidatura: '2024-01-05',
          etapaAtual: 'Entrevista',
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockCandidaturas });

      const result = await candidaturaService.listarMinhasCandidaturas();

      expect(api.get).toHaveBeenCalledWith('/candidaturas/minhas');
      expect(result).toEqual(mockCandidaturas);
    });

    it('deve lidar com erro e registrar no console', async () => {
      const mockError = new Error('Failed to fetch candidaturas');
      vi.mocked(api.get).mockRejectedValue(mockError);

      await expect(candidaturaService.listarMinhasCandidaturas()).rejects.toThrow(
        'Failed to fetch candidaturas'
      );

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching candidaturas:', mockError);
    });

    it('deve retornar array vazio quando não existem candidaturas', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [] });

      const result = await candidaturaService.listarMinhasCandidaturas();

      expect(result).toEqual([]);
    });
  });

  describe('criar', () => {
    it('deve criar candidatura com sucesso', async () => {
      const mockCandidatura = {
        id: 'cand-1',
        vagaId: 'vaga-1',
        candidatoUsuarioId: 'user-1',
        status: 'PENDENTE' as const,
        dataCandidatura: '2024-01-01',
        compatibilidade: 90,
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockCandidatura });

      const result = await candidaturaService.criar('vaga-1', 'user-1');

      expect(api.post).toHaveBeenCalledWith('/vagas/vaga-1/candidaturas', {
        candidatoUsuarioId: 'user-1',
      });
      expect(result).toEqual(mockCandidatura);
    });

    it('deve lidar com erro ao criar candidatura', async () => {
      vi.mocked(api.post).mockRejectedValue(new Error('Candidatura already exists'));

      await expect(candidaturaService.criar('vaga-1', 'user-1')).rejects.toThrow(
        'Candidatura already exists'
      );
    });

    it('deve criar candidatura sem campos opcionais', async () => {
      const mockCandidatura = {
        id: 'cand-1',
        vagaId: 'vaga-1',
        candidatoUsuarioId: 'user-1',
        status: 'PENDENTE' as const,
        dataCandidatura: '2024-01-01',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockCandidatura });

      const result = await candidaturaService.criar('vaga-1', 'user-1');

      expect(result).toEqual(mockCandidatura);
      expect(result.arquivoCurriculo).toBeUndefined();
      expect(result.compatibilidade).toBeUndefined();
    });
  });

  describe('aceitar', () => {
    it('deve aceitar candidatura com sucesso', async () => {
      const mockCandidatura = {
        id: 'cand-1',
        vagaId: 'vaga-1',
        candidatoUsuarioId: 'user-1',
        status: 'ACEITA' as const,
        dataCandidatura: '2024-01-01',
      };

      vi.mocked(api.patch).mockResolvedValue({ data: mockCandidatura });

      const result = await candidaturaService.aceitar('cand-1');

      expect(api.patch).toHaveBeenCalledWith('/candidaturas/cand-1/aceitar');
      expect(result).toEqual(mockCandidatura);
      expect(result.status).toBe('ACEITA');
    });

    it('deve lidar com erro ao aceitar candidatura', async () => {
      vi.mocked(api.patch).mockRejectedValue(new Error('Candidatura not found'));

      await expect(candidaturaService.aceitar('cand-999')).rejects.toThrow('Candidatura not found');
    });
  });

  describe('rejeitar', () => {
    it('deve rejeitar candidatura com sucesso', async () => {
      const mockCandidatura = {
        id: 'cand-1',
        vagaId: 'vaga-1',
        candidatoUsuarioId: 'user-1',
        status: 'REJEITADA' as const,
        dataCandidatura: '2024-01-01',
      };

      vi.mocked(api.patch).mockResolvedValue({ data: mockCandidatura });

      const result = await candidaturaService.rejeitar('cand-1');

      expect(api.patch).toHaveBeenCalledWith('/candidaturas/cand-1/rejeitar');
      expect(result).toEqual(mockCandidatura);
      expect(result.status).toBe('REJEITADA');
    });

    it('deve lidar com erro ao rejeitar candidatura', async () => {
      vi.mocked(api.patch).mockRejectedValue(new Error('Candidatura not found'));

      await expect(candidaturaService.rejeitar('cand-999')).rejects.toThrow('Candidatura not found');
    });
  });

  describe('transições de status', () => {
    it('deve lidar com status de candidatura como PENDENTE', async () => {
      const mockCandidatura = {
        id: 'cand-1',
        vagaId: 'vaga-1',
        candidatoUsuarioId: 'user-1',
        status: 'PENDENTE' as const,
        dataCandidatura: '2024-01-01',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockCandidatura });

      const result = await candidaturaService.criar('vaga-1', 'user-1');

      expect(result.status).toBe('PENDENTE');
    });

    it('deve lidar com status de candidatura como EM_PROCESSO', async () => {
      const mockCandidaturas = [
        {
          id: 'cand-1',
          vagaId: 'vaga-1',
          vaga: {
            titulo: 'Vaga',
            descricao: 'Desc',
            modalidade: 'REMOTO',
            organizacao: { nome: 'Empresa' },
          },
          status: 'EM_PROCESSO' as const,
          dataCandidatura: '2024-01-01',
          etapaAtual: 'Entrevista',
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockCandidaturas });

      const result = await candidaturaService.listarMinhasCandidaturas();

      expect(result[0].status).toBe('EM_PROCESSO');
    });

    it('deve lidar com status de candidatura como FINALIZADA', async () => {
      const mockCandidaturas = [
        {
          id: 'cand-1',
          vagaId: 'vaga-1',
          vaga: {
            titulo: 'Vaga',
            descricao: 'Desc',
            modalidade: 'REMOTO',
            organizacao: { nome: 'Empresa' },
          },
          status: 'FINALIZADA' as const,
          dataCandidatura: '2024-01-01',
          etapaAtual: null,
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockCandidaturas });

      const result = await candidaturaService.listarMinhasCandidaturas();

      expect(result[0].status).toBe('FINALIZADA');
    });

    it('deve lidar com status de candidatura como DESISTENTE', async () => {
      const mockCandidaturas = [
        {
          id: 'cand-1',
          vagaId: 'vaga-1',
          vaga: {
            titulo: 'Vaga',
            descricao: 'Desc',
            modalidade: 'REMOTO',
            organizacao: { nome: 'Empresa' },
          },
          status: 'DESISTENTE' as const,
          dataCandidatura: '2024-01-01',
          etapaAtual: null,
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockCandidaturas });

      const result = await candidaturaService.listarMinhasCandidaturas();

      expect(result[0].status).toBe('DESISTENTE');
    });
  });
});
