import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from '../App';
import { authService } from '@/services/auth.service';

const mockLocation = { pathname: '/login' };
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    BrowserRouter: ({ children }: { children: React.ReactNode }) => {
      const { MemoryRouter } = actual as any;
      return <MemoryRouter initialEntries={[mockLocation.pathname]}>{children}</MemoryRouter>;
    },
  };
});

vi.mock('@/services/auth.service', () => ({
  authService: {
    isAuthenticated: vi.fn(),
  },
}));

vi.mock('../pages/Login', () => ({
  default: () => <div>Login Page</div>,
}));

vi.mock('../pages/Register', () => ({
  default: () => <div>Register Page</div>,
}));

vi.mock('../pages/RegisterOrganizacao', () => ({
  default: () => <div>Register Organization Page</div>,
}));

vi.mock('../pages/ProfileSetup', () => ({
  default: () => <div>Profile Setup Page</div>,
}));

vi.mock('../pages/EsqueciSenha', () => ({
  default: () => <div>Forgot Password Page</div>,
}));

vi.mock('../pages/RedefinirSenha', () => ({
  default: () => <div>Reset Password Page</div>,
}));

vi.mock('../pages/AceitarConvite', () => ({
  default: () => <div>Accept Invite Page</div>,
}));

vi.mock('../components/layout/DashboardLayout', () => ({
  default: () => <div>Dashboard Layout</div>,
}));

vi.mock('../pages/dashboard/Home', () => ({
  default: () => <div>Home Page</div>,
}));

vi.mock('../pages/dashboard/BuscarVagas', () => ({
  default: () => <div>Buscar Vagas Page</div>,
}));

vi.mock('../pages/dashboard/BuscarCandidatos', () => ({
  default: () => <div>Buscar Candidatos Page</div>,
}));

vi.mock('../pages/dashboard/MinhasVagas', () => ({
  default: () => <div>Minhas Vagas Page</div>,
}));

vi.mock('../pages/dashboard/GerenciarVagasComplete', () => ({
  default: () => <div>Gerenciar Vagas Page</div>,
}));

vi.mock('../pages/dashboard/VagasExternas', () => ({
  default: () => <div>Vagas Externas Page</div>,
}));

vi.mock('../pages/dashboard/CadastrarVagaExterna', () => ({
  default: () => <div>Cadastrar Vaga Externa Page</div>,
}));

vi.mock('../pages/dashboard/EditarCurriculoExterno', () => ({
  default: () => <div>Editar Curriculo Externo Page</div>,
}));

vi.mock('../pages/dashboard/MinhasCandidaturas', () => ({
  default: () => <div>Minhas Candidaturas Page</div>,
}));

vi.mock('../pages/dashboard/Profile', () => ({
  default: () => <div>Profile Page</div>,
}));

vi.mock('../pages/dashboard/GerenciarUsuarios', () => ({
  default: () => <div>Gerenciar Usuarios Page</div>,
}));

vi.mock('../pages/dashboard/GerenciarBeneficios', () => ({
  default: () => <div>Gerenciar Beneficios Page</div>,
}));

vi.mock('../pages/dashboard/GerenciarNiveisExperiencia', () => ({
  default: () => <div>Gerenciar Niveis Experiencia Page</div>,
}));

vi.mock('../pages/dashboard/GerenciarEtapasProcesso', () => ({
  default: () => <div>Gerenciar Etapas Page</div>,
}));

vi.mock('../pages/dashboard/VagasSalvas', () => ({
  default: () => <div>Vagas Salvas Page</div>,
}));

vi.mock('../pages/dashboard/VagaDetalhes', () => ({
  default: () => <div>Vaga Detalhes Page</div>,
}));

vi.mock('../pages/dashboard/OrganizacaoDetalhes', () => ({
  default: () => <div>Organizacao Detalhes Page</div>,
}));

vi.mock('../pages/dashboard/GerarCV', () => ({
  default: () => <div>Gerar CV Page</div>,
}));

vi.mock('../pages/dashboard/AvaliarOrganizacao', () => ({
  default: () => <div>Avaliar Organizacao Page</div>,
}));

vi.mock('../pages/dashboard/MeusFeedbacks', () => ({
  default: () => <div>Meus Feedbacks Page</div>,
}));

