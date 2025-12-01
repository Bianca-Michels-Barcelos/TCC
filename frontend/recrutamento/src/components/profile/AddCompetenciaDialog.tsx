import { useState } from 'react';
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

interface AddCompetenciaDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CompetenciaData) => Promise<void>;
}

export interface CompetenciaData {
  titulo: string;
  nivel: 'BASICO' | 'INTERMEDIARIO' | 'AVANCADO';
  descricao: string;
}

export default function AddCompetenciaDialog({ open, onOpenChange, onSubmit }: AddCompetenciaDialogProps) {
  const [formData, setFormData] = useState<CompetenciaData>({
    titulo: '',
    nivel: 'BASICO',
    descricao: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

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
    setError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      await onSubmit(formData);
      
      setFormData({
        titulo: '',
        nivel: 'BASICO',
        descricao: '',
      });
      setFieldErrors({});
      toast.success('Competência adicionada com sucesso!');
      onOpenChange(false);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Erro ao adicionar competência';
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
          <DialogTitle>Adicionar Competência</DialogTitle>
          <DialogDescription>
            Adicione uma habilidade ou competência ao seu perfil
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
              {loading ? 'Adicionando...' : 'Adicionar Competência'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
