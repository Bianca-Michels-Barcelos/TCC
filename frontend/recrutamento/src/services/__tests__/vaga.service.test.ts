import { describe, it, expect, beforeEach, vi } from 'vitest';
import { vagaService } from '../vaga.service';
import api from '@/lib/api';

vi.mock('@/lib/api');

describe('Serviço de Vagas', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('criar', () => {
    it('deve criar uma nova vaga com sucesso', async () => {
      const mockVaga = {
        id: '1',
        organizacaoId: 'org-1',
        recrutadorUsuarioId: 'user-1',
        titulo: 'Desenvolvedor Backend',
        descricao: 'Vaga para desenvolvedor',
        requisitos: 'Java, Spring Boot',
        salario: 5000,
        dataPublicacao: '2024-01-01',
        status: 'ABERTA',
        tipoContrato: 'CLT',
        modalidade: 'REMOTO',
        horarioTrabalho: 'INTEGRAL',
      };

      const requestData = {
        organizacaoId: 'org-1',
        recrutadorUsuarioId: 'user-1',
        titulo: 'Desenvolvedor Backend',
        descricao: 'Vaga para desenvolvedor',
        requisitos: 'Java, Spring Boot',
        salario: 5000,
        dataPublicacao: '2024-01-01',
        status: 'ABERTA',
        tipoContrato: 'CLT',
        modalidade: 'REMOTO',
        horarioTrabalho: 'INTEGRAL',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockVaga });

      const result = await vagaService.criar(requestData);

      expect(api.post).toHaveBeenCalledWith('/vagas', requestData);
      expect(result).toEqual(mockVaga);
    });

    it('deve lidar com erro ao criar vaga', async () => {
      const requestData = {
        organizacaoId: 'org-1',
        recrutadorUsuarioId: 'user-1',
        titulo: 'Desenvolvedor Backend',
        descricao: 'Vaga para desenvolvedor',
        requisitos: 'Java, Spring Boot',
        dataPublicacao: '2024-01-01',
        status: 'ABERTA',
        tipoContrato: 'CLT',
        modalidade: 'REMOTO',
        horarioTrabalho: 'INTEGRAL',
      };

      vi.mocked(api.post).mockRejectedValue(new Error('Failed to create vaga'));

      await expect(vagaService.criar(requestData)).rejects.toThrow('Failed to create vaga');
    });
  });

  describe('listarPorOrganizacao', () => {
    it('deve listar vagas por organização', async () => {
      const mockVagas = [
        { id: '1', titulo: 'Vaga 1', organizacaoId: 'org-1' },
        { id: '2', titulo: 'Vaga 2', organizacaoId: 'org-1' },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockVagas });

      const result = await vagaService.listarPorOrganizacao('org-1');

      expect(api.get).toHaveBeenCalledWith('/vagas/organizacao/org-1');
      expect(result).toEqual(mockVagas);
    });
  });

  describe('buscar', () => {
    it('deve buscar vaga por id', async () => {
      const mockVaga = { id: '1', titulo: 'Vaga 1' };

      vi.mocked(api.get).mockResolvedValue({ data: mockVaga });

      const result = await vagaService.buscar('1');

      expect(api.get).toHaveBeenCalledWith('/vagas/1');
      expect(result).toEqual(mockVaga);
    });
  });

  describe('buscarPorId', () => {
    it('deve buscar vaga por id usando buscarPorId', async () => {
      const mockVaga = { id: '1', titulo: 'Vaga 1' };

      vi.mocked(api.get).mockResolvedValue({ data: mockVaga });

      const result = await vagaService.buscarPorId('1');

      expect(api.get).toHaveBeenCalledWith('/vagas/1');
      expect(result).toEqual(mockVaga);
    });
  });

  describe('atualizar', () => {
    it('deve atualizar vaga com sucesso', async () => {
      const mockVaga = { id: '1', titulo: 'Vaga Atualizada' };
      const updateData = {
        titulo: 'Vaga Atualizada',
        descricao: 'Nova descrição',
        requisitos: 'Novos requisitos',
        status: 'ABERTA',
        tipoContrato: 'CLT',
        modalidade: 'REMOTO',
        horarioTrabalho: 'INTEGRAL',
      };

      vi.mocked(api.put).mockResolvedValue({ data: mockVaga });

      const result = await vagaService.atualizar('1', updateData);

      expect(api.put).toHaveBeenCalledWith('/vagas/1', updateData);
      expect(result).toEqual(mockVaga);
    });
  });

  describe('deletar', () => {
    it('deve deletar vaga com sucesso', async () => {
      vi.mocked(api.delete).mockResolvedValue({ data: null });

      await vagaService.deletar('1');

      expect(api.delete).toHaveBeenCalledWith('/vagas/1');
    });
  });

  describe('ativar', () => {
    it('deve ativar vaga com sucesso', async () => {
      vi.mocked(api.patch).mockResolvedValue({ data: null });

      await vagaService.ativar('1');

      expect(api.patch).toHaveBeenCalledWith('/vagas/1/ativar');
    });
  });

  describe('desativar', () => {
    it('deve desativar vaga com sucesso', async () => {
      vi.mocked(api.patch).mockResolvedValue({ data: null });

      await vagaService.desativar('1');

      expect(api.patch).toHaveBeenCalledWith('/vagas/1/desativar');
    });
  });

  describe('cancelar', () => {
    it('should cancel vaga with reason', async () => {
      const mockVaga = { id: '1', status: 'CANCELADA', motivoCancelamento: 'Vaga preenchida' };

      vi.mocked(api.patch).mockResolvedValue({ data: mockVaga });

      const result = await vagaService.cancelar('1', 'Vaga preenchida');

      expect(api.patch).toHaveBeenCalledWith('/vagas/1/cancelar', { motivo: 'Vaga preenchida' });
      expect(result).toEqual(mockVaga);
    });
  });

  describe('listarBeneficiosDaVaga', () => {
    it('deve listar benefícios da vaga', async () => {
      const mockBeneficios = ['beneficio-1', 'beneficio-2'];

      vi.mocked(api.get).mockResolvedValue({ data: mockBeneficios });

      const result = await vagaService.listarBeneficiosDaVaga('1');

      expect(api.get).toHaveBeenCalledWith('/vagas/1/beneficios');
      expect(result).toEqual(mockBeneficios);
    });
  });

  describe('adicionarEtapa', () => {
    it('deve adicionar etapa à vaga', async () => {
      const mockEtapa = { id: 'etapa-1', nome: 'Entrevista' };
      const etapaData = {
        nome: 'Entrevista',
        descricao: 'Entrevista técnica',
        tipo: 'ENTREVISTA',
        ordem: 1,
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockEtapa });

      const result = await vagaService.adicionarEtapa('1', etapaData);

      expect(api.post).toHaveBeenCalledWith('/vagas/1/etapas', etapaData);
      expect(result).toEqual(mockEtapa);
    });

    it('deve adicionar etapa com datas opcionais', async () => {
      const mockEtapa = { id: 'etapa-1', nome: 'Entrevista' };
      const etapaData = {
        nome: 'Entrevista',
        descricao: 'Entrevista técnica',
        tipo: 'ENTREVISTA',
        ordem: 1,
        dataInicio: '2024-01-01',
        dataFim: '2024-01-31',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockEtapa });

      const result = await vagaService.adicionarEtapa('1', etapaData);

      expect(api.post).toHaveBeenCalledWith('/vagas/1/etapas', etapaData);
      expect(result).toEqual(mockEtapa);
    });
  });

  describe('listarEtapas', () => {
    it('deve listar etapas da vaga', async () => {
      const mockEtapas = [
        { id: 'etapa-1', nome: 'Triagem' },
        { id: 'etapa-2', nome: 'Entrevista' },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockEtapas });

      const result = await vagaService.listarEtapas('1');

      expect(api.get).toHaveBeenCalledWith('/vagas/1/etapas');
      expect(result).toEqual(mockEtapas);
    });
  });

  describe('buscarInteligente', () => {
    it('deve realizar busca inteligente', async () => {
      const mockResults = [
        {
          vagaId: '1',
          titulo: 'Desenvolvedor Java',
          descricao: 'Vaga para dev',
          requisitos: 'Java, Spring',
          nomeOrganizacao: 'Empresa X',
          scoreRelevancia: 0.95,
          percentualCompatibilidade: 85,
          usouIA: true,
        },
      ];

      vi.mocked(api.post).mockResolvedValue({ data: mockResults });

      const result = await vagaService.buscarInteligente({
        consulta: 'desenvolvedor java',
        limite: 10,
      });

      expect(api.post).toHaveBeenCalledWith('/vagas/busca-inteligente', {
        consulta: 'desenvolvedor java',
        limite: 10,
      });
      expect(result).toEqual(mockResults);
    });

    it('deve usar limite padrão quando não fornecido', async () => {
      const mockResults: any[] = [];

      vi.mocked(api.post).mockResolvedValue({ data: mockResults });

      await vagaService.buscarInteligente({ consulta: 'desenvolvedor' });

      expect(api.post).toHaveBeenCalledWith('/vagas/busca-inteligente', {
        consulta: 'desenvolvedor',
        limite: 20,
      });
    });
  });

  describe('listarComEstatisticas', () => {
    it('deve listar vagas com estatísticas', async () => {
      const mockResponse = {
        content: [
          {
            id: '1',
            titulo: 'Vaga 1',
            totalCandidatos: 10,
            candidatosAtivos: 5,
            candidatosAceitos: 2,
            candidatosRejeitados: 3,
          },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
        first: true,
        last: true,
      };

      vi.mocked(api.get).mockResolvedValue({ data: mockResponse });

      const result = await vagaService.listarComEstatisticas('org-1');

      expect(api.get).toHaveBeenCalledWith('/vagas/organizacao/org-1/with-stats', {
        params: {
          status: undefined,
          modalidade: undefined,
          search: undefined,
          page: 0,
          size: 10,
        },
      });
      expect(result).toEqual(mockResponse);
    });

    it('deve listar vagas com filtros e paginação', async () => {
      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 20,
        number: 2,
        first: false,
        last: true,
      };

      vi.mocked(api.get).mockResolvedValue({ data: mockResponse });

      const result = await vagaService.listarComEstatisticas('org-1', {
        status: 'ABERTA',
        modalidade: 'REMOTO',
        search: 'desenvolvedor',
        page: 2,
        size: 20,
      });

      expect(api.get).toHaveBeenCalledWith('/vagas/organizacao/org-1/with-stats', {
        params: {
          status: 'ABERTA',
          modalidade: 'REMOTO',
          search: 'desenvolvedor',
          page: 2,
          size: 20,
        },
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('fecharVaga', () => {
    it('deve fechar vaga com sucesso', async () => {
      vi.mocked(api.patch).mockResolvedValue({ data: null });

      await vagaService.fecharVaga('1');

      expect(api.patch).toHaveBeenCalledWith('/vagas/1/fechar');
    });
  });

  describe('cancelarVaga', () => {
    it('deve cancelar vaga com motivo', async () => {
      vi.mocked(api.patch).mockResolvedValue({ data: null });

      await vagaService.cancelarVaga('1', 'Posição preenchida');

      expect(api.patch).toHaveBeenCalledWith('/vagas/1/cancelar', { motivo: 'Posição preenchida' });
    });
  });
});

