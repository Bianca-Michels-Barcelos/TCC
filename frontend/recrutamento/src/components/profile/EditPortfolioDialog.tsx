import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import type { Portfolio } from '@/services/candidato.service';

interface EditPortfolioDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  portfolio: Portfolio | null;
  onSubmit: (id: string, data: { titulo: string; link: string }) => Promise<void>;
}

export default function EditPortfolioDialog({ open, onOpenChange, portfolio, onSubmit }: EditPortfolioDialogProps) {
  const [formData, setFormData] = useState({
    titulo: '',
    link: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (portfolio) {
      setFormData({
        titulo: portfolio.titulo,
        link: portfolio.link,
      });
    }
  }, [portfolio]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.titulo.trim()) {
      errors.titulo = 'Título é obrigatório';
    }

    if (!formData.link.trim()) {
      errors.link = 'Link é obrigatório';
    } else {
      try {
        new URL(formData.link);
      } catch {
        errors.link = 'Link inválido. Use um formato como https://exemplo.com';
      }
    }

    setFieldErrors(errors);

    if (Object.keys(errors).length > 0) {
      toast.error('Por favor, preencha todos os campos obrigatórios corretamente');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!portfolio) return;

    setError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      await onSubmit(portfolio.id, formData);
      setFieldErrors({});
      toast.success('Portfólio atualizado com sucesso!');
      onOpenChange(false);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Erro ao atualizar portfólio';
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
          <DialogTitle>Editar Portfólio</DialogTitle>
          <DialogDescription>
            Atualize as informações do seu projeto
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="titulo">Título: <span className="text-destructive">*</span></Label>
            <Input
              id="titulo"
              name="titulo"
              type="text"
              placeholder="Ex: Sistema de Gestão de Vendas, Website Institucional"
              value={formData.titulo}
              onChange={handleChange}
              disabled={loading}
              maxLength={100}
              className={fieldErrors.titulo ? 'border-destructive' : ''}
            />
            {fieldErrors.titulo && (
              <p className="text-xs text-destructive">{fieldErrors.titulo}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="link">Link: <span className="text-destructive">*</span></Label>
            <Input
              id="link"
              name="link"
              type="url"
              placeholder="Ex: https://github.com/usuario/projeto ou https://meusite.com"
              value={formData.link}
              onChange={handleChange}
              disabled={loading}
              maxLength={255}
              className={fieldErrors.link ? 'border-destructive' : ''}
            />
            {fieldErrors.link ? (
              <p className="text-xs text-destructive">{fieldErrors.link}</p>
            ) : (
              <p className="text-xs text-muted-foreground">
                Adicione o link para o projeto no GitHub, site pessoal, ou qualquer outra plataforma
              </p>
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
