import { describe, it, expect, beforeEach, vi } from 'vitest';
import { authService } from '../auth.service';
import api from '@/lib/api';
import * as jwt from '@/lib/jwt';

vi.mock('@/lib/api', () => ({
  default: {
    post: vi.fn(),
  },
}));

vi.mock('@/lib/jwt', () => ({
  getUserFromToken: vi.fn(),
}));

describe('Serviço de Autenticação', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    delete (window as any).location;
    window.location = { href: '' } as any;
  });

  describe('login', () => {
    it('deve fazer login com sucesso e armazenar tokens', async () => {
      const mockResponse = {
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          tokenType: 'Bearer',
          usuarioId: '123',
          nome: 'Test User',
          email: 'test@example.com',
          roles: ['ROLE_CANDIDATO'],
        },
      };

      vi.mocked(api.post).mockResolvedValue(mockResponse);

      const credentials = {
        email: 'test@example.com',
        senha: 'password123',
      };

      const result = await authService.login(credentials);

      expect(api.post).toHaveBeenCalledWith('/auth/login', credentials);
      expect(localStorage.getItem('accessToken')).toBe('mock-access-token');
      expect(localStorage.getItem('refreshToken')).toBe('mock-refresh-token');
      expect(result).toEqual(mockResponse.data);
    });

    it('deve lançar erro em login falhado', async () => {
      const mockError = new Error('Invalid credentials');
      vi.mocked(api.post).mockRejectedValue(mockError);

      const credentials = {
        email: 'test@example.com',
        senha: 'wrongpassword',
      };

      await expect(authService.login(credentials)).rejects.toThrow('Invalid credentials');
      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
    });
  });

  describe('logout', () => {
    it('deve limpar localStorage e redirecionar para login', () => {
      localStorage.setItem('accessToken', 'token');
      localStorage.setItem('refreshToken', 'refresh');
      localStorage.setItem('someOtherData', 'data');

      authService.logout();

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('someOtherData')).toBeNull();
      expect(window.location.href).toBe('/login');
    });
  });

  describe('isAuthenticated', () => {
    it('deve retornar true quando accessToken existe', () => {
      localStorage.setItem('accessToken', 'some-token');

      const result = authService.isAuthenticated();

      expect(result).toBe(true);
    });

    it('deve retornar false quando accessToken não existe', () => {
      const result = authService.isAuthenticated();

      expect(result).toBe(false);
    });

    it('deve retornar false quando accessToken está vazio', () => {
      localStorage.setItem('accessToken', '');

      const result = authService.isAuthenticated();

      expect(result).toBe(false);
    });
  });

  describe('getUser', () => {
    it('deve retornar usuário do token', () => {
      const mockUser = {
        usuarioId: '123',
        nome: 'Test User',
        email: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
      };

      vi.mocked(jwt.getUserFromToken).mockReturnValue(mockUser);

      const result = authService.getUser();

      expect(result).toEqual(mockUser);
      expect(jwt.getUserFromToken).toHaveBeenCalled();
    });

    it('deve retornar null quando não existe token', () => {
      vi.mocked(jwt.getUserFromToken).mockReturnValue(null);

      const result = authService.getUser();

      expect(result).toBeNull();
    });
  });
});

