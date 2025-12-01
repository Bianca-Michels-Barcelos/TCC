import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Separator } from '@/components/ui/separator';
import { AvaliarOrganizacaoModal } from '@/components/AvaliarOrganizacaoModal';
import {
  ArrowLeft,
  Building2,
  Calendar,
  CheckCircle2,
  Clock,
  MessageSquare,
  Briefcase,
  MapPin,
  Circle,
  Check,
  XCircle,
  Star,
} from 'lucide-react';
import api from '@/lib/api';
import { toast } from 'sonner';
import {
  processoSeletivoService,
  etapaProcessoService,
  type ProcessoSeletivo,
  type HistoricoEtapaProcesso,
  type EtapaProcesso,
} from '@/services/processo-seletivo.service';
import { avaliacaoOrganizacaoService, type AvaliacaoOrganizacao } from '@/services/avaliacao-organizacao.service';
import { usePageTitle } from '@/hooks/usePageTitle';

interface VagaInfo {
  id: string;
  titulo: string;
  descricao: string;
  salario?: number;
  modalidade: string;
  organizacao: {
    nome: string;
  };
  endereco?: {
    cidade: string;
    uf: string;
  };
}

export default function ProcessoSeletivo() {
  usePageTitle('Processo Seletivo');
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [processo, setProcesso] = useState<ProcessoSeletivo | null>(null);
  const [vaga, setVaga] = useState<VagaInfo | null>(null);
  const [historico, setHistorico] = useState<HistoricoEtapaProcesso[]>([]);
  const [etapas, setEtapas] = useState<EtapaProcesso[]>([]);
  const [etapaAtual, setEtapaAtual] = useState<EtapaProcesso | null>(null);
  const [loading, setLoading] = useState(true);
  const [avaliacao, setAvaliacao] = useState<AvaliacaoOrganizacao | null>(null);
  const [showAvaliacaoModal, setShowAvaliacaoModal] = useState(false);

  useEffect(() => {
    if (id) {
      loadProcesso();
    }
  }, [id]);

  const loadProcesso = async () => {
    setLoading(true);
    try {
      console.log('Buscando processo para candidatura:', id);

      const response = await processoSeletivoService.buscarPorCandidatura(id!);
      console.log('Resposta completa:', response);

      const processoData = response.processo;
      const vagaId = response.vagaId;

      console.log('Processo encontrado:', processoData);
      console.log('Vaga ID:', vagaId);
      setProcesso(processoData);

      const vagaResp = await api.get(`/vagas/publicas/${vagaId}`);
      console.log('Vaga encontrada:', vagaResp.data);
      setVaga(vagaResp.data);

      const etapasData = await etapaProcessoService.listarPorVaga(vagaId);
      const etapasOrdenadas = etapasData.sort((a, b) => a.ordem - b.ordem);
      console.log('Etapas encontradas:', etapasOrdenadas.length);
      setEtapas(etapasOrdenadas);

      const etapaAtualData = etapasOrdenadas.find(
        (e) => e.id === processoData.etapaProcessoAtualId
      );
      console.log('Etapa atual:', etapaAtualData);

      if (!etapaAtualData) {
        toast.error('Etapa atual do processo não foi encontrada');
      }
      setEtapaAtual(etapaAtualData || null);

      const historicoData = await processoSeletivoService.buscarHistorico(processoData.id);
      console.log('Histórico encontrado:', historicoData.length, 'itens');
      setHistorico(historicoData);

      if (processoData.dataFim) {
        try {
          const avaliacaoData = await avaliacaoOrganizacaoService.buscarPorProcesso(processoData.id);
          if (avaliacaoData) {
            console.log('Avaliação encontrada:', avaliacaoData);
            setAvaliacao(avaliacaoData);
          }
        } catch (error) {
          console.log('Nenhuma avaliação encontrada para este processo');
        }
      }
    } catch (error: any) {
      console.error('Erro ao carregar processo:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Erro desconhecido';

      if (error.response?.status === 404) {
        toast.error('Processo seletivo não encontrado para esta candidatura');
      } else {
        toast.error(`Erro ao carregar processo: ${errorMsg}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAvaliacaoSuccess = async () => {
    if (processo) {
      try {
        const avaliacaoData = await avaliacaoOrganizacaoService.buscarPorProcesso(processo.id);
        setAvaliacao(avaliacaoData);
      } catch (error) {
        console.error('Erro ao recarregar avaliação:', error);
      }
    }
  };

  const formatData = (data: string) => {
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatSalario = (salario?: number) => {
    if (!salario) return 'A combinar';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(salario);
  };

  const getStatusBadge = () => {
    if (!processo) return null;

    if (processo.dataFim) {
      return <Badge className="bg-gray-100 text-gray-800">Finalizado</Badge>;
    }
    return <Badge className="bg-blue-100 text-blue-800">Em Andamento</Badge>;
  };

  const calcularProgresso = () => {
    if (!etapaAtual || etapas.length === 0) return 0;
    const indiceAtual = etapas.findIndex((e) => e.id === etapaAtual.id);
    return ((indiceAtual + 1) / etapas.length) * 100;
  };

  if (loading) {
    return (
      <div className="p-6">
        <p className="text-muted-foreground">Carregando processo seletivo...</p>
      </div>
    );
  }

  if (!processo || !vaga || !etapaAtual) {
    return (
      <div className="p-6">
        <Card>
          <CardContent className="p-12 text-center space-y-4">
            <XCircle className="w-16 h-16 mx-auto text-red-500" />
            <div>
              <h3 className="text-xl font-semibold mb-2">Processo Seletivo Não Encontrado</h3>
              <p className="text-muted-foreground">
                {!processo && 'Não foi possível encontrar o processo seletivo para esta candidatura.'}
                {processo && !vaga && 'Informações da vaga não estão disponíveis.'}
                {processo && vaga && !etapaAtual && 'A etapa atual do processo não foi encontrada.'}
              </p>
              <p className="text-sm text-muted-foreground mt-2">
                Verifique o console do navegador para mais detalhes.
              </p>
            </div>
            <Button onClick={() => navigate('/dashboard/minhas-candidaturas')}>
              Voltar para Minhas Candidaturas
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const progresso = calcularProgresso();
  const etapaAtualIndex = etapas.findIndex((e) => e.id === etapaAtual.id);

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 sm:gap-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => navigate('/dashboard/minhas-candidaturas')}
          className="self-start"
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div className="flex-1 min-w-0">
          <h2 className="text-xl sm:text-2xl lg:text-3xl font-bold">Meu Processo Seletivo</h2>
          <p className="text-sm sm:text-base text-muted-foreground">Acompanhe o andamento da sua candidatura</p>
        </div>
        <div className="self-start sm:self-auto">
          {getStatusBadge()}
        </div>
      </div>

      {/* Informações da Vaga */}
      <Card className="bg-gradient-to-br from-blue-50 to-indigo-50 border-blue-200">
        <CardHeader>
          <div className="flex justify-between items-start">
            <div className="flex-1">
              <CardTitle className="text-2xl mb-2">{vaga.titulo}</CardTitle>
              <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Building2 className="w-4 h-4" />
                  {vaga.organizacao.nome}
                </span>
                {vaga.endereco && (
                  <span className="flex items-center gap-1">
                    <MapPin className="w-4 h-4" />
                    {vaga.endereco.cidade}, {vaga.endereco.uf}
                  </span>
                )}
                <span className="flex items-center gap-1">
                  <Briefcase className="w-4 h-4" />
                  {vaga.modalidade}
                </span>
              </div>
            </div>
            <Badge variant="outline" className="text-base bg-white">
              {formatSalario(vaga.salario)}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">{vaga.descricao}</p>
        </CardContent>
      </Card>

      {/* Banner de Processo Finalizado */}
      {processo.dataFim && etapaAtual.status === 'CONCLUIDA' && (
        <>
          <Card className="border-green-500 border-2 bg-green-50">
            <CardContent className="p-6">
              <div className="flex items-center gap-4">
                <div className="w-16 h-16 rounded-full bg-green-600 flex items-center justify-center">
                  <CheckCircle2 className="w-10 h-10 text-white" />
                </div>
                <div className="flex-1">
                  <h3 className="text-2xl font-bold text-green-900 mb-1">
                    Parabéns! Você foi selecionado!
                  </h3>
                  <p className="text-green-700">
                    Seu processo seletivo foi concluído com sucesso. Em breve a empresa entrará em contato.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Mensagem de Finalização */}
          <Card className="bg-gradient-to-br from-purple-50 to-pink-50 border-purple-200">
            <CardContent className="p-6">
              <div className="text-center">
                <CheckCircle2 className="w-16 h-16 mx-auto mb-4 text-purple-600" />
                <h3 className="font-semibold text-xl mb-2">Processo Finalizado</h3>
                <p className="text-muted-foreground mb-4">
                  Este processo seletivo foi concluído. Obrigado por participar!
                </p>
                <p className="text-sm text-muted-foreground">
                  Finalizado em {formatData(processo.dataFim)}
                </p>
              </div>
            </CardContent>
          </Card>
        </>
      )}

      {/* Banner para processo rejeitado */}
      {processo.dataFim && etapaAtual.status !== 'CONCLUIDA' && (
        <Card className="border-orange-500 border-2 bg-orange-50">
          <CardContent className="p-6">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-full bg-orange-600 flex items-center justify-center">
                <XCircle className="w-10 h-10 text-white" />
              </div>
              <div className="flex-1">
                <h3 className="text-2xl font-bold text-orange-900 mb-1">
                  Processo Seletivo Encerrado
                </h3>
                <p className="text-orange-700">
                  Infelizmente você não foi selecionado desta vez. Continue se candidatando e boa sorte!
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Call to Action ou Avaliação Existente - quando processo finalizado */}
      {processo.dataFim && (
        <>
          {avaliacao ? (
            <Card className="bg-gradient-to-br from-yellow-50 to-amber-50 border-yellow-200">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Star className="w-6 h-6 text-yellow-500 fill-yellow-500" />
                  Sua Avaliação da Organização
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium text-muted-foreground">Nota:</span>
                  <div className="flex gap-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star
                        key={star}
                        className={`w-5 h-5 ${
                          star <= avaliacao.nota
                            ? 'fill-yellow-400 text-yellow-400'
                            : 'text-gray-300'
                        }`}
                      />
                    ))}
                  </div>
                  <span className="text-sm font-medium">({avaliacao.nota}/5)</span>
                </div>
                
                <div className="space-y-2">
                  <p className="text-sm font-medium text-muted-foreground">Comentário:</p>
                  <div className="bg-white rounded-lg p-4 border border-yellow-200">
                    <p className="text-sm">{avaliacao.comentario}</p>
                  </div>
                </div>

                <div className="text-xs text-muted-foreground">
                  Avaliado em {new Date(avaliacao.criadoEm).toLocaleDateString('pt-BR', {
                    day: '2-digit',
                    month: 'long',
                    year: 'numeric',
                  })}
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card className="bg-gradient-to-br from-blue-50 to-indigo-50 border-blue-200">
              <CardContent className="p-6">
                <div className="flex items-center gap-4">
                  <div className="w-16 h-16 rounded-full bg-blue-600 flex items-center justify-center flex-shrink-0">
                    <Star className="w-8 h-8 text-white" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-bold text-blue-900 mb-2">
                      Conte-nos sobre sua experiência!
                    </h3>
                    <p className="text-blue-700 mb-4">
                      Sua opinião é muito importante. Avalie o processo seletivo e ajude outros candidatos.
                    </p>
                    <Button
                      onClick={() => setShowAvaliacaoModal(true)}
                      className="bg-yellow-500 hover:bg-yellow-600 text-white"
                    >
                      <Star className="w-4 h-4 mr-2" />
                      Avaliar Organização
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}

      {/* Progresso Geral */}
      <Card>
        <CardHeader>
          <CardTitle>Progresso no Processo Seletivo</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-sm mb-2">
                <span className="text-muted-foreground">
                  {processo.dataFim && etapaAtual.status === 'CONCLUIDA' 
                    ? 'Processo Concluído' 
                    : `Etapa ${etapaAtualIndex + 1} de ${etapas.length}`
                  }
                </span>
                <span className="font-medium">
                  {processo.dataFim && etapaAtual.status === 'CONCLUIDA' ? '100' : Math.round(progresso)}%
                </span>
              </div>
              <Progress 
                value={processo.dataFim && etapaAtual.status === 'CONCLUIDA' ? 100 : progresso} 
                className="h-3" 
              />
            </div>
            <p className="text-sm text-muted-foreground">
              {processo.dataFim && etapaAtual.status === 'CONCLUIDA' 
                ? <span className="font-semibold text-green-700">Todas as etapas foram concluídas!</span>
                : <>
                    Você está na etapa: <span className="font-semibold text-foreground">{etapaAtual.nome}</span>
                  </>
              }
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Etapas do Processo - Cards */}
      <div className="space-y-4">
        <h3 className="text-xl font-bold">Etapas do Processo</h3>
        <div className="grid gap-4 md:grid-cols-2">
          {etapas.map((etapa, index) => {
            const isConcluida = etapa.status === 'CONCLUIDA' || index < etapaAtualIndex;
            const isAtual = etapa.id === etapaAtual.id && etapa.status !== 'CONCLUIDA';
            const isPendente = index > etapaAtualIndex && etapa.status !== 'CONCLUIDA';

            let bgColor = 'bg-gray-50 border-gray-200';
            let iconColor = 'text-gray-400';
            let icon = <Circle className="w-6 h-6" />;

            if (isConcluida) {
              bgColor = 'bg-green-50 border-green-300';
              iconColor = 'text-green-600';
              icon = <CheckCircle2 className="w-6 h-6" />;
            } else if (isAtual) {
              bgColor = 'bg-blue-50 border-blue-400 border-2';
              iconColor = 'text-blue-600';
              icon = <Clock className="w-6 h-6 animate-pulse" />;
            }

            return (
              <Card key={etapa.id} className={`${bgColor} transition-all`}>
                <CardContent className="p-6">
                  <div className="flex items-start gap-4">
                    <div className={`${iconColor} flex-shrink-0 mt-1`}>{icon}</div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-sm font-medium text-muted-foreground">
                          Etapa {etapa.ordem}
                        </span>
                        {isConcluida && (
                          <Badge className="bg-green-600">Concluída</Badge>
                        )}
                        {isAtual && !isConcluida && (
                          <Badge className="bg-blue-600">Atual</Badge>
                        )}
                        {isPendente && !isConcluida && (
                          <Badge variant="outline">Pendente</Badge>
                        )}
                      </div>
                      <h4 className="font-semibold text-lg mb-1">{etapa.nome}</h4>
                      <p className="text-sm text-muted-foreground">{etapa.descricao}</p>

                      {/* Mostrar data de início/fim se disponível */}
                      {etapa.dataInicio && (
                        <div className="mt-3 text-xs text-muted-foreground flex items-center gap-1">
                          <Calendar className="w-3 h-3" />
                          Iniciada em {new Date(etapa.dataInicio).toLocaleDateString('pt-BR')}
                        </div>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </div>

      {/* Histórico com Feedback */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-green-600" />
            Histórico e Feedbacks
          </CardTitle>
        </CardHeader>
        <CardContent>
          {historico.length === 0 ? (
            <p className="text-center text-muted-foreground py-8">
              Nenhum feedback disponível ainda
            </p>
          ) : (
            <div className="space-y-6">
              {historico.map((item, index) => {
                const etapa = etapas.find((e) => e.id === item.etapaProcessoId);
                return (
                  <div key={item.id}>
                    {index > 0 && <Separator className="my-6" />}

                    <div className="space-y-3">
                      {/* Header do histórico */}
                      <div className="flex justify-between items-start">
                        <div>
                          <h4 className="font-semibold text-lg flex items-center gap-2">
                            <Check className="w-5 h-5 text-green-600" />
                            {etapa?.nome || 'Etapa'}
                          </h4>
                          <p className="text-sm text-muted-foreground">{item.acao}</p>
                        </div>
                        <span className="text-sm text-muted-foreground flex items-center gap-1">
                          <Calendar className="w-4 h-4" />
                          {formatData(item.dataMovimentacao)}
                        </span>
                      </div>

                      {/* Feedback Card */}
                      {item.feedback && (
                        <Card className="bg-blue-50 border-blue-200">
                          <CardContent className="p-4">
                            <div className="flex gap-3">
                              <MessageSquare className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                              <div className="flex-1">
                                <p className="text-sm font-medium text-blue-900 mb-1">
                                  Feedback do Recrutador
                                </p>
                                <p className="text-sm text-blue-800">{item.feedback}</p>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modal de Avaliação de Organização */}
      {vaga && processo && (
        <AvaliarOrganizacaoModal
          isOpen={showAvaliacaoModal}
          onClose={() => setShowAvaliacaoModal(false)}
          processoId={processo.id}
          nomeOrganizacao={vaga.organizacao.nome}
          onSuccess={handleAvaliacaoSuccess}
        />
      )}
    </div>
  );
}
