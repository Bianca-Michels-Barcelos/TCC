import { describe, it, expect, beforeEach, vi } from 'vitest';
import { processoSeletivoService, etapaProcessoService, candidaturaService } from '../processo-seletivo.service';
import api from '@/lib/api';

vi.mock('@/lib/api');

describe('Serviço de Processo Seletivo', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('listarPorVaga', () => {
    it('deve listar processos seletivos por vaga', async () => {
      const mockProcessos = [
        {
          id: 'processo-1',
          candidaturaId: 'cand-1',
          etapaProcessoAtualId: 'etapa-1',
          dataInicio: '2024-01-01',
          dataFim: null,
          dataUltimaMudanca: '2024-01-05',
        },
        {
          id: 'processo-2',
          candidaturaId: 'cand-2',
          etapaProcessoAtualId: 'etapa-1',
          dataInicio: '2024-01-02',
          dataFim: null,
          dataUltimaMudanca: '2024-01-06',
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockProcessos });

      const result = await processoSeletivoService.listarPorVaga('vaga-1');

      expect(api.get).toHaveBeenCalledWith('/vagas/vaga-1/processos-seletivos');
      expect(result).toEqual(mockProcessos);
    });
  });

  describe('listarComCandidatosPorVaga', () => {
    it('deve listar processos com detalhes dos candidatos', async () => {
      const mockProcessos = [
        {
          processoId: 'processo-1',
          candidaturaId: 'cand-1',
          dataInicio: '2024-01-01',
          dataFim: null,
          dataUltimaMudanca: '2024-01-05',
          etapaAtualId: 'etapa-1',
          etapaAtualNome: 'Triagem',
          etapaAtualDescricao: 'Análise inicial',
          etapaAtualOrdem: 1,
          etapaAtualStatus: 'EM_ANDAMENTO',
          candidatoUsuarioId: 'user-1',
          candidatoNome: 'João Silva',
          candidatoEmail: 'joao@example.com',
          statusCandidatura: 'EM_PROCESSO',
          dataCandidatura: '2024-01-01',
          compatibilidade: 85,
          arquivoCurriculo: 'curriculo.pdf',
          vagaId: 'vaga-1',
          vagaTitulo: 'Desenvolvedor Backend',
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockProcessos });

      const result = await processoSeletivoService.listarComCandidatosPorVaga('vaga-1');

      expect(api.get).toHaveBeenCalledWith('/vagas/vaga-1/processos-seletivos/com-candidatos');
      expect(result).toEqual(mockProcessos);
    });
  });

  describe('buscarComCandidatoPorId', () => {
    it('deve buscar processo com detalhes do candidato por id', async () => {
      const mockProcesso = {
        processoId: 'processo-1',
        candidaturaId: 'cand-1',
        dataInicio: '2024-01-01',
        dataFim: null,
        dataUltimaMudanca: '2024-01-05',
        etapaAtualId: 'etapa-2',
        etapaAtualNome: 'Entrevista',
        etapaAtualDescricao: 'Entrevista técnica',
        etapaAtualOrdem: 2,
        etapaAtualStatus: 'EM_ANDAMENTO',
        candidatoUsuarioId: 'user-1',
        candidatoNome: 'Maria Santos',
        candidatoEmail: 'maria@example.com',
        statusCandidatura: 'EM_PROCESSO',
        dataCandidatura: '2024-01-01',
        compatibilidade: 90,
        arquivoCurriculo: null,
        vagaId: 'vaga-1',
        vagaTitulo: 'Desenvolvedor Frontend',
      };

      vi.mocked(api.get).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.buscarComCandidatoPorId('processo-1');

      expect(api.get).toHaveBeenCalledWith('/processos-seletivos/processo-1/com-candidato');
      expect(result).toEqual(mockProcesso);
    });
  });

  describe('buscarPorCandidatura', () => {
    it('deve buscar processo por id da candidatura', async () => {
      const mockResponse = {
        processo: {
          id: 'processo-1',
          candidaturaId: 'cand-1',
          etapaProcessoAtualId: 'etapa-1',
          dataInicio: '2024-01-01',
          dataFim: null,
          dataUltimaMudanca: '2024-01-05',
        },
        vagaId: 'vaga-1',
      };

      vi.mocked(api.get).mockResolvedValue({ data: mockResponse });

      const result = await processoSeletivoService.buscarPorCandidatura('cand-1');

      expect(api.get).toHaveBeenCalledWith('/processos-seletivos/candidatura/cand-1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('buscarHistorico', () => {
    it('deve buscar histórico do processo', async () => {
      const mockHistorico = [
        {
          id: 'hist-1',
          processoSeletivoId: 'processo-1',
          etapaProcessoId: 'etapa-1',
          etapaProcessoNome: 'Triagem',
          usuarioResponsavelId: 'user-rec',
          dataMovimentacao: '2024-01-01',
          feedback: 'Candidato aprovado na triagem',
          acao: 'AVANCAR',
        },
        {
          id: 'hist-2',
          processoSeletivoId: 'processo-1',
          etapaProcessoId: 'etapa-2',
          etapaProcessoNome: 'Entrevista',
          usuarioResponsavelId: 'user-rec',
          dataMovimentacao: '2024-01-05',
          feedback: null,
          acao: 'AVANCAR',
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockHistorico });

      const result = await processoSeletivoService.buscarHistorico('processo-1');

      expect(api.get).toHaveBeenCalledWith('/processos-seletivos/processo-1/historico');
      expect(result).toEqual(mockHistorico);
    });
  });

  describe('avancarParaProximaEtapa', () => {
    it('deve avançar para próxima etapa sem feedback', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-2',
        dataInicio: '2024-01-01',
        dataFim: null,
        dataUltimaMudanca: '2024-01-05',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.avancarParaProximaEtapa('processo-1');

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/avancar', { feedback: undefined });
      expect(result).toEqual(mockProcesso);
    });

    it('deve avançar para próxima etapa com feedback', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-2',
        dataInicio: '2024-01-01',
        dataFim: null,
        dataUltimaMudanca: '2024-01-05',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.avancarParaProximaEtapa(
        'processo-1',
        'Bom desempenho na entrevista'
      );

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/avancar', {
        feedback: 'Bom desempenho na entrevista',
      });
      expect(result).toEqual(mockProcesso);
    });
  });

  describe('avancarParaEtapa', () => {
    it('deve avançar para etapa específica', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-3',
        dataInicio: '2024-01-01',
        dataFim: null,
        dataUltimaMudanca: '2024-01-05',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.avancarParaEtapa(
        'processo-1',
        'etapa-3',
        'Pulando direto para fase final'
      );

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/avancar-para-etapa', {
        etapaId: 'etapa-3',
        feedback: 'Pulando direto para fase final',
      });
      expect(result).toEqual(mockProcesso);
    });

    it('deve avançar para etapa sem feedback', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-3',
        dataInicio: '2024-01-01',
        dataFim: null,
        dataUltimaMudanca: '2024-01-05',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.avancarParaEtapa('processo-1', 'etapa-3');

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/avancar-para-etapa', {
        etapaId: 'etapa-3',
        feedback: undefined,
      });
      expect(result).toEqual(mockProcesso);
    });
  });

  describe('retornarParaEtapa', () => {
    it('deve retornar para etapa anterior', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-1',
        dataInicio: '2024-01-01',
        dataFim: null,
        dataUltimaMudanca: '2024-01-05',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.retornarParaEtapa(
        'processo-1',
        'etapa-1',
        'Precisa refazer a triagem'
      );

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/retornar-para-etapa', {
        etapaId: 'etapa-1',
        feedback: 'Precisa refazer a triagem',
      });
      expect(result).toEqual(mockProcesso);
    });
  });

  describe('finalizar', () => {
    it('deve finalizar processo sem feedback', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-final',
        dataInicio: '2024-01-01',
        dataFim: '2024-01-30',
        dataUltimaMudanca: '2024-01-30',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.finalizar('processo-1');

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/finalizar', { feedback: undefined });
      expect(result).toEqual(mockProcesso);
      expect(result.dataFim).toBe('2024-01-30');
    });

    it('deve finalizar processo com feedback', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-final',
        dataInicio: '2024-01-01',
        dataFim: '2024-01-30',
        dataUltimaMudanca: '2024-01-30',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.finalizar('processo-1', 'Candidato aprovado');

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/finalizar', {
        feedback: 'Candidato aprovado',
      });
      expect(result).toEqual(mockProcesso);
    });
  });

  describe('reprovar', () => {
    it('deve reprovar candidato com feedback', async () => {
      const mockProcesso = {
        id: 'processo-1',
        candidaturaId: 'cand-1',
        etapaProcessoAtualId: 'etapa-2',
        dataInicio: '2024-01-01',
        dataFim: '2024-01-10',
        dataUltimaMudanca: '2024-01-10',
      };

      vi.mocked(api.post).mockResolvedValue({ data: mockProcesso });

      const result = await processoSeletivoService.reprovar(
        'processo-1',
        'Não atendeu aos requisitos técnicos'
      );

      expect(api.post).toHaveBeenCalledWith('/processos-seletivos/processo-1/reprovar', {
        feedback: 'Não atendeu aos requisitos técnicos',
      });
      expect(result).toEqual(mockProcesso);
    });

    it('deve lidar com erro ao reprovar', async () => {
      vi.mocked(api.post).mockRejectedValue(new Error('Processo not found'));

      await expect(
        processoSeletivoService.reprovar('processo-999', 'Feedback')
      ).rejects.toThrow('Processo not found');
    });
  });
});

