import api from "@/lib/api";
import { getUserFromToken } from "@/lib/jwt";

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  usuarioId: string;
  nome: string;
  email: string;
  roles: string[];
  organizacaoId?: string;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>("/auth/login", credentials);

    localStorage.setItem("accessToken", response.data.accessToken);
    localStorage.setItem("refreshToken", response.data.refreshToken);

    return response.data;
  },

  logout() {
    localStorage.clear();
    window.location.href = "/login";
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem("accessToken");
  },

  getUser() {
    return getUserFromToken();
  },
};
