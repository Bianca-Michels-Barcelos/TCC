import { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
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
import { Plus, Trash2 } from "lucide-react";
import { vagaService } from "@/services/vaga.service";
import { toast } from "sonner";

interface Etapa {
  id: string;
  nome: string;
  descricao: string;
  tipo: string;
  ordem: number;
}

interface CadastrarEtapasVagaModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  vagaId: string;
  onComplete: () => void;
}

const TIPOS_ETAPA = [
  { value: "TRIAGEM_CURRICULO", label: "Triagem de Currículo" },
  { value: "ENTREVISTA_TELEFONICA", label: "Entrevista Telefônica" },
  { value: "TESTE_TECNICO", label: "Teste Técnico" },
  { value: "ENTREVISTA_PRESENCIAL", label: "Entrevista Presencial" },
  { value: "ENTREVISTA_ONLINE", label: "Entrevista Online" },
  { value: "DINAMICA_GRUPO", label: "Dinâmica de Grupo" },
  { value: "AVALIACAO_PSICOLOGICA", label: "Avaliação Psicológica" },
  { value: "CASE_NEGOCIO", label: "Case de Negócio" },
  { value: "PROPOSTA_SALARIAL", label: "Proposta Salarial" },
  { value: "OUTRA", label: "Outra" },
];

