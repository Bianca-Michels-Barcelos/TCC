import { useState } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { X } from 'lucide-react';

interface SelecionarModeloModalProps {
  open: boolean;
  onClose: () => void;
  onSelect: (modelo: string) => void;
  isGenerating?: boolean;
}

const modelos = [
  {
    id: 'PROFISSIONAL',
    nome: 'Profissional',
    descricao: 'Formato tradicional e objetivo, ideal para empresas formais e vagas corporativas'
  },
  {
    id: 'CRIATIVO',
    nome: 'Criativo',
    descricao: 'Formato mais visual e diferenciado, ideal para áreas criativas e design'
  },
  {
    id: 'EXECUTIVO',
    nome: 'Executivo',
    descricao: 'Destaca liderança e conquistas, ideal para cargos de gestão e alta senioridade'
  },
  {
    id: 'ACADEMICO',
    nome: 'Acadêmico',
    descricao: 'Enfatiza formação, pesquisas e publicações, adequado para posições acadêmicas'
  }
];

export function SelecionarModeloModal({ open, onClose, onSelect, isGenerating = false }: SelecionarModeloModalProps) {
  const [modeloSelecionado, setModeloSelecionado] = useState<string>('PROFISSIONAL');

  const handleConfirm = () => {
    onSelect(modeloSelecionado);
  };

  return (
    <Dialog.Root open={open} onOpenChange={onClose}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50 data-[state=open]:animate-overlayShow" />
        <Dialog.Content className="fixed top-1/2 left-1/2 max-h-[85vh] w-[90vw] max-w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white p-6 shadow-lg focus:outline-none data-[state=open]:animate-contentShow">
          <Dialog.Title className="text-xl font-semibold text-gray-900 mb-4">
            Selecione o Modelo de Currículo
          </Dialog.Title>
          <Dialog.Description className="text-sm text-gray-600 mb-6">
            Escolha o modelo que melhor se adequa à vaga e ao seu perfil profissional.
          </Dialog.Description>

          <div className="space-y-3 mb-6">
            {modelos.map((modelo) => (
              <label
                key={modelo.id}
                className={`
                  flex items-start p-4 border-2 rounded-lg cursor-pointer transition-all
                  ${modeloSelecionado === modelo.id
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300 bg-white'
                  }
                `}
              >
                <input
                  type="radio"
                  name="modelo"
                  value={modelo.id}
                  checked={modeloSelecionado === modelo.id}
                  onChange={(e) => setModeloSelecionado(e.target.value)}
                  className="mt-1 mr-3"
                  disabled={isGenerating}
                />
                <div className="flex-1">
                  <div className="font-semibold text-gray-900 mb-1">{modelo.nome}</div>
                  <div className="text-sm text-gray-600">{modelo.descricao}</div>
                </div>
              </label>
            ))}
          </div>

          <div className="flex justify-end gap-3">
            <Dialog.Close asChild>
              <button
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={isGenerating}
              >
                Cancelar
              </button>
            </Dialog.Close>
            <button
              onClick={handleConfirm}
              disabled={isGenerating}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {isGenerating ? (
                <>
                  <svg className="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Gerando...
                </>
              ) : (
                'Gerar Currículo'
              )}
            </button>
          </div>

          <Dialog.Close asChild>
            <button
              className="absolute top-4 right-4 inline-flex h-6 w-6 items-center justify-center rounded-full text-gray-400 hover:text-gray-600 focus:outline-none"
              aria-label="Fechar"
              disabled={isGenerating}
            >
              <X size={16} />
            </button>
          </Dialog.Close>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
