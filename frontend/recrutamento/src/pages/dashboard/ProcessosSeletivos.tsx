import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Users,
  Search,
  ArrowLeft,
  Clock,
  CheckCircle2,
  XCircle,
  Loader2,
  TrendingUp,
  Award,
  ChevronRight,
  FileText,
} from 'lucide-react';
import {
  processoSeletivoService,
  etapaProcessoService,
  type ProcessoSeletivoComCandidato,
  type EtapaProcesso,
} from '@/services/processo-seletivo.service';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function ProcessosSeletivos() {
  usePageTitle('Processos Seletivos');
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const vagaId = searchParams.get('vagaId');

  const [processos, setProcessos] = useState<ProcessoSeletivoComCandidato[]>([]);
  const [etapas, setEtapas] = useState<EtapaProcesso[]>([]);
  const [loading, setLoading] = useState(true);
  const [processoSelecionado, setProcessoSelecionado] = useState<string | null>(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [etapaFilter, setEtapaFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');

  useEffect(() => {
    const timer = setTimeout(() => {
      if (vagaId) {
        loadData();
      } else if (!loading) {
        toast.error('ID da vaga não fornecido. Redirecionando...');
        setTimeout(() => navigate('/dashboard/minhas-vagas'), 1500);
      }
    }, 100);

    return () => clearTimeout(timer);
  }, [vagaId]);

  const loadData = async () => {
    if (!vagaId) return;

    setLoading(true);
    try {
      console.log('Carregando processos para vaga:', vagaId);

      const [processosData, etapasData] = await Promise.all([
        processoSeletivoService.listarComCandidatosPorVaga(vagaId),
        etapaProcessoService.listarPorVaga(vagaId),
      ]);

      console.log('Processos carregados:', processosData.length);
      console.log('Etapas carregadas:', etapasData.length);

      setProcessos(processosData);
      setEtapas(etapasData.sort((a, b) => a.ordem - b.ordem));
    } catch (error: any) {
      console.error('Erro ao carregar dados:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Erro desconhecido';
      toast.error(`Erro ao carregar processos: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  const getDiasNaEtapa = (dataUltimaMudanca: string, dataCandidatura: string) => {
    const dataRef = dataUltimaMudanca || dataCandidatura;
    const diff = Math.floor(
      (new Date().getTime() - new Date(dataRef).getTime()) / (1000 * 60 * 60 * 24)
    );
    if (diff === 0) return 'Hoje';
    if (diff === 1) return '1 dia';
    return `${diff} dias`;
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

  const getEtapaStatusColor = (status: string) => {
    const colorMap: Record<string, string> = {
      PENDENTE: 'border-gray-300 bg-gray-50',
      EM_ANDAMENTO: 'border-blue-500 bg-blue-50',
      CONCLUIDA: 'border-green-500 bg-green-50',
      CANCELADA: 'border-red-500 bg-red-50',
    };
    return colorMap[status] || colorMap.PENDENTE;
  };

  const processosFiltrados = processos.filter((processo) => {
    const matchSearch =
      !searchTerm ||
      processo.candidatoNome.toLowerCase().includes(searchTerm.toLowerCase()) ||
      processo.candidatoEmail.toLowerCase().includes(searchTerm.toLowerCase());

    const matchEtapa = !etapaFilter || processo.etapaAtualId === etapaFilter;
    const matchStatus = !statusFilter || processo.statusCandidatura === statusFilter;

    return matchSearch && matchEtapa && matchStatus;
  });

  const stats = {
    total: processos.length,
    emAndamento: processos.filter((p) => !p.dataFim).length,
    finalizados: processos.filter((p) => p.dataFim).length,
    aprovados: processos.filter((p) => p.statusCandidatura === 'ACEITA').length,
    reprovados: processos.filter((p) => p.statusCandidatura === 'REJEITADA').length,
  };

  const porEtapa = etapas.map((etapa) => ({
    etapa,
    count: processos.filter((p) => p.etapaAtualId === etapa.id).length,
  }));

  if (!vagaId) {
    return null;
  }

  if (loading) {
    return (
      <div className="p-6">
        <Card>
          <CardContent className="p-12 flex items-center justify-center">
            <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
            <p className="ml-4 text-muted-foreground">Carregando processos seletivos...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/dashboard/minhas-vagas')}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div className="flex-1">
          <h2 className="text-3xl font-bold">Processos Seletivos</h2>
          <p className="text-muted-foreground mt-1">
            {processos.length > 0 && `${processos[0].vagaTitulo}`}
          </p>
        </div>
      </div>

      {/* Estatísticas */}
      <div className="grid gap-4 md:grid-cols-5">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total</p>
                <p className="text-2xl font-bold">{stats.total}</p>
              </div>
              <Users className="w-8 h-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Em Andamento</p>
                <p className="text-2xl font-bold text-blue-600">{stats.emAndamento}</p>
              </div>
              <Clock className="w-8 h-8 text-blue-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Finalizados</p>
                <p className="text-2xl font-bold">{stats.finalizados}</p>
              </div>
              <CheckCircle2 className="w-8 h-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Aprovados</p>
                <p className="text-2xl font-bold text-green-600">{stats.aprovados}</p>
              </div>
              <TrendingUp className="w-8 h-8 text-green-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Reprovados</p>
                <p className="text-2xl font-bold text-red-600">{stats.reprovados}</p>
              </div>
              <XCircle className="w-8 h-8 text-red-600" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Distribuição por Etapa */}
      {etapas.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Distribuição por Etapa</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-3 md:grid-cols-4 lg:grid-cols-6">
              {porEtapa.map(({ etapa, count }) => (
                <div
                  key={etapa.id}
                  className={`border-2 rounded-lg p-3 ${getEtapaStatusColor(etapa.status)}`}
                >
                  <div className="text-xs font-medium text-muted-foreground mb-1">
                    Etapa {etapa.ordem}
                  </div>
                  <div className="font-semibold text-sm mb-1">{etapa.nome}</div>
                  <div className="text-2xl font-bold">{count}</div>
                </div>
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
                placeholder="Buscar por nome ou email..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9"
              />
            </div>

            <Select value={etapaFilter} onValueChange={setEtapaFilter}>
              <SelectTrigger>
                <SelectValue placeholder="Filtrar por etapa" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todas as etapas</SelectItem>
                {etapas.map((etapa) => (
                  <SelectItem key={etapa.id} value={etapa.id}>
                    {etapa.ordem}. {etapa.nome}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

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

            <Button
              variant="outline"
              onClick={() => {
                setSearchTerm('');
                setEtapaFilter('');
                setStatusFilter('');
              }}
            >
              Limpar Filtros
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Lista de Candidatos */}
      {processosFiltrados.length === 0 ? (
        <Card>
          <CardContent className="p-12 text-center">
            <Users className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">Nenhum candidato encontrado</h3>
            <p className="text-muted-foreground">
              {searchTerm || etapaFilter || statusFilter
                ? 'Tente ajustar os filtros'
                : 'Ainda não há candidatos para esta vaga'}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {processosFiltrados.map((processo) => {
            const isRejeitada = processo.statusCandidatura === 'REJEITADA';
            const isAceita = processo.statusCandidatura === 'ACEITA';
            const isPendente = processo.statusCandidatura === 'PENDENTE';
            
            return (
            <Card 
              key={processo.processoId} 
              className={`hover:shadow-lg transition-all cursor-pointer ${
                processoSelecionado === processo.processoId 
                  ? 'ring-2 ring-primary shadow-xl scale-[1.02]' 
                  : ''
              } ${
                isPendente
                  ? 'border-yellow-500 border-2 bg-yellow-50/30'
                  : isAceita
                  ? 'border-green-500 border-2 bg-green-50/30' 
                  : isRejeitada
                  ? 'border-red-500 border-2 bg-red-50/30'
                  : ''
              }`}
              onClick={() => setProcessoSelecionado(processo.processoId)}
            >
              <CardHeader>
                <div className="flex justify-between items-start gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <CardTitle className="text-lg truncate">
                        {processo.candidatoNome}
                      </CardTitle>
                      {isPendente && (
                        <Badge className="bg-yellow-600 text-white flex items-center gap-1 shrink-0">
                          <Clock className="w-3 h-3" />
                          Aguardando Aprovação
                        </Badge>
                      )}
                      {isAceita && (
                        <Badge className="bg-green-600 text-white flex items-center gap-1 shrink-0">
                          <CheckCircle2 className="w-3 h-3" />
                          Escolhido
                        </Badge>
                      )}
                      {isRejeitada && (
                        <Badge className="bg-red-600 text-white flex items-center gap-1 shrink-0">
                          <XCircle className="w-3 h-3" />
                          Reprovado
                        </Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground truncate">
                      {processo.candidatoEmail}
                    </p>
                  </div>
                  {processo.compatibilidade && (
                    <Badge variant="secondary" className="flex items-center gap-1 shrink-0">
                      <Award className="w-3 h-3" />
                      {Math.round(processo.compatibilidade)}%
                    </Badge>
                  )}
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* Status */}
                <div className="flex items-center justify-between">
                  {getStatusBadge(processo.statusCandidatura)}
                  {processo.dataFim && (
                    <span className={`text-xs font-medium ${
                      isRejeitada ? 'text-red-700' : 'text-green-700'
                    }`}>
                      Finalizado em {new Date(processo.dataFim).toLocaleString('pt-BR', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  )}
                </div>

                {/* Etapa Atual */}
                {isPendente ? (
                  <div className="border-2 border-yellow-500 bg-yellow-50 rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <Clock className="w-5 h-5 text-yellow-600" />
                      <span className="font-semibold text-yellow-900">Aguardando Aprovação</span>
                    </div>
                    <p className="text-sm text-yellow-700">Candidatura precisa ser aceita ou rejeitada</p>
                  </div>
                ) : isAceita ? (
                  <div className="border-2 border-green-500 bg-green-50 rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <CheckCircle2 className="w-5 h-5 text-green-600" />
                      <span className="font-semibold text-green-900">Processo Concluído</span>
                    </div>
                    <p className="text-sm text-green-700">Candidato selecionado para a vaga</p>
                  </div>
                ) : isRejeitada ? (
                  <div className="border-2 border-red-500 bg-red-50 rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <XCircle className="w-5 h-5 text-red-600" />
                      <span className="font-semibold text-red-900">Processo Encerrado</span>
                    </div>
                    <p className="text-sm text-red-700">Candidato não foi selecionado</p>
                  </div>
                ) : (
                  <div className={`border-2 rounded-lg p-3 ${getEtapaStatusColor(processo.etapaAtualStatus)}`}>
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs font-medium text-muted-foreground">
                        Etapa {processo.etapaAtualOrdem}
                      </span>
                      <Badge variant="outline" className="text-xs">
                        <Clock className="w-3 h-3 mr-1" />
                        {getDiasNaEtapa(processo.dataUltimaMudanca, processo.dataCandidatura)}
                      </Badge>
                    </div>
                    <div className="font-semibold text-sm">{processo.etapaAtualNome}</div>
                  </div>
                )}

                {/* Informações adicionais */}
                <div className="text-sm space-y-1 border-t pt-3">
                  <div className="flex items-center justify-between text-muted-foreground">
                    <span>Candidatura:</span>
                    <span>
                      {new Date(processo.dataCandidatura).toLocaleDateString('pt-BR')}
                    </span>
                  </div>
                  {processo.arquivoCurriculo && (
                    <div className="flex items-center gap-1 text-muted-foreground">
                      <FileText className="w-4 h-4" />
                      <span className="text-xs">Currículo enviado</span>
                    </div>
                  )}
                </div>

                {/* Ações */}
                <div className="flex gap-2 pt-2 border-t">
                  <Button
                    variant="outline"
                    size="sm"
                    className="flex-1"
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`/dashboard/gerenciar-processo/${processo.processoId}`);
                    }}
                  >
                    Gerenciar
                    <ChevronRight className="w-4 h-4 ml-1" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          );
          })}
        </div>
      )}
    </div>
  );
}
