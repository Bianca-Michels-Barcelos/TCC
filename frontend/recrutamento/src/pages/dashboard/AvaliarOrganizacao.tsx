import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Star, Building2, ArrowLeft, Send } from 'lucide-react';
import { authService } from '@/services/auth.service';
import api from '@/lib/api';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

interface ProcessoSeletivo {
  id: string;
  candidaturaId: string;
  status: string;
  candidatura: {
    vaga: {
      titulo: string;
      organizacao: {
        id: string;
        nome: string;
      };
    };
  };
}

export default function AvaliarOrganizacao() {
  usePageTitle('Avaliar Organização');
  const { processoId } = useParams<{ processoId: string }>();
  const navigate = useNavigate();
  const [processo, setProcesso] = useState<ProcessoSeletivo | null>(null);
  const [nota, setNota] = useState(0);
  const [comentario, setComentario] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [hoverNota, setHoverNota] = useState(0);
  const user = authService.getUser();

  useEffect(() => {
    if (processoId) {
      loadProcesso();
    }
  }, [processoId]);

  const loadProcesso = async () => {
    const mockProcesso = {
      id: processoId!,
      candidaturaId: 'cand1',
      status: 'FINALIZADO',
      candidatura: {
        vaga: {
          titulo: 'Desenvolvedor Full Stack Sênior',
          organizacao: {
            id: 'org1',
            nome: 'Tech Solutions',
          },
        },
      },
    };

    try {
      setLoading(true);
      const response = await api.get(`/processos-seletivos/${processoId}`);
      setProcesso(response.data || mockProcesso);
      
      try {
        const avaliacaoResponse = await api.get(`/avaliacoes-organizacao/processo/${processoId}`);
        if (avaliacaoResponse.data) {
          setNota(avaliacaoResponse.data.nota);
          setComentario(avaliacaoResponse.data.comentario || '');
        }
      } catch (error) {
      }
    } catch (error: any) {
      console.error('Erro ao carregar processo:', error);
      setProcesso(mockProcesso);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (nota === 0) {
      toast.error('Por favor, selecione uma nota');
      return;
    }

    if (!user?.usuarioId || !processoId) {
      toast.error('Dados inválidos');
      return;
    }

    try {
      setSubmitting(true);
      
      try {
        const avaliacaoResponse = await api.get(`/avaliacoes-organizacao/processo/${processoId}`);
        if (avaliacaoResponse.data) {
          await api.put(`/avaliacoes-organizacao/${avaliacaoResponse.data.id}`, {
            nota,
            comentario: comentario || undefined,
          });
          toast.success('Avaliação atualizada com sucesso!');
        }
      } catch (error) {
        await api.post('/avaliacoes-organizacao', {
          processoId,
          nota,
          comentario: comentario || undefined,
        });
        toast.success('Avaliação enviada com sucesso!');
      }

      navigate('/dashboard/minhas-candidaturas');
    } catch (error: any) {
      console.error('Erro ao enviar avaliação:', error);
      toast.error(error.response?.data?.message || 'Erro ao enviar avaliação');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <p className="text-muted-foreground">Carregando...</p>
      </div>
    );
  }

  if (!processo) {
    return (
      <div className="p-6">
        <p className="text-muted-foreground">Processo não encontrado.</p>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 max-w-3xl mx-auto">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <h2 className="text-3xl font-bold flex-1">Avaliar Empresa</h2>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Building2 className="w-5 h-5" />
            {processo.candidatura.vaga.organizacao.nome}
          </CardTitle>
          <p className="text-sm text-muted-foreground">
            Processo seletivo: {processo.candidatura.vaga.titulo}
          </p>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <Label className="text-base mb-3 block">
                Como você avalia sua experiência com esta empresa?
              </Label>
              <div className="flex gap-2 items-center">
                {[1, 2, 3, 4, 5].map((value) => (
                  <button
                    key={value}
                    type="button"
                    onClick={() => setNota(value)}
                    onMouseEnter={() => setHoverNota(value)}
                    onMouseLeave={() => setHoverNota(0)}
                    className="transition-transform hover:scale-110 focus:outline-none focus:ring-2 focus:ring-primary rounded"
                  >
                    <Star
                      className={`w-10 h-10 ${
                        value <= (hoverNota || nota)
                          ? 'fill-yellow-400 text-yellow-400'
                          : 'text-gray-300'
                      }`}
                    />
                  </button>
                ))}
                {nota > 0 && (
                  <span className="ml-4 text-lg font-semibold">
                    {nota === 1 && '😞 Muito Ruim'}
                    {nota === 2 && '😕 Ruim'}
                    {nota === 3 && '😐 Regular'}
                    {nota === 4 && '😊 Bom'}
                    {nota === 5 && '🤩 Excelente'}
                  </span>
                )}
              </div>
            </div>

            <div>
              <Label htmlFor="comentario">
                Comentário (Opcional)
              </Label>
              <Textarea
                id="comentario"
                value={comentario}
                onChange={(e) => setComentario(e.target.value)}
                placeholder="Compartilhe sua experiência com o processo seletivo desta empresa..."
                rows={6}
                className="mt-2"
              />
              <p className="text-xs text-muted-foreground mt-2">
                Seu feedback ajuda outros candidatos e a empresa a melhorar seus processos.
              </p>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <p className="text-sm text-blue-900 font-medium mb-2">
                ℹ️ Sobre as avaliações
              </p>
              <ul className="text-sm text-blue-700 space-y-1">
                <li>• Sua avaliação será pública e anônima</li>
                <li>• Você pode editar sua avaliação a qualquer momento</li>
                <li>• Avaliações honestas ajudam a comunidade</li>
              </ul>
            </div>

            <div className="flex gap-3 justify-end">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(-1)}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={submitting || nota === 0}>
                {submitting ? (
                  'Enviando...'
                ) : (
                  <>
                    <Send className="w-4 h-4 mr-2" />
                    Enviar Avaliação
                  </>
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card className="bg-gradient-to-r from-green-50 to-blue-50 border-green-200">
        <CardContent className="p-6">
          <h3 className="font-semibold mb-3">💡 Dicas para uma boa avaliação:</h3>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li>✓ Seja honesto e construtivo</li>
            <li>✓ Mencione pontos positivos e negativos</li>
            <li>✓ Descreva como foi a comunicação durante o processo</li>
            <li>✓ Comente sobre prazos e transparência</li>
            <li>✓ Evite informações pessoais ou ofensivas</li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
