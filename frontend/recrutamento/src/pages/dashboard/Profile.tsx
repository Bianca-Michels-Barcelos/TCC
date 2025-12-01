import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { authService } from '@/services/auth.service';
import { candidatoService } from '@/services/candidato.service';
import type { ExperienciaProfissional, HistoricoAcademico, Competencia, Certificado, Portfolio } from '@/services/candidato.service';
import { User, Briefcase, GraduationCap, Award, FileText, Pencil, Trash2, Link as LinkIcon } from 'lucide-react';
import AddExperienceDialog from '@/components/profile/AddExperienceDialog';
import EditExperienceDialog from '@/components/profile/EditExperienceDialog';
import AddHistoricoDialog from '@/components/profile/AddHistoricoDialog';
import EditHistoricoDialog from '@/components/profile/EditHistoricoDialog';
import AddCompetenciaDialog from '@/components/profile/AddCompetenciaDialog';
import EditCompetenciaDialog from '@/components/profile/EditCompetenciaDialog';
import AddCertificadoDialog from '@/components/profile/AddCertificadoDialog';
import EditCertificadoDialog from '@/components/profile/EditCertificadoDialog';
import AddPortfolioDialog from '@/components/profile/AddPortfolioDialog';
import EditPortfolioDialog from '@/components/profile/EditPortfolioDialog';
import type { ExperienceData } from '@/components/profile/AddExperienceDialog';
import type { HistoricoData } from '@/components/profile/AddHistoricoDialog';
import type { CompetenciaData } from '@/components/profile/AddCompetenciaDialog';
import type { CertificadoData } from '@/components/profile/AddCertificadoDialog';
import type { PortfolioData } from '@/components/profile/AddPortfolioDialog';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

