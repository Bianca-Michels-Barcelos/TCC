import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { vagaService } from "@/services/vaga.service";
import type {
  Vaga,
  CriarVagaRequest,
  AtualizarVagaRequest,
} from "@/services/vaga.service";
import { authService } from "@/services/auth.service";
import { CadastrarEtapasVagaModal } from "@/components/vagas/CadastrarEtapasVagaModal";
import { getOrganizacaoIdFromToken } from "@/lib/jwt";
import { beneficioService } from "@/services/beneficio.service";
import type { Beneficio } from "@/services/beneficio.service";
import { nivelExperienciaService } from "@/services/nivelExperiencia.service";
import type { NivelExperiencia } from "@/services/nivelExperiencia.service";
import { Checkbox } from "@/components/ui/checkbox";
import { toast } from "sonner";
import { usePageTitle } from '@/hooks/usePageTitle';

type ViewMode = "create" | "edit";

export default function GerenciarVagasComplete() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const initialMode = id ? "edit" : "create";
  usePageTitle(id ? 'Editar Vaga' : 'Nova Vaga');
  const [viewMode, setViewMode] = useState<ViewMode>(initialMode);
  const [selectedVaga, setSelectedVaga] = useState<Vaga | null>(null);
  const [loading, setLoading] = useState(false);
  const [beneficios, setBeneficios] = useState<Beneficio[]>([]);
  const [loadingBeneficios, setLoadingBeneficios] = useState(false);
  const [selectedBeneficioIds, setSelectedBeneficioIds] = useState<string[]>([]);
  const [niveisExperiencia, setNiveisExperiencia] = useState<NivelExperiencia[]>([]);
  const [loadingNiveisExperiencia, setLoadingNiveisExperiencia] = useState(false);
  const [etapasModalOpen, setEtapasModalOpen] = useState(false);
  const [vagaIdForEtapas, setVagaIdForEtapas] = useState<string | null>(null);

  const user = authService.getUser();
  const organizacaoId = getOrganizacaoIdFromToken();
  const isAdmin = user?.roles.includes("ROLE_ADMIN") || false;

  const [formData, setFormData] = useState({
    titulo: "",
    descricao: "",
    requisitos: "",
    horarioTrabalho: "",
    modalidade: "PRESENCIAL",
    tipoContrato: "CLT",
    salario: "",
    nivelExperienciaId: "",
    cidade: "",
    uf: "",
  });

  useEffect(() => {
    loadBeneficios();
    loadNiveisExperiencia();
  }, []);

  useEffect(() => {
    const loadVagaForEdit = async () => {
      if (id && organizacaoId) {
        try {
          setLoading(true);
          const vaga = await vagaService.buscarPorId(id);
          await handleEdit(vaga);
        } catch (error) {
          console.error('Erro ao carregar vaga para edição:', error);
          navigate('/dashboard/minhas-vagas');
        } finally {
          setLoading(false);
        }
      }
    };

    loadVagaForEdit();
  }, [id, organizacaoId]);

  const loadBeneficios = async () => {
    if (!organizacaoId) return;

    try {
      setLoadingBeneficios(true);
      const data = await beneficioService.listarPorOrganizacao(organizacaoId);
      setBeneficios(data);
    } catch (error) {
      console.error("Erro ao carregar beneficios:", error);
    } finally {
      setLoadingBeneficios(false);
    }
  };

  const loadNiveisExperiencia = async () => {
    if (!organizacaoId) return;

    try {
      setLoadingNiveisExperiencia(true);
      const data = await nivelExperienciaService.listarPorOrganizacao(organizacaoId);
      setNiveisExperiencia(data.filter((nivel) => nivel.ativo));
    } catch (error) {
      console.error("Erro ao carregar níveis de experiência:", error);
    } finally {
      setLoadingNiveisExperiencia(false);
    }
  };


  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const vagaData = {
        organizacaoId,
        recrutadorUsuarioId: user?.usuarioId || "",
        titulo: formData.titulo,
        descricao: formData.descricao,
        requisitos: formData.requisitos,
        salario: formData.salario ? parseFloat(formData.salario) : undefined,
        dataPublicacao: new Date().toISOString().split("T")[0],
        status: "ABERTA",
        tipoContrato: formData.tipoContrato,
        modalidade: formData.modalidade,
        horarioTrabalho: formData.horarioTrabalho,
        nivelExperienciaId: formData.nivelExperienciaId || undefined,
        beneficioIds:
          selectedBeneficioIds.length > 0 ? selectedBeneficioIds : undefined,
        ...(formData.modalidade !== "REMOTO" && {
          cidade: formData.cidade,
          uf: formData.uf,
        }),
      };

      if (viewMode === "create") {
        const vagaCriada = await vagaService.criar(vagaData as CriarVagaRequest);
        resetForm();

        setVagaIdForEtapas(vagaCriada.id);
        setEtapasModalOpen(true);
      } else if (viewMode === "edit" && selectedVaga) {
        await vagaService.atualizar(
          selectedVaga.id,
          vagaData as AtualizarVagaRequest
        );
        toast.success('Vaga atualizada com sucesso!');
        resetForm();
        navigate('/dashboard/minhas-vagas');
      }
    } catch (error: any) {
      console.error("Error saving vaga:", error);
      toast.error(
        error.response?.data?.message || 
        `Erro ao ${viewMode === "create" ? "criar" : "atualizar"} vaga. Tente novamente.`
      );
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = async (vaga: Vaga) => {
    setSelectedVaga(vaga);

    const getValue = (field: string | { value: string } | undefined): string => {
      if (!field) return "";
      if (typeof field === "string") return field;
      if (typeof field === "object" && "value" in field) return field.value;
      return "";
    };

    setFormData({
      titulo: vaga.titulo,
      descricao: vaga.descricao,
      requisitos: vaga.requisitos,
      horarioTrabalho: vaga.horarioTrabalho,
      modalidade: vaga.modalidade,
      tipoContrato: vaga.tipoContrato,
      salario: vaga.salario?.toString() || "",
      nivelExperienciaId: vaga.nivelExperienciaId || "",
      cidade: vaga.endereco?.cidade || "",
      uf: getValue(vaga.endereco?.uf),
    });
    try {
      const beneficioIds = await vagaService.listarBeneficiosDaVaga(vaga.id);
      setSelectedBeneficioIds(beneficioIds);
    } catch (error) {
      console.error("Erro ao carregar benefícios da vaga:", error);
      setSelectedBeneficioIds([]);
    }
    setViewMode("edit");
  };


  const handleEtapasComplete = () => {
    setEtapasModalOpen(false);
    setVagaIdForEtapas(null);
    navigate('/dashboard/minhas-vagas');
  };


  const resetForm = () => {
    setFormData({
      titulo: "",
      descricao: "",
      requisitos: "",
      horarioTrabalho: "",
      modalidade: "PRESENCIAL",
      tipoContrato: "CLT",
      salario: "",
      nivelExperienciaId: "",
      cidade: "",
      uf: "",
    });
    setSelectedVaga(null);
    setSelectedBeneficioIds([]);
  };

  const handleCancel = () => {
    navigate('/dashboard/minhas-vagas');
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <h1 className="text-xl sm:text-2xl lg:text-3xl font-bold">
        {viewMode === "create" ? "Cadastrar Vaga" : "Editar Vaga"}
      </h1>

      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
          {/* Left Column */}
          <div className="space-y-4">
            <div>
              <Label htmlFor="titulo">Título:</Label>
              <Input
                id="titulo"
                value={formData.titulo}
                onChange={(e) => handleChange("titulo", e.target.value)}
                required
              />
            </div>

            <div>
              <Label htmlFor="descricao">Descrição:</Label>
              <Textarea
                id="descricao"
                value={formData.descricao}
                onChange={(e) => handleChange("descricao", e.target.value)}
                rows={6}
                required
              />
            </div>

            <div>
              <Label htmlFor="requisitos">Requisitos:</Label>
              <Textarea
                id="requisitos"
                value={formData.requisitos}
                onChange={(e) => handleChange("requisitos", e.target.value)}
                rows={6}
                required
              />
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-4">
            <div>
              <Label htmlFor="horarioTrabalho">Horário de Trabalho:</Label>
              <Input
                id="horarioTrabalho"
                value={formData.horarioTrabalho}
                onChange={(e) =>
                  handleChange("horarioTrabalho", e.target.value)
                }
                placeholder="Ex: 8h às 18h"
                required
              />
            </div>

            <div>
              <Label htmlFor="tipoContrato">Tipo de Contrato:</Label>
              <Select
                value={formData.tipoContrato}
                onValueChange={(value) => handleChange("tipoContrato", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ESTAGIO">Estágio</SelectItem>
                  <SelectItem value="CLT">CLT</SelectItem>
                  <SelectItem value="PJ">PJ</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="modalidade">Modalidade:</Label>
              <Select
                value={formData.modalidade}
                onValueChange={(value) => handleChange("modalidade", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PRESENCIAL">Presencial</SelectItem>
                  <SelectItem value="REMOTO">Remoto</SelectItem>
                  <SelectItem value="HIBRIDO">Híbrido</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="salario">Salário:</Label>
              <Input
                id="salario"
                type="number"
                min="0"
                step="100"
                value={formData.salario}
                onChange={(e) => handleChange("salario", e.target.value)}
                placeholder="Ex: 5000"
              />
            </div>

            <div>
              <Label>Benefícios:</Label>
              {loadingBeneficios ? (
                <p className="text-sm text-muted-foreground">
                  Carregando benefícios...
                </p>
              ) : beneficios.length === 0 ? (
                <div className="border rounded-lg p-4 bg-muted/30">
                  <p className="text-sm text-muted-foreground mb-2">
                    {isAdmin
                      ? "Nenhum benefício cadastrado. Cadastre benefícios primeiro para associá-los às vagas."
                      : "Nenhum benefício cadastrado. Entre em contato com o administrador para cadastrar benefícios."}
                  </p>
                  {isAdmin && (
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() =>
                        (window.location.href =
                          "/dashboard/gerenciar-beneficios")
                      }
                    >
                      Ir para Gerenciar Benefícios
                    </Button>
                  )}
                </div>
              ) : (
                <div className="border rounded-lg p-4 space-y-3 max-h-60 overflow-y-auto">
                  {beneficios.map((beneficio) => (
                    <div
                      key={beneficio.id}
                      className="flex items-start space-x-3"
                    >
                      <Checkbox
                        id={`beneficio-${beneficio.id}`}
                        checked={selectedBeneficioIds.includes(beneficio.id)}
                        onCheckedChange={(checked) => {
                          if (checked) {
                            setSelectedBeneficioIds([
                              ...selectedBeneficioIds,
                              beneficio.id,
                            ]);
                          } else {
                            setSelectedBeneficioIds(
                              selectedBeneficioIds.filter(
                                (id) => id !== beneficio.id
                              )
                            );
                          }
                        }}
                      />
                      <label
                        htmlFor={`beneficio-${beneficio.id}`}
                        className="flex-1 cursor-pointer"
                      >
                        <div className="font-medium text-sm">
                          {beneficio.nome}
                        </div>
                        <div className="text-xs text-muted-foreground">
                          {beneficio.descricao}
                        </div>
                      </label>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div>
              <Label htmlFor="nivelExperiencia">Nível de Experiência</Label>
              {loadingNiveisExperiencia ? (
                <div className="border rounded-lg p-3 text-sm text-muted-foreground">
                  Carregando níveis de experiência...
                </div>
              ) : niveisExperiencia.length === 0 ? (
                <div className="border rounded-lg p-4 bg-muted/30">
                  <p className="text-sm text-muted-foreground mb-2">
                    {isAdmin
                      ? "Nenhum nível de experiência cadastrado. Cadastre níveis primeiro para associá-los às vagas."
                      : "Nenhum nível de experiência cadastrado. Entre em contato com o administrador."}
                  </p>
                  {isAdmin && (
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() =>
                        (window.location.href =
                          "/dashboard/gerenciar-niveis-experiencia")
                      }
                    >
                      Ir para Gerenciar Níveis
                    </Button>
                  )}
                </div>
              ) : (
                <Select
                  value={formData.nivelExperienciaId}
                  onValueChange={(value) =>
                    handleChange("nivelExperienciaId", value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Selecione" />
                  </SelectTrigger>
                  <SelectContent>
                    {niveisExperiencia.map((nivel) => (
                      <SelectItem key={nivel.id} value={nivel.id}>
                        {nivel.descricao}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </div>

            {/* Address fields - Required for PRESENCIAL and HIBRIDO */}
            {formData.modalidade !== "REMOTO" && (
              <>
                <div className="col-span-2 border-t pt-4 mt-4">
                  <h3 className="font-semibold mb-3">Localização da Vaga</h3>
                </div>

                <div className="grid grid-cols-2 gap-4 col-span-2">
                  <div>
                    <Label htmlFor="cidade">Cidade</Label>
                    <Input
                      id="cidade"
                      value={formData.cidade}
                      onChange={(e) => handleChange("cidade", e.target.value)}
                      placeholder="São Paulo"
                      required={formData.modalidade !== "REMOTO"}
                    />
                  </div>
                  <div>
                    <Label htmlFor="uf">UF</Label>
                    <Input
                      id="uf"
                      value={formData.uf}
                      onChange={(e) =>
                        handleChange("uf", e.target.value.toUpperCase())
                      }
                      placeholder="SP"
                      maxLength={2}
                      required={formData.modalidade !== "REMOTO"}
                    />
                  </div>
                </div>
              </>
            )}

          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-end gap-4 mt-6">
          <Button type="button" variant="outline" onClick={handleCancel}>
            Cancelar
          </Button>
          <Button type="submit" disabled={loading}>
            {loading
              ? "Salvando..."
              : viewMode === "edit"
              ? "Salvar Vaga"
              : "Cadastrar Vaga"}
          </Button>
        </div>
      </form>

      {/* Modal de Cadastro de Etapas - Renderizado globalmente */}
      {vagaIdForEtapas && (
        <CadastrarEtapasVagaModal
          open={etapasModalOpen}
          onOpenChange={setEtapasModalOpen}
          vagaId={vagaIdForEtapas}
          onComplete={handleEtapasComplete}
        />
      )}
    </div>
  );
}
