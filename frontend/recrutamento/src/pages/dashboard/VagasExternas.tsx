import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { vagaExternaService } from '@/services/vagaExterna.service';
import type { VagaExterna } from '@/services/vagaExterna.service';
import { Search, Plus, FileText, Trash2, Calendar, Download, MoreVertical, Edit, FileEdit } from 'lucide-react';
import api from '@/lib/api';
import { SelecionarModeloModal } from '@/components/SelecionarModeloModal';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu';
import { formatDistanceToNow } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function VagasExternas() {
  usePageTitle('Vagas Externas');
  const navigate = useNavigate();
  const [vagas, setVagas] = useState<VagaExterna[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModeloModal, setShowModeloModal] = useState(false);
  const [vagaIdSelecionada, setVagaIdSelecionada] = useState<string | null>(null);
  const [generatingCurriculo, setGeneratingCurriculo] = useState(false);

  useEffect(() => {
    loadVagas();
  }, []);

  const loadVagas = async () => {
    setLoading(true);
    try {
      const data = await vagaExternaService.listar();
      setVagas(data);
    } catch (error) {
      console.error('Error loading external jobs:', error);
    } finally {
      setLoading(false);
    }
  };

  const getDiasDesdeAdicao = (data: string) => {
    return formatDistanceToNow(new Date(data), {
      addSuffix: true,
      locale: ptBR,
    });
  };

  const handleDownloadCurriculo = async (arquivoUrl: string) => {
    try {
      const response = await api.get(
        `/curriculos/download?path=${encodeURIComponent(arquivoUrl)}`,
        { responseType: 'blob' }
      );

      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = arquivoUrl.split('/').pop() || 'curriculo.pdf';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading curriculum:', error);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Tem certeza que deseja deletar esta vaga externa?')) return;
    
    try {
      await vagaExternaService.deletar(id);
      await loadVagas();
    } catch (error) {
      console.error('Error deleting external job:', error);
    }
  };

  const handleOpenModalGerar = (id: string) => {
    setVagaIdSelecionada(id);
    setShowModeloModal(true);
  };

  const handleGerarCurriculo = async (modelo: string) => {
    if (!vagaIdSelecionada) return;

    try {
      setGeneratingCurriculo(true);
      await vagaExternaService.gerarCurriculoComIA(vagaIdSelecionada, modelo);
      setShowModeloModal(false);
      await loadVagas();
      navigate(`/dashboard/vagas-externas/${vagaIdSelecionada}/editar-curriculo`);
    } catch (error) {
      console.error('Error generating curriculum:', error);
    } finally {
      setGeneratingCurriculo(false);
      setVagaIdSelecionada(null);
    }
  };

  const filteredVagas = vagas.filter(vaga => {
    const matchesSearch = vaga.titulo.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesSearch;
  });

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <h2 className="text-2xl sm:text-3xl font-bold">Visualizar Vagas Externas</h2>
        <Button onClick={() => navigate('/dashboard/vagas-externas/cadastrar')} className="w-full sm:w-auto h-11">
          <Plus className="w-4 h-4 mr-2" />
          Cadastrar Vaga Externa
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Buscar por título da vaga..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardContent>
      </Card>

      {/* Content */}
      {loading ? (
        <div className="text-center py-12">
          <p className="text-muted-foreground">Carregando vagas...</p>
        </div>
      ) : filteredVagas.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <FileText className="w-16 h-16 mx-auto text-muted-foreground mb-4" />
            <h3 className="text-xl font-semibold mb-2">Nenhuma vaga externa cadastrada</h3>
            <p className="text-muted-foreground mb-6">
              Cadastre vagas que você encontrou na internet para gerar currículos personalizados
            </p>
            <Button onClick={() => navigate('/dashboard/vagas-externas/cadastrar')}>
              <Plus className="w-4 h-4 mr-2" />
              Cadastrar Primeira Vaga
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          <h3 className="text-lg sm:text-xl font-semibold">Minhas Vagas Externas</h3>

          {/* Cards Grid */}
          <div className="grid gap-3 sm:gap-4 grid-cols-1 md:grid-cols-2">
            {filteredVagas.map((vaga) => {
              let cardStyle = 'hover:shadow-lg transition-shadow';
              if (vaga.arquivoCurriculo) {
                cardStyle += ' border-green-500 border-2 bg-green-50/20';
              }

              return (
                <Card key={vaga.id} className={cardStyle}>
                  <CardHeader>
                    <div className="flex justify-between items-start gap-4">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-2">
                          <CardTitle className="text-xl truncate">{vaga.titulo}</CardTitle>
                          {vaga.arquivoCurriculo ? (
                            <Badge className="bg-green-600 text-white flex items-center gap-1 shrink-0">
                              <FileText className="w-3 h-3" />
                              Currículo Gerado
                            </Badge>
                          ) : (
                            <Badge variant="secondary" className="bg-gray-200 text-gray-700 shrink-0">
                              Pendente
                            </Badge>
                          )}
                        </div>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-sm text-muted-foreground line-clamp-2">
                      {vaga.descricao}
                    </p>

                    {/* Informação de data */}
                    <div className="text-sm border-t pt-3">
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Calendar className="w-4 h-4" />
                        <span>Adicionada {getDiasDesdeAdicao(vaga.criadoEm)}</span>
                      </div>
                    </div>

                    {/* Ações */}
                    <div className="flex gap-2 pt-2 border-t">
                      {!vaga.arquivoCurriculo ? (
                        <Button
                          variant="outline"
                          size="sm"
                          className="flex-1"
                          onClick={() => handleOpenModalGerar(vaga.id)}
                        >
                          <FileText className="w-4 h-4 mr-2 text-green-600" />
                          Gerar Currículo
                        </Button>
                      ) : (
                        <>
                          <Button
                            variant="outline"
                            size="sm"
                            className="flex-1"
                            onClick={() => navigate(`/dashboard/vagas-externas/${vaga.id}/editar-curriculo`)}
                          >
                            <FileEdit className="w-4 h-4 mr-2 text-blue-600" />
                            Editar Currículo
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleDownloadCurriculo(vaga.arquivoCurriculo!)}
                          >
                            <Download className="w-4 h-4 text-green-600" />
                          </Button>
                        </>
                      )}

                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="outline" size="sm">
                            <MoreVertical className="w-4 h-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => navigate(`/dashboard/vagas-externas/${vaga.id}`)}>
                            <Edit className="w-4 h-4 mr-2" />
                            Editar
                          </DropdownMenuItem>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem
                            onClick={() => handleDelete(vaga.id)}
                            className="text-red-600"
                          >
                            <Trash2 className="w-4 h-4 mr-2" />
                            Deletar
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      )}

      {/* Modal de seleção de modelo */}
      <SelecionarModeloModal
        open={showModeloModal}
        onClose={() => {
          setShowModeloModal(false);
          setVagaIdSelecionada(null);
        }}
        onSelect={handleGerarCurriculo}
        isGenerating={generatingCurriculo}
      />
    </div>
  );
}
