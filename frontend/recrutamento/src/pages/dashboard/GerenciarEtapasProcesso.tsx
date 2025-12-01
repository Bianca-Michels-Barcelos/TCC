import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Plus, Pencil, Trash2, ListOrdered } from 'lucide-react';
import { getOrganizacaoIdFromToken } from '@/lib/jwt';
import api from '@/lib/api';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

interface EtapaProcesso {
  id: string;
  nome: string;
  descricao: string;
  ordem: number;
  organizacaoId: string;
}

export default function GerenciarEtapasProcesso() {
  usePageTitle('Gerenciar Etapas do Processo');
  const [etapas, setEtapas] = useState<EtapaProcesso[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingEtapa, setEditingEtapa] = useState<EtapaProcesso | null>(null);
  const [formData, setFormData] = useState({ nome: '', descricao: '', ordem: 1 });
  const organizacaoId = getOrganizacaoIdFromToken();

  useEffect(() => {
    loadEtapas();
  }, []);

  const loadEtapas = async () => {
    if (!organizacaoId) {
      toast.error('Empresa não encontrada');
      setLoading(false);
      return;
    }

    const mockEtapas = [
      { id: '1', nome: 'Triagem de Currículos', descricao: 'Análise inicial dos currículos recebidos', ordem: 1, organizacaoId },
      { id: '2', nome: 'Entrevista com RH', descricao: 'Primeira entrevista com o time de recursos humanos', ordem: 2, organizacaoId },
      { id: '3', nome: 'Teste Técnico', descricao: 'Avaliação das habilidades técnicas do candidato', ordem: 3, organizacaoId },
      { id: '4', nome: 'Entrevista Técnica', descricao: 'Entrevista com o time técnico', ordem: 4, organizacaoId },
      { id: '5', nome: 'Entrevista com Gestor', descricao: 'Conversa com o gestor da área', ordem: 5, organizacaoId },
      { id: '6', nome: 'Entrevista Final', descricao: 'Entrevista com diretoria', ordem: 6, organizacaoId },
      { id: '7', nome: 'Proposta', descricao: 'Apresentação e negociação da proposta', ordem: 7, organizacaoId },
    ];

    try {
      setLoading(true);
      const response = await api.get(`/organizacoes/${organizacaoId}/etapas-processo`);
      const data = response.data.sort((a: EtapaProcesso, b: EtapaProcesso) => a.ordem - b.ordem);
      setEtapas(data.length > 0 ? data : mockEtapas);
    } catch (error: any) {
      console.error('Erro ao carregar etapas:', error);
      setEtapas(mockEtapas);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!organizacaoId) return;

    try {
      if (editingEtapa) {
        await api.put(
          `/organizacoes/${organizacaoId}/etapas-processo/${editingEtapa.id}`,
          formData
        );
        toast.success('Etapa atualizada com sucesso!');
      } else {
        await api.post(`/organizacoes/${organizacaoId}/etapas-processo`, formData);
        toast.success('Etapa criada com sucesso!');
      }
      setDialogOpen(false);
      setFormData({ nome: '', descricao: '', ordem: 1 });
      setEditingEtapa(null);
      loadEtapas();
    } catch (error: any) {
      console.error('Erro ao salvar etapa:', error);
      toast.error(error.response?.data?.message || 'Erro ao salvar etapa');
    }
  };

  const handleEdit = (etapa: EtapaProcesso) => {
    setEditingEtapa(etapa);
    setFormData({ nome: etapa.nome, descricao: etapa.descricao, ordem: etapa.ordem });
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!organizacaoId) return;
    if (!confirm('Tem certeza que deseja excluir esta etapa?')) return;

    try {
      await api.delete(`/organizacoes/${organizacaoId}/etapas-processo/${id}`);
      toast.success('Etapa excluída com sucesso!');
      loadEtapas();
    } catch (error: any) {
      console.error('Erro ao excluir etapa:', error);
      toast.error(error.response?.data?.message || 'Erro ao excluir etapa');
    }
  };

  const handleOpenDialog = () => {
    setEditingEtapa(null);
    setFormData({ nome: '', descricao: '', ordem: etapas.length + 1 });
    setDialogOpen(true);
  };

  const moveEtapa = (index: number, direction: 'up' | 'down') => {
    const newEtapas = [...etapas];
    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    
    if (targetIndex < 0 || targetIndex >= newEtapas.length) return;

    [newEtapas[index], newEtapas[targetIndex]] = [newEtapas[targetIndex], newEtapas[index]];
    
    newEtapas.forEach((etapa, idx) => {
      etapa.ordem = idx + 1;
    });

    setEtapas(newEtapas);
    
    toast.success('Ordem atualizada!');
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <h2 className="text-xl sm:text-2xl lg:text-3xl font-bold flex items-center gap-2">
          <ListOrdered className="w-6 h-6 sm:w-8 sm:h-8" />
          <span>Gerenciar Etapas do Processo Seletivo</span>
        </h2>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleOpenDialog}>
              <Plus className="w-4 h-4 mr-2" />
              Nova Etapa
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editingEtapa ? 'Editar Etapa' : 'Nova Etapa'}
              </DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="nome">Nome da Etapa</Label>
                <Input
                  id="nome"
                  value={formData.nome}
                  onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                  placeholder="Ex: Entrevista Técnica"
                  required
                />
              </div>
              <div>
                <Label htmlFor="descricao">Descrição</Label>
                <Textarea
                  id="descricao"
                  value={formData.descricao}
                  onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
                  placeholder="Descreva o que acontece nesta etapa"
                  rows={4}
                  required
                />
              </div>
              <div>
                <Label htmlFor="ordem">Ordem</Label>
                <Input
                  id="ordem"
                  type="number"
                  min="1"
                  value={formData.ordem}
                  onChange={(e) => setFormData({ ...formData, ordem: parseInt(e.target.value) })}
                  required
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Define a sequência desta etapa no processo
                </p>
              </div>
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                  Cancelar
                </Button>
                <Button type="submit">
                  {editingEtapa ? 'Atualizar' : 'Criar'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Etapas Cadastradas</CardTitle>
          <p className="text-sm text-muted-foreground">
            Defina as etapas padrão do processo seletivo da sua empresa
          </p>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-muted-foreground">Carregando etapas...</p>
          ) : etapas.length === 0 ? (
            <p className="text-muted-foreground">
              Nenhuma etapa cadastrada. Clique em "Nova Etapa" para começar.
            </p>
          ) : (
            <div className="space-y-2">
              {etapas.map((etapa, index) => (
                <div
                  key={etapa.id}
                  className="flex items-center gap-3 p-4 border rounded-lg hover:bg-accent/50 transition-colors"
                >
                  <div className="flex flex-col gap-1">
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-6 w-6"
                      onClick={() => moveEtapa(index, 'up')}
                      disabled={index === 0}
                    >
                      ▲
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-6 w-6"
                      onClick={() => moveEtapa(index, 'down')}
                      disabled={index === etapas.length - 1}
                    >
                      ▼
                    </Button>
                  </div>
                  
                  <div className="flex items-center justify-center w-10 h-10 rounded-full bg-primary text-primary-foreground font-bold">
                    {etapa.ordem}
                  </div>
                  
                  <div className="flex-1">
                    <h4 className="font-semibold text-lg">{etapa.nome}</h4>
                    <p className="text-sm text-muted-foreground">{etapa.descricao}</p>
                  </div>
                  
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleEdit(etapa)}
                    >
                      <Pencil className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="text-destructive hover:text-destructive"
                      onClick={() => handleDelete(etapa.id)}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Card className="bg-gradient-to-r from-blue-50 to-purple-50 border-blue-200">
        <CardContent className="p-6">
          <h3 className="font-semibold mb-3">💡 Sobre as Etapas do Processo</h3>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li>✓ As etapas definidas aqui serão usadas como padrão para novas vagas</li>
            <li>✓ Você pode personalizar as etapas para cada vaga específica</li>
            <li>✓ A ordem das etapas define o fluxo do processo seletivo</li>
            <li>✓ Candidatos podem acompanhar em qual etapa estão</li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