describe('Serviço de Etapa de Processo', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('listarPorVaga', () => {
    it('deve listar etapas por vaga', async () => {
      const mockEtapas = [
        {
          id: 'etapa-1',
          vagaId: 'vaga-1',
          nome: 'Triagem',
          descricao: 'Análise inicial de currículos',
          tipo: 'TRIAGEM',
          ordem: 1,
          dataInicio: '2024-01-01',
          dataFim: '2024-01-15',
          status: 'EM_ANDAMENTO' as const,
        },
        {
          id: 'etapa-2',
          vagaId: 'vaga-1',
          nome: 'Entrevista',
          descricao: 'Entrevista técnica',
          tipo: 'ENTREVISTA',
          ordem: 2,
          dataInicio: '2024-01-16',
          dataFim: '2024-01-31',
          status: 'PENDENTE' as const,
        },
      ];

      vi.mocked(api.get).mockResolvedValue({ data: mockEtapas });

      const result = await etapaProcessoService.listarPorVaga('vaga-1');

      expect(api.get).toHaveBeenCalledWith('/vagas/vaga-1/etapas');
      expect(result).toEqual(mockEtapas);
    });

    it('deve lidar com lista vazia de etapas', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [] });

      const result = await etapaProcessoService.listarPorVaga('vaga-1');

      expect(result).toEqual([]);
    });
  });
});

describe('Serviço de Candidatura (de processo-seletivo)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('buscarPorId', () => {
    it('deve buscar candidatura por id', async () => {
      const mockCandidatura = {
        id: 'cand-1',
        vagaId: 'vaga-1',
        candidatoUsuarioId: 'user-1',
        dataCandidatura: '2024-01-01',
        status: 'EM_PROCESSO',
      };

      vi.mocked(api.get).mockResolvedValue({ data: mockCandidatura });

      const result = await candidaturaService.buscarPorId('cand-1');

      expect(api.get).toHaveBeenCalledWith('/candidaturas/cand-1');
      expect(result).toEqual(mockCandidatura);
    });

    it('deve lidar com erro ao buscar candidatura', async () => {
      vi.mocked(api.get).mockRejectedValue(new Error('Candidatura not found'));

      await expect(candidaturaService.buscarPorId('cand-999')).rejects.toThrow('Candidatura not found');
    });
  });
});
