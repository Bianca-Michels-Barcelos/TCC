import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Bookmark, MapPin, Building2, DollarSign, Trash2, ExternalLink } from 'lucide-react';
import { vagaSalvaService } from '@/services/vagaSalva.service';
import type { VagaSalva } from '@/services/vagaSalva.service';
import { toast } from 'sonner';
import { useNavigate } from 'react-router-dom';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function VagasSalvas() {
  usePageTitle('Vagas Salvas');
  const [vagasSalvas, setVagasSalvas] = useState<VagaSalva[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadVagasSalvas();
  }, []);

  const loadVagasSalvas = async () => {
    try {
      setLoading(true);
      const vagas = await vagaSalvaService.listar();
      setVagasSalvas(vagas);
    } catch (error: any) {
      console.error('Erro ao carregar vagas salvas:', error);
      toast.error('Erro ao carregar vagas salvas');
      setVagasSalvas([]);
    } finally {
      setLoading(false);
    }
  };

  const handleRemover = async (vagaId: string) => {
    if (!confirm('Deseja remover esta vaga das suas favoritas?')) return;

    try {
      await vagaSalvaService.remover(vagaId);
      toast.success('Vaga removida dos favoritos!');
      loadVagasSalvas();
    } catch (error: any) {
      console.error('Erro ao remover vaga salva:', error);
      toast.error(error.response?.data?.message || 'Erro ao remover vaga');
    }
  };

  const handleVerDetalhes = (vagaId: string) => {
    navigate(`/dashboard/vagas/${vagaId}`);
  };

  const formatSalario = (salario?: number) => {
    if (!salario) return 'A combinar';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(salario);
  };

  const formatData = (data: string) => {
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  const getModalidadeBadge = (modalidade?: string) => {
    const colors: Record<string, string> = {
      REMOTO: 'bg-green-100 text-green-800',
      PRESENCIAL: 'bg-blue-100 text-blue-800',
      HIBRIDO: 'bg-purple-100 text-purple-800',
    };
    return colors[modalidade || ''] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <h2 className="text-xl sm:text-2xl lg:text-3xl font-bold flex items-center gap-2">
          <Bookmark className="w-6 h-6 sm:w-8 sm:h-8" />
          <span>Vagas Salvas</span>
        </h2>
        <Button onClick={() => navigate('/dashboard/buscar-vagas')} className="w-full sm:w-auto h-11">
          Buscar Vagas
        </Button>
      </div>

      {loading ? (
        <Card>
          <CardContent className="p-6">
            <p className="text-muted-foreground">Carregando vagas salvas...</p>
          </CardContent>
        </Card>
      ) : vagasSalvas.length === 0 ? (
        <Card>
          <CardContent className="p-6 text-center">
            <Bookmark className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">Nenhuma vaga salva</h3>
            <p className="text-muted-foreground mb-4">
              Você ainda não salvou nenhuma vaga. Explore as vagas disponíveis e salve suas favoritas!
            </p>
            <Button onClick={() => navigate('/dashboard/buscar-vagas')}>
              Buscar Vagas
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4">
          {vagasSalvas.map((vagaSalva) => (
            <Card key={vagaSalva.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <CardTitle className="text-xl mb-2">{vagaSalva.vaga.titulo}</CardTitle>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Building2 className="w-4 h-4" />
                        {vagaSalva.organizacao.nome}
                      </span>
                      {vagaSalva.vaga.endereco && (
                        <span className="flex items-center gap-1">
                          <MapPin className="w-4 h-4" />
                          {vagaSalva.vaga.endereco.cidade}, {vagaSalva.vaga.endereco.uf}
                        </span>
                      )}
                    </div>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="text-destructive hover:text-destructive"
                    onClick={() => handleRemover(vagaSalva.vagaId)}
                  >
                    <Trash2 className="w-5 h-5" />
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground line-clamp-2">
                  {vagaSalva.vaga.descricao}
                </p>

                <div className="flex flex-wrap gap-2">
                  {vagaSalva.vaga.modalidade && (
                    <Badge className={getModalidadeBadge(vagaSalva.vaga.modalidade)}>
                      {vagaSalva.vaga.modalidade}
                    </Badge>
                  )}
                  <Badge variant="outline" className="flex items-center gap-1">
                    <DollarSign className="w-3 h-3" />
                    {formatSalario(vagaSalva.vaga.salario)}
                  </Badge>
                </div>

                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-xs text-muted-foreground">
                    Salva em {formatData(vagaSalva.salvaEm)}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleVerDetalhes(vagaSalva.vagaId)}
                  >
                    <ExternalLink className="w-4 h-4 mr-2" />
                    Ver Detalhes
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
