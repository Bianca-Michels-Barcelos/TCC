import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Plus, Pencil, Trash2, GraduationCap } from "lucide-react";
import { authService } from "@/services/auth.service";
import { getOrganizacaoIdFromToken } from "@/lib/jwt";
import { nivelExperienciaService } from "@/services/nivelExperiencia.service";
import type { NivelExperiencia } from "@/services/nivelExperiencia.service";
import { toast } from "sonner";
import { usePageTitle } from '@/hooks/usePageTitle';

export default function GerenciarNiveisExperiencia() {
  usePageTitle('Gerenciar Níveis de Experiência');
  const navigate = useNavigate();
  const [niveis, setNiveis] = useState<NivelExperiencia[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingNivel, setEditingNivel] = useState<NivelExperiencia | null>(
    null
  );
  const [formData, setFormData] = useState({ descricao: "" });
  const user = authService.getUser();
  const organizacaoId = getOrganizacaoIdFromToken();
  const isAdmin = user?.roles?.includes("ROLE_ADMIN");

  useEffect(() => {
    if (!isAdmin) {
      toast.error(
        "Acesso negado. Apenas administradores podem gerenciar níveis de experiência."
      );
      navigate("/dashboard");
    }
  }, [isAdmin, user]);

  useEffect(() => {
    loadNiveis();
  }, []);

  const loadNiveis = async () => {
    if (!organizacaoId) {
      toast.error("Empresa não encontrada");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      const data = await nivelExperienciaService.listarPorOrganizacao(
        organizacaoId
      );
      setNiveis(data);
    } catch (error: any) {
      console.error("Erro ao carregar níveis de experiência:", error);
      setNiveis([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!organizacaoId) return;

    try {
      if (editingNivel) {
        await nivelExperienciaService.atualizar(
          organizacaoId,
          editingNivel.id,
          formData.descricao
        );
        toast.success("Nível de experiência atualizado com sucesso!");
      } else {
        await nivelExperienciaService.criar(organizacaoId, formData.descricao);
        toast.success("Nível de experiência criado com sucesso!");
      }
      setDialogOpen(false);
      setFormData({ descricao: "" });
      setEditingNivel(null);
      loadNiveis();
    } catch (error: any) {
      console.error("Erro ao salvar nível de experiência:", error);
      toast.error(
        error.response?.data?.message || "Erro ao salvar nível de experiência"
      );
    }
  };

  const handleEdit = (nivel: NivelExperiencia) => {
    setEditingNivel(nivel);
    setFormData({ descricao: nivel.descricao });
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!organizacaoId) return;
    if (!confirm("Tem certeza que deseja excluir este nível de experiência?"))
      return;

    try {
      await nivelExperienciaService.deletar(organizacaoId, id);
      toast.success("Nível de experiência excluído com sucesso!");
      loadNiveis();
    } catch (error: any) {
      console.error("Erro ao excluir nível de experiência:", error);
      toast.error(
        error.response?.data?.message || "Erro ao excluir nível de experiência"
      );
    }
  };

  const handleOpenDialog = () => {
    setEditingNivel(null);
    setFormData({ descricao: "" });
    setDialogOpen(true);
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <h2 className="text-xl sm:text-2xl lg:text-3xl font-bold flex items-center gap-2">
          <GraduationCap className="w-6 h-6 sm:w-8 sm:h-8" />
          <span>Gerenciar Níveis de Experiência</span>
        </h2>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleOpenDialog}>
              <Plus className="w-4 h-4 mr-2" />
              Novo Nível
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editingNivel
                  ? "Editar Nível de Experiência"
                  : "Novo Nível de Experiência"}
              </DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="descricao">Descrição</Label>
                <Input
                  id="descricao"
                  value={formData.descricao}
                  onChange={(e) =>
                    setFormData({ ...formData, descricao: e.target.value })
                  }
                  placeholder="Ex: Júnior, Pleno, Sênior"
                  required
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Máximo 50 caracteres
                </p>
              </div>
              <div className="flex justify-end gap-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setDialogOpen(false)}
                >
                  Cancelar
                </Button>
                <Button type="submit">
                  {editingNivel ? "Atualizar" : "Criar"}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Card className="bg-gradient-to-r from-blue-50 to-purple-50 border-blue-200">
        <CardContent className="p-6">
          <h3 className="font-semibold mb-3">
            💡 Sobre os Níveis de Experiência
          </h3>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li>
              ✓ Os níveis definem as categorias de experiência disponíveis para
              as vagas
            </li>
            <li>
              ✓ Exemplos comuns: Júnior, Pleno, Sênior, Especialista, Gerente
            </li>
            <li>✓ Apenas níveis ativos aparecem no cadastro de vagas</li>
            <li>
              ✓ Estes níveis ajudam candidatos a encontrar vagas adequadas ao
              seu perfil
            </li>
          </ul>
        </CardContent>
      </Card>

      {loading ? (
        <Card>
          <CardContent className="p-12 flex items-center justify-center">
            <p className="text-muted-foreground">Carregando níveis de experiência...</p>
          </CardContent>
        </Card>
      ) : niveis.length === 0 ? (
        <Card>
          <CardContent className="p-12 text-center">
            <GraduationCap className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">
              Nenhum nível de experiência cadastrado
            </h3>
            <p className="text-muted-foreground mb-4">
              Defina os níveis de experiência para categorizar suas vagas
            </p>
            <Button onClick={handleOpenDialog}>
              <Plus className="w-4 h-4 mr-2" />
              Criar Primeiro Nível
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {niveis.map((nivel) => (
            <Card key={nivel.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex justify-between items-start gap-4">
                  <div className="flex-1 min-w-0">
                    <CardTitle className="text-xl truncate flex items-center gap-2">
                      <GraduationCap className="w-5 h-5 text-primary shrink-0" />
                      {nivel.descricao}
                    </CardTitle>
                  </div>
                  <div className="flex gap-1">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleEdit(nivel)}
                      className="h-8 w-8"
                    >
                      <Pencil className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 text-destructive hover:text-destructive"
                      onClick={() => handleDelete(nivel.id)}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Badge
                  variant="outline"
                  className={
                    nivel.ativo
                      ? "bg-green-100 text-green-800"
                      : "bg-red-100 text-red-800"
                  }
                >
                  {nivel.ativo ? "Ativo" : "Inativo"}
                </Badge>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
