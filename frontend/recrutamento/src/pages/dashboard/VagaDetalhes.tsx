import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import {
  Building2,
  MapPin,
  DollarSign,
  Calendar,
  Briefcase,
  Users,
  Bookmark,
  BookmarkCheck,
  ArrowLeft,
  Send,
  Sparkles,
} from 'lucide-react';
import { authService } from '@/services/auth.service';
import { vagaSalvaService } from '@/services/vagaSalva.service';
import { PersonalizarCurriculoModal } from '@/components/PersonalizarCurriculoModal';
import api from '@/lib/api';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

interface Vaga {
  id: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  salario?: number;
  modalidade?: string;
  status: string;
  dataAbertura: string;
  dataFechamento?: string;
  organizacao: {
    id: string;
    nome: string;
    descricao?: string;
  };
  endereco?: {
    cidade: string;
    uf: string;
  };
  beneficios?: Array<{
    id: string;
    nome: string;
    descricao: string;
  }>;
  nivelExperiencia?: {
    id: string;
    descricao: string;
  };
}

export default function VagaDetalhes() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [vaga, setVaga] = useState<Vaga | null>(null);
  usePageTitle(vaga?.titulo || 'Detalhes da Vaga');
  const [loading, setLoading] = useState(true);
  const [isSalva, setIsSalva] = useState(false);
  const [jaCandidatou, setJaCandidatou] = useState(false);
  const [isApplying, setIsApplying] = useState(false);
  const [showPersonalizarModal, setShowPersonalizarModal] = useState(false);
  const [compatibilidade, setCompatibilidade] = useState<{
    percentual: number;
    justificativa: string;
  } | null>(null);
  const user = authService.getUser();

  useEffect(() => {
    if (id) {
      loadVaga();
      checkIfSalva();
      checkIfCandidatou();
      loadCompatibilidade();
    }
  }, [id]);

  const loadVaga = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/vagas/publicas/${id}`);
      const data = response.data;

      const vagaFormatada: Vaga = {
        id: data.id,
        titulo: data.titulo,
        descricao: data.descricao,
        requisitos: data.requisitos,
        salario: data.salario,
        modalidade: data.modalidade,
        status: data.status,
        dataAbertura: data.dataPublicacao,
        organizacao: {
          id: data.organizacao?.id || '',
          nome: data.organizacao?.nome || 'Empresa não informada',
          descricao: data.organizacao?.descricao,
        },
        endereco: data.endereco ? {
          cidade: data.endereco.cidade || '',
          uf: typeof data.endereco.uf === 'string' ? data.endereco.uf : data.endereco.uf?.value || '',
        } : undefined,
        beneficios: data.beneficios || [],
        nivelExperiencia: data.nivelExperiencia,
      };

      setVaga(vagaFormatada);
    } catch (error: any) {
      console.error('Erro ao carregar vaga:', error);
      toast.error('Erro ao carregar detalhes da vaga');
      if (error.response?.status === 404) {
        navigate('/dashboard/buscar-vagas');
      }
    } finally {
      setLoading(false);
    }
  };

  const checkIfSalva = async () => {
    if (!id) return;
    try {
      const salva = await vagaSalvaService.verificarSalva(id);
      setIsSalva(salva);
    } catch (error) {
      console.error('Erro ao verificar vaga salva:', error);
    }
  };

  const checkIfCandidatou = async () => {
    if (!user?.usuarioId || !id) return;
    try {
      const response = await api.get(`/candidatos/${user.usuarioId}/candidaturas`);
      const candidaturas = response.data;
      setJaCandidatou(candidaturas.some((c: any) => c.vagaId === id));
    } catch (error) {
      console.error('Erro ao verificar candidatura:', error);
    }
  };

  const loadCompatibilidade = async () => {
    if (!user?.usuarioId || !id || !user?.roles?.includes('ROLE_CANDIDATO')) return;
    try {
      const response = await api.get(`/compatibilidade/candidato/${user.usuarioId}/vaga/${id}`);
      setCompatibilidade({
        percentual: response.data.percentualCompatibilidade,
        justificativa: response.data.justificativa,
      });
    } catch (error) {
      console.error('Erro ao carregar compatibilidade:', error);
    }
  };

  const handleSalvar = async () => {
    if (!id) return;

    try {
      if (isSalva) {
        await vagaSalvaService.remover(id);
        toast.success('Vaga removida dos favoritos!');
        setIsSalva(false);
      } else {
        await vagaSalvaService.salvar(id);
        toast.success('Vaga salva nos favoritos!');
        setIsSalva(true);
      }
    } catch (error: any) {
      console.error('Erro ao salvar vaga:', error);
      toast.error(error.response?.data?.message || 'Erro ao salvar vaga');
    }
  };

  const handleCandidatar = () => {
    setShowPersonalizarModal(true);
  };

  const handleSubmitCandidatura = async (modelo: string, conteudoPersonalizado: string) => {
    if (!user?.usuarioId || !id) return;

    setIsApplying(true);
    try {
      await api.post(`/vagas/${id}/candidaturas`, {
        candidatoUsuarioId: user.usuarioId,
        modeloCurriculo: modelo,
        conteudoPersonalizado: conteudoPersonalizado,
      });
      toast.success('Candidatura realizada com sucesso!');
      setJaCandidatou(true);
      setShowPersonalizarModal(false);
      navigate('/dashboard/minhas-candidaturas');
    } catch (error: any) {
      console.error('Erro ao candidatar:', error);
      toast.error(error.response?.data?.message || 'Erro ao realizar candidatura');
    } finally {
      setIsApplying(false);
    }
  };

  const formatSalario = (salario?: number) => {
    if (!salario) return 'A combinar';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(salario);
  };

  const formatData = (data: string) => {
    return new Date(data).toLocaleDateString('pt-BR');
  };

  if (loading) {
    return (
      <div className="p-6">
        <p className="text-muted-foreground">Carregando detalhes da vaga...</p>
      </div>
    );
  }

  if (!vaga) {
    return (
      <div className="p-6">
        <p className="text-muted-foreground">Vaga não encontrada.</p>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <h2 className="text-3xl font-bold flex-1">{vaga.titulo}</h2>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handleSalvar}>
            {isSalva ? (
              <>
                <BookmarkCheck className="w-4 h-4 mr-2" />
                Salva
              </>
            ) : (
              <>
                <Bookmark className="w-4 h-4 mr-2" />
                Salvar
              </>
            )}
          </Button>
          {user?.roles?.includes('ROLE_CANDIDATO') && (
            <Button onClick={handleCandidatar} disabled={jaCandidatou || isApplying}>
              <Send className="w-4 h-4 mr-2" />
              {isApplying ? 'Candidatando...' : jaCandidatou ? 'Já Candidatado' : 'Candidatar-se'}
            </Button>
          )}
        </div>
      </div>

      {/* Modal de Personalização de Currículo */}
      <PersonalizarCurriculoModal
        open={showPersonalizarModal}
        onClose={() => setShowPersonalizarModal(false)}
        onSubmit={handleSubmitCandidatura}
        vagaTitulo={vaga.titulo}
        vagaId={vaga.id}
        isProcessing={isApplying}
      />

      <div className="grid gap-6 md:grid-cols-3">
        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Descrição da Vaga</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <p className="text-muted-foreground whitespace-pre-line">{vaga.descricao}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Requisitos</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground whitespace-pre-line">{vaga.requisitos}</p>
            </CardContent>
          </Card>

          {vaga.beneficios && vaga.beneficios.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Benefícios</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {vaga.beneficios.map((beneficio) => (
                    <li key={beneficio.id} className="flex items-start gap-2">
                      <span className="text-primary mt-1">•</span>
                      <div>
                        <p className="font-medium">{beneficio.nome}</p>
                        <p className="text-sm text-muted-foreground">{beneficio.descricao}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="space-y-6">
          {/* Compatibilidade IA */}
          {compatibilidade && user?.roles?.includes('ROLE_CANDIDATO') && (
            <Card className="border-2 border-blue-200 bg-gradient-to-br from-blue-50 to-indigo-50">
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-blue-900">
                  <Sparkles className="w-5 h-5" />
                  Sua Compatibilidade
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="text-center">
                  <div className={`text-5xl font-bold ${
                    compatibilidade.percentual >= 70 ? 'text-green-600' :
                    compatibilidade.percentual >= 40 ? 'text-yellow-600' :
                    'text-red-600'
                  }`}>
                    {compatibilidade.percentual}%
                  </div>
                  <p className="text-sm text-muted-foreground mt-2">
                    de compatibilidade com esta vaga
                  </p>
                </div>
                {compatibilidade.justificativa && (
                  <div className="bg-white rounded-lg p-3 border border-blue-200">
                    <p className="text-sm text-gray-700 leading-relaxed">
                      {compatibilidade.justificativa}
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Building2 className="w-5 h-5" />
                Sobre a Empresa
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div>
                <p className="font-semibold">{vaga.organizacao.nome}</p>
                {vaga.organizacao.descricao && (
                  <p className="text-sm text-muted-foreground mt-1">
                    {vaga.organizacao.descricao}
                  </p>
                )}
              </div>
              <Button
                variant="outline"
                className="w-full"
                onClick={() => navigate(`/dashboard/organizacao/${vaga.organizacao.id}`)}
              >
                <Building2 className="w-4 h-4 mr-2" />
                Ver Perfil da Empresa
              </Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Informações da Vaga</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center gap-2 text-sm">
                <DollarSign className="w-4 h-4 text-muted-foreground" />
                <span className="font-medium">{formatSalario(vaga.salario)}</span>
              </div>

              {vaga.modalidade && (
                <div className="flex items-center gap-2 text-sm">
                  <Briefcase className="w-4 h-4 text-muted-foreground" />
                  <Badge>{vaga.modalidade}</Badge>
                </div>
              )}

              {vaga.nivelExperiencia && (
                <div className="flex items-center gap-2 text-sm">
                  <Users className="w-4 h-4 text-muted-foreground" />
                  <span>{vaga.nivelExperiencia.descricao}</span>
                </div>
              )}

              {vaga.endereco && (
                <div className="flex items-start gap-2 text-sm">
                  <MapPin className="w-4 h-4 text-muted-foreground mt-0.5" />
                  <div>
                    <p>
                      {vaga.endereco.cidade} - {vaga.endereco.uf}
                    </p>
                  </div>
                </div>
              )}

              <Separator />

              <div className="flex items-center gap-2 text-sm">
                <Calendar className="w-4 h-4 text-muted-foreground" />
                <span>Aberta em {formatData(vaga.dataAbertura)}</span>
              </div>

              {vaga.dataFechamento && (
                <div className="flex items-center gap-2 text-sm">
                  <Calendar className="w-4 h-4 text-muted-foreground" />
                  <span>Fecha em {formatData(vaga.dataFechamento)}</span>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
