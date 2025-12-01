import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Plus,
  Edit,
  Users,
  Calendar,
  MapPin,
  Search,
  Briefcase,
  Clock,
  TrendingUp,
  CheckCircle2,
  XCircle,
  Loader2,
  MoreVertical,
  Lock,
  Ban,
} from 'lucide-react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { getOrganizacaoIdFromToken } from '@/lib/jwt';
import { vagaService, type VagaComEstatisticas, type Vaga } from '@/services/vaga.service';
import { toast } from 'sonner';
import { VagaDetailsView } from '@/components/vagas/VagaDetailsView';
import { CancelarVagaDialog } from '@/components/vagas/CancelarVagaDialog';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function MinhasVagas() {
  usePageTitle('Minhas Vagas');
  const [vagas, setVagas] = useState<VagaComEstatisticas[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [vagaParaFechar, setVagaParaFechar] = useState<string | null>(null);
  const [vagaParaCancelar, setVagaParaCancelar] = useState<string | null>(null);
  const [motivoCancelamento, setMotivoCancelamento] = useState('');
  const [viewMode, setViewMode] = useState<'list' | 'view'>('list');
  const [selectedVaga, setSelectedVaga] = useState<Vaga | null>(null);
  const [etapasVaga, setEtapasVaga] = useState<any[]>([]);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [vagaToCancel, setVagaToCancel] = useState<Vaga | null>(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [modalidadeFilter, setModalidadeFilter] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const navigate = useNavigate();
  const organizacaoId = getOrganizacaoIdFromToken();

  useEffect(() => {
    if (organizacaoId) {
      loadVagas();
    }
  }, [organizacaoId, searchTerm, statusFilter, modalidadeFilter, currentPage, pageSize]);

  const loadVagas = async () => {
    if (!organizacaoId) {
      toast.error('Empresa não identificada');
      return;
    }

    setLoading(true);
    try {
      const response = await vagaService.listarComEstatisticas(organizacaoId, {
        status: statusFilter || undefined,
        modalidade: modalidadeFilter || undefined,
        search: searchTerm || undefined,
        page: currentPage,
        size: pageSize,
      });

      setVagas(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Erro ao carregar vagas:', error);
      toast.error('Não foi possível carregar as vagas');
      setVagas([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFecharVaga = async () => {
    if (!vagaParaFechar) return;

    try {
      await vagaService.fecharVaga(vagaParaFechar);
      toast.success('Vaga fechada com sucesso');
      setVagaParaFechar(null);
      loadVagas();
    } catch (error: any) {
      console.error('Erro ao fechar vaga:', error);
      toast.error(error.response?.data?.message || 'Erro ao fechar vaga');
    }
  };

  const handleCancelarVaga = async () => {
    if (!vagaParaCancelar || !motivoCancelamento.trim()) {
      toast.error('Por favor, informe o motivo do cancelamento');
      return;
    }

    try {
      await vagaService.cancelarVaga(vagaParaCancelar, motivoCancelamento);
      toast.success('Vaga cancelada com sucesso');
      setVagaParaCancelar(null);
      setMotivoCancelamento('');
      loadVagas();
    } catch (error: any) {
      console.error('Erro ao cancelar vaga:', error);
      toast.error(error.response?.data?.message || 'Erro ao cancelar vaga');
    }
  };

  const handleViewVaga = async (vaga: VagaComEstatisticas) => {
    try {
      const vagaCompleta = await vagaService.buscarPorId(vaga.id);
      setSelectedVaga(vagaCompleta);
      
      const etapas = await vagaService.listarEtapas(vaga.id);
      setEtapasVaga(etapas);
      
      setViewMode('view');
    } catch (error) {
      console.error('Erro ao carregar detalhes da vaga:', error);
      toast.error('Não foi possível carregar os detalhes da vaga');
    }
  };

  const handleBackToList = () => {
    setSelectedVaga(null);
    setEtapasVaga([]);
    setViewMode('list');
  };

  const handleEditFromView = () => {
    if (selectedVaga) {
      navigate(`/dashboard/editar-vaga/${selectedVaga.id}`);
    }
  };

  const handleCancelFromView = () => {
    if (selectedVaga) {
      setVagaToCancel(selectedVaga);
      setCancelDialogOpen(true);
    }
  };

  const handleCancelConfirm = async (motivo: string) => {
    if (!vagaToCancel) return;

    try {
      await vagaService.cancelarVaga(vagaToCancel.id, motivo);
      toast.success('Vaga cancelada com sucesso');
      setCancelDialogOpen(false);
      setVagaToCancel(null);
      handleBackToList();
      loadVagas();
    } catch (error: any) {
      console.error('Erro ao cancelar vaga:', error);
      toast.error(error.response?.data?.message || 'Erro ao cancelar vaga');
      throw error;
    }
  };

  const getDiasDesdePub = (data: string) => {
    const publicacao = new Date(data);
    const hoje = new Date();
    const diff = Math.floor((hoje.getTime() - publicacao.getTime()) / (1000 * 60 * 60 * 24));
    if (diff === 0) return 'Hoje';
    if (diff === 1) return 'Ontem';
    return `${diff} dias atrás`;
  };

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      ABERTA: { label: 'Aberta', className: 'bg-green-100 text-green-800 hover:bg-green-200' },
      FECHADA: { label: 'Fechada', className: 'bg-orange-100 text-orange-800 hover:bg-orange-200' },
      CANCELADA: { label: 'Cancelada', className: 'bg-red-100 text-red-800 hover:bg-red-200' },
    };
    const config = statusMap[status] || statusMap.ABERTA;
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

  const getTipoContratoBadge = (tipo: string) => {
    const tipoMap: Record<string, { label: string }> = {
      ESTAGIO: { label: 'Estágio' },
      CLT: { label: 'CLT' },
      PJ: { label: 'PJ' },
    };
    const config = tipoMap[tipo] || tipoMap.CLT;
    return <Badge variant="secondary">{config.label}</Badge>;
  };

  const formatSalario = (salario?: number) => {
    if (!salario) return 'A combinar';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(salario);
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setStatusFilter('');
    setModalidadeFilter('');
    setCurrentPage(0);
  };

  const estadisticasGerais = {
    totalVagas: totalElements,
    vagasAbertas: vagas.filter((v) => v.status === 'ABERTA').length,
    totalCandidatos: vagas.reduce((sum, v) => sum + v.totalCandidatos, 0),
    mediaCandidatos: vagas.length > 0
      ? Math.round(vagas.reduce((sum, v) => sum + v.totalCandidatos, 0) / vagas.length)
      : 0,
  };

  if (viewMode === 'view' && selectedVaga) {
    return (
      <>
        <VagaDetailsView
          vaga={selectedVaga}
          etapas={etapasVaga}
          candidatosCount={selectedVaga.id ? (vagas.find(v => v.id === selectedVaga.id)?.totalCandidatos || 0) : 0}
          onBack={handleBackToList}
          onEdit={handleEditFromView}
          onCancel={handleCancelFromView}
        />

        <CancelarVagaDialog
          open={cancelDialogOpen}
          onOpenChange={setCancelDialogOpen}
          vagaTitulo={vagaToCancel?.titulo || ''}
          onConfirm={handleCancelConfirm}
        />
      </>
    );
  }

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div className="flex-1">
          <h2 className="text-2xl sm:text-3xl font-bold">Minhas Vagas</h2>
          <p className="text-sm sm:text-base text-muted-foreground mt-1">
            Gerencie suas vagas e acompanhe candidaturas
          </p>
        </div>
        <Button onClick={() => navigate('/dashboard/nova-vaga')} className="w-full sm:w-auto h-11">
          <Plus className="w-4 h-4 mr-2" />
          Nova Vaga
        </Button>
      </div>

      {/* Estatísticas */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total de Vagas</p>
                <p className="text-2xl font-bold">{estadisticasGerais.totalVagas}</p>
              </div>
              <Briefcase className="w-8 h-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Vagas Abertas</p>
                <p className="text-2xl font-bold text-green-600">
                  {estadisticasGerais.vagasAbertas}
                </p>
              </div>
              <TrendingUp className="w-8 h-8 text-green-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total Candidatos</p>
                <p className="text-2xl font-bold">{estadisticasGerais.totalCandidatos}</p>
              </div>
              <Users className="w-8 h-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Média/Vaga</p>
                <p className="text-2xl font-bold">{estadisticasGerais.mediaCandidatos}</p>
              </div>
              <Users className="w-8 h-8 text-blue-600" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filtros */}
      <Card>
        <CardContent className="p-6">
          <div className="grid gap-4 md:grid-cols-4">
            <div className="relative">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por título ou descrição..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(0);
                }}
                className="pl-9"
              />
            </div>

            <Select
              value={statusFilter}
              onValueChange={(value) => {
                setStatusFilter(value);
                setCurrentPage(0);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder="Filtrar por status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todos os status</SelectItem>
                <SelectItem value="ABERTA">Abertas</SelectItem>
                <SelectItem value="FECHADA">Fechadas</SelectItem>
                <SelectItem value="CANCELADA">Canceladas</SelectItem>
              </SelectContent>
            </Select>

            <Select
              value={modalidadeFilter}
              onValueChange={(value) => {
                setModalidadeFilter(value);
                setCurrentPage(0);
              }}
            >
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

      {/* Lista de Vagas */}
      {loading ? (
        <Card>
          <CardContent className="p-12 flex items-center justify-center">
            <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
            <p className="ml-4 text-muted-foreground">Carregando vagas...</p>
          </CardContent>
        </Card>
      ) : vagas.length === 0 ? (
        <Card>
          <CardContent className="p-12 text-center">
            <Briefcase className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">
              {searchTerm || statusFilter || modalidadeFilter
                ? 'Nenhuma vaga encontrada'
                : 'Nenhuma vaga cadastrada'}
            </h3>
            <p className="text-muted-foreground mb-4">
              {searchTerm || statusFilter || modalidadeFilter
                ? 'Tente ajustar os filtros ou criar uma nova vaga'
                : 'Comece criando sua primeira vaga para atrair candidatos'}
            </p>
            {!searchTerm && !statusFilter && !modalidadeFilter && (
              <Button onClick={() => navigate('/dashboard/nova-vaga')}>
                <Plus className="w-4 h-4 mr-2" />
                Criar Primeira Vaga
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-2">
            {vagas.map((vaga) => {
              let cardStyle = 'hover:shadow-lg transition-shadow';
              if (vaga.status === 'CANCELADA') {
                cardStyle += ' border-red-500 border-2 bg-red-50/20 opacity-75';
              } else if (vaga.status === 'FECHADA') {
                cardStyle += ' border-orange-500 border-2 bg-orange-50/20';
              } else if (vaga.candidatosAceitos > 0) {
                cardStyle += ' border-green-500 border-2 bg-green-50/20';
              }

              return (
              <Card 
                key={vaga.id} 
                className={`${cardStyle} cursor-pointer`}
                onClick={() => handleViewVaga(vaga)}
              >
                <CardHeader>
                  <div className="flex justify-between items-start gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-2">
                        <CardTitle className="text-xl truncate">{vaga.titulo}</CardTitle>
                        {vaga.status === 'CANCELADA' && (
                          <Badge className="bg-red-600 text-white flex items-center gap-1 shrink-0">
                            <Ban className="w-3 h-3" />
                            Cancelada
                          </Badge>
                        )}
                        {vaga.status === 'FECHADA' && (
                          <Badge className="bg-orange-600 text-white flex items-center gap-1 shrink-0">
                            <Lock className="w-3 h-3" />
                            Fechada
                          </Badge>
                        )}
                        {vaga.status === 'ABERTA' && vaga.candidatosAceitos > 0 && (
                          <Badge className="bg-green-600 text-white flex items-center gap-1 shrink-0">
                            <CheckCircle2 className="w-3 h-3" />
                            {vaga.candidatosAceitos} escolhido{vaga.candidatosAceitos > 1 ? 's' : ''}
                          </Badge>
                        )}
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {getStatusBadge(vaga.status)}
                        {getModalidadeBadge(vaga.modalidade)}
                        {getTipoContratoBadge(vaga.tipoContrato)}
                      </div>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <p className="text-sm text-muted-foreground line-clamp-2">
                    {vaga.descricao}
                  </p>

                  {/* Informações principais */}
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="flex items-center gap-2">
                      <Users className="w-4 h-4 text-blue-600" />
                      <span className="font-semibold">{vaga.totalCandidatos}</span>
                      <span className="text-muted-foreground">candidatos</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <TrendingUp className="w-4 h-4 text-green-600" />
                      <span className="font-semibold">{vaga.candidatosAtivos}</span>
                      <span className="text-muted-foreground">ativos</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                      <span className="font-semibold">{vaga.candidatosAceitos}</span>
                      <span className="text-muted-foreground">aceitos</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <XCircle className="w-4 h-4 text-red-600" />
                      <span className="font-semibold">{vaga.candidatosRejeitados}</span>
                      <span className="text-muted-foreground">rejeitados</span>
                    </div>
                  </div>

                  {/* Detalhes adicionais */}
                  <div className="space-y-2 text-sm border-t pt-3">
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <Calendar className="w-4 h-4" />
                      <span>Publicada {getDiasDesdePub(vaga.dataPublicacao)}</span>
                    </div>
                    {vaga.endereco && (
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <MapPin className="w-4 h-4" />
                        <span>
                          {typeof vaga.endereco.cidade === 'string'
                            ? vaga.endereco.cidade
                            : vaga.endereco.cidade},{' '}
                          {typeof vaga.endereco.uf === 'string'
                            ? vaga.endereco.uf
                            : vaga.endereco.uf.value}
                        </span>
                      </div>
                    )}
                    {vaga.salario && (
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Briefcase className="w-4 h-4" />
                        <span>{formatSalario(vaga.salario)}</span>
                      </div>
                    )}
                    {vaga.nomeNivelExperiencia && (
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Clock className="w-4 h-4" />
                        <span>{vaga.nomeNivelExperiencia}</span>
                      </div>
                    )}
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <TrendingUp className="w-4 h-4" />
                      <span>{vaga.totalEtapas} etapas configuradas</span>
                    </div>
                  </div>

                  {/* Ações */}
                  <div className="flex gap-2 pt-2 border-t">
                    <Button
                      variant="outline"
                      size="sm"
                      className="flex-1"
                      onClick={(e) => {
                        e.stopPropagation();
                        if (vaga.id) {
                          navigate(`/dashboard/processos-seletivos?vagaId=${vaga.id}`);
                        } else {
                          toast.error('ID da vaga não está disponível');
                        }
                      }}
                    >
                      <Users className="w-4 h-4 mr-2" />
                      Ver Candidatos
                    </Button>
                    
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={(e) => e.stopPropagation()}
                        >
                          <MoreVertical className="w-4 h-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/dashboard/editar-vaga/${vaga.id}`);
                        }}>
                          <Edit className="w-4 h-4 mr-2" />
                          Editar Vaga
                        </DropdownMenuItem>
                        
                        {vaga.status === 'ABERTA' && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem 
                              onClick={(e) => {
                                e.stopPropagation();
                                setVagaParaFechar(vaga.id);
                              }}
                              className="text-orange-600"
                            >
                              <Lock className="w-4 h-4 mr-2" />
                              Fechar Vaga
                            </DropdownMenuItem>
                          </>
                        )}
                        
                        {vaga.status !== 'CANCELADA' && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem 
                              onClick={(e) => {
                                e.stopPropagation();
                                setVagaParaCancelar(vaga.id);
                              }}
                              className="text-red-600"
                            >
                              <Ban className="w-4 h-4 mr-2" />
                              Cancelar Vaga
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </CardContent>
              </Card>
            );
            })}
          </div>

          {/* Paginação */}
          {totalPages > 1 && (
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Select
                      value={pageSize.toString()}
                      onValueChange={(value) => {
                        setPageSize(Number(value));
                        setCurrentPage(0);
                      }}
                    >
                      <SelectTrigger className="w-32">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="5">5 por página</SelectItem>
                        <SelectItem value="10">10 por página</SelectItem>
                        <SelectItem value="20">20 por página</SelectItem>
                        <SelectItem value="50">50 por página</SelectItem>
                      </SelectContent>
                    </Select>
                    <p className="text-sm text-muted-foreground">
                      Mostrando {currentPage * pageSize + 1} -{' '}
                      {Math.min((currentPage + 1) * pageSize, totalElements)} de{' '}
                      {totalElements} vagas
                    </p>
                  </div>

                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(currentPage - 1)}
                      disabled={currentPage === 0}
                    >
                      Anterior
                    </Button>
                    <span className="text-sm">
                      Página {currentPage + 1} de {totalPages}
                    </span>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(currentPage + 1)}
                      disabled={currentPage >= totalPages - 1}
                    >
                      Próxima
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}

      {/* Dialog para Fechar Vaga */}
      <AlertDialog open={!!vagaParaFechar} onOpenChange={() => setVagaParaFechar(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Fechar Vaga</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja fechar esta vaga? A vaga não aceitará mais candidaturas, 
              mas você ainda poderá gerenciar os candidatos existentes.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleFecharVaga} className="bg-orange-600 hover:bg-orange-700">
              Fechar Vaga
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Dialog para Cancelar Vaga */}
      <AlertDialog open={!!vagaParaCancelar} onOpenChange={(open) => {
        if (!open) {
          setVagaParaCancelar(null);
          setMotivoCancelamento('');
        }
      }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancelar Vaga</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja cancelar esta vaga? Esta ação não pode ser desfeita. 
              A vaga será removida e todos os processos seletivos serão encerrados.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="py-4">
            <label className="text-sm font-medium mb-2 block">Motivo do cancelamento *</label>
            <Textarea
              placeholder="Informe o motivo do cancelamento..."
              value={motivoCancelamento}
              onChange={(e) => setMotivoCancelamento(e.target.value)}
              rows={4}
              className="resize-none"
            />
          </div>
          <AlertDialogFooter>
            <AlertDialogCancel>Voltar</AlertDialogCancel>
            <AlertDialogAction onClick={handleCancelarVaga} className="bg-red-600 hover:bg-red-700">
              Cancelar Vaga
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
