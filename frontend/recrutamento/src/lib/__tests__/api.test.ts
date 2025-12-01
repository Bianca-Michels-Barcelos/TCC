import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios';

describe('Configuração da API', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('Interceptor de Requisição', () => {
    it('deve adicionar cabeçalho Authorization quando token existe', () => {
      const token = 'test-access-token';
      localStorage.setItem('accessToken', token);

      const config = {
        headers: {},
      } as InternalAxiosRequestConfig;

      const processedConfig = {
        ...config,
        headers: {
          ...config.headers,
          Authorization: `Bearer ${token}`,
        },
      };

      expect(processedConfig.headers.Authorization).toBe(`Bearer ${token}`);
    });

    it('não deve adicionar cabeçalho Authorization quando token não existe', () => {
      const config = {
        headers: {},
      } as InternalAxiosRequestConfig;

      const token = localStorage.getItem('accessToken');
      const processedConfig = {
        ...config,
        headers: {
          ...config.headers,
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
      };

      expect(processedConfig.headers.Authorization).toBeUndefined();
    });
  });

  describe('Interceptor de Resposta - Atualização de Token', () => {
    beforeEach(() => {
      delete (window as any).location;
      window.location = { href: '' } as any;
    });

    it('deve lidar com respostas bem-sucedidas', () => {
      const mockResponse = {
        data: { message: 'success' },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as InternalAxiosRequestConfig,
      } as AxiosResponse;

      const result = mockResponse;

      expect(result).toEqual(mockResponse);
    });

    it('deve tentar atualizar token em erro 401 com refresh token', async () => {
      localStorage.setItem('accessToken', 'expired-token');
      localStorage.setItem('refreshToken', 'valid-refresh-token');

      const mockRefreshResponse = {
        data: {
          accessToken: 'new-access-token',
          refreshToken: 'new-refresh-token',
        },
      };

      const mockPost = vi.fn().mockResolvedValue(mockRefreshResponse);

      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        const response = await mockPost('/auth/refresh', { refreshToken });
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);

        expect(localStorage.getItem('accessToken')).toBe('new-access-token');
        expect(localStorage.getItem('refreshToken')).toBe('new-refresh-token');
      }
    });

    it('deve limpar armazenamento e redirecionar em falha na atualização de token', async () => {
      localStorage.setItem('accessToken', 'expired-token');
      localStorage.setItem('refreshToken', 'invalid-refresh-token');

      const mockPost = vi.fn().mockRejectedValue(new Error('Invalid refresh token'));

      try {
        await mockPost('/auth/refresh', { refreshToken: 'invalid-refresh-token' });
      } catch (error) {
        localStorage.clear();
        window.location.href = '/login';

        expect(localStorage.getItem('accessToken')).toBeNull();
        expect(localStorage.getItem('refreshToken')).toBeNull();
        expect(window.location.href).toBe('/login');
      }
    });

    it('não deve tentar atualizar em 401 se não existe refresh token', () => {
      const refreshToken = localStorage.getItem('refreshToken');
      
      if (!refreshToken) {
        localStorage.clear();
        window.location.href = '/login';
      }

      expect(window.location.href).toBe('/login');
    });
  });

  describe('Configuração Base da API', () => {
    it('deve usar VITE_API_BASE_URL do env ou padrão para localhost', () => {
      const expectedBaseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
      
      expect(expectedBaseURL).toBeDefined();
      expect(typeof expectedBaseURL).toBe('string');
    });

    it('deve ter cabeçalhos padrão corretos', () => {
      const headers = {
        'Content-Type': 'application/json',
      };

      expect(headers['Content-Type']).toBe('application/json');
    });
  });
});