vi.mock('../pages/dashboard/ProcessoSeletivo', () => ({
  default: () => <div>Processo Seletivo Page</div>,
}));

vi.mock('../pages/dashboard/ProcessosSeletivos', () => ({
  default: () => <div>Processos Seletivos Page</div>,
}));

vi.mock('../pages/dashboard/GerenciarProcesso', () => ({
  default: () => <div>Gerenciar Processo Page</div>,
}));

describe('Roteamento da Aplicação', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rotas Públicas', () => {
    it('deve renderizar página de login em /login', () => {
      mockLocation.pathname = '/login';
      render(<App />);

      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });

    it('deve renderizar página de cadastro em /cadastro', () => {
      mockLocation.pathname = '/cadastro';
      render(<App />);

      expect(screen.getByText('Register Page')).toBeInTheDocument();
    });

    it('deve renderizar página de cadastro de organização em /cadastro/organizacao', () => {
      mockLocation.pathname = '/cadastro/organizacao';
      render(<App />);

      expect(screen.getByText('Register Organization Page')).toBeInTheDocument();
    });

    it('deve renderizar página de configuração de perfil em /cadastro/perfil', () => {
      mockLocation.pathname = '/cadastro/perfil';
      render(<App />);

      expect(screen.getByText('Profile Setup Page')).toBeInTheDocument();
    });

    it('deve renderizar página de esqueci senha em /esqueci-senha', () => {
      mockLocation.pathname = '/esqueci-senha';
      render(<App />);

      expect(screen.getByText('Forgot Password Page')).toBeInTheDocument();
    });

    it('deve renderizar página de redefinir senha em /redefinir-senha', () => {
      mockLocation.pathname = '/redefinir-senha';
      render(<App />);

      expect(screen.getByText('Reset Password Page')).toBeInTheDocument();
    });

    it('deve renderizar página de aceitar convite em /auth/convite/:token', () => {
      mockLocation.pathname = '/auth/convite/test-token';
      render(<App />);

      expect(screen.getByText('Accept Invite Page')).toBeInTheDocument();
    });
  });

  describe('Rotas Protegidas', () => {
    it('deve renderizar dashboard quando autenticado', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(true);
      mockLocation.pathname = '/dashboard';

      render(<App />);

      expect(screen.getByText('Dashboard Layout')).toBeInTheDocument();
    });

    it('deve redirecionar para login quando não autenticado e acessando dashboard', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(false);
      mockLocation.pathname = '/dashboard';

      render(<App />);

      expect(screen.getByText('Login Page')).toBeInTheDocument();
      expect(screen.queryByText('Dashboard Layout')).not.toBeInTheDocument();
    });

    it('deve redirecionar para login quando não autenticado e acessando perfil', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(false);
      mockLocation.pathname = '/dashboard/perfil';

      render(<App />);

      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });

    it('deve redirecionar para login quando não autenticado e acessando qualquer rota protegida', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(false);
      mockLocation.pathname = '/dashboard/buscar-vagas';

      render(<App />);

      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
  });

  describe('Redirecionamento Raiz', () => {
    it('deve redirecionar caminho raiz para /dashboard', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(true);
      mockLocation.pathname = '/';

      render(<App />);

      expect(screen.getByText('Dashboard Layout')).toBeInTheDocument();
    });

    it('deve redirecionar raiz para dashboard, então para login se não autenticado', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(false);
      mockLocation.pathname = '/';

      render(<App />);

      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
  });

  describe('Componente ProtectedRoute', () => {
    it('deve renderizar children quando autenticado', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(true);
      mockLocation.pathname = '/dashboard';

      render(<App />);

      expect(authService.isAuthenticated).toHaveBeenCalled();
      expect(screen.getByText('Dashboard Layout')).toBeInTheDocument();
    });

    it('não deve renderizar children quando não autenticado', () => {
      vi.mocked(authService.isAuthenticated).mockReturnValue(false);
      mockLocation.pathname = '/dashboard';

      render(<App />);

      expect(authService.isAuthenticated).toHaveBeenCalled();
      expect(screen.queryByText('Dashboard Layout')).not.toBeInTheDocument();
    });
  });
});
