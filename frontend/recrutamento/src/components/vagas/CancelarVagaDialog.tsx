import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';

interface CancelarVagaDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  vagaTitulo: string;
  onConfirm: (motivo: string) => Promise<void>;
}

export function CancelarVagaDialog({
  open,
  onOpenChange,
  vagaTitulo,
  onConfirm,
}: CancelarVagaDialogProps) {
  const [motivo, setMotivo] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!motivo.trim()) {
      setError('O motivo do cancelamento é obrigatório');
      return;
    }

    try {
      setLoading(true);
      setError('');
      await onConfirm(motivo);
      setMotivo('');
      onOpenChange(false);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao cancelar vaga');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setMotivo('');
    setError('');
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Cancelamento de Vaga</DialogTitle>
            <DialogDescription>
              Título da vaga que está sendo cancelada
            </DialogDescription>
          </DialogHeader>

          <div className="py-4 space-y-4">
            <div className="text-sm font-medium">{vagaTitulo}</div>

            <div className="space-y-2">
              <Label htmlFor="motivo">Motivo do cancelamento:</Label>
              <Textarea
                id="motivo"
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
                placeholder="Descreva o motivo do cancelamento..."
                rows={8}
                className="resize-none"
                required
              />
              {error && (
                <p className="text-sm text-destructive">{error}</p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleCancel}
              disabled={loading}
            >
              Voltar
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Cancelando...' : 'Cancelar Vaga'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
