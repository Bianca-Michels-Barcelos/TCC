import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import {
  ArrowLeft,
  Building2,
  MapPin,
  Star,
  MessageSquare,
  Calendar,
  TrendingUp,
  Award,
} from 'lucide-react';
import { organizacaoService, type OrganizacaoPublica } from '@/services/organizacao.service';
import { 
  avaliacaoOrganizacaoService, 
  type AvaliacaoOrganizacao,
  type EstatisticasOrganizacao 
} from '@/services/avaliacao-organizacao.service';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function OrganizacaoDetalhes() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [organizacao, setOrganizacao] = useState<OrganizacaoPublica | null>(null);
  const [avaliacoes, setAvaliacoes] = useState<AvaliacaoOrganizacao[]>([]);
  const [estatisticas, setEstatisticas] = useState<EstatisticasOrganizacao | null>(null);
  const [loading, setLoading] = useState(true);

  usePageTitle(organizacao?.nome || 'Detalhes da Organização');

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  const loadData = async () => {
    if (!id) return;

    setLoading(true);
    try {
      const [orgData, avaliacoesData, estatisticasData] = await Promise.all([
        organizacaoService.buscarPublica(id),
        avaliacaoOrganizacaoService.listarPorOrganizacao(id),
        avaliacaoOrganizacaoService.buscarEstatisticas(id),
      ]);

      const avaliacoesOrdenadas = avaliacoesData.sort((a, b) => 
        new Date(b.criadoEm).getTime() - new Date(a.criadoEm).getTime()
      );

      setOrganizacao(orgData);
      setAvaliacoes(avaliacoesOrdenadas);
      setEstatisticas(estatisticasData);
    } catch (error: any) {
      console.error('Erro ao carregar dados da organização:', error);
      toast.error('Erro ao carregar informações da organização');
      if (error.response?.status === 404) {
        toast.error('Organização não encontrada');
        navigate('/dashboard/buscar-vagas');
      }
    } finally {
      setLoading(false);
    }
  };

  const formatData = (data: string) => {
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
  };

  const renderStars = (nota: number, size: 'sm' | 'md' | 'lg' = 'md') => {
    const sizeClasses = {
      sm: 'w-4 h-4',
      md: 'w-5 h-5',
      lg: 'w-8 h-8',
    };

    return (
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`${sizeClasses[size]} ${
              star <= nota
                ? 'fill-yellow-400 text-yellow-400'
                : 'text-gray-300'
            }`}
          />
        ))}
      </div>
    );
  };

  const getNotaBadge = (nota: number) => {
    if (nota >= 4) {
      return <Badge className="bg-green-100 text-green-800">Positiva</Badge>;
    } else if (nota >= 3) {
      return <Badge className="bg-yellow-100 text-yellow-800">Neutra</Badge>;
    } else {
      return <Badge className="bg-red-100 text-red-800">Negativa</Badge>;
    }
  };

  if (loading) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10 rounded" />
          <Skeleton className="h-10 flex-1" />
        </div>
        <Skeleton className="h-48 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (!organizacao) {
    return (
      <div className="p-6">
        <Card>
          <CardContent className="p-12 text-center">
            <Building2 className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">Organização não encontrada</h3>
            <p className="text-muted-foreground mb-4">
              Não foi possível encontrar esta organização.
            </p>
            <Button onClick={() => navigate('/dashboard/buscar-vagas')}>
              Voltar para Buscar Vagas
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div className="flex-1">
          <h2 className="text-3xl font-bold">{organizacao.nome}</h2>
          <div className="flex items-center gap-4 mt-2">
            <p className="text-muted-foreground flex items-center gap-2">
              <Building2 className="w-4 h-4" />
              Empresa
            </p>
            <p className="text-muted-foreground flex items-center gap-2">
              <MapPin className="w-4 h-4" />
              {organizacao.endereco.cidade}, {organizacao.endereco.uf}
            </p>
          </div>
        </div>
      </div>

      {/* Estatísticas - Cards */}
      {estatisticas && estatisticas.totalAvaliacoes > 0 ? (
        <div className="grid gap-4 md:grid-cols-3">
          {/* Card: Nota Média */}
          <Card className="border-l-4 border-l-yellow-500">
            <CardContent className="p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="w-12 h-12 rounded-full bg-yellow-100 flex items-center justify-center">
                  <Award className="w-6 h-6 text-yellow-600" />
                </div>
                <div className="text-right">
                  <p className="text-3xl font-bold text-foreground">
                    {estatisticas.notaMedia.toFixed(1)}
                  </p>
                  <p className="text-sm text-muted-foreground">de 5.0</p>
                </div>
              </div>
              <div className="space-y-2">
                {renderStars(Math.round(estatisticas.notaMedia), 'md')}
                <p className="text-sm font-medium text-muted-foreground">
                  Avaliação Média
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Card: Total de Avaliações */}
          <Card className="border-l-4 border-l-blue-500">
            <CardContent className="p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center">
                  <MessageSquare className="w-6 h-6 text-blue-600" />
                </div>
                <div className="text-right">
                  <p className="text-3xl font-bold text-foreground">
                    {estatisticas.totalAvaliacoes}
                  </p>
                  <p className="text-sm text-muted-foreground">
                    {estatisticas.totalAvaliacoes === 1 ? 'avaliação' : 'avaliações'}
                  </p>
                </div>
              </div>
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">
                  Total de Feedbacks
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Card: Reputação */}
          <Card className="border-l-4 border-l-green-500">
            <CardContent className="p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center">
                  <TrendingUp className="w-6 h-6 text-green-600" />
                </div>
                <div className="text-right">
                  <Badge className={
                    estatisticas.notaMedia >= 4 
                      ? 'bg-green-100 text-green-800' 
                      : estatisticas.notaMedia >= 3 
                      ? 'bg-yellow-100 text-yellow-800'
                      : 'bg-orange-100 text-orange-800'
                  }>
                    {estatisticas.notaMedia >= 4 
                      ? 'Excelente' 
                      : estatisticas.notaMedia >= 3 
                      ? 'Boa'
                      : 'Regular'}
                  </Badge>
                </div>
              </div>
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">
                  Reputação Geral
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      ) : (
        <Card>
          <CardContent className="p-12 text-center">
            <div className="w-16 h-16 rounded-full bg-gray-100 mx-auto mb-4 flex items-center justify-center">
              <MessageSquare className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-xl font-semibold mb-2">Ainda sem avaliações</h3>
            <p className="text-muted-foreground">
              Esta organização ainda não recebeu avaliações de candidatos.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Lista de Avaliações */}
      {avaliacoes.length > 0 && (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <h3 className="text-2xl font-bold">Avaliações dos Candidatos</h3>
            <Badge variant="secondary" className="text-sm">
              {avaliacoes.length} {avaliacoes.length === 1 ? 'avaliação' : 'avaliações'}
            </Badge>
          </div>

          <div className="space-y-4">
            {avaliacoes.map((avaliacao) => (
              <Card key={avaliacao.id} className="hover:shadow-md transition-all">
                <CardContent className="p-6">
                  <div className="space-y-4">
                    {/* Header da Avaliação */}
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-center gap-3">
                        {renderStars(avaliacao.nota, 'md')}
                        {getNotaBadge(avaliacao.nota)}
                      </div>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <Calendar className="w-4 h-4" />
                        <span>{formatData(avaliacao.criadoEm)}</span>
                      </div>
                    </div>

                    {/* Comentário */}
                    <div className="pt-2">
                      <p className="text-sm leading-relaxed text-foreground whitespace-pre-line">
                        {avaliacao.comentario}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

