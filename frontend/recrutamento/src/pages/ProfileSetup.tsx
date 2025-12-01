import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { candidatoService } from '@/services/candidato.service';
import { authService } from '@/services/auth.service';
import { Calendar, Briefcase, GraduationCap, Award, FileText, FolderGit2, Plus, Trash2 } from 'lucide-react';
import { usePageTitle } from '@/hooks/usePageTitle';
import { toast } from 'sonner';
import AddExperienceDialog, { type ExperienceData } from '@/components/profile/AddExperienceDialog';
import AddHistoricoDialog, { type HistoricoData } from '@/components/profile/AddHistoricoDialog';
import AddCompetenciaDialog, { type CompetenciaData } from '@/components/profile/AddCompetenciaDialog';
import AddCertificadoDialog, { type CertificadoData } from '@/components/profile/AddCertificadoDialog';
import AddPortfolioDialog, { type PortfolioData } from '@/components/profile/AddPortfolioDialog';

export default function ProfileSetup() {
  usePageTitle('Configurar Perfil');
  const navigate = useNavigate();
  const [currentTab, setCurrentTab] = useState('personal');
  const [profileData, setProfileData] = useState({
    nome: '',
    dataNascimento: '',
    logradouro: '',
    numero: '',
    complemento: '',
    cep: '',
    cidade: '',
    uf: '',
  });
  
  const [experiencias, setExperiencias] = useState<(ExperienceData & { id: string })[]>([]);
  const [historicos, setHistoricos] = useState<(HistoricoData & { id: string })[]>([]);
  const [competencias, setCompetencias] = useState<(CompetenciaData & { id: string })[]>([]);
  const [certificados, setCertificados] = useState<(CertificadoData & { id: string })[]>([]);
  const [portfolios, setPortfolios] = useState<(PortfolioData & { id: string })[]>([]);
  
  const [showExperienceDialog, setShowExperienceDialog] = useState(false);
  const [showHistoricoDialog, setShowHistoricoDialog] = useState(false);
  const [showCompetenciaDialog, setShowCompetenciaDialog] = useState(false);
  const [showCertificadoDialog, setShowCertificadoDialog] = useState(false);
  const [showPortfolioDialog, setShowPortfolioDialog] = useState(false);
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProfileData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleAddExperiencia = async (data: ExperienceData) => {
    setExperiencias(prev => [...prev, { ...data, id: Date.now().toString() }]);
  };

  const handleAddHistorico = async (data: HistoricoData) => {
    setHistoricos(prev => [...prev, { ...data, id: Date.now().toString() }]);
  };

  const handleAddCompetencia = async (data: CompetenciaData) => {
    setCompetencias(prev => [...prev, { ...data, id: Date.now().toString() }]);
  };

  const handleAddCertificado = async (data: CertificadoData) => {
    setCertificados(prev => [...prev, { ...data, id: Date.now().toString() }]);
  };

  const handleAddPortfolio = async (data: PortfolioData) => {
    setPortfolios(prev => [...prev, { ...data, id: Date.now().toString() }]);
  };

  const removeExperiencia = (id: string) => setExperiencias(prev => prev.filter(item => item.id !== id));
  const removeHistorico = (id: string) => setHistoricos(prev => prev.filter(item => item.id !== id));
  const removeCompetencia = (id: string) => setCompetencias(prev => prev.filter(item => item.id !== id));
  const removeCertificado = (id: string) => setCertificados(prev => prev.filter(item => item.id !== id));
  const removePortfolio = (id: string) => setPortfolios(prev => prev.filter(item => item.id !== id));

  const validateProfileData = (): boolean => {
    if (!profileData.dataNascimento) {
      setError('⚠️ Data de nascimento é obrigatória');
      setCurrentTab('personal');
      toast.error('Data de nascimento é obrigatória');
      return false;
    }

    if (!profileData.cep || !profileData.cep.trim()) {
      setError('⚠️ CEP é obrigatório');
      setCurrentTab('personal');
      toast.error('CEP é obrigatório');
      return false;
    }

    const cepDigits = profileData.cep.replace(/\D/g, '');
    if (cepDigits.length !== 8) {
      setError('⚠️ CEP deve conter 8 dígitos');
      setCurrentTab('personal');
      toast.error('CEP deve conter 8 dígitos');
      return false;
    }

    if (!profileData.logradouro || !profileData.logradouro.trim()) {
      setError('⚠️ Logradouro é obrigatório');
      setCurrentTab('personal');
      toast.error('Logradouro é obrigatório');
      return false;
    }

    if (!profileData.numero || !profileData.numero.trim()) {
      setError('⚠️ Número é obrigatório');
      setCurrentTab('personal');
      toast.error('Número é obrigatório');
      return false;
    }

    if (!profileData.cidade || !profileData.cidade.trim()) {
      setError('⚠️ Cidade é obrigatória');
      setCurrentTab('personal');
      toast.error('Cidade é obrigatória');
      return false;
    }

    if (!profileData.uf || !profileData.uf.trim()) {
      setError('⚠️ UF é obrigatória');
      setCurrentTab('personal');
      toast.error('UF é obrigatória');
      return false;
    }

    if (profileData.uf.length !== 2) {
      setError('⚠️ UF deve ter 2 caracteres (ex: SP, RJ)');
      setCurrentTab('personal');
      toast.error('UF deve ter 2 caracteres');
      return false;
    }

    for (let i = 0; i < experiencias.length; i++) {
      const exp = experiencias[i];
      if (!exp.cargo || !exp.cargo.trim()) {
        setError(`⚠️ Experiência ${i + 1}: Cargo é obrigatório`);
        setCurrentTab('experience');
        toast.error(`Experiência ${i + 1}: Cargo é obrigatório`);
        return false;
      }
      if (!exp.empresa || !exp.empresa.trim()) {
        setError(`⚠️ Experiência ${i + 1}: Empresa é obrigatória`);
        setCurrentTab('experience');
        toast.error(`Experiência ${i + 1}: Empresa é obrigatória`);
        return false;
      }
      if (!exp.descricao || !exp.descricao.trim()) {
        setError(`⚠️ Experiência ${i + 1}: Descrição é obrigatória`);
        setCurrentTab('experience');
        toast.error(`Experiência ${i + 1}: Descrição é obrigatória`);
        return false;
      }
      if (!exp.dataInicio) {
        setError(`⚠️ Experiência ${i + 1}: Data de início é obrigatória`);
        setCurrentTab('experience');
        toast.error(`Experiência ${i + 1}: Data de início é obrigatória`);
        return false;
      }
      if (exp.dataFim && exp.dataInicio) {
        const dataInicio = new Date(exp.dataInicio);
        const dataFim = new Date(exp.dataFim);
        if (dataFim < dataInicio) {
          setError(`⚠️ Experiência ${i + 1}: Data de saída não pode ser anterior à data de entrada`);
          setCurrentTab('experience');
          toast.error(`Experiência ${i + 1}: Data de saída não pode ser anterior à data de entrada`);
          return false;
        }
      }
    }

    for (let i = 0; i < historicos.length; i++) {
      const hist = historicos[i];
      if (!hist.titulo || !hist.titulo.trim()) {
        setError(`⚠️ Formação ${i + 1}: Título é obrigatório`);
        setCurrentTab('education');
        toast.error(`Formação ${i + 1}: Título é obrigatório`);
        return false;
      }
      if (!hist.instituicao || !hist.instituicao.trim()) {
        setError(`⚠️ Formação ${i + 1}: Instituição é obrigatória`);
        setCurrentTab('education');
        toast.error(`Formação ${i + 1}: Instituição é obrigatória`);
        return false;
      }
      if (!hist.dataInicio) {
        setError(`⚠️ Formação ${i + 1}: Data de início é obrigatória`);
        setCurrentTab('education');
        toast.error(`Formação ${i + 1}: Data de início é obrigatória`);
        return false;
      }
      if (hist.dataFim && hist.dataInicio) {
        const dataInicio = new Date(hist.dataInicio);
        const dataFim = new Date(hist.dataFim);
        if (dataFim < dataInicio) {
          setError(`⚠️ Formação ${i + 1}: Data de conclusão não pode ser anterior à data de início`);
          setCurrentTab('education');
          toast.error(`Formação ${i + 1}: Data de conclusão não pode ser anterior à data de início`);
          return false;
        }
      }
    }

    for (let i = 0; i < competencias.length; i++) {
      const comp = competencias[i];
      if (!comp.titulo || !comp.titulo.trim()) {
        setError(`⚠️ Competência ${i + 1}: Título é obrigatório`);
        setCurrentTab('skills');
        toast.error(`Competência ${i + 1}: Título é obrigatório`);
        return false;
      }
      if (!comp.descricao || !comp.descricao.trim()) {
        setError(`⚠️ Competência ${i + 1}: Descrição é obrigatória`);
        setCurrentTab('skills');
        toast.error(`Competência ${i + 1}: Descrição é obrigatória`);
        return false;
      }
      if (!comp.nivel) {
        setError(`⚠️ Competência ${i + 1}: Nível é obrigatório`);
        setCurrentTab('skills');
        toast.error(`Competência ${i + 1}: Nível é obrigatório`);
        return false;
      }
    }

    for (let i = 0; i < certificados.length; i++) {
      const cert = certificados[i];
      if (!cert.titulo || !cert.titulo.trim()) {
        setError(`⚠️ Certificado ${i + 1}: Título é obrigatório`);
        setCurrentTab('certificates');
        toast.error(`Certificado ${i + 1}: Título é obrigatório`);
        return false;
      }
      if (!cert.instituicao || !cert.instituicao.trim()) {
        setError(`⚠️ Certificado ${i + 1}: Instituição é obrigatória`);
        setCurrentTab('certificates');
        toast.error(`Certificado ${i + 1}: Instituição é obrigatória`);
        return false;
      }
      if (!cert.dataEmissao) {
        setError(`⚠️ Certificado ${i + 1}: Data de emissão é obrigatória`);
        setCurrentTab('certificates');
        toast.error(`Certificado ${i + 1}: Data de emissão é obrigatória`);
        return false;
      }
      if (cert.dataValidade && cert.dataEmissao) {
        const dataEmissao = new Date(cert.dataEmissao);
        const dataValidade = new Date(cert.dataValidade);
        if (dataValidade < dataEmissao) {
          setError(`⚠️ Certificado ${i + 1}: Data de validade não pode ser anterior à data de emissão`);
          setCurrentTab('certificates');
          toast.error(`Certificado ${i + 1}: Data de validade não pode ser anterior à data de emissão`);
          return false;
        }
      }
    }

    for (let i = 0; i < portfolios.length; i++) {
      const port = portfolios[i];
      if (!port.titulo || !port.titulo.trim()) {
        setError(`⚠️ Portfólio ${i + 1}: Título é obrigatório`);
        setCurrentTab('portfolio');
        toast.error(`Portfólio ${i + 1}: Título é obrigatório`);
        return false;
      }
      if (!port.link || !port.link.trim()) {
        setError(`⚠️ Portfólio ${i + 1}: Link é obrigatório`);
        setCurrentTab('portfolio');
        toast.error(`Portfólio ${i + 1}: Link é obrigatório`);
        return false;
      }
      try {
        new URL(port.link);
      } catch {
        setError(`⚠️ Portfólio ${i + 1}: Link inválido. Use formato completo (ex: https://exemplo.com)`);
        setCurrentTab('portfolio');
        toast.error(`Portfólio ${i + 1}: Link inválido`);
        return false;
      }
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const registrationDataStr = sessionStorage.getItem('registrationData');
      if (!registrationDataStr) {
        setError('Dados de cadastro não encontrados. Por favor, inicie o cadastro novamente.');
        setLoading(false);
        navigate('/cadastro');
        return;
      }

      const registrationData = JSON.parse(registrationDataStr);

      if (!validateProfileData()) {
        toast.error('Por favor, corrija os erros antes de continuar');
        setLoading(false);
        return;
      }

      const cepDigits = profileData.cep.replace(/\D/g, '');

      const fullData = {
        ...registrationData,
        perfilCandidato: {
          dataNascimento: profileData.dataNascimento,
          nome: profileData.nome || registrationData.nome,
          logradouro: profileData.logradouro,
          numero: profileData.numero,
          complemento: profileData.complemento || undefined,
          cep: cepDigits,
          cidade: profileData.cidade,
          uf: profileData.uf,
        },
      };

      console.log('Registrando candidato...', fullData);
      await candidatoService.registrar(fullData);
      
      console.log('Fazendo login...');
      const loginResponse = await authService.login({
        email: registrationData.email,
        senha: registrationData.senha,
      });

      const usuarioId = loginResponse.usuarioId;
      console.log('Login realizado. Usuario ID:', usuarioId);

      if (experiencias.length > 0 || historicos.length > 0 || competencias.length > 0 || certificados.length > 0 || portfolios.length > 0) {
        const toastId = toast.loading('Salvando informações do perfil...');
        
        try {
          for (const exp of experiencias) {
            await candidatoService.adicionarExperiencia(usuarioId, {
              cargo: exp.cargo,
              empresa: exp.empresa,
              descricao: exp.descricao,
              dataInicio: exp.dataInicio,
              dataFim: exp.dataFim,
            });
          }

          for (const hist of historicos) {
            await candidatoService.adicionarHistorico(usuarioId, {
              titulo: hist.titulo,
              instituicao: hist.instituicao,
              descricao: hist.descricao,
              dataInicio: hist.dataInicio,
              dataFim: hist.dataFim,
            });
          }

          for (const comp of competencias) {
            await candidatoService.adicionarCompetencia(usuarioId, comp);
          }

          for (const cert of certificados) {
            await candidatoService.adicionarCertificado(usuarioId, cert);
          }

          for (const port of portfolios) {
            await candidatoService.adicionarPortfolio(usuarioId, port);
          }

          toast.dismiss(toastId);
        } catch (profileErr: any) {
          toast.dismiss(toastId);
          console.error('Erro ao adicionar itens do perfil:', profileErr);
          
          toast.warning('Cadastro criado! Porém, houve erro ao adicionar alguns itens. Você pode adicioná-los na página de perfil.');
          sessionStorage.removeItem('registrationData');
          navigate('/dashboard/perfil');
          return;
        }
      }

      sessionStorage.removeItem('registrationData');
      toast.success('Perfil criado com sucesso!');
      navigate('/dashboard');
    } catch (err: any) {
      console.error('Erro ao criar perfil:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Erro ao completar cadastro. Tente novamente.';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const getNivelLabel = (nivel: string) => {
    const labels: Record<string, string> = { BASICO: 'Básico', INTERMEDIARIO: 'Intermediário', AVANCADO: 'Avançado' };
    return labels[nivel] || nivel;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20 p-6">
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="text-center space-y-2">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">
            Complete seu Perfil
          </h1>
          <p className="text-muted-foreground text-lg">
            Crie um perfil completo para destacar suas qualificações e encontrar as melhores oportunidades
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <Tabs value={currentTab} onValueChange={setCurrentTab} className="w-full">
            <TabsList className="grid w-full grid-cols-6 h-auto">
              <TabsTrigger value="personal" className="flex flex-col items-center gap-1 py-3">
                <Calendar className="w-5 h-5" />
                <span className="text-xs">Pessoal</span>
              </TabsTrigger>
              <TabsTrigger value="experience" className="flex flex-col items-center gap-1 py-3">
                <Briefcase className="w-5 h-5" />
                <span className="text-xs">Experiência</span>
                {experiencias.length > 0 && <span className="text-xs text-primary font-semibold">({experiencias.length})</span>}
              </TabsTrigger>
              <TabsTrigger value="education" className="flex flex-col items-center gap-1 py-3">
                <GraduationCap className="w-5 h-5" />
                <span className="text-xs">Educação</span>
                {historicos.length > 0 && <span className="text-xs text-primary font-semibold">({historicos.length})</span>}
              </TabsTrigger>
              <TabsTrigger value="skills" className="flex flex-col items-center gap-1 py-3">
                <Award className="w-5 h-5" />
                <span className="text-xs">Competências</span>
                {competencias.length > 0 && <span className="text-xs text-primary font-semibold">({competencias.length})</span>}
              </TabsTrigger>
              <TabsTrigger value="certificates" className="flex flex-col items-center gap-1 py-3">
                <FileText className="w-5 h-5" />
                <span className="text-xs">Certificados</span>
                {certificados.length > 0 && <span className="text-xs text-primary font-semibold">({certificados.length})</span>}
              </TabsTrigger>
              <TabsTrigger value="portfolio" className="flex flex-col items-center gap-1 py-3">
                <FolderGit2 className="w-5 h-5" />
                <span className="text-xs">Portfólio</span>
                {portfolios.length > 0 && <span className="text-xs text-primary font-semibold">({portfolios.length})</span>}
              </TabsTrigger>
            </TabsList>

            {/* Personal Information Tab */}
            <TabsContent value="personal" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>Informações Pessoais</CardTitle>
                  <CardDescription>Preencha seus dados pessoais e endereço</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="dataNascimento">Data de nascimento: <span className="text-destructive">*</span></Label>
                      <div className="relative">
                        <Input id="dataNascimento" name="dataNascimento" type="date" value={profileData.dataNascimento} onChange={handleChange} required disabled={loading} className="pr-10 [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-inner-spin-button]:hidden" />
                        <Calendar className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground pointer-events-none" />
                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="cep">CEP: <span className="text-destructive">*</span></Label>
                      <Input id="cep" name="cep" type="text" placeholder="00000-000" value={profileData.cep} onChange={handleChange} required disabled={loading} maxLength={9} />
                    </div>
                  </div>
                  <div className="grid md:grid-cols-3 gap-4">
                    <div className="md:col-span-2 space-y-2">
                      <Label htmlFor="logradouro">Logradouro: <span className="text-destructive">*</span></Label>
                      <Input id="logradouro" name="logradouro" type="text" value={profileData.logradouro} onChange={handleChange} required disabled={loading} />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="numero">Número: <span className="text-destructive">*</span></Label>
                      <Input id="numero" name="numero" type="text" value={profileData.numero} onChange={handleChange} required disabled={loading} />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="complemento">Complemento:</Label>
                    <Input id="complemento" name="complemento" type="text" value={profileData.complemento} onChange={handleChange} disabled={loading} />
                  </div>
                  <div className="grid md:grid-cols-3 gap-4">
                    <div className="md:col-span-2 space-y-2">
                      <Label htmlFor="cidade">Cidade: <span className="text-destructive">*</span></Label>
                      <Input id="cidade" name="cidade" type="text" value={profileData.cidade} onChange={handleChange} required disabled={loading} />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="uf">UF: <span className="text-destructive">*</span></Label>
                      <Input id="uf" name="uf" type="text" placeholder="SP" value={profileData.uf} onChange={handleChange} required disabled={loading} maxLength={2} />
                    </div>
                  </div>
                  <div className="flex justify-end pt-4">
                    <Button type="button" onClick={() => setCurrentTab('experience')} disabled={loading}>Próximo: Experiência</Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Experience Tab */}
            <TabsContent value="experience" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>Experiência Profissional</CardTitle>
                  <CardDescription>Adicione suas experiências profissionais (opcional, mas recomendado)</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {experiencias.length === 0 ? (
                    <div className="text-center py-12 border-2 border-dashed rounded-lg">
                      <Briefcase className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground mb-4">Nenhuma experiência adicionada ainda</p>
                      <Button type="button" onClick={() => setShowExperienceDialog(true)} disabled={loading}><Plus className="w-4 h-4 mr-2" />Adicionar Experiência</Button>
                    </div>
                  ) : (
                    <>
                      <div className="space-y-3">
                        {experiencias.map((exp) => (
                          <Card key={exp.id} className="border-l-4 border-l-primary">
                            <CardContent className="pt-4">
                              <div className="flex justify-between items-start">
                                <div className="flex-1">
                                  <h4 className="font-semibold text-lg">{exp.cargo}</h4>
                                  <p className="text-muted-foreground">{exp.empresa}</p>
                                  <p className="text-sm text-muted-foreground mt-1">{new Date(exp.dataInicio).toLocaleDateString('pt-BR')} - {exp.dataFim ? new Date(exp.dataFim).toLocaleDateString('pt-BR') : 'Atual'}</p>
                                  <p className="text-sm mt-2">{exp.descricao}</p>
                                </div>
                                <Button type="button" variant="ghost" size="sm" onClick={() => removeExperiencia(exp.id)} disabled={loading}><Trash2 className="w-4 h-4 text-destructive" /></Button>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                      <Button type="button" variant="outline" onClick={() => setShowExperienceDialog(true)} disabled={loading} className="w-full"><Plus className="w-4 h-4 mr-2" />Adicionar Outra Experiência</Button>
                    </>
                  )}
                  <div className="flex justify-between pt-4">
                    <Button type="button" variant="outline" onClick={() => setCurrentTab('personal')} disabled={loading}>Voltar</Button>
                    <Button type="button" onClick={() => setCurrentTab('education')} disabled={loading}>Próximo: Educação</Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Education Tab */}
            <TabsContent value="education" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>Histórico Acadêmico</CardTitle>
                  <CardDescription>Adicione sua formação acadêmica (opcional, mas recomendado)</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {historicos.length === 0 ? (
                    <div className="text-center py-12 border-2 border-dashed rounded-lg">
                      <GraduationCap className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground mb-4">Nenhuma formação adicionada ainda</p>
                      <Button type="button" onClick={() => setShowHistoricoDialog(true)} disabled={loading}><Plus className="w-4 h-4 mr-2" />Adicionar Formação</Button>
                    </div>
                  ) : (
                    <>
                      <div className="space-y-3">
                        {historicos.map((hist) => (
                          <Card key={hist.id} className="border-l-4 border-l-primary">
                            <CardContent className="pt-4">
                              <div className="flex justify-between items-start">
                                <div className="flex-1">
                                  <h4 className="font-semibold text-lg">{hist.titulo}</h4>
                                  <p className="text-muted-foreground">{hist.instituicao}</p>
                                  <p className="text-sm text-muted-foreground mt-1">{new Date(hist.dataInicio).toLocaleDateString('pt-BR')} - {hist.dataFim ? new Date(hist.dataFim).toLocaleDateString('pt-BR') : 'Em andamento'}</p>
                                  {hist.descricao && <p className="text-sm mt-2">{hist.descricao}</p>}
                                </div>
                                <Button type="button" variant="ghost" size="sm" onClick={() => removeHistorico(hist.id)} disabled={loading}><Trash2 className="w-4 h-4 text-destructive" /></Button>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                      <Button type="button" variant="outline" onClick={() => setShowHistoricoDialog(true)} disabled={loading} className="w-full"><Plus className="w-4 h-4 mr-2" />Adicionar Outra Formação</Button>
                    </>
                  )}
                  <div className="flex justify-between pt-4">
                    <Button type="button" variant="outline" onClick={() => setCurrentTab('experience')} disabled={loading}>Voltar</Button>
                    <Button type="button" onClick={() => setCurrentTab('skills')} disabled={loading}>Próximo: Competências</Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Skills Tab */}
            <TabsContent value="skills" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>Competências</CardTitle>
                  <CardDescription>Adicione suas habilidades e competências (opcional, mas recomendado)</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {competencias.length === 0 ? (
                    <div className="text-center py-12 border-2 border-dashed rounded-lg">
                      <Award className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground mb-4">Nenhuma competência adicionada ainda</p>
                      <Button type="button" onClick={() => setShowCompetenciaDialog(true)} disabled={loading}><Plus className="w-4 h-4 mr-2" />Adicionar Competência</Button>
                    </div>
                  ) : (
                    <>
                      <div className="grid md:grid-cols-2 gap-3">
                        {competencias.map((comp) => (
                          <Card key={comp.id} className="border-l-4 border-l-primary">
                            <CardContent className="pt-4">
                              <div className="flex justify-between items-start">
                                <div className="flex-1">
                                  <h4 className="font-semibold">{comp.titulo}</h4>
                                  <p className="text-sm text-primary font-medium">{getNivelLabel(comp.nivel)}</p>
                                  <p className="text-sm text-muted-foreground mt-1">{comp.descricao}</p>
                                </div>
                                <Button type="button" variant="ghost" size="sm" onClick={() => removeCompetencia(comp.id)} disabled={loading}><Trash2 className="w-4 h-4 text-destructive" /></Button>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                      <Button type="button" variant="outline" onClick={() => setShowCompetenciaDialog(true)} disabled={loading} className="w-full"><Plus className="w-4 h-4 mr-2" />Adicionar Outra Competência</Button>
                    </>
                  )}
                  <div className="flex justify-between pt-4">
                    <Button type="button" variant="outline" onClick={() => setCurrentTab('education')} disabled={loading}>Voltar</Button>
                    <Button type="button" onClick={() => setCurrentTab('certificates')} disabled={loading}>Próximo: Certificados</Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Certificates Tab */}
            <TabsContent value="certificates" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>Certificados</CardTitle>
                  <CardDescription>Adicione seus certificados e cursos (opcional)</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {certificados.length === 0 ? (
                    <div className="text-center py-12 border-2 border-dashed rounded-lg">
                      <FileText className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground mb-4">Nenhum certificado adicionado ainda</p>
                      <Button type="button" onClick={() => setShowCertificadoDialog(true)} disabled={loading}><Plus className="w-4 h-4 mr-2" />Adicionar Certificado</Button>
                    </div>
                  ) : (
                    <>
                      <div className="space-y-3">
                        {certificados.map((cert) => (
                          <Card key={cert.id} className="border-l-4 border-l-primary">
                            <CardContent className="pt-4">
                              <div className="flex justify-between items-start">
                                <div className="flex-1">
                                  <h4 className="font-semibold">{cert.titulo}</h4>
                                  <p className="text-muted-foreground">{cert.instituicao}</p>
                                  <p className="text-sm text-muted-foreground mt-1">Emitido em: {new Date(cert.dataEmissao).toLocaleDateString('pt-BR')}{cert.dataValidade && ` • Válido até: ${new Date(cert.dataValidade).toLocaleDateString('pt-BR')}`}</p>
                                  {cert.descricao && <p className="text-sm mt-2">{cert.descricao}</p>}
                                </div>
                                <Button type="button" variant="ghost" size="sm" onClick={() => removeCertificado(cert.id)} disabled={loading}><Trash2 className="w-4 h-4 text-destructive" /></Button>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                      <Button type="button" variant="outline" onClick={() => setShowCertificadoDialog(true)} disabled={loading} className="w-full"><Plus className="w-4 h-4 mr-2" />Adicionar Outro Certificado</Button>
                    </>
                  )}
                  <div className="flex justify-between pt-4">
                    <Button type="button" variant="outline" onClick={() => setCurrentTab('skills')} disabled={loading}>Voltar</Button>
                    <Button type="button" onClick={() => setCurrentTab('portfolio')} disabled={loading}>Próximo: Portfólio</Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Portfolio Tab */}
            <TabsContent value="portfolio" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>Portfólio</CardTitle>
                  <CardDescription>Adicione links para seus projetos e trabalhos (opcional)</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {portfolios.length === 0 ? (
                    <div className="text-center py-12 border-2 border-dashed rounded-lg">
                      <FolderGit2 className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground mb-4">Nenhum item de portfólio adicionado ainda</p>
                      <Button type="button" onClick={() => setShowPortfolioDialog(true)} disabled={loading}><Plus className="w-4 h-4 mr-2" />Adicionar ao Portfólio</Button>
                    </div>
                  ) : (
                    <>
                      <div className="grid md:grid-cols-2 gap-3">
                        {portfolios.map((port) => (
                          <Card key={port.id} className="border-l-4 border-l-primary">
                            <CardContent className="pt-4">
                              <div className="flex justify-between items-start">
                                <div className="flex-1">
                                  <h4 className="font-semibold">{port.titulo}</h4>
                                  <a href={port.link} target="_blank" rel="noopener noreferrer" className="text-sm text-primary hover:underline break-all">{port.link}</a>
                                </div>
                                <Button type="button" variant="ghost" size="sm" onClick={() => removePortfolio(port.id)} disabled={loading}><Trash2 className="w-4 h-4 text-destructive" /></Button>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                      <Button type="button" variant="outline" onClick={() => setShowPortfolioDialog(true)} disabled={loading} className="w-full"><Plus className="w-4 h-4 mr-2" />Adicionar Outro Item</Button>
                    </>
                  )}
                  <div className="flex justify-between pt-4">
                    <Button type="button" variant="outline" onClick={() => setCurrentTab('certificates')} disabled={loading}>Voltar</Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>

          {error && <div className="mt-4 p-3 text-sm text-destructive bg-destructive/10 rounded-md">{error}</div>}

          <div className="mt-6 flex justify-between items-center">
            <p className="text-sm text-muted-foreground">
              {experiencias.length + historicos.length + competencias.length + certificados.length + portfolios.length > 0 
                ? `${experiencias.length + historicos.length + competencias.length + certificados.length + portfolios.length} itens adicionados ao perfil` 
                : 'Preencha pelo menos as informações pessoais para continuar'}
            </p>
            <Button type="submit" size="lg" disabled={loading}>
              {loading ? 'Criando perfil...' : 'Finalizar Cadastro'}
            </Button>
          </div>
        </form>

        {/* Dialogs */}
        <AddExperienceDialog open={showExperienceDialog} onOpenChange={setShowExperienceDialog} onSubmit={handleAddExperiencia} />
        <AddHistoricoDialog open={showHistoricoDialog} onOpenChange={setShowHistoricoDialog} onSubmit={handleAddHistorico} />
        <AddCompetenciaDialog open={showCompetenciaDialog} onOpenChange={setShowCompetenciaDialog} onSubmit={handleAddCompetencia} />
        <AddCertificadoDialog open={showCertificadoDialog} onOpenChange={setShowCertificadoDialog} onSubmit={handleAddCertificado} />
        <AddPortfolioDialog open={showPortfolioDialog} onOpenChange={setShowPortfolioDialog} onSubmit={handleAddPortfolio} />
      </div>
    </div>
  );
}
