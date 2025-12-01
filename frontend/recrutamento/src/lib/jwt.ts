import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  sub: string;
  roles: string[];
  organizacaoId?: string;
  organizacaoNome?: string;
  usuarioId?: string;
  nome?: string;
  iat: number;
  exp: number;
}

export interface UserFromToken {
  usuarioId: string | null;
  nome: string | null;
  email: string | null;
  roles: string[];
}

export function decodeToken(token: string): JwtPayload | null {
  try {
    return jwtDecode<JwtPayload>(token);
  } catch (error) {
    console.error('Error decoding token:', error);
    return null;
  }
}

export function getOrganizacaoIdFromToken(): string | null {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;

  const decoded = decodeToken(token);
  return decoded?.organizacaoId || null;
}

export function getOrganizacaoNomeFromToken(): string | null {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;

  const decoded = decodeToken(token);
  return decoded?.organizacaoNome || null;
}

export function getRolesFromToken(): string[] {
  const token = localStorage.getItem('accessToken');
  if (!token) return [];

  const decoded = decodeToken(token);
  return decoded?.roles || [];
}

export function getEmailFromToken(): string | null {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;

  const decoded = decodeToken(token);
  return decoded?.sub || null;
}

export function getUsuarioIdFromToken(): string | null {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;

  const decoded = decodeToken(token);
  return decoded?.usuarioId || null;
}

export function getNomeFromToken(): string | null {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;

  const decoded = decodeToken(token);
  return decoded?.nome || null;
}

export function getUserFromToken(): UserFromToken | null {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;

  const decoded = decodeToken(token);
  if (!decoded) return null;

  return {
    usuarioId: decoded.usuarioId || null,
    nome: decoded.nome || null,
    email: decoded.sub || null,
    roles: decoded.roles || [],
  };
}
