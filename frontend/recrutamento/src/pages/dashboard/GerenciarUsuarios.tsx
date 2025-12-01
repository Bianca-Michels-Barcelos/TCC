import { useState, useEffect, useMemo } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { Users, Shield, UserCog, Trash2, MoreVertical, Search, Briefcase } from "lucide-react";
import api from "@/lib/api";
import { getOrganizacaoIdFromToken } from "@/lib/jwt";
import { authService } from "@/services/auth.service";
import { recrutadorService } from "@/services/recrutador.service";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";
import { Skeleton } from "@/components/ui/skeleton";
import { usePageTitle } from '@/hooks/usePageTitle';

interface Usuario {
  id: string;
  nome: string;
  email: string;
  papel?: string;
  ativo?: boolean;
}

interface MembroOrganizacao {
  usuarioId: string;
  papel: 'ADMIN' | 'RECRUTADOR';
  ativo: boolean;
}

export default function GerenciarUsuarios() {
  usePageTitle('Gerenciar Usuários');
  const navigate = useNavigate();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [membros, setMembros] = useState<MembroOrganizacao[]>([]);
  const [loading, setLoading] = useState(false);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [sendingInvite, setSendingInvite] = useState(false);
  const [organizacaoId] = useState<string | null>(getOrganizacaoIdFromToken());

  const [searchTerm, setSearchTerm] = useState("");
  const [papelFilter, setPapelFilter] = useState<string>("all");
  const [mostrarInativos, setMostrarInativos] = useState(false);

  const [usuarioParaAlterar, setUsuarioParaAlterar] = useState<Usuario | null>(null);
  const [novoPapel, setNovoPapel] = useState<'ADMIN' | 'RECRUTADOR'>('RECRUTADOR');
  const [usuarioParaTransferir, setUsuarioParaTransferir] = useState<Usuario | null>(null);
  const [usuarioDestinoTransferencia, setUsuarioDestinoTransferencia] = useState<string>("");
  const [usuarioParaRemover, setUsuarioParaRemover] = useState<Usuario | null>(null);

  const [dialogAlterarOpen, setDialogAlterarOpen] = useState(false);
  const [dialogTransferirOpen, setDialogTransferirOpen] = useState(false);

  const user = authService.getUser();
  const isAdmin = user?.roles?.includes("ROLE_ADMIN");
  const usuarioLogadoId = user?.usuarioId;

  useEffect(() => {
    if (!isAdmin) {
      toast.error("Acesso negado. Apenas administradores podem gerenciar usuários.");
      navigate("/dashboard");
    }
  }, [isAdmin, navigate]);

  useEffect(() => {
    loadUsuarios();
  }, []);

  const loadUsuarios = async () => {
    if (!organizacaoId) return;

    try {
      setLoading(true);

      const responseMembros = await api.get(`/organizacoes/${organizacaoId}/membros`);
      setMembros(responseMembros.data || []);

      const response = await recrutadorService.listarRecrutadores(organizacaoId);

      const usuariosNormalizados = response.map((usuario: any) => ({
        id: usuario.id,
        nome: getValue(usuario.nome),
        email: getValue(usuario.email),
      }));

      setUsuarios(usuariosNormalizados);
    } catch (error) {
      console.error("Error loading usuarios:", error);
      toast.error("Erro ao carregar usuários");
      setUsuarios([]);
    } finally {
      setLoading(false);
    }
  };

  const getValue = (field: any): string => {
    if (field == null) return "";
    if (typeof field === "object" && "value" in field) return String(field.value);
    return String(field);
  };

  const getPapelUsuario = (usuarioId: string): 'ADMIN' | 'RECRUTADOR' => {
    const membro = membros.find(m => m.usuarioId === usuarioId);
    return membro?.papel || 'RECRUTADOR';
  };

  const isUsuarioAtivo = (usuarioId: string): boolean => {
    const membro = membros.find(m => m.usuarioId === usuarioId);
    return membro?.ativo ?? true;
  };

  const usuariosFiltrados = useMemo(() => {
    return usuarios.filter((usuario) => {
      if (searchTerm) {
        const searchLower = searchTerm.toLowerCase();
        const matchNome = usuario.nome.toLowerCase().includes(searchLower);
        const matchEmail = usuario.email.toLowerCase().includes(searchLower);
        if (!matchNome && !matchEmail) return false;
      }

      if (papelFilter !== "all") {
        const membro = membros.find(m => m.usuarioId === usuario.id);
        const papel = membro?.papel || 'RECRUTADOR';
        if (papel !== papelFilter) return false;
      }

      if (!mostrarInativos) {
        const membro = membros.find(m => m.usuarioId === usuario.id);
        const ativo = membro?.ativo ?? true;
        if (!ativo) return false;
      }

      return true;
    });
  }, [usuarios, searchTerm, papelFilter, mostrarInativos, usuarioLogadoId, membros]);

  const estatisticas = useMemo(() => {
    const totalUsuarios = usuarios.length;
    const totalAdmins = usuarios.filter(u => {
      const membro = membros.find(m => m.usuarioId === u.id);
      return membro?.papel === 'ADMIN';
    }).length;
    const totalRecrutadores = usuarios.filter(u => {
      const membro = membros.find(m => m.usuarioId === u.id);
      return membro?.papel === 'RECRUTADOR';
    }).length;
    const totalInativos = usuarios.filter(u => {
      const membro = membros.find(m => m.usuarioId === u.id);
      return !(membro?.ativo ?? true);
    }).length;

    return { totalUsuarios, totalAdmins, totalRecrutadores, totalInativos };
  }, [usuarios, usuarioLogadoId, membros]);

  const handleEnviarConvite = async () => {
    if (!organizacaoId) {
      toast.error("Organização não encontrada. Faça login novamente.");
      return;
    }

    if (!email.trim()) {
      toast.error("Por favor, preencha o e-mail");
      return;
    }

    try {
      setSendingInvite(true);
      await api.post(`/organizacoes/${organizacaoId}/convites`, {
        email,
        nome: nome || undefined,
      });
      toast.success("Convite enviado com sucesso!");
      setEmail("");
      setNome("");
    } catch (error: any) {
      console.error("Error sending invite:", error);
      toast.error(error?.response?.data?.message || "Erro ao enviar convite");
    } finally {
      setSendingInvite(false);
    }
  };

  const handleAlterarPapel = async () => {
    if (!usuarioParaAlterar || !organizacaoId) return;

    try {
      await recrutadorService.alterarPapel(
        organizacaoId,
        usuarioParaAlterar.id,
        { papel: novoPapel }
      );
      toast.success(`Papel alterado para ${novoPapel === 'ADMIN' ? 'Administrador' : 'Recrutador'} com sucesso`);
      setDialogAlterarOpen(false);
      setTimeout(() => {
        setUsuarioParaAlterar(null);
      }, 100);
      loadUsuarios();
    } catch (error: any) {
      console.error("Error changing role:", error);
      toast.error(error?.response?.data?.message || "Erro ao alterar papel");
    }
  };

  const handleTransferirVagas = async () => {
    if (!usuarioParaTransferir || !usuarioDestinoTransferencia || !organizacaoId) {
      toast.error("Selecione o usuário de destino");
      return;
    }

    try {
      const result = await recrutadorService.transferirVagas(
        organizacaoId,
        usuarioParaTransferir.id,
        { usuarioIdDestino: usuarioDestinoTransferencia }
      );
      toast.success(`${result.quantidadeTransferida} vaga(s) transferida(s) com sucesso`);
      setDialogTransferirOpen(false);
      setTimeout(() => {
        setUsuarioParaTransferir(null);
        setUsuarioDestinoTransferencia("");
      }, 100);
      loadUsuarios();
    } catch (error: any) {
      console.error("Error transferring jobs:", error);
      toast.error(error?.response?.data?.message || "Erro ao transferir vagas");
    }
  };

  const handleRemover = async () => {
    if (!usuarioParaRemover || !organizacaoId) return;

    try {
      await recrutadorService.removerRecrutador(organizacaoId, usuarioParaRemover.id);
      toast.success("Usuário removido com sucesso");
      setUsuarioParaRemover(null);
      loadUsuarios();
    } catch (error: any) {
      console.error("Error removing user:", error);
      toast.error(error?.response?.data?.message || "Erro ao remover usuário");
    }
  };

  const getPapelBadge = (papel: 'ADMIN' | 'RECRUTADOR') => {
    if (papel === 'ADMIN') {
      return <Badge className="bg-purple-100 text-purple-800 hover:bg-purple-200">Administrador</Badge>;
    }
    return <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-200">Recrutador</Badge>;
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl sm:text-3xl font-bold">Gerenciar Usuários</h2>
        <p className="text-sm sm:text-base text-muted-foreground mt-1">
          Convide novos membros e gerencie permissões da equipe
        </p>
      </div>

      {/* Estatísticas */}
      <div className="grid gap-3 sm:gap-4 grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total de Usuários</p>
                <p className="text-2xl font-bold">{estatisticas.totalUsuarios}</p>
              </div>
              <Users className="w-8 h-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Administradores</p>
                <p className="text-2xl font-bold text-purple-600">{estatisticas.totalAdmins}</p>
              </div>
              <Shield className="w-8 h-8 text-purple-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Recrutadores</p>
                <p className="text-2xl font-bold text-blue-600">{estatisticas.totalRecrutadores}</p>
              </div>
              <UserCog className="w-8 h-8 text-blue-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Inativos</p>
                <p className="text-2xl font-bold text-gray-600">{estatisticas.totalInativos}</p>
              </div>
              <Users className="w-8 h-8 text-gray-600" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Add User Card */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base sm:text-lg">Convidar Novo Membro</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 items-stretch sm:items-end">
            <div className="flex-1 sm:min-w-[200px]">
              <Label htmlFor="nome" className="text-sm">Nome (opcional):</Label>
              <Input
                id="nome"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Nome completo"
                className="h-11"
              />
            </div>
            <div className="flex-1 sm:min-w-[200px]">
              <Label htmlFor="email" className="text-sm">E-mail:</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="email@exemplo.com"
                className="h-11"
              />
            </div>
            <Button
              onClick={handleEnviarConvite}
              disabled={sendingInvite || !organizacaoId}
              className="h-11 w-full sm:w-auto"
            >
              {sendingInvite ? "Enviando..." : "Enviar Convite"}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Filtros */}
      <Card>
        <CardContent className="p-4 sm:p-6">
          <div className="grid gap-3 sm:gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
            <div className="relative sm:col-span-2">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por nome ou email..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9 h-11"
              />
            </div>

            <Select value={papelFilter} onValueChange={setPapelFilter}>
              <SelectTrigger className="h-11">
                <SelectValue placeholder="Filtrar por papel" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todos os papéis</SelectItem>
                <SelectItem value="ADMIN">Administradores</SelectItem>
                <SelectItem value="RECRUTADOR">Recrutadores</SelectItem>
              </SelectContent>
            </Select>

            <div className="flex items-center gap-2 h-11">
              <input
                type="checkbox"
                id="mostrar-inativos"
                checked={mostrarInativos}
                onChange={(e) => setMostrarInativos(e.target.checked)}
                className="w-4 h-4"
              />
              <Label htmlFor="mostrar-inativos" className="cursor-pointer text-sm">
                Mostrar inativos
              </Label>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Lista de Usuários */}
      {loading ? (
        <div className="grid gap-3 sm:gap-4 grid-cols-1 md:grid-cols-2">
          {[1, 2, 3, 4].map((i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-3/4" />
                <Skeleton className="h-4 w-1/2 mt-2" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-2/3 mt-2" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : usuariosFiltrados.length === 0 ? (
        <Card>
          <CardContent className="p-12 text-center">
            <Users className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-xl font-semibold mb-2">
              {searchTerm || papelFilter !== "all" ? "Nenhum usuário encontrado" : "Nenhum usuário cadastrado"}
            </h3>
            <p className="text-muted-foreground">
              {searchTerm || papelFilter !== "all"
                ? "Tente ajustar os filtros"
                : "Comece convidando membros para sua equipe"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-3 sm:gap-4 grid-cols-1 md:grid-cols-2">
          {usuariosFiltrados.map((usuario) => {
            const papel = getPapelUsuario(usuario.id);
            const ativo = isUsuarioAtivo(usuario.id);

            return (
              <Card
                key={usuario.id}
                className={`hover:shadow-lg transition-shadow ${!ativo ? 'opacity-60 bg-gray-50' : ''}`}
              >
                <CardHeader className="pb-3 sm:pb-6">
                  <div className="flex justify-between items-start gap-3 sm:gap-4">
                    <div className="flex-1 min-w-0">
                      <CardTitle className="text-lg sm:text-xl truncate">{usuario.nome}</CardTitle>
                      <p className="text-xs sm:text-sm text-muted-foreground truncate mt-1">{usuario.email}</p>
                      <div className="flex flex-wrap gap-2 mt-2 sm:mt-3">
                        {getPapelBadge(papel)}
                        {usuario.id === usuarioLogadoId && (
                          <Badge className="bg-blue-100 text-blue-800">Você</Badge>
                        )}
                        {!ativo && (
                          <Badge className="bg-gray-100 text-gray-800">Inativo</Badge>
                        )}
                      </div>
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreVertical className="w-4 h-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => {
                            setUsuarioParaAlterar(usuario);
                            setNovoPapel(papel === 'ADMIN' ? 'RECRUTADOR' : 'ADMIN');
                            setDialogAlterarOpen(true);
                          }}
                        >
                          <Shield className="w-4 h-4 mr-2" />
                          Alterar Papel
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          onClick={() => {
                            setUsuarioParaTransferir(usuario);
                            setDialogTransferirOpen(true);
                          }}
                        >
                          <Briefcase className="w-4 h-4 mr-2" />
                          Transferir Vagas
                        </DropdownMenuItem>
                        {usuario.id !== usuarioLogadoId && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setUsuarioParaRemover(usuario)}
                              className="text-red-600"
                            >
                              <Trash2 className="w-4 h-4 mr-2" />
                              Remover Usuário
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </CardHeader>
                <CardContent className="pt-0">
                  <div className="flex items-center gap-2 text-xs sm:text-sm text-muted-foreground">
                    <UserCog className="w-3 h-3 sm:w-4 sm:h-4" />
                    <span>{papel === 'ADMIN' ? 'Acesso total ao sistema' : 'Acesso de recrutador'}</span>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Dialog: Alterar Papel */}
      <Dialog
        open={dialogAlterarOpen}
        onOpenChange={(open) => {
          setDialogAlterarOpen(open);
          if (!open) {
            setTimeout(() => {
              setUsuarioParaAlterar(null);
            }, 100);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Alterar Papel do Usuário</DialogTitle>
            <DialogDescription>
              Alterar o papel de {usuarioParaAlterar?.nome}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <Label>Novo Papel</Label>
              <Select
                value={novoPapel}
                onValueChange={(value) => setNovoPapel(value as 'ADMIN' | 'RECRUTADOR')}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ADMIN">Administrador</SelectItem>
                  <SelectItem value="RECRUTADOR">Recrutador</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {novoPapel === 'RECRUTADOR' && getPapelUsuario(usuarioParaAlterar?.id || '') === 'ADMIN' && (
              <p className="text-sm text-amber-600">
                ⚠️ Atenção: Você está removendo privilégios de administrador deste usuário.
              </p>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogAlterarOpen(false)}>
              Cancelar
            </Button>
            <Button onClick={handleAlterarPapel}>Confirmar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Dialog: Transferir Vagas */}
      <Dialog
        open={dialogTransferirOpen}
        onOpenChange={(open) => {
          setDialogTransferirOpen(open);
          if (!open) {
            setTimeout(() => {
              setUsuarioParaTransferir(null);
              setUsuarioDestinoTransferencia("");
            }, 100);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Transferir Vagas</DialogTitle>
            <DialogDescription>
              Transferir todas as vagas de {usuarioParaTransferir?.nome} para outro usuário
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <Label>Usuário de Destino</Label>
              <Select
                value={usuarioDestinoTransferencia}
                onValueChange={setUsuarioDestinoTransferencia}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione o usuário" />
                </SelectTrigger>
                <SelectContent>
                  {usuarios
                    .filter(u => u.id !== usuarioParaTransferir?.id && isUsuarioAtivo(u.id))
                    .map(u => (
                      <SelectItem key={u.id} value={u.id}>
                        {u.nome} ({getPapelUsuario(u.id) === 'ADMIN' ? 'Admin' : 'Recrutador'})
                        {u.id === usuarioLogadoId && ' - Você'}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>
            <p className="text-sm text-muted-foreground">
              Esta ação transferirá todas as vagas (abertas, fechadas e canceladas) para o usuário selecionado.
              As candidaturas ativas serão mantidas.
            </p>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogTransferirOpen(false)}>
              Cancelar
            </Button>
            <Button onClick={handleTransferirVagas} disabled={!usuarioDestinoTransferencia}>
              Transferir
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* AlertDialog: Remover Usuário */}
      <AlertDialog open={!!usuarioParaRemover} onOpenChange={(open) => !open && setUsuarioParaRemover(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Remover Usuário</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja remover <strong>{usuarioParaRemover?.nome}</strong>?
              <span className="block mt-2 text-muted-foreground">
                Esta ação não pode ser desfeita. Se o usuário possuir vagas cadastradas, elas ficarão sem responsável.
              </span>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleRemover} className="bg-red-600 hover:bg-red-700">
              Remover
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
