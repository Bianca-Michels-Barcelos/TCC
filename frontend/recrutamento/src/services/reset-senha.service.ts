import api from "@/lib/api";

export interface ValidarTokenResponse {
  valido: boolean;
  email: string | null;
}

const resetSenhaService = {
  async solicitarReset(email: string): Promise<void> {
    await api.post("/public/reset-senha/solicitar", { email });
  },

  async validarToken(token: string): Promise<ValidarTokenResponse> {
    const response = await api.get<ValidarTokenResponse>(
      `/public/reset-senha/validar/${token}`
    );
    return response.data;
  },

  async resetarSenha(token: string, novaSenha: string): Promise<void> {
    await api.post("/public/reset-senha/confirmar", {
      token,
      novaSenha,
    });
  },
};

export default resetSenhaService;

