import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { PersonalizarCurriculoModal } from '@/components/PersonalizarCurriculoModal';
import { AvaliarOrganizacaoModal } from '@/components/AvaliarOrganizacaoModal';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Search,
  Briefcase,
  Building2,
  MapPin,
  Calendar,
  Award,
  ChevronRight,
  Loader2,
  CheckCircle2,
  XCircle,
  Clock,
  Send,
  Mail,
  Check,
  X,
  Star,
} from 'lucide-react';
import { candidaturaService, type CandidaturaComDetalhes } from '@/services/candidatura.service';
import { candidatoService, type ConviteProcesso } from '@/services/candidato.service';
import { processoSeletivoService } from '@/services/processo-seletivo.service';
import { avaliacaoOrganizacaoService } from '@/services/avaliacao-organizacao.service';
import { getUsuarioIdFromToken } from '@/lib/jwt';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

interface CandidaturaStatus {
  processoFinalizado: boolean;
  processoId: string | null;
  jaAvaliou: boolean;
}

export default function MinhasCandidaturas() {
  usePageTitle('Minhas Candidaturas');
  const [candidaturas, setCandidaturas] = useState<CandidaturaComDetalhes[]>([]);
  const [convites, setConvites] = useState<ConviteProcesso[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [modalidadeFilter, setModalidadeFilter] = useState<string>('all');
  const [processingInvite, setProcessingInvite] = useState<string | null>(null);
  const [showPersonalizarModal, setShowPersonalizarModal] = useState(false);
  const [conviteSelecionado, setConviteSelecionado] = useState<ConviteProcesso | null>(null);
  
  const [showAvaliacaoModal, setShowAvaliacaoModal] = useState(false);
  const [candidaturaParaAvaliar, setCandidaturaParaAvaliar] = useState<CandidaturaComDetalhes | null>(null);
  const [processoIdParaAvaliar, setProcessoIdParaAvaliar] = useState<string | null>(null);
  const [candidaturasStatus, setCandidaturasStatus] = useState<Map<string, CandidaturaStatus>>(new Map());

  const navigate = useNavigate();

  useEffect(() => {
    loadCandidaturas();
    loadConvites();
  }, []);

  const loadCandidaturas = async () => {
    setLoading(true);
    try {
      const data = await candidaturaService.listarMinhasCandidaturas();
      console.log('Candidaturas carregadas:', data.length);
      setCandidaturas(data);
      
      await loadCandidaturasStatus(data);
    } catch (error) {
      console.error('Erro ao carregar candidaturas:', error);
      toast.error('Erro ao carregar candidaturas');
      setCandidaturas([]);
    } finally {
      setLoading(false);
    }
  };

  const loadCandidaturasStatus = async (candidaturas: CandidaturaComDetalhes[]) => {
    const statusMap = new Map<string, CandidaturaStatus>();
    
    await Promise.all(
      candidaturas.map(async (candidatura) => {
        try {
          const response = await processoSeletivoService.buscarPorCandidatura(candidatura.id);
          const processo = response.processo;
          
          if (processo) {
            const processoFinalizado = processo.dataFim !== null;
            let jaAvaliou = false;
            
            if (processoFinalizado) {
              const avaliacao = await avaliacaoOrganizacaoService.buscarPorProcesso(processo.id);
              jaAvaliou = avaliacao !== null;
            }
            
            statusMap.set(candidatura.id, {
              processoFinalizado,
              processoId: processo.id,
              jaAvaliou,
            });
          }
        } catch (error) {
          console.error(`Erro ao carregar status da candidatura ${candidatura.id}:`, error);
          statusMap.set(candidatura.id, {
            processoFinalizado: false,
            processoId: null,
            jaAvaliou: false,
          });
        }
      })
    );
    
    setCandidaturasStatus(statusMap);
  };

  const loadConvites = async () => {
    try {
      const usuarioId = getUsuarioIdFromToken();
      if (!usuarioId) {
        console.error('UsuarioId not found in token');
        return;
      }
      const data = await candidatoService.listarConvites(usuarioId);
      console.log('Convites carregados:', data.length);
      setConvites(data);
    } catch (error) {
      console.error('Erro ao carregar convites:', error);
      setConvites([]);
    }
  };

  const handleAcceptInvite = (convite: ConviteProcesso) => {
    setConviteSelecionado(convite);
    setShowPersonalizarModal(true);
  };

  const handleSubmitAceitarConvite = async (modelo: string, conteudoPersonalizado: string) => {
    if (!conviteSelecionado) return;

    setProcessingInvite(conviteSelecionado.id);
    try {
      const usuarioId = getUsuarioIdFromToken();
      if (!usuarioId) {
        toast.error('Erro ao obter usuário');
        return;
      }
      
      await candidatoService.aceitarConviteComCurriculo(
        usuarioId, 
        conviteSelecionado.id,
        modelo,
        conteudoPersonalizado
      );
      
      toast.success('Convite aceito! Candidatura criada com sucesso.');
      setShowPersonalizarModal(false);
      setConviteSelecionado(null);
      await loadConvites();
      await loadCandidaturas();
    } catch (error: any) {
      console.error('Erro ao aceitar convite:', error);
      toast.error(error.response?.data?.message || 'Erro ao aceitar convite');
    } finally {
      setProcessingInvite(null);
    }
  };

  const handleRejectInvite = async (conviteId: string) => {
    setProcessingInvite(conviteId);
    try {
      const usuarioId = getUsuarioIdFromToken();
      if (!usuarioId) {
        toast.error('Erro ao obter usuário');
        return;
      }
      await candidatoService.recusarConvite(usuarioId, conviteId);
      toast.success('Convite recusado.');
      await loadConvites();
    } catch (error: any) {
      console.error('Erro ao recusar convite:', error);
      toast.error(error.response?.data?.message || 'Erro ao recusar convite');
    } finally {
      setProcessingInvite(null);
    }
  };

  const handleOpenAvaliacaoModal = (candidatura: CandidaturaComDetalhes, processoId: string) => {
    setCandidaturaParaAvaliar(candidatura);
    setProcessoIdParaAvaliar(processoId);
    setShowAvaliacaoModal(true);
  };

  const handleAvaliacaoSuccess = async () => {
    await loadCandidaturasStatus(candidaturas);
    setCandidaturaParaAvaliar(null);
    setProcessoIdParaAvaliar(null);
  };

  const podeAvaliarOrganizacao = (candidaturaId: string): { pode: boolean; processoId: string | null } => {
    const status = candidaturasStatus.get(candidaturaId);
    
    if (!status || !status.processoId) {
      return { pode: false, processoId: null };
    }
    
    const pode = status.processoFinalizado && !status.jaAvaliou;
    return { pode, processoId: status.processoId };
  };

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      PENDENTE: { label: 'Pendente', className: 'bg-yellow-100 text-yellow-800' },
      ACEITA: { label: 'Aceita', className: 'bg-green-100 text-green-800' },
      REJEITADA: { label: 'Rejeitada', className: 'bg-red-100 text-red-800' },
      EM_PROCESSO: { label: 'Em Processo', className: 'bg-blue-100 text-blue-800' },
      FINALIZADA: { label: 'Finalizada', className: 'bg-gray-100 text-gray-800' },
      DESISTENTE: { label: 'Desistente', className: 'bg-orange-100 text-orange-800' },
    };
    const config = statusMap[status] || statusMap.PENDENTE;
    return <Badge className={config.className}>{config.label}</Badge>;
  };

  const getModalidadeBadge = (modalidade: string) => {
    const modalidadeMap: Record<string, { label: string; className: string }> = {
      PRESENCIAL: { label: 'Presencial', className: 'bg-blue-100 text-blue-800' },
      REMOTO: { label: 'Remoto', className: 'bg-purple-100 text-purple-800' },
      HIBRIDO: { label: 'Híbrido', className: 'bg-indigo-100 text-indigo-800' },
    };
    const config = modalidadeMap[modalidade] || modalidadeMap.PRESENCIAL;
    return <Badge variant="outline" className={config.className}>{config.label}</Badge>;
  };

  const formatSalario = (salario?: number) => {
    if (!salario) return 'A combinar';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(salario);
  };

  const getDiasDesde = (data: string) => {
    const candidatura = new Date(data);
    const hoje = new Date();
    const diff = Math.floor((hoje.getTime() - candidatura.getTime()) / (1000 * 60 * 60 * 24));
    if (diff === 0) return 'Hoje';
    if (diff === 1) return 'Ontem';
    return `Há ${diff} dias`;
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setStatusFilter('all');
    setModalidadeFilter('all');
  };

  const candidaturasFiltradas = candidaturas.filter((candidatura) => {
    const matchSearch =
      !searchTerm ||
      candidatura.vaga.titulo.toLowerCase().includes(searchTerm.toLowerCase()) ||
      candidatura.vaga.organizacao.nome.toLowerCase().includes(searchTerm.toLowerCase());

    const matchStatus = !statusFilter || statusFilter === 'all' || candidatura.status === statusFilter;
    const matchModalidade = !modalidadeFilter || modalidadeFilter === 'all' || candidatura.vaga.modalidade === modalidadeFilter;

    return matchSearch && matchStatus && matchModalidade;
  });

  const convitesPendentes = convites.filter((c) => c.status === 'PENDENTE');

  const stats = {
    total: candidaturas.length,
    emProcesso: candidaturas.filter((c) => c.status === 'EM_PROCESSO').length,
    aceitas: candidaturas.filter((c) => c.status === 'ACEITA').length,
    rejeitadas: candidaturas.filter((c) => c.status === 'REJEITADA').length,
    convitesPendentes: convitesPendentes.length,
  };

  if (loading) {
    return (
      <div className="p-6">
        <Card>
          <CardContent className="p-12 flex items-center justify-center">
            <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
            <p className="ml-4 text-muted-foreground">Carregando candidaturas...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl sm:text-3xl font-bold">Minhas Candidaturas</h2>
        <p className="text-sm sm:text-base text-muted-foreground mt-1">
          Acompanhe o status das suas candidaturas e processos seletivos
        </p>
      </div>

      {/* Estatísticas */}
      <div className="grid gap-3 sm:gap-4 grid-cols-2 sm:grid-cols-3 lg:grid-cols-5">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total</p>
                <p className="text-2xl font-bold">{stats.total}</p>
              </div>
              <Send className="w-8 h-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Em Processo</p>
                <p className="text-2xl font-bold text-blue-600">{stats.emProcesso}</p>
              </div>
              <Clock className="w-8 h-8 text-blue-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Aceitas</p>
                <p className="text-2xl font-bold text-green-600">{stats.aceitas}</p>
              </div>
              <CheckCircle2 className="w-8 h-8 text-green-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Rejeitadas</p>
                <p className="text-2xl font-bold text-red-600">{stats.rejeitadas}</p>
              </div>
              <XCircle className="w-8 h-8 text-red-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Convites</p>
                <p className="text-2xl font-bold text-purple-600">{stats.convitesPendentes}</p>
              </div>
              <Mail className="w-8 h-8 text-purple-600" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Convites Pendentes */}
      {convitesPendentes.length > 0 && (
        <Card className="border-purple-200 bg-purple-50">
          <CardHeader>
            <div className="flex items-center gap-2">
              <Mail className="w-5 h-5 text-purple-600" />
              <CardTitle className="text-lg text-purple-900">Convites Pendentes</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {convitesPendentes.map((convite) => (
                <Card key={convite.id} className="bg-white">
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 space-y-3">
                        <div className="flex items-center gap-2">
                          <h4 className="font-semibold text-lg">{convite.tituloVaga}</h4>
                          <Badge className="bg-purple-100 text-purple-800">Convite</Badge>
                        </div>
                        
                        {/* Organization and Location */}
                        <div className="flex items-center gap-4 text-sm">
                          <span className="flex items-center gap-1 text-muted-foreground">
                            <Building2 className="w-4 h-4" />
                            {convite.nomeOrganizacao}
                          </span>
                          {convite.cidade && convite.uf && (
                            <span className="flex items-center gap-1 text-muted-foreground">
                              <MapPin className="w-4 h-4" />
                              {convite.cidade}, {convite.uf}
                            </span>
                          )}
                        </div>

                        {/* Modalidade and Salary */}
                        <div className="flex items-center gap-4 text-sm">
                          {convite.modalidade && (
                            <Badge variant="secondary">
                              {convite.modalidade === 'PRESENCIAL' ? 'Presencial' : 
                               convite.modalidade === 'REMOTO' ? 'Remoto' : 'Híbrido'}
                            </Badge>
                          )}
                          {(convite.salarioMinimo || convite.salarioMaximo) && (
                            <span className="text-muted-foreground">
                              {convite.salarioMinimo && convite.salarioMaximo
                                ? `R$ ${convite.salarioMinimo.toLocaleString()} - R$ ${convite.salarioMaximo.toLocaleString()}`
                                : convite.salarioMinimo
                                ? `A partir de R$ ${convite.salarioMinimo.toLocaleString()}`
                                : `Até R$ ${convite.salarioMaximo?.toLocaleString()}`}
                            </span>
                          )}
                        </div>

                        {/* Description */}
                        {convite.descricaoVaga && (
                          <p className="text-sm text-gray-600 line-clamp-2">
                            {convite.descricaoVaga}
                          </p>
                        )}

                        {/* Recruiter info */}
                        <p className="text-sm text-muted-foreground">
                          Convidado por: <span className="font-medium">{convite.nomeRecrutador}</span>
                        </p>
                        
                        {/* Custom message */}
                        {convite.mensagem && (
                          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                            <p className="text-sm text-blue-900">
                              <span className="font-medium">Mensagem:</span> {convite.mensagem}
                            </p>
                          </div>
                        )}
                        
                        {/* Dates */}
                        <div className="flex items-center gap-4 text-xs text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <Calendar className="w-3 h-3" />
                            Enviado: {new Date(convite.dataEnvio).toLocaleDateString('pt-BR')}
                          </span>
                          <span className="flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            Expira: {new Date(convite.dataExpiracao).toLocaleDateString('pt-BR')}
                          </span>
                        </div>
                      </div>
                      <div className="flex flex-col gap-2">
                        <Button
                          size="sm"
                          onClick={() => handleAcceptInvite(convite)}
                          disabled={processingInvite === convite.id}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <Check className="w-4 h-4 mr-1" />
                          Aceitar
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleRejectInvite(convite.id)}
                          disabled={processingInvite === convite.id}
                          className="border-red-300 text-red-600 hover:bg-red-50"
                        >
                          <X className="w-4 h-4 mr-1" />
                          Recusar
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Filtros */}
      <Card>
        <CardContent className="p-6">
          <div className="grid gap-4 md:grid-cols-4">
            <div className="relative">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por vaga ou empresa..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9"
              />
            </div>

            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger>
                <SelectValue placeholder="Filtrar por status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todos os status</SelectItem>
                <SelectItem value="PENDENTE">Pendente</SelectItem>
                <SelectItem value="EM_PROCESSO">Em Processo</SelectItem>
                <SelectItem value="ACEITA">Aceita</SelectItem>
                <SelectItem value="REJEITADA">Rejeitada</SelectItem>
                <SelectItem value="FINALIZADA">Finalizada</SelectItem>
              </SelectContent>
            </Select>

            <Select value={modalidadeFilter} onValueChange={setModalidadeFilter}>
              <SelectTrigger>
                <SelectValue placeholder="Filtrar por modalidade" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todas as modalidades</SelectItem>
                <SelectItem value="PRESENCIAL">Presencial</SelectItem>
                <SelectItem value="REMOTO">Remoto</SelectItem>
                <SelectItem value="HIBRIDO">Híbrido</SelectItem>
              </SelectContent>
            </Select>

            <Button variant="outline" onClick={handleClearFilters}>
              Limpar Filtros
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Lista de Candidaturas */}
      {candidaturasFiltradas.length === 0 ? (
        <Card>
          <CardContent className="p-12 text-center">
            <Briefcase className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">
              {searchTerm || statusFilter || modalidadeFilter
                ? 'Nenhuma candidatura encontrada'
                : 'Você ainda não se candidatou a nenhuma vaga'}
            </h3>
            <p className="text-muted-foreground mb-4">
              {searchTerm || statusFilter || modalidadeFilter
                ? 'Tente ajustar os filtros'
                : 'Explore as vagas disponíveis e candidate-se às que mais combinam com você'}
            </p>
            {!searchTerm && !statusFilter && !modalidadeFilter && (
              <Button onClick={() => navigate('/dashboard/buscar-vagas')}>
                <Search className="w-4 h-4 mr-2" />
                Buscar Vagas
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {candidaturasFiltradas.map((candidatura) => (
            <Card key={candidatura.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="space-y-2">
                  <div className="flex justify-between items-start gap-2">
                    <CardTitle className="text-lg line-clamp-2">
                      {candidatura.vaga.titulo}
                    </CardTitle>
                    {candidatura.compatibilidade && (
                      <Badge variant="secondary" className="flex items-center gap-1 shrink-0">
                        <Award className="w-3 h-3" />
                        {Math.round(candidatura.compatibilidade)}%
                      </Badge>
                    )}
                  </div>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Building2 className="w-4 h-4" />
                    <span className="truncate">{candidatura.vaga.organizacao.nome}</span>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* Status e Modalidade */}
                <div className="flex flex-wrap gap-2">
                  {getStatusBadge(candidatura.status)}
                  {getModalidadeBadge(candidatura.vaga.modalidade)}
                </div>

                {/* Informações da vaga */}
                <div className="space-y-2 text-sm">
                  {candidatura.vaga.endereco && (
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <MapPin className="w-4 h-4" />
                      <span>
                        {candidatura.vaga.endereco.cidade}, {candidatura.vaga.endereco.uf}
                      </span>
                    </div>
                  )}
                  {candidatura.vaga.salario && (
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <Briefcase className="w-4 h-4" />
                      <span>{formatSalario(candidatura.vaga.salario)}</span>
                    </div>
                  )}
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <Calendar className="w-4 h-4" />
                    <span>Candidatura: {getDiasDesde(candidatura.dataCandidatura)}</span>
                  </div>
                </div>

                {/* Ações */}
                <div className="pt-2 border-t">
                  <div className="flex flex-col gap-2 min-h-[76px]">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => navigate(`/dashboard/processo-seletivo/${candidatura.id}`)}
                    >
                      Ver Processo
                      <ChevronRight className="w-4 h-4 ml-1" />
                    </Button>
                    
                    {/* Botão de Avaliação - só aparece se processo finalizado e não avaliado */}
                    {(() => {
                      const avaliacaoInfo = podeAvaliarOrganizacao(candidatura.id);
                      if (avaliacaoInfo.pode && avaliacaoInfo.processoId) {
                        return (
                          <Button
                            size="sm"
                            variant="default"
                            className="bg-yellow-500 hover:bg-yellow-600 text-white"
                            onClick={() => handleOpenAvaliacaoModal(candidatura, avaliacaoInfo.processoId!)}
                          >
                            <Star className="w-4 h-4 mr-1" />
                            Avaliar Empresa
                          </Button>
                        );
                      }
                      return null;
                    })()}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Modal de Personalização de Currículo */}
      {conviteSelecionado && (
        <PersonalizarCurriculoModal
          open={showPersonalizarModal}
          onClose={() => {
            setShowPersonalizarModal(false);
            setConviteSelecionado(null);
          }}
          onSubmit={handleSubmitAceitarConvite}
          vagaTitulo={conviteSelecionado.tituloVaga}
          vagaId={conviteSelecionado.vagaId}
          isProcessing={processingInvite === conviteSelecionado.id}
        />
      )}

      {/* Modal de Avaliação de Organização */}
      {candidaturaParaAvaliar && processoIdParaAvaliar && (
        <AvaliarOrganizacaoModal
          isOpen={showAvaliacaoModal}
          onClose={() => {
            setShowAvaliacaoModal(false);
            setCandidaturaParaAvaliar(null);
            setProcessoIdParaAvaliar(null);
          }}
          processoId={processoIdParaAvaliar}
          nomeOrganizacao={candidaturaParaAvaliar.vaga.organizacao.nome}
          onSuccess={handleAvaliacaoSuccess}
        />
      )}
    </div>
  );
}
