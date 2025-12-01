import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Building2, User } from 'lucide-react';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function Register() {
  usePageTitle('Cadastro');
  const navigate = useNavigate();
  const [userType, setUserType] = useState<'candidato' | 'organizacao' | null>(null);
  const [formData, setFormData] = useState({
    nome: '',
    cpf: '',
    email: '',
    senha: '',
    confirmarSenha: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (formData.senha !== formData.confirmarSenha) {
      setError('As senhas não coincidem');
      return;
    }

    const cpfDigits = formData.cpf.replace(/\D/g, '');
    if (cpfDigits.length !== 11) {
      setError('CPF deve conter 11 dígitos');
      return;
    }

    setLoading(true);

    try {
      sessionStorage.setItem('registrationData', JSON.stringify({
        nome: formData.nome,
        cpf: cpfDigits,
        email: formData.email,
        senha: formData.senha,
      }));

      if (userType === 'organizacao') {
        navigate('/cadastro/organizacao');
      } else {
        navigate('/cadastro/perfil');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao processar cadastro');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary/10 via-primary/5 to-background items-center justify-center p-8 lg:p-12">
        <div className="max-w-md space-y-6 text-center">
          <div className="w-48 h-48 lg:w-64 lg:h-64 mx-auto bg-muted rounded-lg flex items-center justify-center">
            <div className="text-6xl text-muted-foreground">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-24 h-24 lg:w-32 lg:h-32"
              >
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </div>
          </div>
          <h2 className="text-2xl lg:text-3xl font-bold">Junte-se a nós!</h2>
          <p className="text-sm lg:text-base text-muted-foreground">
            Crie sua conta e encontre as melhores oportunidades de carreira
          </p>
        </div>
      </div>

      <div className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-background">
        <div className="w-full max-w-md space-y-6 sm:space-y-8">
          <div className="flex flex-col sm:flex-row justify-end items-center gap-2 text-sm">
            <span className="text-muted-foreground">Já possui uma conta?</span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate('/login')}
            >
              Entrar
            </Button>
          </div>

          <Card>
            <CardHeader className="space-y-1">
              <CardTitle className="text-xl sm:text-2xl font-bold text-center">
                Faça seu cadastro
              </CardTitle>
              <CardDescription className="text-center text-sm">
                {!userType ? 'Escolha o tipo de conta' : 'Preencha seus dados para começar'}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {!userType ? (
                <div className="space-y-3 sm:space-y-4">
                  <Button
                    type="button"
                    variant="outline"
                    className="w-full h-20 sm:h-24 flex flex-col gap-2"
                    onClick={() => setUserType('candidato')}
                  >
                    <User className="w-6 h-6 sm:w-8 sm:h-8" />
                    <div>
                      <div className="font-semibold text-sm sm:text-base">Sou Candidato</div>
                      <div className="text-xs text-muted-foreground">Busco oportunidades de emprego</div>
                    </div>
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    className="w-full h-20 sm:h-24 flex flex-col gap-2"
                    onClick={() => setUserType('organizacao')}
                  >
                    <Building2 className="w-6 h-6 sm:w-8 sm:h-8" />
                    <div>
                      <div className="font-semibold text-sm sm:text-base">Sou Empresa</div>
                      <div className="text-xs text-muted-foreground">Busco candidatos para minha empresa</div>
                    </div>
                  </Button>
                </div>
              ) : (
                <form onSubmit={handleSubmit} className="space-y-4">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setUserType(null)}
                    className="mb-2 h-auto"
                  >
                    ← Voltar
                  </Button>
                <div className="space-y-2">
                  <Label htmlFor="nome">Nome</Label>
                  <Input
                    id="nome"
                    name="nome"
                    type="text"
                    placeholder="Seu nome completo"
                    value={formData.nome}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    tabIndex={1}
                    className="h-11"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="email">E-mail</Label>
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    placeholder="seu@email.com"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    tabIndex={2}
                    className="h-11"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="cpf">CPF</Label>
                  <Input
                    id="cpf"
                    name="cpf"
                    type="text"
                    placeholder="000.000.000-00"
                    value={formData.cpf}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    maxLength={14}
                    tabIndex={3}
                    className="h-11"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="senha">Senha</Label>
                  <Input
                    id="senha"
                    name="senha"
                    type="password"
                    placeholder="••••••••"
                    value={formData.senha}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    minLength={6}
                    tabIndex={4}
                    className="h-11"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confirmarSenha">Confirme sua senha</Label>
                  <Input
                    id="confirmarSenha"
                    name="confirmarSenha"
                    type="password"
                    placeholder="••••••••"
                    value={formData.confirmarSenha}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    minLength={6}
                    tabIndex={5}
                    className="h-11"
                  />
                </div>

                {error && (
                  <div className="p-3 text-sm text-destructive bg-destructive/10 rounded-md">
                    {error}
                  </div>
                )}

                <Button
                  type="submit"
                  className="w-full h-11"
                  disabled={loading}
                  tabIndex={6}
                >
                  {loading ? 'Processando...' : 'Continuar'}
                </Button>
              </form>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
