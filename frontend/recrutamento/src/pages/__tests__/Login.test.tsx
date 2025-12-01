import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Login from '../Login';
import { authService } from '@/services/auth.service';

vi.mock('@/services/auth.service', () => ({
  authService: {
    login: vi.fn(),
  },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('@/hooks/usePageTitle', () => ({
  usePageTitle: vi.fn(),
}));

describe('Componente Login', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderLogin = () => {
    return render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    );
  };

  it('deve renderizar formulário de login', () => {
    renderLogin();

    expect(screen.getByText('Acesse sua conta')).toBeInTheDocument();
    expect(screen.getByLabelText('Usuário')).toBeInTheDocument();
    expect(screen.getByLabelText('Senha')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument();
  });

  it('deve renderizar links de navegação', () => {
    renderLogin();

    expect(screen.getByText('Não possui uma conta?')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cadastre-se' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Esqueceu sua senha?' })).toBeInTheDocument();
  });

  it('deve atualizar valores dos inputs ao digitar', async () => {
    const user = userEvent.setup();
    renderLogin();

    const emailInput = screen.getByLabelText('Usuário') as HTMLInputElement;
    const senhaInput = screen.getByLabelText('Senha') as HTMLInputElement;

    await user.type(emailInput, 'test@example.com');
    await user.type(senhaInput, 'password123');

    expect(emailInput.value).toBe('test@example.com');
    expect(senhaInput.value).toBe('password123');
  });

  it('deve chamar authService.login e navegar em login bem-sucedido', async () => {
    const user = userEvent.setup();
    vi.mocked(authService.login).mockResolvedValue({
      accessToken: 'token',
      refreshToken: 'refresh',
      tokenType: 'Bearer',
      usuarioId: '123',
      nome: 'Test User',
      email: 'test@example.com',
      roles: ['ROLE_CANDIDATO'],
    });

    renderLogin();

    const emailInput = screen.getByLabelText('Usuário');
    const senhaInput = screen.getByLabelText('Senha');
    const submitButton = screen.getByRole('button', { name: 'Entrar' });

    await user.type(emailInput, 'test@example.com');
    await user.type(senhaInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        senha: 'password123',
      });
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('deve exibir mensagem de erro em login falhado', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Invalid credentials';
    
    vi.mocked(authService.login).mockRejectedValue({
      response: {
        data: {
          message: errorMessage,
        },
      },
    });

    renderLogin();

    const emailInput = screen.getByLabelText('Usuário');
    const senhaInput = screen.getByLabelText('Senha');
    const submitButton = screen.getByRole('button', { name: 'Entrar' });

    await user.type(emailInput, 'test@example.com');
    await user.type(senhaInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('deve exibir mensagem de erro padrão quando nenhum erro específico é fornecido', async () => {
    const user = userEvent.setup();
    
    vi.mocked(authService.login).mockRejectedValue(new Error('Network error'));

    renderLogin();

    const emailInput = screen.getByLabelText('Usuário');
    const senhaInput = screen.getByLabelText('Senha');
    const submitButton = screen.getByRole('button', { name: 'Entrar' });

    await user.type(emailInput, 'test@example.com');
    await user.type(senhaInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Falha ao fazer login. Verifique suas credenciais.')).toBeInTheDocument();
    });
  });

  it('deve desabilitar inputs e mostrar estado de carregamento durante login', async () => {
    const user = userEvent.setup();
    
    vi.mocked(authService.login).mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 100))
    );

    renderLogin();

    const emailInput = screen.getByLabelText('Usuário');
    const senhaInput = screen.getByLabelText('Senha');
    const submitButton = screen.getByRole('button', { name: 'Entrar' });

    await user.type(emailInput, 'test@example.com');
    await user.type(senhaInput, 'password123');
    await user.click(submitButton);

    expect(screen.getByRole('button', { name: 'Entrando...' })).toBeInTheDocument();
    expect(emailInput).toBeDisabled();
    expect(senhaInput).toBeDisabled();
    expect(submitButton).toBeDisabled();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument();
    });
  });

  it('deve navegar para página de cadastro ao clicar em Cadastre-se', async () => {
    const user = userEvent.setup();
    renderLogin();

    const registerButton = screen.getByRole('button', { name: 'Cadastre-se' });
    await user.click(registerButton);

    expect(mockNavigate).toHaveBeenCalledWith('/cadastro');
  });

  it('deve navegar para página de esqueci senha ao clicar em Esqueceu sua senha', async () => {
    const user = userEvent.setup();
    renderLogin();

    const forgotPasswordButton = screen.getByRole('button', { name: 'Esqueceu sua senha?' });
    await user.click(forgotPasswordButton);

    expect(mockNavigate).toHaveBeenCalledWith('/esqueci-senha');
  });
});
