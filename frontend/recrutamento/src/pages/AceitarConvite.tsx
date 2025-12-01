import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Building2, Mail, Loader2, CheckCircle2, XCircle } from 'lucide-react';
import { toast } from 'sonner';
import api from '@/lib/api';
import { usePageTitle } from '@/hooks/usePageTitle';

interface ConviteInfo {
  id: string;
  organizacaoId: string;
  email: string;
  status: string;
  dataEnvio: string;
  dataExpiracao: string;
}

interface OrganizacaoInfo {
  id: string;
  nome: string;
  endereco: {
    cidade: string;
    uf: string;
  };
}

export default function AceitarConvite() {
  usePageTitle('Aceitar Convite');
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [convite, setConvite] = useState<ConviteInfo | null>(null);
  const [organizacao, setOrganizacao] = useState<OrganizacaoInfo | null>(null);
  const [error, setError] = useState<string>('');
  const [conviteValido, setConviteValido] = useState(false);

  const [formData, setFormData] = useState({
    nome: '',
    cpf: '',
    senha: '',
    confirmarSenha: '',
  });

  useEffect(() => {
    loadConviteInfo();
  }, [token]);

  const loadConviteInfo = async () => {
    if (!token) {
      setError('Token de convite inválido');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      
      const conviteResponse = await api.get(`/auth/convite/${token}`);
      const conviteData = conviteResponse.data;
      
      setConvite(conviteData);

      console.log('Convite recebido:', conviteData);
      console.log('Status:', conviteData.status);
      console.log('Data expiração:', conviteData.dataExpiracao);

      if (conviteData.status !== 'PENDENTE') {
        console.error('Status inválido:', conviteData.status);
        setError('Este convite já foi utilizado ou está expirado');
        setConviteValido(false);
        setLoading(false);
        return;
      }

      const dataExpiracao = new Date(conviteData.dataExpiracao);
      const agora = new Date();
      console.log('Data expiração:', dataExpiracao);
      console.log('Data atual:', agora);
      console.log('Expirou?', dataExpiracao < agora);
      
      if (dataExpiracao < agora) {
        setError('Este convite expirou');
        setConviteValido(false);
        setLoading(false);
        return;
      }

      const orgResponse = await api.get(`/organizacoes/publicas/${conviteData.organizacaoId}`);
      setOrganizacao(orgResponse.data);

      setFormData(prev => ({ ...prev, email: conviteData.email }));
      
      setConviteValido(true);
      setError('');
    } catch (err: any) {
      console.error('Erro ao carregar convite:', err);
      setError(err.response?.data?.message || 'Erro ao validar convite. O link pode estar incorreto ou expirado.');
      setConviteValido(false);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!formData.nome.trim()) {
      setError('Por favor, preencha seu nome');
      return;
    }

    const cpfDigits = formData.cpf.replace(/\D/g, '');
    if (cpfDigits.length !== 11) {
      setError('CPF deve conter 11 dígitos');
      return;
    }

    if (formData.senha.length < 6) {
      setError('A senha deve ter pelo menos 6 caracteres');
      return;
    }

    if (formData.senha !== formData.confirmarSenha) {
      setError('As senhas não coincidem');
      return;
    }

    try {
      setSubmitting(true);

      const cadastroData = {
        nome: formData.nome,
        cpf: cpfDigits,
        email: convite!.email,
        senha: formData.senha,
        organizacaoId: convite!.organizacaoId,
        tokenConvite: token
      };

      await api.post('/recrutadores/cadastro-via-convite', cadastroData);

      await api.post(`/auth/convite/${token}/aceitar`);

      toast.success('Cadastro realizado com sucesso! Faça login para continuar.');
      
      setTimeout(() => {
        navigate('/login', { 
          state: { 
            email: convite!.email,
            message: 'Cadastro concluído! Faça login para acessar o sistema.'
          } 
        });
      }, 2000);

    } catch (err: any) {
      console.error('Erro ao aceitar convite:', err);
      setError(err.response?.data?.message || 'Erro ao processar cadastro. Tente novamente.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRecusar = async () => {
    if (!window.confirm('Tem certeza que deseja recusar este convite?')) {
      return;
    }

    try {
      await api.post(`/auth/convite/${token}/recusar`);
      toast.success('Convite recusado');
      navigate('/login');
    } catch (err: any) {
      console.error('Erro ao recusar convite:', err);
      toast.error('Erro ao recusar convite');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <Card className="w-full max-w-md">
          <CardContent className="p-12 text-center">
            <Loader2 className="w-12 h-12 mx-auto mb-4 animate-spin text-blue-600" />
            <p className="text-muted-foreground">Validando convite...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!conviteValido || error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center">
            <div className="mx-auto mb-4">
              <XCircle className="w-16 h-16 text-red-500" />
            </div>
            <CardTitle className="text-2xl">Convite Inválido</CardTitle>
            <CardDescription className="text-base mt-2">
              {error}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button 
              className="w-full" 
              onClick={() => navigate('/login')}
            >
              Ir para Login
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center space-y-4">
          <div className="mx-auto bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center">
            <Building2 className="w-8 h-8 text-blue-600" />
          </div>
          <div>
            <CardTitle className="text-3xl mb-2">Você foi convidado!</CardTitle>
            <CardDescription className="text-base">
              Você recebeu um convite para se juntar à <strong>{organizacao?.nome}</strong> como recrutador
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent className="space-y-6">
          {/* Informações da Organização */}
          <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
            <h3 className="font-semibold text-blue-900 mb-2 flex items-center gap-2">
              <Building2 className="w-4 h-4" />
              Informações da Organização
            </h3>
            <div className="space-y-1 text-sm text-blue-800">
              <p><strong>Nome:</strong> {organizacao?.nome}</p>
              <p><strong>Localização:</strong> {organizacao?.endereco.cidade}, {organizacao?.endereco.uf}</p>
              <div className="flex items-center gap-2 mt-2">
                <Mail className="w-4 h-4" />
                <span>{convite?.email}</span>
              </div>
            </div>
          </div>

          {/* Benefícios */}
          <div className="bg-green-50 p-4 rounded-lg border border-green-200">
            <h3 className="font-semibold text-green-900 mb-3">Como recrutador, você poderá:</h3>
            <ul className="space-y-2 text-sm text-green-800">
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>Criar e gerenciar vagas de emprego</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>Avaliar candidatos e currículos</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>Gerenciar processos seletivos completos</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>Acessar relatórios e estatísticas detalhadas</span>
              </li>
            </ul>
          </div>

          {/* Formulário de Cadastro */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="border-t pt-4">
              <h3 className="font-semibold mb-4">Complete seu cadastro</h3>
              
              <div className="space-y-4">
                <div>
                  <Label htmlFor="nome">Nome Completo *</Label>
                  <Input
                    id="nome"
                    name="nome"
                    value={formData.nome}
                    onChange={handleChange}
                    placeholder="Seu nome completo"
                    required
                  />
                </div>

                <div>
                  <Label htmlFor="cpf">CPF *</Label>
                  <Input
                    id="cpf"
                    name="cpf"
                    value={formData.cpf}
                    onChange={handleChange}
                    placeholder="000.000.000-00"
                    maxLength={14}
                    required
                  />
                </div>

                <div>
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    value={convite?.email}
                    disabled
                    className="bg-gray-100"
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    Este é o email vinculado ao convite
                  </p>
                </div>

                <div>
                  <Label htmlFor="senha">Senha *</Label>
                  <Input
                    id="senha"
                    name="senha"
                    type="password"
                    value={formData.senha}
                    onChange={handleChange}
                    placeholder="Mínimo 6 caracteres"
                    required
                  />
                </div>

                <div>
                  <Label htmlFor="confirmarSenha">Confirmar Senha *</Label>
                  <Input
                    id="confirmarSenha"
                    name="confirmarSenha"
                    type="password"
                    value={formData.confirmarSenha}
                    onChange={handleChange}
                    placeholder="Repita sua senha"
                    required
                  />
                </div>
              </div>
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-800 p-3 rounded-lg text-sm">
                {error}
              </div>
            )}

            <div className="flex gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={handleRecusar}
                className="flex-1"
                disabled={submitting}
              >
                Recusar Convite
              </Button>
              <Button
                type="submit"
                className="flex-1"
                disabled={submitting}
              >
                {submitting ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Processando...
                  </>
                ) : (
                  'Aceitar e Criar Conta'
                )}
              </Button>
            </div>
          </form>

          <p className="text-xs text-muted-foreground text-center">
            Ao aceitar este convite, você concorda em se juntar à organização como recrutador.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

