import { useState, useEffect } from 'react';
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
import type { Certificado } from '@/services/candidato.service';

interface EditCertificadoDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  certificado: Certificado | null;
  onSubmit: (id: string, data: { titulo: string; instituicao: string; dataEmissao: string; dataValidade?: string; descricao?: string }) => Promise<void>;
}

export default function EditCertificadoDialog({ open, onOpenChange, certificado, onSubmit }: EditCertificadoDialogProps) {
  const [formData, setFormData] = useState({
    titulo: '',
    instituicao: '',
    dataEmissao: '',
    dataValidade: '',
    descricao: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (certificado) {
      setFormData({
        titulo: certificado.titulo,
        instituicao: certificado.instituicao,
        dataEmissao: certificado.dataEmissao,
        dataValidade: certificado.dataValidade || '',
        descricao: certificado.descricao || '',
      });
    }
  }, [certificado]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
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

    if (!formData.instituicao.trim()) {
      errors.instituicao = 'Instituição é obrigatória';
    }

    if (!formData.dataEmissao) {
      errors.dataEmissao = 'Data de emissão é obrigatória';
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
    if (!certificado) return;

    setError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      await onSubmit(certificado.id, formData);
      setFieldErrors({});
      toast.success('Certificado atualizado com sucesso!');
      onOpenChange(false);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Erro ao atualizar certificado';
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
          <DialogTitle>Editar Certificado</DialogTitle>
          <DialogDescription>
            Atualize as informações do certificado
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="titulo">Título: <span className="text-destructive">*</span></Label>
            <Input
              id="titulo"
              name="titulo"
              type="text"
              placeholder="Ex: AWS Certified Solutions Architect"
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
            <Label htmlFor="instituicao">Instituição: <span className="text-destructive">*</span></Label>
            <Input
              id="instituicao"
              name="instituicao"
              type="text"
              placeholder="Ex: Amazon Web Services, Udemy, Coursera"
              value={formData.instituicao}
              onChange={handleChange}
              disabled={loading}
              className={fieldErrors.instituicao ? 'border-destructive' : ''}
            />
            {fieldErrors.instituicao && (
              <p className="text-xs text-destructive">{fieldErrors.instituicao}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="dataEmissao">Data de Emissão: <span className="text-destructive">*</span></Label>
              <div className="relative">
                <Input
                  id="dataEmissao"
                  name="dataEmissao"
                  type="date"
                  value={formData.dataEmissao}
                  onChange={handleChange}
                  disabled={loading}
                  className={`pr-10 [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-inner-spin-button]:hidden ${fieldErrors.dataEmissao ? 'border-destructive' : ''}`}
                />
                <Calendar className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground pointer-events-none" />
              </div>
              {fieldErrors.dataEmissao && (
                <p className="text-xs text-destructive">{fieldErrors.dataEmissao}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="dataValidade">Data de Validade (opcional):</Label>
              <div className="relative">
                <Input
                  id="dataValidade"
                  name="dataValidade"
                  type="date"
                  value={formData.dataValidade}
                  onChange={handleChange}
                  disabled={loading}
                  className="pr-10 [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-inner-spin-button]:hidden"
                />
                <Calendar className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground pointer-events-none" />
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="descricao">Descrição (opcional):</Label>
            <Textarea
              id="descricao"
              name="descricao"
              value={formData.descricao}
              onChange={handleChange}
              disabled={loading}
              rows={4}
              placeholder="Descreva o que você aprendeu, principais tópicos abordados..."
            />
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
