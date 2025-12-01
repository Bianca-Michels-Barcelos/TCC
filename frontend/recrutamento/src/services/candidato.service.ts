import api from '@/lib/api';

export interface RegistrarCandidatoRequest {
  nome: string;
  cpf: string;
  email: string;
  senha: string;
  perfilCandidato: {
    dataNascimento: string;
    nome: string;
    logradouro: string;
    numero: string;
    complemento?: string;
    cep: string;
    cidade: string;
    uf: string;
  };
}

export interface AdicionarExperienciaRequest {
  cargo: string;
  empresa: string;
  descricao: string;
  dataInicio: string;
  dataFim?: string;
}

export interface AdicionarHistoricoRequest {
  titulo: string;
  descricao: string;
  instituicao: string;
  dataInicio: string;
  dataFim?: string;
}

export interface ExperienciaProfissional {
  id: string;
  cargo: string;
  empresa: string;
  descricao: string;
  dataInicio: string;
  dataFim?: string;
}

export interface HistoricoAcademico {
  id: string;
  titulo: string;
  descricao: string;
  instituicao: string;
  dataInicio: string;
  dataFim?: string;
}

export interface Competencia {
  id: string;
  titulo: string;
  descricao: string;
  nivel: 'BASICO' | 'INTERMEDIARIO' | 'AVANCADO';
}

export interface AdicionarCompetenciaRequest {
  titulo: string;
  descricao: string;
  nivel: 'BASICO' | 'INTERMEDIARIO' | 'AVANCADO';
}

export interface AtualizarCompetenciaRequest {
  titulo: string;
  descricao: string;
  nivel: 'BASICO' | 'INTERMEDIARIO' | 'AVANCADO';
}

export interface Certificado {
  id: string;
  titulo: string;
  instituicao: string;
  dataEmissao: string;
  dataValidade?: string;
  descricao?: string;
}

export interface AdicionarCertificadoRequest {
  titulo: string;
  instituicao: string;
  dataEmissao: string;
  dataValidade?: string;
  descricao?: string;
}

export interface AtualizarCertificadoRequest {
  titulo: string;
  instituicao: string;
  dataEmissao: string;
  dataValidade?: string;
  descricao?: string;
}

export interface Portfolio {
  id: string;
  titulo: string;
  link: string;
}

export interface AdicionarPortfolioRequest {
  titulo: string;
  link: string;
}

export interface AtualizarPortfolioRequest {
  titulo: string;
  link: string;
}

