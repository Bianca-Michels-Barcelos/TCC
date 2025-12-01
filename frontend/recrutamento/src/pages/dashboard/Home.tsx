import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { authService } from '@/services/auth.service';
import { getOrganizacaoIdFromToken } from '@/lib/jwt';
import { Users, Briefcase, TrendingUp, AlertCircle, Calendar, Plus, Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import dashboardService from '@/services/dashboard.service';
import type {
  DashboardRecrutador,
  DashboardCandidato,
} from '@/services/dashboard.service';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function Home() {
  usePageTitle('Dashboard');
  const user = authService.getUser();
  const navigate = useNavigate();
  const isRecrutador = user?.roles?.includes('ROLE_RECRUTADOR') || user?.roles?.includes('ROLE_ADMIN');
  const organizacaoId = getOrganizacaoIdFromToken();

  const [dashboardRecrutador, setDashboardRecrutador] = useState<DashboardRecrutador | null>(null);
  const [dashboardCandidato, setDashboardCandidato] = useState<DashboardCandidato | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true);
        setError(null);

        if (isRecrutador && organizacaoId) {
          const data = await dashboardService.getDashboardRecrutador(organizacaoId);
          setDashboardRecrutador(data);
        } else {
          const data = await dashboardService.getDashboardCandidato();
          setDashboardCandidato(data);
        }
      } catch (err) {
        console.error('Erro ao carregar dashboard:', err);
        setError('Erro ao carregar dados do dashboard');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, [isRecrutador, organizacaoId]);

  const formatarDataRelativa = (dataStr: string) => {
    const data = new Date(dataStr);
    const now = new Date();
    const diffMs = now.getTime() - data.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Agora';
    if (diffMins < 60) return `${diffMins}min atrás`;
    if (diffHours < 24) return `${diffHours}h atrás`;
    if (diffDays === 1) return 'Ontem';
    if (diffDays < 7) return `${diffDays} dias atrás`;

    return format(data, 'dd/MM/yyyy', { locale: ptBR });
  };

  const getCorAtividade = (tipo: string) => {
    switch (tipo) {
      case 'NOVA_CANDIDATURA':
        return 'bg-green-500';
      case 'MUDANCA_ETAPA':
      case 'NOVA_ETAPA':
        return 'bg-blue-500';
      case 'AVALIACAO':
        return 'bg-yellow-500';
      case 'MUDANCA_STATUS':
        return 'bg-purple-500';
      default:
        return 'bg-gray-500';
    }
  };

  if (loading) {
    return (
      <div className="p-6 space-y-6">
        <div>
          <Skeleton className="h-10 w-64 mb-2" />
          <Skeleton className="h-6 w-48" />
        </div>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {[...Array(4)].map((_, i) => (
            <Card key={i}>
              <CardHeader className="pb-2">
                <Skeleton className="h-4 w-24" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-16 mb-2" />
                <Skeleton className="h-3 w-32" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <Card className="border-destructive">
          <CardContent className="pt-6 flex items-center gap-4">
            <AlertCircle className="h-5 w-5 text-destructive" />
            <p className="text-destructive">{error}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isRecrutador && dashboardRecrutador) {
    return (
      <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
        <div>
          <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3 mb-2">
            <h2 className="text-2xl sm:text-3xl font-bold">Bem-vindo, {user?.nome}!</h2>
            <Badge variant="secondary" className="w-fit">
              {user?.roles?.includes('ROLE_ADMIN') ? 'Admin' : 'Recrutador'}
            </Badge>
          </div>
        </div>

        {/* Métricas Principais */}
        <div className="grid gap-4 sm:gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
          <Card className="border-blue-200 bg-gradient-to-br from-blue-50 to-white">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-blue-900">Vagas Ativas</CardTitle>
              <div className="p-2 bg-blue-500 rounded-lg">
                <Briefcase className="h-4 w-4 text-white" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-blue-700">{dashboardRecrutador.vagasAtivasCount}</div>
              <p className="text-xs text-blue-600 mt-1">
                Vagas abertas no momento
              </p>
            </CardContent>
          </Card>

          <Card className="border-purple-200 bg-gradient-to-br from-purple-50 to-white">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-purple-900">Total de Candidatos</CardTitle>
              <div className="p-2 bg-purple-500 rounded-lg">
                <Users className="h-4 w-4 text-white" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-purple-700">{dashboardRecrutador.totalCandidatosCount}</div>
              <p className="text-xs text-purple-600 mt-1">
                Candidaturas recebidas
              </p>
            </CardContent>
          </Card>

          <Card className="border-yellow-200 bg-gradient-to-br from-yellow-50 to-white">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-yellow-900">Candidaturas Pendentes</CardTitle>
              <div className="p-2 bg-yellow-500 rounded-lg">
                <AlertCircle className="h-4 w-4 text-white" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-yellow-700">{dashboardRecrutador.candidaturasPendentesCount}</div>
              <p className="text-xs text-yellow-600 mt-1">
                Aguardando avaliação
              </p>
            </CardContent>
          </Card>

          <Card className="border-green-200 bg-gradient-to-br from-green-50 to-white">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-green-900">Taxa de Conversão</CardTitle>
              <div className="p-2 bg-green-500 rounded-lg">
                <TrendingUp className="h-4 w-4 text-white" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-green-700">
                {dashboardRecrutador.taxaConversao.toFixed(1)}%
              </div>
              <p className="text-xs text-green-600 mt-1">
                Candidatos aceitos
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Ações Rápidas */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">Ações Rápidas</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3">
              <Button onClick={() => navigate('/dashboard/nova-vaga')} className="w-full h-11">
                <Plus className="mr-2 h-4 w-4" />
                Criar Nova Vaga
              </Button>
              <Button variant="outline" onClick={() => navigate('/dashboard/buscar-candidatos')} className="w-full h-11">
                <Search className="mr-2 h-4 w-4" />
                Buscar Candidatos
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Vagas que Precisam de Atenção */}
        {dashboardRecrutador.vagasAtencao.length > 0 && (
          <Card className="border-yellow-500">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <AlertCircle className="h-5 w-5 text-yellow-500" />
                Vagas Precisando de Atenção
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {dashboardRecrutador.vagasAtencao.map((vaga) => (
                  <div
                    key={vaga.vagaId}
                    className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent cursor-pointer"
                    onClick={() => navigate(`/dashboard/minhas-vagas`)}
                  >
                    <div>
                      <p className="font-medium">{vaga.titulo}</p>
                      <p className="text-sm text-muted-foreground">
                        {vaga.quantidadePendente} candidaturas pendentes
                      </p>
                    </div>
                    <Badge variant="outline" className="bg-yellow-50">
                      {vaga.motivoAlerta.replace('_', ' ')}
                    </Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Entrevistas Próximas */}
        {dashboardRecrutador.entrevistasProximas.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="h-5 w-5" />
                Entrevistas Próximas
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {dashboardRecrutador.entrevistasProximas.map((entrevista) => (
                  <div
                    key={entrevista.conviteId}
                    className="flex items-center justify-between p-3 border rounded-lg"
                  >
                    <div className="flex-1">
                      <p className="font-medium">{entrevista.nomeCandidato}</p>
                      <p className="text-sm text-muted-foreground">{entrevista.tituloVaga}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        Expira em: {format(new Date(entrevista.dataExpiracao), 'dd/MM/yyyy HH:mm', { locale: ptBR })}
                      </p>
                    </div>
                    <Badge variant={entrevista.status === 'PENDENTE' ? 'secondary' : 'default'}>
                      {entrevista.status}
                    </Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Atividades Recentes */}
        <Card>
          <CardHeader>
            <CardTitle>Atividades Recentes</CardTitle>
          </CardHeader>
          <CardContent>
            {dashboardRecrutador.atividadesRecentes.length === 0 ? (
              <p className="text-muted-foreground text-center py-8">
                Nenhuma atividade recente
              </p>
            ) : (
              <div className="space-y-4">
                {dashboardRecrutador.atividadesRecentes.map((atividade, index) => (
                  <div
                    key={`${atividade.candidaturaId}-${index}`}
                    className="flex items-center gap-4 p-3 border rounded-lg hover:bg-accent cursor-pointer"
                    onClick={() => {
                      if (atividade.processoId) {
                        navigate(`/dashboard/gerenciar-processo/${atividade.processoId}`);
                      } else {
                        navigate(`/dashboard/minhas-vagas`);
                      }
                    }}
                  >
                    <div className={`w-2 h-2 rounded-full ${getCorAtividade(atividade.tipo)}`} />
                    <div className="flex-1">
                      <p className="font-medium">{atividade.descricao}</p>
                      <p className="text-sm text-muted-foreground">
                        {atividade.nomeCandidato} - {atividade.tituloVaga}
                      </p>
                    </div>
                    <span className="text-sm text-muted-foreground">
                      {formatarDataRelativa(atividade.dataHora)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    );
  }

  if (dashboardCandidato) {
    return (
      <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
        <div>
          <h2 className="text-2xl sm:text-3xl font-bold">Bem-vindo, {user?.nome}!</h2>
          <p className="text-sm sm:text-base text-muted-foreground mt-2">
            Aqui está um resumo das suas atividades
          </p>
        </div>

        {/* Métricas Principais */}
        <div className="grid gap-4 sm:gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
          <Card className="border-blue-200 bg-gradient-to-br from-blue-50 to-white">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-blue-900">
                <div className="p-2 bg-blue-500 rounded-lg">
                  <Briefcase className="h-4 w-4 text-white" />
                </div>
                Candidaturas Ativas
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-blue-700">{dashboardCandidato.candidaturasAtivasCount}</div>
              <p className="text-sm text-blue-600 mt-2">
                Processos em andamento
              </p>
            </CardContent>
          </Card>

          <Card className="border-purple-200 bg-gradient-to-br from-purple-50 to-white">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-purple-900">
                <div className="p-2 bg-purple-500 rounded-lg">
                  <Calendar className="h-4 w-4 text-white" />
                </div>
                Vagas Salvas
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-purple-700">{dashboardCandidato.vagasSalvasCount}</div>
              <p className="text-sm text-purple-600 mt-2">
                Para candidatura futura
              </p>
            </CardContent>
          </Card>

          <Card className="border-green-200 bg-gradient-to-br from-green-50 to-white">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-green-900">
                <div className="p-2 bg-green-500 rounded-lg">
                  <TrendingUp className="h-4 w-4 text-white" />
                </div>
                Taxa de Resposta
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-green-700">{dashboardCandidato.taxaResposta.toFixed(1)}%</div>
              <p className="text-sm text-green-600 mt-2">
                Das suas candidaturas
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Ações Rápidas */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">Ações Rápidas</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3">
              <Button onClick={() => navigate('/dashboard/buscar-vagas')} className="w-full h-11">
                <Search className="mr-2 h-4 w-4" />
                Buscar Vagas
              </Button>
              <Button variant="outline" onClick={() => navigate('/dashboard/minhas-candidaturas')} className="w-full h-11">
                Ver Minhas Candidaturas
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Próximas Etapas */}
        {dashboardCandidato.proximasEtapas.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="h-5 w-5" />
                Próximas Etapas
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {dashboardCandidato.proximasEtapas.map((etapa, index) => (
                  <div
                    key={`${etapa.processoId || etapa.vagaId}-${index}`}
                    className="flex items-center justify-between p-3 border rounded-lg"
                  >
                    <div className="flex-1">
                      <p className="font-medium">{etapa.tituloVaga}</p>
                      <p className="text-sm text-muted-foreground">{etapa.nomeOrganizacao}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        Etapa: {etapa.etapaAtual} - {etapa.acao}
                      </p>
                    </div>
                    <Badge variant="secondary">{etapa.tipoEtapa}</Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Atualizações Recentes */}
        <Card>
          <CardHeader>
            <CardTitle>Atualizações Recentes</CardTitle>
          </CardHeader>
          <CardContent>
            {dashboardCandidato.atualizacoesRecentes.length === 0 ? (
              <p className="text-muted-foreground text-center py-8">
                Nenhuma atualização recente
              </p>
            ) : (
              <div className="space-y-4">
                {dashboardCandidato.atualizacoesRecentes.map((atualizacao, index) => (
                  <div
                    key={`${atualizacao.candidaturaId}-${index}`}
                    className="flex items-center gap-4 p-3 border rounded-lg hover:bg-accent cursor-pointer"
                    onClick={() => {
                      if (atualizacao.candidaturaId) {
                        navigate(`/dashboard/processo-seletivo/${atualizacao.candidaturaId}`);
                      } else {
                        navigate('/dashboard/minhas-candidaturas');
                      }
                    }}
                  >
                    <div className={`w-2 h-2 rounded-full ${getCorAtividade(atualizacao.tipo)}`} />
                    <div className="flex-1">
                      <p className="font-medium">{atualizacao.descricao}</p>
                      <p className="text-sm text-muted-foreground">
                        {atualizacao.tituloVaga} - {atualizacao.nomeOrganizacao}
                      </p>
                    </div>
                    <span className="text-sm text-muted-foreground">
                      {formatarDataRelativa(atualizacao.dataHora)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    );
  }

  return null;
}
