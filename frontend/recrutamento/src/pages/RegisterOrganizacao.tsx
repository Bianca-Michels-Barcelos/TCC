import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { organizacaoService } from "@/services/organizacao.service";
import { authService } from "@/services/auth.service";
import { Building2 } from "lucide-react";
import { usePageTitle } from '@/hooks/usePageTitle';

export default function RegisterOrganizacao() {
  usePageTitle('Cadastro de Organização');
  const navigate = useNavigate();
  const [adminData, setAdminData] = useState<any>(null);
  const [formData, setFormData] = useState({
    cnpj: "",
    nomeOrganizacao: "",
    logradouro: "",
    numero: "",
    complemento: "",
    cep: "",
    cidade: "",
    uf: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const registrationData = sessionStorage.getItem("registrationData");
    if (!registrationData) {
      navigate("/cadastro");
      return;
    }
    setAdminData(JSON.parse(registrationData));
  }, [navigate]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    const cnpjDigits = formData.cnpj.replace(/\D/g, "");
    if (cnpjDigits.length !== 14) {
      setError("CNPJ deve conter 14 dígitos");
      return;
    }

    const cepDigits = formData.cep.replace(/\D/g, "");
    if (cepDigits.length !== 8) {
      setError("CEP deve conter 8 dígitos");
      return;
    }

    if (formData.uf.length !== 2) {
      setError("UF deve ter 2 caracteres");
      return;
    }

    setLoading(true);

    try {
      await organizacaoService.registrar({
        cnpj: cnpjDigits,
        nome: formData.nomeOrganizacao,
        logradouro: formData.logradouro,
        numero: formData.numero,
        complemento: formData.complemento,
        cep: cepDigits,
        cidade: formData.cidade,
        uf: formData.uf.toUpperCase(),
        adminRecruiter: {
          nome: adminData.nome,
          cpf: adminData.cpf,
          email: adminData.email,
          senha: adminData.senha,
        },
      });

      sessionStorage.removeItem("registrationData");

      await authService.login({
        email: adminData.email,
        senha: adminData.senha,
      });

      navigate("/dashboard");
    } catch (err: any) {
      setError(err.response?.data?.message || "Erro ao cadastrar empresa");
    } finally {
      setLoading(false);
    }
  };

  if (!adminData) {
    return null;
  }

  return (
    <div className="min-h-screen flex">
      {/* Left side - Illustration */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary/10 via-primary/5 to-background items-center justify-center p-8 lg:p-12">
        <div className="max-w-md space-y-6 text-center">
          <div className="w-48 h-48 lg:w-64 lg:h-64 mx-auto bg-muted rounded-lg flex items-center justify-center">
            <Building2 className="w-24 h-24 lg:w-32 lg:h-32 text-muted-foreground" />
          </div>
          <h2 className="text-2xl lg:text-3xl font-bold">Cadastre sua Empresa</h2>
          <p className="text-sm lg:text-base text-muted-foreground">
            Complete o cadastro da sua empresa e comece a buscar os melhores
            talentos
          </p>
        </div>
      </div>

      {/* Right side - Organization form */}
      <div className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-background">
        <div className="w-full max-w-md space-y-6 sm:space-y-8">
          {/* Header */}
          <div className="text-center">
            <h1 className="text-xl sm:text-2xl font-bold">Dados da Empresa</h1>
            <p className="text-sm text-muted-foreground mt-2">
              Administrador: {adminData.nome}
            </p>
          </div>

          <Card>
            <CardHeader className="space-y-1">
              <CardTitle className="text-lg sm:text-xl font-bold">
                Informações da Empresa
              </CardTitle>
              <CardDescription className="text-sm">
                Preencha os dados da sua empresa
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="cnpj">CNPJ</Label>
                  <Input
                    id="cnpj"
                    name="cnpj"
                    type="text"
                    placeholder="00.000.000/0000-00"
                    value={formData.cnpj}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    maxLength={18}
                    tabIndex={1}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="nomeOrganizacao">Nome da Empresa</Label>
                  <Input
                    id="nomeOrganizacao"
                    name="nomeOrganizacao"
                    type="text"
                    placeholder="Nome da empresa"
                    value={formData.nomeOrganizacao}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    maxLength={100}
                    tabIndex={2}
                  />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="sm:col-span-2 space-y-2">
                    <Label htmlFor="logradouro">Logradouro</Label>
                    <Input
                      id="logradouro"
                      name="logradouro"
                      type="text"
                      placeholder="Rua, Avenida..."
                      value={formData.logradouro}
                      onChange={handleChange}
                      required
                      disabled={loading}
                      tabIndex={3}
                      className="h-11"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="numero">Número</Label>
                    <Input
                      id="numero"
                      name="numero"
                      type="text"
                      placeholder="123"
                      value={formData.numero}
                      onChange={handleChange}
                      required
                      disabled={loading}
                      tabIndex={4}
                      className="h-11"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="complemento">Complemento (opcional)</Label>
                  <Input
                    id="complemento"
                    name="complemento"
                    type="text"
                    placeholder="Sala, Andar..."
                    value={formData.complemento}
                    onChange={handleChange}
                    disabled={loading}
                    tabIndex={5}
                  />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="cep">CEP</Label>
                    <Input
                      id="cep"
                      name="cep"
                      type="text"
                      placeholder="00000-000"
                      value={formData.cep}
                      onChange={handleChange}
                      required
                      disabled={loading}
                      maxLength={9}
                      tabIndex={6}
                      className="h-11"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="uf">UF</Label>
                    <Input
                      id="uf"
                      name="uf"
                      type="text"
                      placeholder="SP"
                      value={formData.uf}
                      onChange={handleChange}
                      required
                      disabled={loading}
                      maxLength={2}
                      tabIndex={7}
                      className="h-11"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="cidade">Cidade</Label>
                  <Input
                    id="cidade"
                    name="cidade"
                    type="text"
                    placeholder="São Paulo"
                    value={formData.cidade}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    tabIndex={8}
                  />
                </div>

                {error && (
                  <div className="p-3 text-sm text-destructive bg-destructive/10 rounded-md">
                    {error}
                  </div>
                )}

                <div className="flex flex-col sm:flex-row gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    className="flex-1 h-11"
                    onClick={() => navigate("/cadastro")}
                    disabled={loading}
                    tabIndex={10}
                  >
                    Voltar
                  </Button>
                  <Button type="submit" className="flex-1 h-11" disabled={loading} tabIndex={9}>
                    {loading ? "Cadastrando..." : "Finalizar Cadastro"}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
