import { useState, useEffect } from 'react';
import { Search, Filter, Sparkles, UserPlus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { recrutadorService } from '@/services/recrutador.service';
import type { BuscarCandidatoResponse, Vaga } from '@/services/recrutador.service';
import { getOrganizacaoIdFromToken } from '@/lib/jwt';
import CandidatoPerfilPanel from '@/components/CandidatoPerfilPanel';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function BuscarCandidatos() {
  usePageTitle('Buscar Candidatos');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedVagaId, setSelectedVagaId] = useState<string>('');
  const [vagas, setVagas] = useState<Vaga[]>([]);
  const [loadingVagas, setLoadingVagas] = useState(false);
  const [candidatos, setCandidatos] = useState<BuscarCandidatoResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalResults, setTotalResults] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [hasSearched, setHasSearched] = useState(false);
  const [selectedCandidato, setSelectedCandidato] = useState<BuscarCandidatoResponse | null>(null);
  const [isPanelOpen, setIsPanelOpen] = useState(false);
  const [isInviteDialogOpen, setIsInviteDialogOpen] = useState(false);
  const [candidatoToInvite, setCandidatoToInvite] = useState<BuscarCandidatoResponse | null>(null);
  const [inviteMessage, setInviteMessage] = useState('');
  const [sendingInvite, setSendingInvite] = useState(false);

  useEffect(() => {
    const loadVagas = async () => {
      setLoadingVagas(true);
      try {
        const organizacaoId = getOrganizacaoIdFromToken();
        if (!organizacaoId) {
          console.error('OrganizacaoId not found in token');
          return;
        }

        const vagasRecentes = await recrutadorService.getVagasRecentes(organizacaoId, 20);
        setVagas(vagasRecentes);

        if (vagasRecentes.length > 0) {
          setSelectedVagaId(vagasRecentes[0].id);
        }
      } catch (error) {
        console.error('Error loading vagas:', error);
      } finally {
        setLoadingVagas(false);
      }
    };

    loadVagas();
  }, []);

  useEffect(() => {
    if (hasSearched && selectedVagaId) {
      performSearch();
    }
  }, [currentPage, pageSize]);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedVagaId) {
      toast.error('Por favor, selecione uma vaga antes de buscar candidatos.');
      return;
    }

    setCurrentPage(0);
    setHasSearched(true);
    performSearch();
  };

  const performSearch = async () => {
    if (!selectedVagaId) return;

    setLoading(true);

    try {
      const organizacaoId = getOrganizacaoIdFromToken();
      if (!organizacaoId) {
        console.error('OrganizacaoId not found in token');
        return;
      }

      const result = await recrutadorService.buscarCandidatos(
        organizacaoId,
        {
          vagaId: selectedVagaId,
          consulta: searchTerm,
        },
        currentPage,
        pageSize
      );

      setCandidatos(result.content);
      setTotalResults(result.totalElements);
      setTotalPages(result.totalPages);
    } catch (error) {
      console.error('Error searching candidates:', error);
      toast.error('Erro ao buscar candidatos');
      setCandidatos([]);
      setTotalResults(0);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 70) return 'text-green-600';
    if (score >= 40) return 'text-yellow-600';
    return 'text-red-600';
  };

  const handleCandidatoClick = (candidato: BuscarCandidatoResponse) => {
    setSelectedCandidato(candidato);
    setIsPanelOpen(true);
  };

  const handleClosePanel = () => {
    setIsPanelOpen(false);
    setSelectedCandidato(null);
  };

  const handleInviteClick = (candidato: BuscarCandidatoResponse) => {
    setCandidatoToInvite(candidato);
    setInviteMessage('');
    setIsInviteDialogOpen(true);
  };

  const handleSendInvite = async () => {
    if (!candidatoToInvite || !selectedVagaId) return;

    setSendingInvite(true);
    try {
      const organizacaoId = getOrganizacaoIdFromToken();
      if (!organizacaoId) {
        toast.error('Erro ao obter organização');
        return;
      }

      await recrutadorService.enviarConvite(organizacaoId, {
        vagaId: selectedVagaId,
        candidatoUsuarioId: candidatoToInvite.usuarioId,
        mensagem: inviteMessage || 'Você foi convidado para participar deste processo seletivo.',
      });

      toast.success(`Convite enviado para ${candidatoToInvite.nome}`);
      setIsInviteDialogOpen(false);
      setCandidatoToInvite(null);
      setInviteMessage('');

      setCandidatos(prevCandidatos => 
        prevCandidatos.map(c => 
          c.usuarioId === candidatoToInvite.usuarioId 
            ? { ...c, jaConvidado: true } 
            : c
        )
      );
    } catch (error: any) {
      console.error('Erro ao enviar convite:', error);
      toast.error(error.response?.data?.message || 'Erro ao enviar convite');
    } finally {
      setSendingInvite(false);
    }
  };

  return (
    <div className="min-h-full bg-background">
      <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">Buscar Candidatos</h1>
        </div>

        {/* Search Section */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Filter className="w-5 h-5" />
              <CardTitle className="text-lg">Filtros</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSearch} className="space-y-4">
              <div className="flex gap-4">
                {/* Vaga Dropdown */}
                <div className="w-64">
                  <Select value={selectedVagaId} onValueChange={setSelectedVagaId}>
                    <SelectTrigger>
                      <SelectValue placeholder={loadingVagas ? "Carregando vagas..." : "Selecione uma vaga"} />
                    </SelectTrigger>
                    <SelectContent>
                      {vagas.map((vaga) => (
                        <SelectItem key={vaga.id} value={vaga.id}>
                          {vaga.titulo}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {/* Search Input */}
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <Input
                    type="text"
                    placeholder="Buscar por nome, habilidades... (deixe vazio para ver todos ordenados por compatibilidade)"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-10"
                  />
                </div>

                {/* Search Button */}
                <Button type="submit" disabled={loading || !selectedVagaId}>
                  {loading ? 'Pesquisando...' : 'Pesquisar'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Results Section */}
        {hasSearched && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <p className="text-lg font-medium">
                {totalResults} {totalResults === 1 ? 'candidato encontrado' : 'candidatos encontrados'}
              </p>
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">Resultados por página:</span>
                <Select value={pageSize.toString()} onValueChange={(value) => {
                  setPageSize(parseInt(value));
                  setCurrentPage(0);
                }}>
                  <SelectTrigger className="w-20">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="10">10</SelectItem>
                    <SelectItem value="20">20</SelectItem>
                    <SelectItem value="50">50</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Candidate Cards */}
            <div className="space-y-4">
              {candidatos.map((candidato) => (
                <Card key={candidato.usuarioId} className="hover:shadow-md transition-shadow">
                  <CardContent className="pt-6">
                    <div className="flex justify-between items-start">
                      <div className="flex-1 space-y-3">
                        {/* Candidate Name and Score */}
                        <div className="flex items-center justify-between">
                          <h3
                            className="text-lg font-semibold text-primary hover:underline cursor-pointer"
                            onClick={() => handleCandidatoClick(candidato)}
                          >
                            {candidato.nome}
                          </h3>
                          <div className="flex items-center gap-3">
                            <span className={`text-2xl font-bold ${getScoreColor(candidato.scoreRelevancia)}`}>
                              {candidato.scoreRelevancia}%
                            </span>
                            <Button
                              size="sm"
                              onClick={() => handleInviteClick(candidato)}
                              disabled={candidato.jaConvidado}
                              variant={candidato.jaConvidado ? "outline" : "default"}
                              className="flex items-center gap-2"
                            >
                              <UserPlus className="w-4 h-4" />
                              {candidato.jaConvidado ? 'Já convidado' : 'Convidar'}
                            </Button>
                          </div>
                        </div>

                        {/* AI-Generated Summary */}
                        {candidato.resumo && (
                          <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-3">
                            <div className="flex items-center gap-2 mb-2">
                              <Sparkles className="w-4 h-4 text-blue-600" />
                              <Badge variant="secondary" className="text-xs bg-blue-100 text-blue-700">
                                Gerado por IA
                              </Badge>
                            </div>
                            <p className="text-sm text-gray-700 leading-relaxed">
                              {candidato.resumo}
                            </p>
                          </div>
                        )}

                        {/* Competencias */}
                        {candidato.competencias && candidato.competencias.length > 0 && (
                          <div className="flex flex-wrap gap-2 pt-2">
                            {candidato.competencias.slice(0, 5).map((comp, idx) => (
                              <Badge key={idx} variant="secondary">
                                {comp}
                              </Badge>
                            ))}
                          </div>
                        )}

                        {/* Location and Experience */}
                        <div className="flex gap-4 text-sm text-muted-foreground pt-2">
                          {candidato.cidade && candidato.uf && (
                            <span>📍 {candidato.cidade}, {candidato.uf}</span>
                          )}
                          {candidato.experiencias && candidato.experiencias.length > 0 && (
                            <span>💼 {candidato.experiencias[0]}</span>
                          )}
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex justify-center items-center gap-2 pt-4">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage(currentPage - 1)}
                >
                  Anterior
                </Button>

                {/* Page numbers */}
                <div className="flex gap-1">
                  {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                    let pageNum;
                    if (totalPages <= 5) {
                      pageNum = i;
                    } else if (currentPage < 3) {
                      pageNum = i;
                    } else if (currentPage >= totalPages - 3) {
                      pageNum = totalPages - 5 + i;
                    } else {
                      pageNum = currentPage - 2 + i;
                    }

                    return (
                      <Button
                        key={pageNum}
                        variant={currentPage === pageNum ? "default" : "outline"}
                        size="sm"
                        className="w-10"
                        onClick={() => setCurrentPage(pageNum)}
                      >
                        {pageNum + 1}
                      </Button>
                    );
                  })}
                </div>

                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage(currentPage + 1)}
                >
                  Próxima
                </Button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Candidate Profile Panel */}
      <CandidatoPerfilPanel
        candidato={selectedCandidato}
        isOpen={isPanelOpen}
        onClose={handleClosePanel}
      />

      {/* Invite Dialog */}
      <Dialog open={isInviteDialogOpen} onOpenChange={setIsInviteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Convidar Candidato</DialogTitle>
            <DialogDescription>
              Envie um convite para {candidatoToInvite?.nome} participar do processo seletivo.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Mensagem (opcional)</label>
              <Textarea
                placeholder="Escreva uma mensagem personalizada para o candidato..."
                value={inviteMessage}
                onChange={(e) => setInviteMessage(e.target.value)}
                rows={4}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsInviteDialogOpen(false)}
              disabled={sendingInvite}
            >
              Cancelar
            </Button>
            <Button onClick={handleSendInvite} disabled={sendingInvite}>
              {sendingInvite ? 'Enviando...' : 'Enviar Convite'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
