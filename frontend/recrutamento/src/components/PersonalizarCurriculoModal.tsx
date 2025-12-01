import { useState, useEffect } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { X, Loader2, Send, FileText } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import { Bold, Italic, Underline as UnderlineIcon, List, ListOrdered } from 'lucide-react';
import { candidatoService } from '@/services/candidato.service';
import { authService } from '@/services/auth.service';
import { toast } from 'sonner';
import '../styles/tiptap.css';

interface PersonalizarCurriculoModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (modelo: string, conteudoPersonalizado: string) => Promise<void>;
  vagaTitulo: string;
  vagaId: string;
  isProcessing?: boolean;
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

export function PersonalizarCurriculoModal({
  open,
  onClose,
  onSubmit,
  vagaTitulo,
  vagaId,
  isProcessing = false
}: PersonalizarCurriculoModalProps) {
  const user = authService.getUser();
  const [step, setStep] = useState<'modelo' | 'editar'>(open ? 'modelo' : 'modelo');
  const [modeloSelecionado, setModeloSelecionado] = useState<string>('PROFISSIONAL');
  const [conteudoGerado, setConteudoGerado] = useState<string>('');
  const [isGenerating, setIsGenerating] = useState(false);

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: {
          levels: [1, 2, 3],
        },
      }),
      Underline,
    ],
    content: conteudoGerado,
    onUpdate: ({ editor }) => {
      setConteudoGerado(editor.getHTML());
    },
  });

  useEffect(() => {
    if (open) {
      setStep('modelo');
      setModeloSelecionado('PROFISSIONAL');
      setConteudoGerado('');
      setIsGenerating(false);
    }
  }, [open]);

  useEffect(() => {
    if (editor && conteudoGerado && editor.getHTML() !== conteudoGerado) {
      editor.commands.setContent(conteudoGerado);
    }
  }, [conteudoGerado, editor]);

  const handleGerarRascunho = async () => {
    if (!user?.usuarioId) {
      toast.error('Usuário não autenticado');
      return;
    }

    setIsGenerating(true);
    try {
      const response = await candidatoService.gerarCurriculoComIA(
        user.usuarioId,
        vagaId,
        modeloSelecionado
      );
      
      setConteudoGerado(response.curriculo);
      setStep('editar');
      toast.success('Currículo gerado com sucesso!');
    } catch (error: any) {
      console.error('Erro ao gerar currículo:', error);
      toast.error(error.response?.data?.message || 'Erro ao gerar currículo com IA');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSubmitCandidatura = async () => {
    await onSubmit(modeloSelecionado, conteudoGerado);
  };

  const handleClose = () => {
    if (!isProcessing && !isGenerating) {
      onClose();
    }
  };

  return (
    <Dialog.Root open={open} onOpenChange={handleClose}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50 data-[state=open]:animate-overlayShow z-50" />
        <Dialog.Content className="fixed top-1/2 left-1/2 max-h-[90vh] w-[90vw] max-w-[900px] -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white shadow-lg focus:outline-none data-[state=open]:animate-contentShow overflow-hidden z-50 flex flex-col">
          <div className="p-6 border-b">
            <Dialog.Title className="text-2xl font-semibold text-gray-900">
              Personalizar Currículo
            </Dialog.Title>
            <Dialog.Description className="text-sm text-gray-600 mt-1">
              Vaga: <span className="font-medium">{vagaTitulo}</span>
            </Dialog.Description>
          </div>

          <div className="flex-1 overflow-y-auto p-6">
            {step === 'modelo' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold mb-2">Passo 1: Escolha o Modelo</h3>
                  <p className="text-sm text-gray-600 mb-4">
                    Selecione o modelo de currículo que melhor se adequa à vaga.
                  </p>
                </div>

                <div className="space-y-3">
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
              </div>
            )}

            {step === 'editar' && (
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold mb-2">Passo 2: Personalize seu Currículo</h3>
                  <p className="text-sm text-gray-600 mb-4">
                    Edite o conteúdo abaixo para destacar suas qualificações relevantes para esta vaga.
                  </p>
                </div>

                {/* Toolbar */}
                {editor && (
                  <div className="border border-gray-300 rounded-t-md bg-gray-50 p-2 flex flex-wrap gap-1">
                    <button
                      type="button"
                      onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
                      className={`px-3 py-1 rounded text-sm font-medium ${
                        editor.isActive('heading', { level: 1 })
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      H1
                    </button>
                    <button
                      type="button"
                      onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
                      className={`px-3 py-1 rounded text-sm font-medium ${
                        editor.isActive('heading', { level: 2 })
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      H2
                    </button>
                    
                    <div className="w-px h-6 bg-gray-300 mx-1"></div>
                    
                    <button
                      type="button"
                      onClick={() => editor.chain().focus().toggleBold().run()}
                      className={`p-2 rounded ${
                        editor.isActive('bold')
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-700 hover:bg-gray-100'
                      }`}
                      title="Negrito"
                    >
                      <Bold size={16} />
                    </button>
                    <button
                      type="button"
                      onClick={() => editor.chain().focus().toggleItalic().run()}
                      className={`p-2 rounded ${
                        editor.isActive('italic')
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-700 hover:bg-gray-100'
                      }`}
                      title="Itálico"
                    >
                      <Italic size={16} />
                    </button>
                    <button
                      type="button"
                      onClick={() => editor.chain().focus().toggleUnderline().run()}
                      className={`p-2 rounded ${
                        editor.isActive('underline')
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-700 hover:bg-gray-100'
                      }`}
                      title="Sublinhado"
                    >
                      <UnderlineIcon size={16} />
                    </button>
                    
                    <div className="w-px h-6 bg-gray-300 mx-1"></div>
                    
                    <button
                      type="button"
                      onClick={() => editor.chain().focus().toggleBulletList().run()}
                      className={`p-2 rounded ${
                        editor.isActive('bulletList')
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-700 hover:bg-gray-100'
                      }`}
                      title="Lista com marcadores"
                    >
                      <List size={16} />
                    </button>
                    <button
                      type="button"
                      onClick={() => editor.chain().focus().toggleOrderedList().run()}
                      className={`p-2 rounded ${
                        editor.isActive('orderedList')
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-700 hover:bg-gray-100'
                      }`}
                      title="Lista numerada"
                    >
                      <ListOrdered size={16} />
                    </button>
                  </div>
                )}
                
                {/* Editor */}
                <EditorContent
                  editor={editor}
                  className="border border-gray-300 border-t-0 rounded-b-md p-4 min-h-[300px] max-h-[400px] overflow-y-auto prose prose-sm max-w-none focus:outline-none"
                />
              </div>
            )}
          </div>

          <div className="p-6 border-t bg-gray-50 flex justify-between items-center">
            {step === 'modelo' ? (
              <>
                <Button
                  variant="outline"
                  onClick={handleClose}
                  disabled={isGenerating || isProcessing}
                >
                  Cancelar
                </Button>
                <Button
                  onClick={handleGerarRascunho}
                  disabled={isGenerating || isProcessing}
                  className="flex items-center gap-2"
                >
                  {isGenerating ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Gerando...
                    </>
                  ) : (
                    <>
                      <FileText className="w-4 h-4" />
                      Gerar Rascunho
                    </>
                  )}
                </Button>
              </>
            ) : (
              <>
                <Button
                  variant="outline"
                  onClick={() => setStep('modelo')}
                  disabled={isProcessing}
                >
                  Voltar
                </Button>
                <Button
                  onClick={handleSubmitCandidatura}
                  disabled={isProcessing || !conteudoGerado}
                  className="flex items-center gap-2"
                >
                  {isProcessing ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Enviando...
                    </>
                  ) : (
                    <>
                      <Send className="w-4 h-4" />
                      Enviar Candidatura
                    </>
                  )}
                </Button>
              </>
            )}
          </div>

          <Dialog.Close asChild>
            <button
              className="absolute top-4 right-4 inline-flex h-6 w-6 items-center justify-center rounded-full text-gray-400 hover:text-gray-600 focus:outline-none"
              aria-label="Fechar"
              disabled={isProcessing || isGenerating}
            >
              <X size={16} />
            </button>
          </Dialog.Close>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
