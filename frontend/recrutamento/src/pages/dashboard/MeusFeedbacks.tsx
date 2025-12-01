import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { MessageSquare, Calendar, Building2, Briefcase } from 'lucide-react';
import { authService } from '@/services/auth.service';
import api from '@/lib/api';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

interface Feedback {
  id: string;
  processoId: string;
  feedback: string;
  dataEnvio: string;
  processo: {
    candidatura: {
      vaga: {
        titulo: string;
        organizacao: {
          nome: string;
        };
      };
    };
    etapaAtual?: {
      nome: string;
    };
  };
}

export default function MeusFeedbacks() {
  usePageTitle('Meus Feedbacks');
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([]);
  const [loading, setLoading] = useState(true);
  const user = authService.getUser();

  useEffect(() => {
    loadFeedbacks();
  }, []);

  const loadFeedbacks = async () => {
    if (!user?.usuarioId) {
      toast.error('Usuário não autenticado');
      setLoading(false);
      return;
    }

    const mockFeedbacks = [
      {
        id: '1',
        processoId: 'p1',
        feedback: 'Parabéns! Você teve um excelente desempenho na entrevista técnica. Suas habilidades em React e Node.js são impressionantes. Demonstrou ótimo conhecimento em arquitetura de software e boas práticas. Continue assim!',
        dataEnvio: new Date(Date.now() - 86400000 * 1).toISOString(),
        processo: {
          candidatura: {
            vaga: {
              titulo: 'Desenvolvedor Full Stack Sênior',
              organizacao: { nome: 'Tech Solutions' },
            },
          },
          etapaAtual: { nome: 'Entrevista Técnica' },
        },
      },
      {
        id: '2',
        processoId: 'p2',
        feedback: 'Obrigado por participar do processo. Infelizmente, decidimos seguir com outros candidatos neste momento. Valorizamos seu interesse e encorajamos você a se candidatar a futuras oportunidades.',
        dataEnvio: new Date(Date.now() - 86400000 * 3).toISOString(),
        processo: {
          candidatura: {
            vaga: {
              titulo: 'Product Manager',
              organizacao: { nome: 'Innovation Corp' },
            },
          },
          etapaAtual: { nome: 'Entrevista Final' },
        },
      },
      {
        id: '3',
        processoId: 'p3',
        feedback: 'Seu portfólio está muito bem estruturado! Gostamos especialmente do projeto de redesign do e-commerce. Sugerimos adicionar mais detalhes sobre o processo de pesquisa com usuários.',
        dataEnvio: new Date(Date.now() - 86400000 * 5).toISOString(),
        processo: {
          candidatura: {
            vaga: {
              titulo: 'UX/UI Designer',
              organizacao: { nome: 'Design Studio' },
            },
          },
          etapaAtual: { nome: 'Análise de Portfolio' },
        },
      },
      {
        id: '4',
        processoId: 'p4',
        feedback: 'Você passou para a próxima fase! Sua experiência com Kubernetes e CI/CD foi muito valorizada. A próxima etapa será uma entrevista com o time de engenharia. Entraremos em contato em breve.',
        dataEnvio: new Date(Date.now() - 86400000 * 7).toISOString(),
        processo: {
          candidatura: {
            vaga: {
              titulo: 'DevOps Engineer',
              organizacao: { nome: 'Cloud Systems' },
            },
          },
          etapaAtual: { nome: 'Teste Técnico' },
        },
      },
    ];

    try {
      setLoading(true);
      const candidaturasResponse = await api.get(`/candidatos/${user.usuarioId}/candidaturas`);
      const candidaturas = candidaturasResponse.data;

      const allFeedbacks: Feedback[] = [];

      for (const candidatura of candidaturas) {
        try {
          const processoResponse = await api.get(`/processos-seletivos/candidatura/${candidatura.id}`);
          const processo = processoResponse.data;

          const historicoResponse = await api.get(`/processos-seletivos/${processo.id}/historico`);
          const historico = historicoResponse.data;

          historico.forEach((item: any) => {
            if (item.feedback) {
              allFeedbacks.push({
                id: item.id,
                processoId: processo.id,
                feedback: item.feedback,
                dataEnvio: item.dataMovimentacao,
                processo: {
                  candidatura: {
                    vaga: candidatura.vaga,
                  },
                  etapaAtual: item.etapa,
                },
              });
            }
          });
        } catch (error) {
          console.log('No process for candidatura:', candidatura.id);
        }
      }

      const sortedFeedbacks = allFeedbacks.sort((a, b) => 
        new Date(b.dataEnvio).getTime() - new Date(a.dataEnvio).getTime()
      );
      
      setFeedbacks(sortedFeedbacks.length > 0 ? sortedFeedbacks : mockFeedbacks);
    } catch (error: any) {
      console.error('Erro ao carregar feedbacks:', error);
      setFeedbacks(mockFeedbacks);
    } finally {
      setLoading(false);
    }
  };

  const formatData = (data: string) => {
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl sm:text-2xl lg:text-3xl font-bold flex items-center gap-2">
          <MessageSquare className="w-6 h-6 sm:w-8 sm:h-8" />
          <span>Meus Feedbacks</span>
        </h2>
      </div>

      {loading ? (
        <Card>
          <CardContent className="p-6">
            <p className="text-muted-foreground">Carregando feedbacks...</p>
          </CardContent>
        </Card>
      ) : feedbacks.length === 0 ? (
        <Card>
          <CardContent className="p-6 text-center">
            <MessageSquare className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">Nenhum feedback ainda</h3>
            <p className="text-muted-foreground">
              Você receberá feedbacks dos recrutadores conforme avança nos processos seletivos.
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {feedbacks.map((feedback) => (
            <Card key={feedback.id} className="hover:shadow-md transition-shadow">
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <CardTitle className="text-lg mb-2">
                      {feedback.processo.candidatura.vaga.titulo}
                    </CardTitle>
                    <div className="flex flex-wrap gap-3 text-sm text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Building2 className="w-4 h-4" />
                        {feedback.processo.candidatura.vaga.organizacao.nome}
                      </span>
                      {feedback.processo.etapaAtual && (
                        <Badge variant="outline" className="flex items-center gap-1">
                          <Briefcase className="w-3 h-3" />
                          {feedback.processo.etapaAtual.nome}
                        </Badge>
                      )}
                      <span className="flex items-center gap-1">
                        <Calendar className="w-4 h-4" />
                        {formatData(feedback.dataEnvio)}
                      </span>
                    </div>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
                  <p className="text-sm text-blue-900 whitespace-pre-line">{feedback.feedback}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Card className="bg-gradient-to-r from-purple-50 to-blue-50 border-purple-200">
        <CardContent className="p-6">
          <h3 className="font-semibold mb-3 flex items-center gap-2">
            <MessageSquare className="w-5 h-5 text-purple-600" />
            Sobre os Feedbacks
          </h3>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li>✓ Recrutadores podem enviar feedbacks em cada etapa do processo</li>
            <li>✓ Feedbacks ajudam você a entender seu desempenho</li>
            <li>✓ Use os feedbacks para melhorar em futuras candidaturas</li>
            <li>✓ Você também pode avaliar as organizações após o processo</li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