export default function Profile() {
  usePageTitle('Perfil');
  const navigate = useNavigate();
  const user = authService.getUser();
  const isCandidato = user?.roles?.includes('ROLE_CANDIDATO');
  const [experienceDialogOpen, setExperienceDialogOpen] = useState(false);
  const [historicoDialogOpen, setHistoricoDialogOpen] = useState(false);
  const [competenciaDialogOpen, setCompetenciaDialogOpen] = useState(false);
  const [editCompetenciaDialogOpen, setEditCompetenciaDialogOpen] = useState(false);
  const [editingCompetencia, setEditingCompetencia] = useState<Competencia | null>(null);
  const [certificadoDialogOpen, setCertificadoDialogOpen] = useState(false);
  const [editCertificadoDialogOpen, setEditCertificadoDialogOpen] = useState(false);
  const [editingCertificado, setEditingCertificado] = useState<Certificado | null>(null);
  const [portfolioDialogOpen, setPortfolioDialogOpen] = useState(false);
  const [editPortfolioDialogOpen, setEditPortfolioDialogOpen] = useState(false);
  const [editingPortfolio, setEditingPortfolio] = useState<Portfolio | null>(null);
  const [editExperienceDialogOpen, setEditExperienceDialogOpen] = useState(false);
  const [editingExperiencia, setEditingExperiencia] = useState<ExperienciaProfissional | null>(null);
  const [editHistoricoDialogOpen, setEditHistoricoDialogOpen] = useState(false);
  const [editingHistorico, setEditingHistorico] = useState<HistoricoAcademico | null>(null);

  const [experiencias, setExperiencias] = useState<ExperienciaProfissional[]>([]);
  const [historicos, setHistoricos] = useState<HistoricoAcademico[]>([]);
  const [competencias, setCompetencias] = useState<Competencia[]>([]);
  const [certificados, setCertificados] = useState<Certificado[]>([]);
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isCandidato) {
      toast.error('Acesso negado. Apenas candidatos podem acessar o perfil.');
      navigate('/dashboard');
    }
  }, [isCandidato, user]);

  useEffect(() => {
    loadProfileData();
  }, []);

  const loadProfileData = async () => {
    if (!user?.usuarioId) return;

    setLoading(true);
    try {
      const [expData, histData, compData, certData, portData] = await Promise.all([
        candidatoService.listarExperiencias(user.usuarioId),
        candidatoService.listarHistoricos(user.usuarioId),
        candidatoService.listarCompetencias(user.usuarioId),
        candidatoService.listarCertificados(user.usuarioId),
        candidatoService.listarPortfolios(user.usuarioId),
      ]);
      setExperiencias(expData);
      setHistoricos(histData);
      setCompetencias(compData);
      setCertificados(certData);
      setPortfolios(portData);
    } catch (error) {
      console.error('Error loading profile data:', error);
      toast.error('Erro ao carregar dados do perfil');
    } finally {
      setLoading(false);
    }
  };

  const handleAddExperience = async (data: ExperienceData) => {
    if (!user?.usuarioId) return;
    const result = await candidatoService.adicionarExperiencia(user.usuarioId, data);
    
    const newExp: ExperienciaProfissional = {
      id: result.experienciaId || crypto.randomUUID(),
      ...data,
    };
    candidatoService.saveExperienciaLocal(user.usuarioId, newExp);
    
    await loadProfileData();
  };

  const handleAddHistorico = async (data: HistoricoData) => {
    if (!user?.usuarioId) return;
    const result = await candidatoService.adicionarHistorico(user.usuarioId, data);
    
    const newHist: HistoricoAcademico = {
      id: result.historicoId || crypto.randomUUID(),
      ...data,
    };
    candidatoService.saveHistoricoLocal(user.usuarioId, newHist);
    
    await loadProfileData();
  };

  const handleAddCompetencia = async (data: CompetenciaData) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.adicionarCompetencia(user.usuarioId, data);
      toast.success('Competência adicionada com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error adding competencia:', error);
      toast.error('Erro ao adicionar competência');
      throw error;
    }
  };

  const handleEditCompetencia = async (id: string, data: { titulo: string; descricao: string; nivel: string }) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.atualizarCompetencia(user.usuarioId, id, {
        ...data,
        nivel: data.nivel as 'BASICO' | 'INTERMEDIARIO' | 'AVANCADO'
      });
      toast.success('Competência atualizada com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error updating competencia:', error);
      toast.error('Erro ao atualizar competência');
      throw error;
    }
  };

  const handleDeleteCompetencia = async (id: string) => {
    if (!user?.usuarioId) return;
    if (!confirm('Tem certeza que deseja remover esta competência?')) return;

    try {
      await candidatoService.removerCompetencia(user.usuarioId, id);
      toast.success('Competência removida com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error deleting competencia:', error);
      toast.error('Erro ao remover competência');
    }
  };

  const openEditCompetenciaDialog = (competencia: Competencia) => {
    setEditingCompetencia(competencia);
    setEditCompetenciaDialogOpen(true);
  };

  const getNivelBadgeColor = (nivel: string) => {
    switch (nivel) {
      case 'BASICO':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'INTERMEDIARIO':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'AVANCADO':
        return 'bg-green-100 text-green-800 border-green-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getNivelLabel = (nivel: string) => {
    switch (nivel) {
      case 'BASICO':
        return 'Básico';
      case 'INTERMEDIARIO':
        return 'Intermediário';
      case 'AVANCADO':
        return 'Avançado';
      default:
        return nivel;
    }
  };

  const handleAddCertificado = async (data: CertificadoData) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.adicionarCertificado(user.usuarioId, data);
      toast.success('Certificado adicionado com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error adding certificado:', error);
      toast.error('Erro ao adicionar certificado');
      throw error;
    }
  };

  const handleEditCertificado = async (id: string, data: { titulo: string; instituicao: string; dataEmissao: string; dataValidade?: string; descricao?: string }) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.atualizarCertificado(user.usuarioId, id, data);
      toast.success('Certificado atualizado com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error updating certificado:', error);
      toast.error('Erro ao atualizar certificado');
      throw error;
    }
  };

  const handleDeleteCertificado = async (id: string) => {
    if (!user?.usuarioId) return;
    if (!confirm('Tem certeza que deseja remover este certificado?')) return;

    try {
      await candidatoService.removerCertificado(user.usuarioId, id);
      toast.success('Certificado removido com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error deleting certificado:', error);
      toast.error('Erro ao remover certificado');
    }
  };

  const openEditCertificadoDialog = (certificado: Certificado) => {
    setEditingCertificado(certificado);
    setEditCertificadoDialogOpen(true);
  };

  const handleAddPortfolio = async (data: PortfolioData) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.adicionarPortfolio(user.usuarioId, data);
      toast.success('Portfólio adicionado com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error adding portfolio:', error);
      toast.error('Erro ao adicionar portfólio');
      throw error;
    }
  };

  const handleEditPortfolio = async (id: string, data: { titulo: string; link: string }) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.atualizarPortfolio(user.usuarioId, id, data);
      toast.success('Portfólio atualizado com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error updating portfolio:', error);
      toast.error('Erro ao atualizar portfólio');
      throw error;
    }
  };

  const handleDeletePortfolio = async (id: string) => {
    if (!user?.usuarioId) return;
    if (!confirm('Tem certeza que deseja remover este portfólio?')) return;

    try {
      await candidatoService.removerPortfolio(user.usuarioId, id);
      toast.success('Portfólio removido com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error deleting portfolio:', error);
      toast.error('Erro ao remover portfólio');
    }
  };

  const openEditPortfolioDialog = (portfolio: Portfolio) => {
    setEditingPortfolio(portfolio);
    setEditPortfolioDialogOpen(true);
  };

  const openEditExperienciaDialog = (experiencia: ExperienciaProfissional) => {
    setEditingExperiencia(experiencia);
    setEditExperienceDialogOpen(true);
  };

  const handleEditExperiencia = async (id: string, data: { empresa: string; cargo: string; dataInicio: string; dataFim?: string; descricao: string }) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.atualizarExperiencia(user.usuarioId, id, data);
      toast.success('Experiência atualizada com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error updating experiencia:', error);
      toast.error('Erro ao atualizar experiência');
      throw error;
    }
  };

  const openEditHistoricoDialog = (historico: HistoricoAcademico) => {
    setEditingHistorico(historico);
    setEditHistoricoDialogOpen(true);
  };

  const handleEditHistorico = async (id: string, data: { titulo: string; instituicao: string; dataInicio: string; dataFim?: string; descricao: string }) => {
    if (!user?.usuarioId) return;
    try {
      await candidatoService.atualizarHistorico(user.usuarioId, id, data);
      toast.success('Histórico atualizado com sucesso!');
      await loadProfileData();
    } catch (error) {
      console.error('Error updating historico:', error);
      toast.error('Erro ao atualizar histórico');
      throw error;
    }
  };

  const handleDeleteExperiencia = (id: string) => {
    if (!user?.usuarioId) return;
    const updated = experiencias.filter(exp => exp.id !== id);
    setExperiencias(updated);
    localStorage.setItem(`experiencias_${user.usuarioId}`, JSON.stringify(updated));
  };

  const handleDeleteHistorico = (id: string) => {
    if (!user?.usuarioId) return;
    const updated = historicos.filter(hist => hist.id !== id);
    setHistoricos(updated);
    localStorage.setItem(`historicos_${user.usuarioId}`, JSON.stringify(updated));
  };

  const formatDate = (date?: string) => {
    if (!date) return 'Presente';
    return new Date(date).toLocaleDateString('pt-BR', { month: 'short', year: 'numeric' });
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <h2 className="text-2xl sm:text-3xl font-bold">Meu Perfil</h2>
        <Button onClick={() => navigate('/dashboard')} className="w-full sm:w-auto">Voltar ao Dashboard</Button>
      </div>

      {/* User Info */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <User className="w-5 h-5" />
            Informações Pessoais
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div>
            <span className="font-medium">Nome:</span> {user?.nome}
          </div>
          <div>
            <span className="font-medium">Email:</span> {user?.email}
          </div>
        </CardContent>
      </Card>

      {/* Professional Experience */}
      <Card>
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-lg sm:text-xl">
            <Briefcase className="w-5 h-5" />
            Experiência Profissional
          </CardTitle>
          <Button variant="outline" size="sm" onClick={() => setExperienceDialogOpen(true)} className="w-full sm:w-auto">
            Adicionar
          </Button>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-muted-foreground">Carregando...</p>
          ) : experiencias.length === 0 ? (
            <p className="text-muted-foreground">
              Nenhuma experiência profissional cadastrada ainda.
            </p>
          ) : (
            <div className="space-y-3 sm:space-y-4">
              {experiencias.map((exp) => (
                <div key={exp.id} className="border rounded-lg p-3 sm:p-4 space-y-2">
                  <div className="flex flex-col sm:flex-row justify-between items-start gap-2">
                    <div className="flex-1 min-w-0 w-full">
                      <h4 className="font-semibold text-base sm:text-lg break-words">{exp.cargo}</h4>
                      <p className="text-sm sm:text-base text-muted-foreground truncate">{exp.empresa}</p>
                      <p className="text-xs sm:text-sm text-muted-foreground">
                        {formatDate(exp.dataInicio)} - {formatDate(exp.dataFim)}
                      </p>
                    </div>
                    <div className="flex gap-2 self-end sm:self-start">
                      <Button 
                        variant="ghost" 
                        size="icon" 
                        className="h-8 w-8"
                        onClick={() => openEditExperienciaDialog(exp)}
                      >
                        <Pencil className="w-3 h-3 sm:w-4 sm:h-4" />
                      </Button>
                      <Button 
                        variant="ghost" 
                        size="icon" 
                        className="h-8 w-8 text-destructive"
                        onClick={() => handleDeleteExperiencia(exp.id)}
                      >
                        <Trash2 className="w-3 h-3 sm:w-4 sm:h-4" />
                      </Button>
                    </div>
                  </div>
                  <p className="text-xs sm:text-sm">{exp.descricao}</p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Academic History */}
      <Card>
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-lg sm:text-xl">
            <GraduationCap className="w-5 h-5" />
            Histórico Acadêmico
          </CardTitle>
          <Button variant="outline" size="sm" onClick={() => setHistoricoDialogOpen(true)} className="w-full sm:w-auto">
            Adicionar
          </Button>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-muted-foreground">Carregando...</p>
          ) : historicos.length === 0 ? (
            <p className="text-muted-foreground">
              Nenhum histórico acadêmico cadastrado ainda.
            </p>
          ) : (
            <div className="space-y-4">
              {historicos.map((hist) => (
                <div key={hist.id} className="border rounded-lg p-4 space-y-2">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <h4 className="font-semibold text-lg">{hist.titulo}</h4>
                      <p className="text-muted-foreground">{hist.instituicao}</p>
                      <p className="text-sm text-muted-foreground">
                        {formatDate(hist.dataInicio)} - {formatDate(hist.dataFim)}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <Button 
                        variant="ghost" 
                        size="icon" 
                        className="h-8 w-8"
                        onClick={() => openEditHistoricoDialog(hist)}
                      >
                        <Pencil className="w-4 h-4" />
                      </Button>
                      <Button 
                        variant="ghost" 
                        size="icon" 
                        className="h-8 w-8 text-destructive"
                        onClick={() => handleDeleteHistorico(hist.id)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  <p className="text-sm">{hist.descricao}</p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Skills/Competências */}
      <Card>
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-lg sm:text-xl">
            <Award className="w-5 h-5" />
            Competências
          </CardTitle>
          <Button variant="outline" size="sm" onClick={() => setCompetenciaDialogOpen(true)} className="w-full sm:w-auto">
            Adicionar
          </Button>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-muted-foreground">Carregando...</p>
          ) : competencias.length === 0 ? (
            <p className="text-muted-foreground">
              Nenhuma competência cadastrada ainda.
            </p>
          ) : (
            <div className="space-y-4">
              {competencias.map((comp) => (
                <div key={comp.id} className="border rounded-lg p-4 space-y-2">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <h4 className="font-semibold text-lg">{comp.titulo}</h4>
                        <span className={`px-2 py-1 text-xs font-medium rounded-md border ${getNivelBadgeColor(comp.nivel)}`}>
                          {getNivelLabel(comp.nivel)}
                        </span>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => openEditCompetenciaDialog(comp)}
                      >
                        <Pencil className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-destructive"
                        onClick={() => handleDeleteCompetencia(comp.id)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  <p className="text-sm">{comp.descricao}</p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Certificates */}
      <Card>
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-lg sm:text-xl">
            <FileText className="w-5 h-5" />
            Certificados
          </CardTitle>
          <Button variant="outline" size="sm" onClick={() => setCertificadoDialogOpen(true)} className="w-full sm:w-auto">
            Adicionar
          </Button>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-muted-foreground">Carregando...</p>
          ) : certificados.length === 0 ? (
            <p className="text-muted-foreground">
              Nenhum certificado cadastrado ainda.
            </p>
          ) : (
            <div className="space-y-4">
              {certificados.map((cert) => (
                <div key={cert.id} className="border rounded-lg p-4 space-y-2">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <h4 className="font-semibold text-lg">{cert.titulo}</h4>
                      <p className="text-muted-foreground">{cert.instituicao}</p>
                      <p className="text-sm text-muted-foreground">
                        Emitido em: {new Date(cert.dataEmissao).toLocaleDateString('pt-BR')}
                        {cert.dataValidade && ` • Válido até: ${new Date(cert.dataValidade).toLocaleDateString('pt-BR')}`}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => openEditCertificadoDialog(cert)}
                      >
                        <Pencil className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-destructive"
                        onClick={() => handleDeleteCertificado(cert.id)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  {cert.descricao && <p className="text-sm">{cert.descricao}</p>}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Portfolio */}
      <Card>
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-lg sm:text-xl">
            <LinkIcon className="w-5 h-5" />
            Portfólio
          </CardTitle>
          <Button variant="outline" size="sm" onClick={() => setPortfolioDialogOpen(true)} className="w-full sm:w-auto">
            Adicionar
          </Button>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-muted-foreground">Carregando...</p>
          ) : portfolios.length === 0 ? (
            <p className="text-muted-foreground">
              Nenhum portfólio cadastrado ainda.
            </p>
          ) : (
            <div className="space-y-4">
              {portfolios.map((portfolio) => (
                <div key={portfolio.id} className="border rounded-lg p-4 space-y-2">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <h4 className="font-semibold text-lg">{portfolio.titulo}</h4>
                      <a 
                        href={portfolio.link} 
                        target="_blank" 
                        rel="noopener noreferrer"
                        className="text-sm text-blue-600 hover:text-blue-800 hover:underline flex items-center gap-1"
                      >
                        <LinkIcon className="w-3 h-3" />
                        {portfolio.link}
                      </a>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => openEditPortfolioDialog(portfolio)}
                      >
                        <Pencil className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-destructive"
                        onClick={() => handleDeletePortfolio(portfolio.id)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialogs */}
      <AddExperienceDialog
        open={experienceDialogOpen}
        onOpenChange={setExperienceDialogOpen}
        onSubmit={handleAddExperience}
      />
      <AddHistoricoDialog
        open={historicoDialogOpen}
        onOpenChange={setHistoricoDialogOpen}
        onSubmit={handleAddHistorico}
      />
      <AddCompetenciaDialog
        open={competenciaDialogOpen}
        onOpenChange={setCompetenciaDialogOpen}
        onSubmit={handleAddCompetencia}
      />
      <EditCompetenciaDialog
        open={editCompetenciaDialogOpen}
        onOpenChange={setEditCompetenciaDialogOpen}
        competencia={editingCompetencia}
        onSubmit={handleEditCompetencia}
      />
      <AddCertificadoDialog
        open={certificadoDialogOpen}
        onOpenChange={setCertificadoDialogOpen}
        onSubmit={handleAddCertificado}
      />
      <EditCertificadoDialog
        open={editCertificadoDialogOpen}
        onOpenChange={setEditCertificadoDialogOpen}
        certificado={editingCertificado}
        onSubmit={handleEditCertificado}
      />
      <AddPortfolioDialog
        open={portfolioDialogOpen}
        onOpenChange={setPortfolioDialogOpen}
        onSubmit={handleAddPortfolio}
      />
      <EditPortfolioDialog
        open={editPortfolioDialogOpen}
        onOpenChange={setEditPortfolioDialogOpen}
        portfolio={editingPortfolio}
        onSubmit={handleEditPortfolio}
      />
      <EditExperienceDialog
        open={editExperienceDialogOpen}
        onOpenChange={setEditExperienceDialogOpen}
        experiencia={editingExperiencia}
        onSubmit={handleEditExperiencia}
      />
      <EditHistoricoDialog
        open={editHistoricoDialogOpen}
        onOpenChange={setEditHistoricoDialogOpen}
        historico={editingHistorico}
        onSubmit={handleEditHistorico}
      />
    </div>
  );
}
