import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  decodeToken,
  getOrganizacaoIdFromToken,
  getOrganizacaoNomeFromToken,
  getRolesFromToken,
  getEmailFromToken,
  getUsuarioIdFromToken,
  getNomeFromToken,
  getUserFromToken,
} from '../jwt';

vi.mock('jwt-decode', () => ({
  jwtDecode: vi.fn(),
}));

import { jwtDecode } from 'jwt-decode';

describe('Utilitários JWT', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('decodeToken', () => {
    it('deve decodificar um token válido', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
        usuarioId: '123',
        nome: 'Test User',
        iat: 1234567890,
        exp: 9999999999,
      };

      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = decodeToken('valid.token.here');
      expect(result).toEqual(mockPayload);
      expect(jwtDecode).toHaveBeenCalledWith('valid.token.here');
    });

    it('deve retornar null para token inválido', () => {
      vi.mocked(jwtDecode).mockImplementation(() => {
        throw new Error('Invalid token');
      });

      const result = decodeToken('invalid.token');
      expect(result).toBeNull();
    });
  });

  describe('getOrganizacaoIdFromToken', () => {
    it('deve retornar organizacaoId do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_RECRUTADOR'],
        organizacaoId: 'org-123',
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getOrganizacaoIdFromToken();
      expect(result).toBe('org-123');
    });

    it('deve retornar null quando não existe token', () => {
      const result = getOrganizacaoIdFromToken();
      expect(result).toBeNull();
    });

    it('deve retornar null quando organizacaoId está ausente do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getOrganizacaoIdFromToken();
      expect(result).toBeNull();
    });
  });

  describe('getOrganizacaoNomeFromToken', () => {
    it('deve retornar organizacaoNome do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_RECRUTADOR'],
        organizacaoNome: 'Test Organization',
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getOrganizacaoNomeFromToken();
      expect(result).toBe('Test Organization');
    });

    it('deve retornar null quando não existe token', () => {
      const result = getOrganizacaoNomeFromToken();
      expect(result).toBeNull();
    });
  });

  describe('getRolesFromToken', () => {
    it('deve retornar array de roles do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO', 'ROLE_RECRUTADOR'],
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getRolesFromToken();
      expect(result).toEqual(['ROLE_CANDIDATO', 'ROLE_RECRUTADOR']);
    });

    it('deve retornar array vazio quando não existe token', () => {
      const result = getRolesFromToken();
      expect(result).toEqual([]);
    });

    it('deve retornar array vazio quando roles estão ausentes do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getRolesFromToken();
      expect(result).toEqual([]);
    });
  });

  describe('getEmailFromToken', () => {
    it('deve retornar email do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getEmailFromToken();
      expect(result).toBe('test@example.com');
    });

    it('deve retornar null quando não existe token', () => {
      const result = getEmailFromToken();
      expect(result).toBeNull();
    });
  });

  describe('getUsuarioIdFromToken', () => {
    it('deve retornar usuarioId do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
        usuarioId: '456',
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getUsuarioIdFromToken();
      expect(result).toBe('456');
    });

    it('deve retornar null quando não existe token', () => {
      const result = getUsuarioIdFromToken();
      expect(result).toBeNull();
    });
  });

  describe('getNomeFromToken', () => {
    it('deve retornar nome do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
        nome: 'Test User',
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getNomeFromToken();
      expect(result).toBe('Test User');
    });

    it('deve retornar null quando não existe token', () => {
      const result = getNomeFromToken();
      expect(result).toBeNull();
    });
  });

  describe('getUserFromToken', () => {
    it('deve retornar objeto completo do usuário do token', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
        usuarioId: '789',
        nome: 'Test User',
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getUserFromToken();
      expect(result).toEqual({
        usuarioId: '789',
        nome: 'Test User',
        email: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
      });
    });

    it('deve retornar null quando não existe token', () => {
      const result = getUserFromToken();
      expect(result).toBeNull();
    });

    it('deve retornar null quando decodificação do token falha', () => {
      localStorage.setItem('accessToken', 'invalid.token');
      vi.mocked(jwtDecode).mockImplementation(() => {
        throw new Error('Invalid token');
      });

      const result = getUserFromToken();
      expect(result).toBeNull();
    });

    it('deve lidar com campos opcionais ausentes', () => {
      const mockPayload = {
        sub: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
        iat: 1234567890,
        exp: 9999999999,
      };

      localStorage.setItem('accessToken', 'valid.token.here');
      vi.mocked(jwtDecode).mockReturnValue(mockPayload);

      const result = getUserFromToken();
      expect(result).toEqual({
        usuarioId: null,
        nome: null,
        email: 'test@example.com',
        roles: ['ROLE_CANDIDATO'],
      });
    });
  });
});

