import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Star } from 'lucide-react';
import { toast } from 'sonner';
import { avaliacaoOrganizacaoService, type CriarAvaliacaoRequest } from '@/services/avaliacao-organizacao.service';

interface AvaliarOrganizacaoModalProps {
  isOpen: boolean;
  onClose: () => void;
  processoId: string;
  nomeOrganizacao: string;
  onSuccess?: () => void;
}

export function AvaliarOrganizacaoModal({
  isOpen,
  onClose,
  processoId,
  nomeOrganizacao,
  onSuccess,
}: AvaliarOrganizacaoModalProps) {
  const [nota, setNota] = useState<number>(0);
  const [hoveredStar, setHoveredStar] = useState<number>(0);
  const [comentario, setComentario] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (nota === 0) {
      toast.error('Por favor, selecione uma nota de 1 a 5 estrelas');
      return;
    }

    if (!comentario.trim()) {
      toast.error('Por favor, escreva um comentário sobre sua experiência');
      return;
    }

    if (comentario.trim().length < 10) {
      toast.error('O comentário deve ter pelo menos 10 caracteres');
      return;
    }

    setIsSubmitting(true);
    try {
      const request: CriarAvaliacaoRequest = {
        processoId,
        nota,
        comentario: comentario.trim(),
      };

      await avaliacaoOrganizacaoService.criar(request);
      
      toast.success('Avaliação enviada com sucesso! Obrigado pelo seu feedback.');
      
      setNota(0);
      setComentario('');
      
      if (onSuccess) {
        onSuccess();
      }
      
      onClose();
    } catch (error: any) {
      console.error('Erro ao enviar avaliação:', error);
      const errorMsg = error.response?.data?.message || 'Erro ao enviar avaliação';
      toast.error(errorMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setNota(0);
      setComentario('');
      setHoveredStar(0);
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle className="text-2xl">Avaliar Empresa</DialogTitle>
          <DialogDescription>
            Como foi sua experiência com <span className="font-semibold">{nomeOrganizacao}</span>? 
            Seu feedback é importante e ajuda outros candidatos.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6 py-4">
          {/* Star Rating */}
          <div className="space-y-2">
            <Label className="text-base font-semibold">
              Avaliação Geral
            </Label>
            <div className="flex items-center gap-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  onClick={() => setNota(star)}
                  onMouseEnter={() => setHoveredStar(star)}
                  onMouseLeave={() => setHoveredStar(0)}
                  className="transition-transform hover:scale-110 focus:outline-none focus:ring-2 focus:ring-yellow-500 rounded"
                  disabled={isSubmitting}
                >
                  <Star
                    className={`w-10 h-10 transition-colors ${
                      star <= (hoveredStar || nota)
                        ? 'fill-yellow-400 text-yellow-400'
                        : 'text-gray-300'
                    }`}
                  />
                </button>
              ))}
              {nota > 0 && (
                <span className="ml-2 text-sm font-medium text-muted-foreground">
                  {nota} {nota === 1 ? 'estrela' : 'estrelas'}
                </span>
              )}
            </div>
            {nota > 0 && (
              <p className="text-sm text-muted-foreground">
                {nota === 1 && '⭐ Muito insatisfeito'}
                {nota === 2 && '⭐⭐ Insatisfeito'}
                {nota === 3 && '⭐⭐⭐ Neutro'}
                {nota === 4 && '⭐⭐⭐⭐ Satisfeito'}
                {nota === 5 && '⭐⭐⭐⭐⭐ Muito satisfeito'}
              </p>
            )}
          </div>

          {/* Comment */}
          <div className="space-y-2">
            <Label htmlFor="comentario" className="text-base font-semibold">
              Comentário *
            </Label>
            <Textarea
              id="comentario"
              placeholder="Conte-nos sobre sua experiência no processo seletivo. Como foi a comunicação? O processo foi claro e transparente?"
              value={comentario}
              onChange={(e) => setComentario(e.target.value)}
              className="min-h-[120px] resize-none"
              disabled={isSubmitting}
            />
            <p className="text-xs text-muted-foreground">
              {comentario.length} caracteres (mínimo 10)
            </p>
          </div>

          {/* Info Box */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-sm text-blue-800">
              <strong>Importante:</strong> Sua avaliação será pública e não poderá ser editada após o envio. 
              Por favor, seja respeitoso e construtivo em seu feedback.
            </p>
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-3">
          <Button
            variant="outline"
            onClick={handleClose}
            disabled={isSubmitting}
          >
            Cancelar
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={isSubmitting || nota === 0 || !comentario.trim()}
            className="bg-yellow-500 hover:bg-yellow-600 text-white"
          >
            {isSubmitting ? 'Enviando...' : 'Enviar Avaliação'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