export function CadastrarEtapasVagaModal({
  open,
  onOpenChange,
  vagaId,
  onComplete,
}: CadastrarEtapasVagaModalProps) {
  const [etapas, setEtapas] = useState<Etapa[]>([]);
  const [currentEtapa, setCurrentEtapa] = useState({
    nome: "",
    descricao: "",
    tipo: "TRIAGEM_CURRICULO",
  });
  const [loading, setLoading] = useState(false);
  const [isAddingEtapa, setIsAddingEtapa] = useState(false);
  const [loadingEtapas, setLoadingEtapas] = useState(false);

  useEffect(() => {
    if (open && vagaId) {
      loadEtapasExistentes();
    }
  }, [open, vagaId]);

  const loadEtapasExistentes = async () => {
    try {
      setLoadingEtapas(true);
      const etapasExistentes = await vagaService.listarEtapas(vagaId);
      if (etapasExistentes && etapasExistentes.length > 0) {
        setEtapas(etapasExistentes.sort((a, b) => a.ordem - b.ordem));
      }
    } catch (error) {
      console.error("Erro ao carregar etapas existentes:", error);
    } finally {
      setLoadingEtapas(false);
    }
  };

  const handleAddEtapa = () => {
    if (!currentEtapa.nome || !currentEtapa.tipo) {
      alert("Preencha o nome e tipo da etapa");
      return;
    }

    const newEtapa: Etapa = {
      id: Math.random().toString(36).substr(2, 9),
      nome: currentEtapa.nome,
      descricao: currentEtapa.descricao,
      tipo: currentEtapa.tipo,
      ordem: etapas.length + 1,
    };

    setEtapas([...etapas, newEtapa]);
    setCurrentEtapa({
      nome: "",
      descricao: "",
      tipo: "TRIAGEM_CURRICULO",
    });
    setIsAddingEtapa(false);
  };

  const handleRemoveEtapa = (id: string) => {
    const updated = etapas.filter((e) => e.id !== id);
    const reordered = updated.map((e, idx) => ({ ...e, ordem: idx + 1 }));
    setEtapas(reordered);
  };

  const moveEtapa = (index: number, direction: "up" | "down") => {
    const newEtapas = [...etapas];
    const targetIndex = direction === "up" ? index - 1 : index + 1;

    if (targetIndex < 0 || targetIndex >= newEtapas.length) return;

    [newEtapas[index], newEtapas[targetIndex]] = [
      newEtapas[targetIndex],
      newEtapas[index],
    ];

    const reordered = newEtapas.map((e, idx) => ({ ...e, ordem: idx + 1 }));
    setEtapas(reordered);
  };

  const handleSaveAll = async () => {
    const novasEtapas = etapas.filter((etapa) => !etapa.id.includes("-"));
    
    if (novasEtapas.length === 0) {
      onComplete();
      onOpenChange(false);
      return;
    }

    try {
      setLoading(true);
      await Promise.all(
        novasEtapas.map((etapa) =>
          vagaService.adicionarEtapa(vagaId, {
            nome: etapa.nome,
            descricao: etapa.descricao,
            tipo: etapa.tipo,
            ordem: etapa.ordem,
          })
        )
      );
      toast.success(`${novasEtapas.length} etapa(s) adicionada(s) com sucesso!`);
      onComplete();
      onOpenChange(false);
    } catch (error) {
      console.error("Erro ao salvar etapas:", error);
      toast.error("Erro ao salvar etapas. Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  const handleSkip = () => {
    onComplete();
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Cadastrar Etapas do Processo Seletivo</DialogTitle>
          <DialogDescription>
            A etapa de Triagem já foi criada automaticamente. Adicione outras etapas que os candidatos precisarão passar nesta vaga.
            Você pode pular esta etapa e adicionar as etapas depois.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {loadingEtapas ? (
            <div className="text-center py-4 text-muted-foreground">
              Carregando etapas...
            </div>
          ) : (
            <>
          {/* Lista de Etapas Adicionadas */}
          {etapas.length > 0 && (
            <div className="space-y-2">
              <Label className="text-base font-semibold">
                Etapas Adicionadas ({etapas.length})
              </Label>
              <div className="border rounded-lg divide-y">
                {etapas.map((etapa, index) => (
                  <div
                    key={etapa.id}
                    className="flex items-center gap-3 p-3 hover:bg-accent/50"
                  >
                    <div className="flex flex-col gap-1">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6"
                        onClick={() => moveEtapa(index, "up")}
                        disabled={index === 0 || index === 1}
                        title={index === 1 ? "A etapa de Triagem deve permanecer na primeira posição" : ""}
                      >
                        ▲
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6"
                        onClick={() => moveEtapa(index, "down")}
                        disabled={index === etapas.length - 1 || etapa.ordem === 1}
                        title={etapa.ordem === 1 ? "A etapa de Triagem deve permanecer na primeira posição" : ""}
                      >
                        ▼
                      </Button>
                    </div>

                    <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary text-primary-foreground font-bold text-sm">
                      {etapa.ordem}
                    </div>

                    <div className="flex-1">
                      <div className="font-semibold">{etapa.nome}</div>
                      <div className="text-sm text-muted-foreground">
                        {etapa.descricao}
                      </div>
                      <div className="text-xs text-muted-foreground mt-1">
                        {TIPOS_ETAPA.find((t) => t.value === etapa.tipo)
                          ?.label || etapa.tipo}
                      </div>
                    </div>

                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => handleRemoveEtapa(etapa.id)}
                      disabled={etapa.ordem === 1}
                      title={etapa.ordem === 1 ? "A etapa de Triagem não pode ser removida" : "Remover etapa"}
                    >
                      <Trash2 className={`w-4 h-4 ${etapa.ordem === 1 ? 'text-gray-400' : 'text-red-600'}`} />
                    </Button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Formulário para Adicionar Nova Etapa */}
          {!isAddingEtapa && (
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsAddingEtapa(true)}
              className="w-full"
            >
              <Plus className="w-4 h-4 mr-2" />
              Adicionar Etapa
            </Button>
          )}

          {isAddingEtapa && (
            <div className="border rounded-lg p-4 space-y-4 bg-accent/20">
              <Label className="text-base font-semibold">Nova Etapa</Label>

              <div>
                <Label htmlFor="tipo">Tipo de Etapa</Label>
                <Select
                  value={currentEtapa.tipo}
                  onValueChange={(value) =>
                    setCurrentEtapa({ ...currentEtapa, tipo: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TIPOS_ETAPA.map((tipo) => (
                      <SelectItem key={tipo.value} value={tipo.value}>
                        {tipo.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div>
                <Label htmlFor="nome">Nome da Etapa</Label>
                <Input
                  id="nome"
                  value={currentEtapa.nome}
                  onChange={(e) =>
                    setCurrentEtapa({ ...currentEtapa, nome: e.target.value })
                  }
                  placeholder="Ex: Entrevista com o Time de Engenharia"
                />
              </div>

              <div>
                <Label htmlFor="descricao">Descrição (opcional)</Label>
                <Textarea
                  id="descricao"
                  value={currentEtapa.descricao}
                  onChange={(e) =>
                    setCurrentEtapa({
                      ...currentEtapa,
                      descricao: e.target.value,
                    })
                  }
                  placeholder="Descreva o que acontece nesta etapa"
                  rows={3}
                />
              </div>

              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setIsAddingEtapa(false);
                    setCurrentEtapa({
                      nome: "",
                      descricao: "",
                      tipo: "TRIAGEM_CURRICULO",
                    });
                  }}
                  className="flex-1"
                >
                  Cancelar
                </Button>
                <Button
                  type="button"
                  onClick={handleAddEtapa}
                  className="flex-1"
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Adicionar
                </Button>
              </div>
            </div>
          )}
            </>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="ghost"
            onClick={handleSkip}
            disabled={loading}
          >
            Pular
          </Button>
          <Button onClick={handleSaveAll} disabled={loading}>
            {loading ? "Salvando..." : "Concluir"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
