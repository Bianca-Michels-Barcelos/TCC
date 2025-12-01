import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  ArrowLeft,
  Edit,
  Ban,
  Building2,
  Home,
  RefreshCw,
  MapPin,
  DollarSign,
  Clock,
  FileText,
  Users,
  Calendar,
  ListOrdered,
  Gift,
} from "lucide-react";
import type { Vaga } from "@/services/vaga.service";
import { beneficioService } from "@/services/beneficio.service";
import type { Beneficio } from "@/services/beneficio.service";
import { nivelExperienciaService } from "@/services/nivelExperiencia.service";
import { getOrganizacaoIdFromToken } from "@/lib/jwt";

interface EtapaProcesso {
  id: string;
  vagaId: string;
  nome: string;
  descricao: string;
  tipo: string;
  ordem: number;
  status: string;
  dataInicio?: string;
  dataFim?: string;
  dataCriacao: string;
}

interface VagaDetailsViewProps {
  vaga: Vaga;
  etapas: EtapaProcesso[];
  candidatosCount: number;
  onBack: () => void;
  onEdit: () => void;
  onCancel: () => void;
}

export function VagaDetailsView({
  vaga,
  etapas,
  candidatosCount,
  onBack,
  onEdit,
  onCancel,
}: VagaDetailsViewProps) {
  const [beneficios, setBeneficios] = useState<Beneficio[]>([]);
  const [nivelExperiencia, setNivelExperiencia] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const organizacaoId = getOrganizacaoIdFromToken();

  useEffect(() => {
    loadDetails();
  }, [vaga.id, organizacaoId]);

  const loadDetails = async () => {
    try {
      setLoading(true);

      if (organizacaoId) {
        const beneficioIds = await beneficioService.listarBeneficiosDaVaga(vaga.id);
        const beneficiosData = await Promise.all(
          beneficioIds.map((id) =>
            beneficioService.buscar(organizacaoId, id)
          )
        );
        setBeneficios(beneficiosData);

        if (vaga.nivelExperienciaId) {
          try {
            const nivel = await nivelExperienciaService.buscar(
              organizacaoId,
              vaga.nivelExperienciaId
            );
            setNivelExperiencia(nivel.descricao);
          } catch (error) {
            console.error("Erro ao carregar nível de experiência:", error);
          }
        }
      }
    } catch (error) {
      console.error("Erro ao carregar detalhes:", error);
    } finally {
      setLoading(false);
    }
  };

  const getModalidadeIcon = () => {
    if (vaga.modalidade === "PRESENCIAL")
      return <Building2 className="w-5 h-5" />;
    if (vaga.modalidade === "REMOTO") return <Home className="w-5 h-5" />;
    if (vaga.modalidade === "HIBRIDO") return <RefreshCw className="w-5 h-5" />;
    return null;
  };

  const getModalidadeColor = () => {
    if (vaga.modalidade === "PRESENCIAL") return "bg-blue-100 text-blue-800";
    if (vaga.modalidade === "REMOTO") return "bg-purple-100 text-purple-800";
    if (vaga.modalidade === "HIBRIDO") return "bg-orange-100 text-orange-800";
    return "bg-gray-100 text-gray-800";
  };

  const getStatusColor = () => {
    if (vaga.status === "ABERTA") return "bg-green-100 text-green-800";
    if (vaga.status === "CANCELADA") return "bg-red-100 text-red-800";
    if (vaga.status === "FECHADA") return "bg-gray-100 text-gray-800";
    return "bg-gray-100 text-gray-800";
  };

  const getValue = (field: string | { value: string } | undefined): string => {
    if (!field) return "";
    if (typeof field === "string") return field;
    if (typeof field === "object" && "value" in field) return field.value;
    return "";
  };

  return (
    <div className="p-6 space-y-6">
      {/* Header with actions */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={onBack}>
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold">{vaga.titulo}</h1>
            <p className="text-muted-foreground">
              Publicada em{" "}
              {new Date(vaga.dataPublicacao).toLocaleDateString("pt-BR")}
            </p>
          </div>
        </div>

        <div className="flex gap-2">
          {vaga.status !== "CANCELADA" && (
            <>
              <Button variant="outline" onClick={onEdit}>
                <Edit className="w-4 h-4 mr-2" />
                Editar Vaga
              </Button>
              <Button variant="destructive" onClick={onCancel}>
                <Ban className="w-4 h-4 mr-2" />
                Cancelar Vaga
              </Button>
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Card 1 - Informações Básicas */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              Informações Básicas
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="text-sm font-semibold text-muted-foreground">
                Status
              </label>
              <div className="mt-1">
                <Badge className={getStatusColor()}>{vaga.status}</Badge>
              </div>
            </div>

            <div>
              <label className="text-sm font-semibold text-muted-foreground">
                Modalidade
              </label>
              <div className="mt-1">
                <Badge
                  className={`inline-flex items-center gap-1 ${getModalidadeColor()}`}
                >
                  {getModalidadeIcon()}
                  {vaga.modalidade}
                </Badge>
              </div>
            </div>

            <div>
              <label className="text-sm font-semibold text-muted-foreground">
                Descrição
              </label>
              <p className="mt-1 text-sm whitespace-pre-wrap">
                {vaga.descricao}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Card 2 - Requisitos e Detalhes */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <ListOrdered className="w-5 h-5" />
              Requisitos e Detalhes
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="text-sm font-semibold text-muted-foreground">
                Requisitos
              </label>
              <p className="mt-1 text-sm whitespace-pre-wrap">
                {vaga.requisitos}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-semibold text-muted-foreground flex items-center gap-1">
                  <DollarSign className="w-4 h-4" />
                  Salário
                </label>
                <p className="mt-1 text-sm">
                  {vaga.salario
                    ? `R$ ${vaga.salario.toLocaleString("pt-BR")}`
                    : "Não informado"}
                </p>
              </div>

              <div>
                <label className="text-sm font-semibold text-muted-foreground flex items-center gap-1">
                  <Clock className="w-4 h-4" />
                  Horário
                </label>
                <p className="mt-1 text-sm">{vaga.horarioTrabalho}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-semibold text-muted-foreground">
                  Tipo de Contrato
                </label>
                <p className="mt-1 text-sm">{vaga.tipoContrato}</p>
              </div>

              <div>
                <label className="text-sm font-semibold text-muted-foreground">
                  Nível de Experiência
                </label>
                <p className="mt-1 text-sm">
                  {loading
                    ? "Carregando..."
                    : nivelExperiencia || "Não informado"}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Card 3 - Localização */}
        {vaga.modalidade !== "REMOTO" && vaga.endereco && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="w-5 h-5" />
                Localização da Vaga
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <p className="text-sm">
                {vaga.endereco.cidade} - {getValue(vaga.endereco.uf)}
              </p>
            </CardContent>
          </Card>
        )}

        {/* Card 4 - Benefícios */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Gift className="w-5 h-5" />
              Benefícios
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-sm text-muted-foreground">
                Carregando benefícios...
              </p>
            ) : beneficios.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Nenhum benefício associado a esta vaga
              </p>
            ) : (
              <ul className="space-y-2">
                {beneficios.map((beneficio) => (
                  <li key={beneficio.id} className="text-sm">
                    <span className="font-semibold">{beneficio.nome}</span>
                    {beneficio.descricao && (
                      <p className="text-muted-foreground">
                        {beneficio.descricao}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        {/* Card 5 - Etapas do Processo Seletivo */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <ListOrdered className="w-5 h-5" />
              Etapas do Processo Seletivo
            </CardTitle>
          </CardHeader>
          <CardContent>
            {etapas.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Nenhuma etapa cadastrada para esta vaga
              </p>
            ) : (
              <div className="space-y-3">
                {etapas.map((etapa) => (
                  <div
                    key={etapa.id}
                    className="flex items-start gap-3 p-3 border rounded-lg"
                  >
                    <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary text-primary-foreground font-bold text-sm flex-shrink-0">
                      {etapa.ordem}
                    </div>
                    <div className="flex-1">
                      <h4 className="font-semibold">{etapa.nome}</h4>
                      {etapa.descricao && (
                        <p className="text-sm text-muted-foreground">
                          {etapa.descricao}
                        </p>
                      )}
                      <div className="flex gap-2 mt-2">
                        <Badge variant="outline" className="text-xs">
                          {etapa.tipo}
                        </Badge>
                        <Badge
                          variant="outline"
                          className={`text-xs ${
                            etapa.status === "PENDENTE"
                              ? "bg-gray-100"
                              : etapa.status === "EM_ANDAMENTO"
                              ? "bg-blue-100"
                              : etapa.status === "CONCLUIDA"
                              ? "bg-green-100"
                              : "bg-red-100"
                          }`}
                        >
                          {etapa.status}
                        </Badge>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Card 6 - Estatísticas */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="w-5 h-5" />
              Estatísticas
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">
                Total de Candidatos
              </span>
              <span className="text-2xl font-bold">{candidatosCount}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground flex items-center gap-1">
                <Calendar className="w-4 h-4" />
                Data de Publicação
              </span>
              <span className="text-sm font-medium">
                {new Date(vaga.dataPublicacao).toLocaleDateString("pt-BR")}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
