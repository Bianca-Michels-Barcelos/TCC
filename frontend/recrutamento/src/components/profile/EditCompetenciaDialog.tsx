import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import type { Competencia } from '@/services/candidato.service';

interface EditCompetenciaDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  competencia: Competencia | null;
  onSubmit: (id: string, data: { titulo: string; descricao: string; nivel: string }) => Promise<void>;
}

export default function EditCompetenciaDialog({ open, onOpenChange, competencia, onSubmit }: EditCompetenciaDialogProps) {
  const [formData, setFormData] = useState({
    titulo: '',
    nivel: 'BASICO' as 'BASICO' | 'INTERMEDIARIO' | 'AVANCADO',
    descricao: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (competencia) {
      setFormData({
        titulo: competencia.titulo,
        nivel: competencia.nivel,
        descricao: competencia.descricao,
      });
    }
  }, [competencia]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleNivelChange = (value: string) => {
    setFormData(prev => ({
      ...prev,
      nivel: value as 'BASICO' | 'INTERMEDIARIO' | 'AVANCADO'
    }));
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.titulo.trim()) {
      errors.titulo = 'Título é obrigatório';
    }

    if (!formData.descricao.trim()) {
      errors.descricao = 'Descrição é obrigatória';
    } else if (formData.descricao.trim().length < 10) {
      errors.descricao = 'Descrição deve ter pelo menos 10 caracteres';
    }

    setFieldErrors(errors);

    if (Object.keys(errors).length > 0) {
      toast.error('Por favor, preencha todos os campos obrigatórios');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!competencia) return;

    setError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      await onSubmit(competencia.id, formData);
      setFieldErrors({});
      toast.success('Competência atualizada com sucesso!');
      onOpenChange(false);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Erro ao atualizar competência';
      setError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Editar Competência</DialogTitle>
          <DialogDescription>
            Atualize as informações da competência
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="titulo">Título: <span className="text-destructive">*</span></Label>
            <Input
              id="titulo"
              name="titulo"
              type="text"
              placeholder="Ex: JavaScript, Liderança, Gestão de Projetos"
              value={formData.titulo}
              onChange={handleChange}
              disabled={loading}
              className={fieldErrors.titulo ? 'border-destructive' : ''}
            />
            {fieldErrors.titulo && (
              <p className="text-xs text-destructive">{fieldErrors.titulo}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="nivel">Nível:</Label>
            <Select value={formData.nivel} onValueChange={handleNivelChange} disabled={loading}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione o nível" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="BASICO">Básico</SelectItem>
                <SelectItem value="INTERMEDIARIO">Intermediário</SelectItem>
                <SelectItem value="AVANCADO">Avançado</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="descricao">Descrição: <span className="text-destructive">*</span></Label>
            <Textarea
              id="descricao"
              name="descricao"
              value={formData.descricao}
              onChange={handleChange}
              disabled={loading}
              rows={4}
              placeholder="Descreva sua experiência com esta competência..."
              className={fieldErrors.descricao ? 'border-destructive' : ''}
            />
            {fieldErrors.descricao && (
              <p className="text-xs text-destructive">{fieldErrors.descricao}</p>
            )}
          </div>

          {error && (
            <div className="p-3 text-sm text-destructive bg-destructive/10 rounded-md">
              {error}
            </div>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={loading}>
              Cancelar
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Salvando...' : 'Salvar Alterações'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
