import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent } from "@/components/ui/card";
import { vagaExternaService } from "@/services/vagaExterna.service";
import { ArrowLeft } from "lucide-react";
import { authService } from "@/services/auth.service";
import { usePageTitle } from '@/hooks/usePageTitle';

export default function CadastrarVagaExterna() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = !!id;
  usePageTitle(isEditing ? 'Editar Vaga Externa' : 'Cadastrar Vaga Externa');
  const user = authService.getUser();

  const [formData, setFormData] = useState({
    titulo: "",
    descricao: "",
    requisitos: "",
    candidatoUsuarioId: user?.usuarioId || "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isEditing && id) {
      loadVaga(id);
    }
  }, [id, isEditing]);

  const loadVaga = async (vagaId: string) => {
    try {
      const vaga = await vagaExternaService.buscar(vagaId);
      setFormData({
        titulo: vaga.titulo,
        descricao: vaga.descricao,
        requisitos: vaga.requisitos,
        candidatoUsuarioId: vaga.candidatoUsuarioId,
      });
    } catch (error) {
      console.error("Error loading external job:", error);
      setError("Erro ao carregar vaga externa");
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      if (isEditing && id) {
        await vagaExternaService.atualizar(id, formData);
      } else {
        await vagaExternaService.criar(formData);
      }
      navigate("/dashboard/vagas-externas");
    } catch (err: any) {
      setError(err.response?.data?.message || "Erro ao salvar vaga externa");
    } finally {
      setLoading(false);
    }
  };

  const handleGerarCurriculo = async () => {
    if (!id) return;

    setLoading(true);
    try {
      await vagaExternaService.gerarCurriculo(id);
      alert(
        "Currículo personalizado está sendo gerado. Você será notificado quando estiver pronto."
      );
    } catch (err: any) {
      setError(err.response?.data?.message || "Erro ao gerar currículo");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => navigate("/dashboard/vagas-externas")}
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <h2 className="text-3xl font-bold">
          {isEditing ? "Editar Vaga Externa" : "Cadastrar Vaga Externa"}
        </h2>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card>
          <CardContent className="pt-6 space-y-4">
            <div className="space-y-2">
              <Label htmlFor="titulo">Título:</Label>
              <Input
                id="titulo"
                name="titulo"
                type="text"
                value={formData.titulo}
                onChange={handleChange}
                required
                disabled={loading}
                maxLength={50}
                placeholder="Ex: Desenvolvedor Full Stack"
              />
              <p className="text-xs text-muted-foreground">
                Máximo 50 caracteres
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="descricao">Descrição:</Label>
              <Textarea
                id="descricao"
                name="descricao"
                value={formData.descricao}
                onChange={handleChange}
                required
                disabled={loading}
                rows={8}
                placeholder="Descreva a vaga, responsabilidades, benefícios..."
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="requisitos">Requisitos:</Label>
              <Textarea
                id="requisitos"
                name="requisitos"
                value={formData.requisitos}
                onChange={handleChange}
                required
                disabled={loading}
                rows={8}
                placeholder="Liste os requisitos necessários para a vaga..."
              />
            </div>

            {error && (
              <div className="p-3 text-sm text-destructive bg-destructive/10 rounded-md">
                {error}
              </div>
            )}
          </CardContent>
        </Card>

        <div className="flex justify-end gap-4">
          {isEditing && (
            <Button
              type="button"
              variant="outline"
              onClick={handleGerarCurriculo}
              disabled={loading}
            >
              Gerar Currículo Personalizado
            </Button>
          )}
          <Button type="submit" disabled={loading} size="lg">
            {loading ? "Salvando..." : "Salvar Vaga Externa"}
          </Button>
        </div>
      </form>
    </div>
  );
}
