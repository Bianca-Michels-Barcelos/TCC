import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Calendar } from 'lucide-react';
import { toast } from 'sonner';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

interface AddExperienceDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: ExperienceData) => Promise<void>;
}

export interface ExperienceData {
  empresa: string;
  cargo: string;
  dataInicio: string;
  dataFim?: string;
  descricao: string;
}

export default function AddExperienceDialog({ open, onOpenChange, onSubmit }: AddExperienceDialogProps) {
  const [formData, setFormData] = useState<ExperienceData>({
    empresa: '',
    cargo: '',
    dataInicio: '',
    dataFim: '',
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

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.empresa.trim()) {
      errors.empresa = 'Empresa é obrigatória';
    }

    if (!formData.cargo.trim()) {
      errors.cargo = 'Cargo é obrigatório';
    }

    if (!formData.dataInicio) {
      errors.dataInicio = 'Data de entrada é obrigatória';
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
      await onSubmit({
        ...formData,
        dataFim: formData.dataFim || undefined,
      });
      
      setFormData({
        empresa: '',
        cargo: '',
        dataInicio: '',
        dataFim: '',
        descricao: '',
      });
      setFieldErrors({});
      toast.success('Experiência profissional adicionada com sucesso!');
      onOpenChange(false);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Erro ao adicionar experiência';
      setError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Adicionar Experiência Profissional</DialogTitle>
          <DialogDescription>
            Preencha os detalhes da sua experiência profissional
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="empresa">Empresa: <span className="text-destructive">*</span></Label>
              <Input
                id="empresa"
                name="empresa"
                type="text"
                value={formData.empresa}
                onChange={handleChange}
                disabled={loading}
                className={fieldErrors.empresa ? 'border-destructive' : ''}
              />
              {fieldErrors.empresa && (
                <p className="text-xs text-destructive">{fieldErrors.empresa}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="cargo">Cargo: <span className="text-destructive">*</span></Label>
              <Input
                id="cargo"
                name="cargo"
                type="text"
                value={formData.cargo}
                onChange={handleChange}
                disabled={loading}
                className={fieldErrors.cargo ? 'border-destructive' : ''}
              />
              {fieldErrors.cargo && (
                <p className="text-xs text-destructive">{fieldErrors.cargo}</p>
              )}
            </div>
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="dataInicio">Data de Entrada: <span className="text-destructive">*</span></Label>
              <div className="relative">
                <Input
                  id="dataInicio"
                  name="dataInicio"
                  type="date"
                  value={formData.dataInicio}
                  onChange={handleChange}
                  disabled={loading}
                  className={`pr-10 [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-inner-spin-button]:hidden ${fieldErrors.dataInicio ? 'border-destructive' : ''}`}
                />
                <Calendar className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground pointer-events-none" />
              </div>
              {fieldErrors.dataInicio && (
                <p className="text-xs text-destructive">{fieldErrors.dataInicio}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="dataFim">Data de Saída:</Label>
              <div className="relative">
                <Input
                  id="dataFim"
                  name="dataFim"
                  type="date"
                  value={formData.dataFim}
                  onChange={handleChange}
                  disabled={loading}
                  className="pr-10 [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-inner-spin-button]:hidden"
                />
                <Calendar className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground pointer-events-none" />
              </div>
              <p className="text-xs text-muted-foreground">Deixe em branco se ainda trabalha aqui</p>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="descricao">Descrição: <span className="text-destructive">*</span></Label>
            <Textarea
              id="descricao"
              name="descricao"
              value={formData.descricao}
              onChange={handleChange}
              disabled={loading}
              rows={6}
              placeholder="Descreva suas responsabilidades e conquistas..."
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
              {loading ? 'Adicionando...' : 'Adicionar Experiência'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
