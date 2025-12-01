import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Plus, Pencil, Trash2, Gift } from "lucide-react";
import { authService } from "@/services/auth.service";
import { getOrganizacaoIdFromToken } from "@/lib/jwt";
import api from "@/lib/api";
import { toast } from "sonner";
import { usePageTitle } from '@/hooks/usePageTitle';

interface Beneficio {
  id: string;
  nome: string;
  descricao: string;
  organizacaoId: string;
}

export default function GerenciarBeneficios() {
  usePageTitle('Gerenciar Benefícios');
  const navigate = useNavigate();
  const [beneficios, setBeneficios] = useState<Beneficio[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingBeneficio, setEditingBeneficio] = useState<Beneficio | null>(
    null
  );
  const [formData, setFormData] = useState({ nome: "", descricao: "" });
  const user = authService.getUser();
  const organizacaoId = getOrganizacaoIdFromToken();
  const isAdmin = user?.roles?.includes("ROLE_ADMIN");

  useEffect(() => {
    if (!isAdmin) {
      toast.error(
        "Acesso negado. Apenas administradores podem gerenciar benefícios."
      );
      navigate("/dashboard");
    }
  }, [isAdmin, user, navigate]);

  useEffect(() => {
    loadBeneficios();
  }, []);

  const loadBeneficios = async () => {
    if (!organizacaoId) {
      toast.error("Empresa não encontrada");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      const response = await api.get(
        `/organizacoes/${organizacaoId}/beneficios`
      );
      setBeneficios(response.data.length > 0 ? response.data : []);
    } catch (error: any) {
      console.error("Erro ao carregar benefícios:", error);
      setBeneficios([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!organizacaoId) return;

    try {
      if (editingBeneficio) {
        await api.put(
          `/organizacoes/${organizacaoId}/beneficios/${editingBeneficio.id}`,
          formData
        );
        toast.success("Benefício atualizado com sucesso!");
      } else {
        await api.post(`/organizacoes/${organizacaoId}/beneficios`, formData);
        toast.success("Benefício criado com sucesso!");
      }
      setDialogOpen(false);
      setFormData({ nome: "", descricao: "" });
      setEditingBeneficio(null);
      loadBeneficios();
    } catch (error: any) {
      console.error("Erro ao salvar benefício:", error);
      toast.error(error.response?.data?.message || "Erro ao salvar benefício");
    }
  };

  const handleEdit = (beneficio: Beneficio) => {
    setEditingBeneficio(beneficio);
    setFormData({ nome: beneficio.nome, descricao: beneficio.descricao });
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!organizacaoId) return;
    if (!confirm("Tem certeza que deseja excluir este benefício?")) return;

    try {
      await api.delete(`/organizacoes/${organizacaoId}/beneficios/${id}`);
      toast.success("Benefício excluído com sucesso!");
      loadBeneficios();
    } catch (error: any) {
      console.error("Erro ao excluir benefício:", error);
      toast.error(error.response?.data?.message || "Erro ao excluir benefício");
    }
  };

  const handleOpenDialog = () => {
    setEditingBeneficio(null);
    setFormData({ nome: "", descricao: "" });
    setDialogOpen(true);
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <h2 className="text-xl sm:text-2xl lg:text-3xl font-bold flex items-center gap-2">
          <Gift className="w-6 h-6 sm:w-8 sm:h-8" />
          <span>Gerenciar Benefícios</span>
        </h2>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleOpenDialog}>
              <Plus className="w-4 h-4 mr-2" />
              Novo Benefício
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editingBeneficio ? "Editar Benefício" : "Novo Benefício"}
              </DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="nome">Nome do Benefício</Label>
                <Input
                  id="nome"
                  value={formData.nome}
                  onChange={(e) =>
                    setFormData({ ...formData, nome: e.target.value })
                  }
                  placeholder="Ex: Vale Refeição"
                  required
                />
              </div>
              <div>
                <Label htmlFor="descricao">Descrição</Label>
                <Textarea
                  id="descricao"
                  value={formData.descricao}
                  onChange={(e) =>
                    setFormData({ ...formData, descricao: e.target.value })
                  }
                  placeholder="Descreva os detalhes do benefício"
                  rows={4}
                  required
                />
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
                  {editingBeneficio ? "Atualizar" : "Criar"}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Card className="bg-gradient-to-r from-green-50 to-emerald-50 border-green-200">
        <CardContent className="p-6">
          <h3 className="font-semibold mb-3">💡 Sobre os Benefícios</h3>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li>
              ✓ Os benefícios são vantagens oferecidas aos colaboradores além do
              salário
            </li>
            <li>
              ✓ Exemplos comuns: Vale Refeição, Vale Transporte, Plano de Saúde,
              Home Office
            </li>
            <li>
              ✓ Benefícios atrativos ajudam a conquistar os melhores talentos
            </li>
            <li>
              ✓ Candidatos podem filtrar vagas pelos benefícios oferecidos
            </li>
          </ul>
        </CardContent>
      </Card>

      {loading ? (
        <Card>
          <CardContent className="p-12 flex items-center justify-center">
            <p className="text-muted-foreground">Carregando benefícios...</p>
          </CardContent>
        </Card>
      ) : beneficios.length === 0 ? (
        <Card>
          <CardContent className="p-12 text-center">
            <Gift className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">
              Nenhum benefício cadastrado
            </h3>
            <p className="text-muted-foreground mb-4">
              Comece criando seu primeiro benefício para oferecer aos candidatos
            </p>
            <Button onClick={handleOpenDialog}>
              <Plus className="w-4 h-4 mr-2" />
              Criar Primeiro Benefício
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {beneficios.map((beneficio) => (
            <Card
              key={beneficio.id}
              className="hover:shadow-lg transition-shadow"
            >
              <CardHeader>
                <div className="flex justify-between items-start gap-4">
                  <div className="flex-1 min-w-0">
                    <CardTitle className="text-xl truncate flex items-center gap-2">
                      <Gift className="w-5 h-5 text-primary shrink-0" />
                      {beneficio.nome}
                    </CardTitle>
                  </div>
                  <div className="flex gap-1">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleEdit(beneficio)}
                      className="h-8 w-8"
                    >
                      <Pencil className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 text-destructive hover:text-destructive"
                      onClick={() => handleDelete(beneficio.id)}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground line-clamp-3">
                  {beneficio.descricao}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