export const candidatoService = {
  async registrar(data: RegistrarCandidatoRequest) {
    const response = await api.post('/candidatos', data);
    return response.data;
  },

  async adicionarExperiencia(usuarioId: string, data: AdicionarExperienciaRequest) {
    const response = await api.post(`/candidatos/${usuarioId}/experiencias`, data);
    return response.data;
  },

  async adicionarHistorico(usuarioId: string, data: AdicionarHistoricoRequest) {
    const response = await api.post(`/candidatos/${usuarioId}/historicos`, data);
    return response.data;
  },

  async adicionarProjeto(usuarioId: string, experienciaId: string, data: { nome: string; descricao: string }) {
    const response = await api.post(
      `/candidatos/${usuarioId}/experiencias/${experienciaId}/projetos`,
      data
    );
    return response.data;
  },

  async listarExperiencias(usuarioId: string): Promise<ExperienciaProfissional[]> {
    const response = await api.get(`/candidatos/${usuarioId}/experiencias`);
    return response.data;
  },

  async listarHistoricos(usuarioId: string): Promise<HistoricoAcademico[]> {
    const response = await api.get(`/candidatos/${usuarioId}/historicos`);
    return response.data;
  },

  async atualizarExperiencia(usuarioId: string, experienciaId: string, data: AdicionarExperienciaRequest) {
    const response = await api.put(`/candidatos/${usuarioId}/experiencias/${experienciaId}`, data);
    return response.data;
  },

  async atualizarHistorico(usuarioId: string, historicoId: string, data: AdicionarHistoricoRequest) {
    const response = await api.put(`/candidatos/${usuarioId}/historicos/${historicoId}`, data);
    return response.data;
  },

  async listarCompetencias(usuarioId: string): Promise<Competencia[]> {
    const response = await api.get(`/candidatos/${usuarioId}/competencias`);
    return response.data;
  },

  async adicionarCompetencia(usuarioId: string, data: AdicionarCompetenciaRequest) {
    const response = await api.post(`/candidatos/${usuarioId}/competencias`, data);
    return response.data;
  },

  async atualizarCompetencia(usuarioId: string, competenciaId: string, data: AtualizarCompetenciaRequest) {
    const response = await api.put(`/candidatos/${usuarioId}/competencias/${competenciaId}`, data);
    return response.data;
  },

  async removerCompetencia(usuarioId: string, competenciaId: string) {
    await api.delete(`/candidatos/${usuarioId}/competencias/${competenciaId}`);
  },

  async listarCertificados(usuarioId: string): Promise<Certificado[]> {
    const response = await api.get(`/candidatos/${usuarioId}/certificados`);
    return response.data;
  },

  async adicionarCertificado(usuarioId: string, data: AdicionarCertificadoRequest) {
    const response = await api.post(`/candidatos/${usuarioId}/certificados`, data);
    return response.data;
  },

  async atualizarCertificado(usuarioId: string, certificadoId: string, data: AtualizarCertificadoRequest) {
    const response = await api.put(`/candidatos/${usuarioId}/certificados/${certificadoId}`, data);
    return response.data;
  },

  async removerCertificado(usuarioId: string, certificadoId: string) {
    await api.delete(`/candidatos/${usuarioId}/certificados/${certificadoId}`);
  },

  async listarPortfolios(usuarioId: string): Promise<Portfolio[]> {
    const response = await api.get(`/candidatos/${usuarioId}/portfolios`);
    return response.data;
  },

  async adicionarPortfolio(usuarioId: string, data: AdicionarPortfolioRequest) {
    const response = await api.post(`/candidatos/${usuarioId}/portfolios`, data);
    return response.data;
  },

  async atualizarPortfolio(usuarioId: string, portfolioId: string, data: AtualizarPortfolioRequest) {
    const response = await api.put(`/candidatos/${usuarioId}/portfolios/${portfolioId}`, data);
    return response.data;
  },

  async removerPortfolio(usuarioId: string, portfolioId: string) {
    await api.delete(`/candidatos/${usuarioId}/portfolios/${portfolioId}`);
  },

  async listarConvites(usuarioId: string): Promise<ConviteProcesso[]> {
    const response = await api.get(`/candidatos/${usuarioId}/convites`);
    return response.data;
  },

  async aceitarConviteComCurriculo(
    usuarioId: string, 
    conviteId: string, 
    modeloCurriculo: string, 
    conteudoPersonalizado: string
  ): Promise<ConviteProcesso> {
    const response = await api.post(`/candidatos/${usuarioId}/convites/${conviteId}/aceitar`, {
      modeloCurriculo,
      conteudoPersonalizado,
    });
    return response.data;
  },

  async recusarConvite(usuarioId: string, conviteId: string): Promise<ConviteProcesso> {
    const response = await api.post(`/candidatos/${usuarioId}/convites/${conviteId}/recusar`);
    return response.data;
  },

  async gerarCurriculoComIA(
    usuarioId: string, 
    vagaId: string, 
    modelo: string, 
    observacoes?: string
  ): Promise<{ curriculo: string; modelo: string }> {
    const response = await api.post(`/candidatos/${usuarioId}/curriculos/gerar-com-ia`, {
      vagaId,
      modelo,
      observacoes,
    });
    return response.data;
  },

  saveExperienciaLocal(usuarioId: string, experiencia: ExperienciaProfissional) {
    const stored = localStorage.getItem(`experiencias_${usuarioId}`);
    const experiencias = stored ? JSON.parse(stored) : [];
    experiencias.push(experiencia);
    localStorage.setItem(`experiencias_${usuarioId}`, JSON.stringify(experiencias));
  },

  saveHistoricoLocal(usuarioId: string, historico: HistoricoAcademico) {
    const stored = localStorage.getItem(`historicos_${usuarioId}`);
    const historicos = stored ? JSON.parse(stored) : [];
    historicos.push(historico);
    localStorage.setItem(`historicos_${usuarioId}`, JSON.stringify(historicos));
  },
};

export interface ConviteProcesso {
  id: string;
  vagaId: string;
  tituloVaga: string;
  descricaoVaga: string;
  nomeOrganizacao: string;
  modalidade: string;
  cidade: string;
  uf: string;
  salarioMinimo: number | null;
  salarioMaximo: number | null;
  nomeRecrutador: string;
  mensagem: string;
  status: 'PENDENTE' | 'ACEITO' | 'RECUSADO' | 'EXPIRADO';
  dataEnvio: string;
  dataExpiracao: string;
  dataResposta: string | null;
}
