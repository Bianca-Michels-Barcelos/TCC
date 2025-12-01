import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'sonner';
import Login from './pages/Login';
import Register from './pages/Register';
import RegisterOrganizacao from './pages/RegisterOrganizacao';
import ProfileSetup from './pages/ProfileSetup';
import AceitarConvite from './pages/AceitarConvite';
import EsqueciSenha from './pages/EsqueciSenha';
import RedefinirSenha from './pages/RedefinirSenha';
import DashboardLayout from './components/layout/DashboardLayout';
import Home from './pages/dashboard/Home';
import BuscarVagas from './pages/dashboard/BuscarVagas';
import BuscarCandidatos from './pages/dashboard/BuscarCandidatos';
import MinhasVagas from './pages/dashboard/MinhasVagas';
import GerenciarVagasComplete from './pages/dashboard/GerenciarVagasComplete';
import VagasExternas from './pages/dashboard/VagasExternas';
import CadastrarVagaExterna from './pages/dashboard/CadastrarVagaExterna';
import EditarCurriculoExterno from './pages/dashboard/EditarCurriculoExterno';
import MinhasCandidaturas from './pages/dashboard/MinhasCandidaturas';
import Profile from './pages/dashboard/Profile';
import GerenciarUsuarios from './pages/dashboard/GerenciarUsuarios';
import GerenciarBeneficios from './pages/dashboard/GerenciarBeneficios';
import GerenciarNiveisExperiencia from './pages/dashboard/GerenciarNiveisExperiencia';
import GerenciarEtapasProcesso from './pages/dashboard/GerenciarEtapasProcesso';
import VagasSalvas from './pages/dashboard/VagasSalvas';
import VagaDetalhes from './pages/dashboard/VagaDetalhes';
import OrganizacaoDetalhes from './pages/dashboard/OrganizacaoDetalhes';
import GerarCV from './pages/dashboard/GerarCV';
import AvaliarOrganizacao from './pages/dashboard/AvaliarOrganizacao';
import MeusFeedbacks from './pages/dashboard/MeusFeedbacks';
import ProcessoSeletivo from './pages/dashboard/ProcessoSeletivo';
import ProcessosSeletivos from './pages/dashboard/ProcessosSeletivos';
import GerenciarProcesso from './pages/dashboard/GerenciarProcesso';
import { authService } from './services/auth.service';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  return authService.isAuthenticated() ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Register />} />
          <Route path="/cadastro/perfil" element={<ProfileSetup />} />
          <Route path="/cadastro/organizacao" element={<RegisterOrganizacao />} />
          <Route path="/auth/convite/:token" element={<AceitarConvite />} />
          <Route path="/esqueci-senha" element={<EsqueciSenha />} />
          <Route path="/redefinir-senha" element={<RedefinirSenha />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Home />} />
            <Route path="buscar-vagas" element={<BuscarVagas />} />
            <Route path="buscar-candidatos" element={<BuscarCandidatos />} />
            <Route path="processos-seletivos" element={<ProcessosSeletivos />} />
            <Route path="gerenciar-processo/:processoId" element={<GerenciarProcesso />} />
            <Route path="minhas-vagas" element={<MinhasVagas />} />
            <Route path="nova-vaga" element={<GerenciarVagasComplete />} />
            <Route path="editar-vaga/:id" element={<GerenciarVagasComplete />} />
            <Route path="vagas-externas" element={<VagasExternas />} />
            <Route path="vagas-externas/cadastrar" element={<CadastrarVagaExterna />} />
            <Route path="vagas-externas/:id" element={<CadastrarVagaExterna />} />
            <Route path="vagas-externas/:id/editar-curriculo" element={<EditarCurriculoExterno />} />
            <Route path="minhas-candidaturas" element={<MinhasCandidaturas />} />
            <Route path="perfil" element={<Profile />} />
            <Route path="gerenciar-usuarios" element={<GerenciarUsuarios />} />

            {/* New routes */}
            <Route path="gerenciar-beneficios" element={<GerenciarBeneficios />} />
            <Route path="gerenciar-niveis-experiencia" element={<GerenciarNiveisExperiencia />} />
            <Route path="gerenciar-etapas" element={<GerenciarEtapasProcesso />} />
            <Route path="vagas-salvas" element={<VagasSalvas />} />
            <Route path="vagas/:id" element={<VagaDetalhes />} />
            <Route path="organizacao/:id" element={<OrganizacaoDetalhes />} />
            <Route path="gerar-cv" element={<GerarCV />} />
            <Route path="avaliar-organizacao/:processoId" element={<AvaliarOrganizacao />} />
            <Route path="meus-feedbacks" element={<MeusFeedbacks />} />
            <Route path="processo-seletivo/:id" element={<ProcessoSeletivo />} />
          </Route>
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </BrowserRouter>
      <Toaster position="top-right" richColors />
    </>
  );
}

export default App;
