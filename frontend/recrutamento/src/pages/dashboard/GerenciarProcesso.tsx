import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  ArrowLeft,
  Calendar,
  CheckCircle2,
  Clock,
  MessageSquare,
  User,
  Award,
  ChevronRight,
  ChevronLeft,
  XCircle,
  Download,
} from 'lucide-react';
import {
  processoSeletivoService,
  etapaProcessoService,
  type ProcessoSeletivoComCandidato,
  type HistoricoEtapaProcesso,
  type EtapaProcesso,
} from '@/services/processo-seletivo.service';
import { candidaturaService } from '@/services/candidatura.service';
import { toast } from 'sonner';
import api from '@/lib/api';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function GerenciarProcesso() {
  usePageTitle('Gerenciar Processo');
  const { processoId } = useParams<{ processoId: string }>();
  const navigate = useNavigate();

  const [processoCompleto, setProcessoCompleto] = useState<ProcessoSeletivoComCandidato | null>(null);
  const [historico, setHistorico] = useState<HistoricoEtapaProcesso[]>([]);
  const [etapas, setEtapas] = useState<EtapaProcesso[]>([]);
  const [etapaAtual, setEtapaAtual] = useState<EtapaProcesso | null>(null);
  const [loading, setLoading] = useState(true);

  const [showAvancarDialog, setShowAvancarDialog] = useState(false);
  const [showSaltarDialog, setShowSaltarDialog] = useState(false);
  const [showRetornarDialog, setShowRetornarDialog] = useState(false);
  const [showFinalizarDialog, setShowFinalizarDialog] = useState(false);
  const [showReprovarDialog, setShowReprovarDialog] = useState(false);
  const [showAceitarDialog, setShowAceitarDialog] = useState(false);
  const [showRejeitarDialog, setShowRejeitarDialog] = useState(false);

  const [feedback, setFeedback] = useState('');
  const [etapaSelecionada, setEtapaSelecionada] = useState('');
  const [processando, setProcessando] = useState(false);

  useEffect(() => {
    if (processoId) {
      loadProcesso();
    }
  }, [processoId]);

  const loadProcesso = async () => {
    if (!processoId) return;

    setLoading(true);
    try {
      const processoData = await processoSeletivoService.buscarComCandidatoPorId(processoId);
      setProcessoCompleto(processoData);

      const etapasData = await etapaProcessoService.listarPorVaga(processoData.vagaId);
      setEtapas(etapasData.sort((a, b) => a.ordem - b.ordem));

      const etapaAtualData = etapasData.find(
        (e) => e.id === processoData.etapaAtualId
      );
      setEtapaAtual(etapaAtualData || null);

      const historicoData = await processoSeletivoService.buscarHistorico(processoData.processoId);
      setHistorico(historicoData);
    } catch (error) {
      console.error('Erro ao carregar processo:', error);
      toast.error('Não foi possível carregar o processo seletivo');
    } finally {
      setLoading(false);
    }
  };

  const handleAvancarProximaEtapa = async () => {
    if (!processoId) return;

    setProcessando(true);
    try {
      await processoSeletivoService.avancarParaProximaEtapa(processoId, feedback || undefined);
      toast.success('Candidato avançado para a próxima etapa');
      setShowAvancarDialog(false);
      setFeedback('');
      loadProcesso();
    } catch (error: any) {
      console.error('Erro ao avançar etapa:', error);
      toast.error(error.response?.data?.message || 'Erro ao avançar etapa');
    } finally {
      setProcessando(false);
    }
  };

  const handleSaltarParaEtapa = async () => {
    if (!processoId || !etapaSelecionada) return;

    setProcessando(true);
    try {
      await processoSeletivoService.avancarParaEtapa(processoId, etapaSelecionada, feedback || undefined);
      toast.success('Candidato movido para a etapa selecionada');
      setShowSaltarDialog(false);
      setFeedback('');
      setEtapaSelecionada('');
      loadProcesso();
    } catch (error: any) {
      console.error('Erro ao saltar etapa:', error);
      toast.error(error.response?.data?.message || 'Erro ao saltar para etapa');
    } finally {
      setProcessando(false);
    }
  };

  const handleRetornarParaEtapa = async () => {
    if (!processoId || !etapaSelecionada) return;

    setProcessando(true);
    try {
      await processoSeletivoService.retornarParaEtapa(processoId, etapaSelecionada, feedback || undefined);
      toast.success('Candidato retornado para a etapa selecionada');
      setShowRetornarDialog(false);
      setFeedback('');
      setEtapaSelecionada('');
      loadProcesso();
    } catch (error: any) {
      console.error('Erro ao retornar etapa:', error);
      toast.error(error.response?.data?.message || 'Erro ao retornar para etapa');
    } finally {
      setProcessando(false);
    }
  };

  const handleFinalizar = async () => {
    if (!processoId) return;

    setProcessando(true);
    try {
      await processoSeletivoService.finalizar(processoId, feedback || undefined);
      toast.success('Processo seletivo finalizado');
      setShowFinalizarDialog(false);
      setFeedback('');
      loadProcesso();
    } catch (error: any) {
      console.error('Erro ao finalizar processo:', error);
      toast.error(error.response?.data?.message || 'Erro ao finalizar processo');
    } finally {
      setProcessando(false);
    }
  };

  const handleReprovar = async () => {
    if (!processoId || !feedback) return;

    setProcessando(true);
    try {
      await processoSeletivoService.reprovar(processoId, feedback);
      toast.success('Candidato reprovado');
      setShowReprovarDialog(false);
      setFeedback('');
      loadProcesso();
    } catch (error: any) {
      console.error('Erro ao reprovar candidato:', error);
      toast.error(error.response?.data?.message || 'Erro ao reprovar candidato');
    } finally {
      setProcessando(false);
    }
  };

  const handleAceitarCandidatura = async () => {
    if (!processoCompleto) return;

    setProcessando(true);
    try {
      await candidaturaService.aceitar(processoCompleto.candidaturaId);
      toast.success('Candidatura aceita! Processo seletivo iniciado.');
      setShowAceitarDialog(false);
      loadProcesso();
    } catch (error: any) {
      console.error('Erro ao aceitar candidatura:', error);
      toast.error(error.response?.data?.message || 'Erro ao aceitar candidatura');
    } finally {
      setProcessando(false);
    }
  };

  const handleRejeitarCandidatura = async () => {
    if (!processoCompleto) return;

    setProcessando(true);
    try {
      await candidaturaService.rejeitar(processoCompleto.candidaturaId);
      toast.success('Candidatura rejeitada');
      setShowRejeitarDialog(false);
      navigate(-1);
    } catch (error: any) {
      console.error('Erro ao rejeitar candidatura:', error);
      toast.error(error.response?.data?.message || 'Erro ao rejeitar candidatura');
    } finally {
      setProcessando(false);
    }
  };

  const formatData = (data: string) => {
    if (!data) return 'Data não disponível';
    try {
      const date = new Date(data);
      if (isNaN(date.getTime())) return 'Data inválida';
      return date.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
      });
    } catch (error) {
      return 'Data inválida';
    }
  };

  const downloadCurriculo = async () => {
    if (!processoCompleto?.arquivoCurriculo) return;

    try {
      const response = await api.get(`/curriculos/download`, {
        params: { path: processoCompleto.arquivoCurriculo },
        responseType: 'blob',
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      
      const isMarkdown = processoCompleto.arquivoCurriculo.endsWith('.md');
      const extension = isMarkdown ? 'md' : 'pdf';
      
      link.setAttribute('download', `curriculo_${processoCompleto.candidatoNome.replace(/ /g, '_')}.${extension}`);
      document.body.appendChild(link);
      link.click();
      link.remove();

      toast.success('Currículo baixado com sucesso');
    } catch (error) {
      console.error('Erro ao baixar currículo:', error);
      toast.error('Não foi possível baixar o currículo');
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <Card>
          <CardContent className="p-12 text-center">
            <p className="text-muted-foreground">Carregando processo seletivo...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!processoCompleto || !etapaAtual) {
    return (
      <div className="p-6">
        <Card>
          <CardContent className="p-12 text-center">
            <p className="text-muted-foreground">Processo seletivo não encontrado</p>
            <Button className="mt-4" onClick={() => navigate(-1)}>
              Voltar
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const etapaAtualIndex = etapas.findIndex((e) => e.id === etapaAtual.id);
  const isUltimaEtapa = etapaAtualIndex === etapas.length - 1;
  const isPrimeiraEtapa = etapaAtualIndex === 0;

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div className="flex-1">
          <h2 className="text-3xl font-bold">Gerenciar Processo Seletivo</h2>
          <p className="text-muted-foreground mt-1">Acompanhe e gerencie o candidato</p>
        </div>
        {processoCompleto.dataFim ? (
          <Badge className="bg-gray-100 text-gray-800">Finalizado</Badge>
        ) : (
          <Badge className="bg-blue-100 text-blue-800">Em Andamento</Badge>
        )}
      </div>

      {/* Informações do Candidato */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <User className="w-5 h-5" />
            Informações do Candidato
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-muted-foreground">Nome</p>
              <p className="text-lg font-semibold">{processoCompleto.candidatoNome}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Email</p>
              <p className="text-lg">{processoCompleto.candidatoEmail}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Data da Candidatura</p>
              <p className="text-lg">
                {new Date(processoCompleto.dataCandidatura).toLocaleDateString('pt-BR')}
              </p>
            </div>
            {processoCompleto.compatibilidade && (
              <div>
                <p className="text-sm text-muted-foreground">Compatibilidade</p>
                <div className="flex items-center gap-2">
                  <Award className="w-5 h-5 text-yellow-600" />
                  <p className="text-lg font-semibold">{Math.round(Number(processoCompleto.compatibilidade))}%</p>
                </div>
              </div>
            )}
          </div>
          {processoCompleto.arquivoCurriculo && (
            <div className="mt-4 pt-4 border-t">
              <Button variant="outline" onClick={downloadCurriculo}>
                <Download className="w-4 h-4 mr-2" />
                Baixar Currículo
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Etapa Atual */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Clock className="w-5 h-5 text-blue-600" />
            Etapa Atual
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4 mb-4">
            <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center">
              <span className="text-lg font-bold text-blue-600">{etapaAtual.ordem}</span>
            </div>
            <div className="flex-1">
              <h3 className="text-xl font-semibold">{etapaAtual.nome}</h3>
              <p className="text-sm text-muted-foreground">{etapaAtual.descricao}</p>
            </div>
            <Badge variant="outline">{etapaAtual.status}</Badge>
          </div>

          {/* Ações */}
          {!processoCompleto.dataFim && (
            <div className="flex flex-wrap gap-2 pt-4 border-t">
              {processoCompleto.statusCandidatura === 'PENDENTE' ? (
                <>
                  <Button
                    className="bg-green-600 hover:bg-green-700"
                    onClick={() => setShowAceitarDialog(true)}
                  >
                    <CheckCircle2 className="w-4 h-4 mr-2" />
                    Aceitar Candidatura
                  </Button>

                  <Button
                    variant="destructive"
                    onClick={() => setShowRejeitarDialog(true)}
                  >
                    <XCircle className="w-4 h-4 mr-2" />
                    Rejeitar Candidatura
                  </Button>
                </>
              ) : (
                <>
                  {!isUltimaEtapa && (
                    <Button onClick={() => setShowAvancarDialog(true)}>
                      <ChevronRight className="w-4 h-4 mr-2" />
                      Avançar para Próxima Etapa
                    </Button>
                  )}

                  {!isUltimaEtapa && etapas.length > 2 && (
                    <Button variant="outline" onClick={() => setShowSaltarDialog(true)}>
                      Saltar para Etapa Específica
                    </Button>
                  )}

                  {!isPrimeiraEtapa && (
                    <Button variant="outline" onClick={() => setShowRetornarDialog(true)}>
                      <ChevronLeft className="w-4 h-4 mr-2" />
                      Retornar para Etapa Anterior
                    </Button>
                  )}

                  <Button
                    variant="outline"
                    className="bg-green-50 hover:bg-green-100 border-green-200"
                    onClick={() => setShowFinalizarDialog(true)}
                  >
                    <CheckCircle2 className="w-4 h-4 mr-2" />
                    Finalizar Processo
                  </Button>

                  <Button
                    variant="outline"
                    className="bg-red-50 hover:bg-red-100 border-red-200"
                    onClick={() => setShowReprovarDialog(true)}
                  >
                    <XCircle className="w-4 h-4 mr-2" />
                    Reprovar Candidato
                  </Button>
                </>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Histórico */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-green-600" />
            Histórico do Processo
          </CardTitle>
        </CardHeader>
        <CardContent>
          {historico.length === 0 ? (
            <p className="text-center text-muted-foreground py-8">Nenhum histórico disponível</p>
          ) : (
            <div className="space-y-6">
              {historico.map((item, index) => {
                return (
                  <div key={item.id} className="relative">
                    {/* Timeline line */}
                    {index < historico.length - 1 && (
                      <div className="absolute left-6 top-12 bottom-0 w-0.5 bg-border" />
                    )}

                    <div className="flex gap-4">
                      {/* Timeline dot */}
                      <div className="relative">
                        <div className="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center">
                          <CheckCircle2 className="w-6 h-6 text-green-600" />
                        </div>
                      </div>

                      {/* Content */}
                      <div className="flex-1 pb-6">
                        <div className="flex justify-between items-start mb-2">
                          <div>
                            <h4 className="font-semibold text-lg">{item.etapaProcessoNome}</h4>
                            <p className="text-sm text-muted-foreground">{item.acao}</p>
                          </div>
                          <span className="text-sm text-muted-foreground flex items-center gap-1">
                            <Calendar className="w-4 h-4" />
                            {formatData(item.dataMovimentacao)}
                          </span>
                        </div>

                        {item.feedback && (
                          <Card className="mt-3 bg-blue-50 border-blue-200">
                            <CardContent className="p-4">
                              <div className="flex gap-2">
                                <MessageSquare className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                                <div className="flex-1">
                                  <p className="text-sm font-medium text-blue-900 mb-1">
                                    Feedback
                                  </p>
                                  <p className="text-sm text-blue-800">{item.feedback}</p>
                                </div>
                              </div>
                            </CardContent>
                          </Card>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialogs */}
      <Dialog open={showAvancarDialog} onOpenChange={setShowAvancarDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Avançar para Próxima Etapa</DialogTitle>
            <DialogDescription>
              O candidato será movido para a próxima etapa do processo seletivo.
              {etapaAtualIndex < etapas.length - 1 && (
                <span className="block mt-2 font-medium">
                  Próxima etapa: {etapas[etapaAtualIndex + 1]?.nome}
                </span>
              )}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="feedback-avancar">Feedback</Label>
              <Textarea
                id="feedback-avancar"
                placeholder="Deixe uma observação sobre esta etapa..."
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows={4}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowAvancarDialog(false)}>
              Cancelar
            </Button>
            <Button onClick={handleAvancarProximaEtapa} disabled={processando}>
              {processando ? 'Processando...' : 'Avançar'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showSaltarDialog} onOpenChange={setShowSaltarDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Saltar para Etapa Específica</DialogTitle>
            <DialogDescription>
              Escolha uma etapa para mover o candidato diretamente.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="etapa-saltar">Selecione a Etapa</Label>
              <Select value={etapaSelecionada} onValueChange={setEtapaSelecionada}>
                <SelectTrigger id="etapa-saltar">
                  <SelectValue placeholder="Escolha uma etapa" />
                </SelectTrigger>
                <SelectContent>
                  {etapas
                    .filter((e) => e.id !== etapaAtual?.id)
                    .map((etapa) => (
                      <SelectItem key={etapa.id} value={etapa.id}>
                        {etapa.ordem}. {etapa.nome}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="feedback-saltar">Feedback</Label>
              <Textarea
                id="feedback-saltar"
                placeholder="Deixe uma observação sobre esta mudança..."
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows={4}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowSaltarDialog(false)}>
              Cancelar
            </Button>
            <Button onClick={handleSaltarParaEtapa} disabled={!etapaSelecionada || processando}>
              {processando ? 'Processando...' : 'Saltar'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showRetornarDialog} onOpenChange={setShowRetornarDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Retornar para Etapa Anterior</DialogTitle>
            <DialogDescription>
              Escolha uma etapa anterior para retornar o candidato.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="etapa-retornar">Selecione a Etapa</Label>
              <Select value={etapaSelecionada} onValueChange={setEtapaSelecionada}>
                <SelectTrigger id="etapa-retornar">
                  <SelectValue placeholder="Escolha uma etapa" />
                </SelectTrigger>
                <SelectContent>
                  {etapas
                    .filter((e) => e.ordem < (etapaAtual?.ordem || 0))
                    .map((etapa) => (
                      <SelectItem key={etapa.id} value={etapa.id}>
                        {etapa.ordem}. {etapa.nome}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="feedback-retornar">Motivo do Retorno</Label>
              <Textarea
                id="feedback-retornar"
                placeholder="Explique por que o candidato está retornando para esta etapa..."
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows={4}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowRetornarDialog(false)}>
              Cancelar
            </Button>
            <Button onClick={handleRetornarParaEtapa} disabled={!etapaSelecionada || !feedback || processando}>
              {processando ? 'Processando...' : 'Retornar'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showFinalizarDialog} onOpenChange={setShowFinalizarDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Finalizar Processo Seletivo</DialogTitle>
            <DialogDescription>
              Esta ação finalizará o processo seletivo. Você poderá adicionar um feedback final.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="feedback-finalizar">Feedback Final (opcional)</Label>
              <Textarea
                id="feedback-finalizar"
                placeholder="Deixe uma observação final sobre este processo..."
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows={4}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowFinalizarDialog(false)}>
              Cancelar
            </Button>
            <Button onClick={handleFinalizar} disabled={processando} className="bg-green-600 hover:bg-green-700">
              {processando ? 'Processando...' : 'Finalizar'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showReprovarDialog} onOpenChange={setShowReprovarDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reprovar Candidato</DialogTitle>
            <DialogDescription>
              Esta ação reprovará o candidato e finalizará o processo. Um feedback é obrigatório.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="feedback-reprovar">Motivo da Reprovação *</Label>
              <Textarea
                id="feedback-reprovar"
                placeholder="Explique o motivo da reprovação..."
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows={4}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowReprovarDialog(false)}>
              Cancelar
            </Button>
            <Button
              onClick={handleReprovar}
              disabled={!feedback || processando}
              variant="destructive"
            >
              {processando ? 'Processando...' : 'Reprovar'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showAceitarDialog} onOpenChange={setShowAceitarDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Aceitar Candidatura</DialogTitle>
            <DialogDescription>
              Esta ação aceitará a candidatura e permitirá que o candidato avance no processo seletivo.
              O candidato está atualmente na etapa de Triagem.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowAceitarDialog(false)}>
              Cancelar
            </Button>
            <Button
              onClick={handleAceitarCandidatura}
              disabled={processando}
              className="bg-green-600 hover:bg-green-700"
            >
              {processando ? 'Processando...' : 'Aceitar Candidatura'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showRejeitarDialog} onOpenChange={setShowRejeitarDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Rejeitar Candidatura</DialogTitle>
            <DialogDescription>
              Esta ação rejeitará a candidatura. O candidato não entrará no processo seletivo.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowRejeitarDialog(false)}>
              Cancelar
            </Button>
            <Button
              onClick={handleRejeitarCandidatura}
              disabled={processando}
              variant="destructive"
            >
              {processando ? 'Processando...' : 'Rejeitar Candidatura'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
