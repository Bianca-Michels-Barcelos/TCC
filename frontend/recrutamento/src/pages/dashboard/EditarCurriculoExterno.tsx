import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '@/lib/api';
import { ArrowLeft, Download, Save, RefreshCw, Bold, Italic, Underline as UnderlineIcon, List, ListOrdered } from 'lucide-react';
import { SelecionarModeloModal } from '../../components/SelecionarModeloModal';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import '../../styles/tiptap.css';
import { usePageTitle } from '@/hooks/usePageTitle';

interface VagaExterna {
  id: string;
  titulo: string;
  descricao: string;
  requisitos: string;
  arquivoCurriculo: string | null;
  modeloCurriculo: string | null;
}

export default function EditarCurriculoExterno() {
  usePageTitle('Editar Currículo Externo');
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [conteudo, setConteudo] = useState('');
  const [modeloAtual, setModeloAtual] = useState('');
  const [vaga, setVaga] = useState<VagaExterna | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [regenerating, setRegenerating] = useState(false);
  const [showModeloModal, setShowModeloModal] = useState(false);

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: {
          levels: [1, 2, 3],
        },
      }),
      Underline,
    ],
    content: conteudo,
    onUpdate: ({ editor }) => {
      setConteudo(editor.getHTML());
    },
  });

  useEffect(() => {
    carregarDados();
  }, [id]);

  useEffect(() => {
    if (editor && conteudo && editor.getHTML() !== conteudo) {
      editor.commands.setContent(conteudo);
    }
  }, [conteudo, editor]);

  const carregarDados = async () => {
    try {
      setLoading(true);

      const vagaResponse = await api.get(`/vagas-externas/${id}`);
      setVaga(vagaResponse.data);

      await new Promise(resolve => setTimeout(resolve, 500));

      const curriculoResponse = await api.get(
        `/vagas-externas/${id}/curriculo/conteudo`
      );

      setConteudo(curriculoResponse.data.conteudo);
      setModeloAtual(curriculoResponse.data.modelo);
    } catch (error: any) {
      console.error('Erro ao carregar currículo:', error);

      if (error.response?.status === 404) {
        alert('Currículo ainda não foi gerado. Você será redirecionado para a página de vagas externas.');
      } else {
        alert('Erro ao carregar currículo');
      }

      navigate('/dashboard/vagas-externas');
    } finally {
      setLoading(false);
    }
  };

  const handleSalvar = async () => {
    try {
      setSaving(true);

      await api.put(
        `/vagas-externas/${id}/curriculo`,
        { conteudo }
      );

      await carregarDados();
    } catch (error) {
      console.error('Erro ao salvar currículo:', error);
      alert('Erro ao salvar currículo');
    } finally {
      setSaving(false);
    }
  };

  const handleTrocarModelo = async (novoModelo: string) => {
    try {
      setRegenerating(true);

      await api.post(
        `/vagas-externas/${id}/curriculo/regenerar`,
        { modelo: novoModelo }
      );

      setShowModeloModal(false);
      await carregarDados();
    } catch (error) {
      console.error('Erro ao trocar modelo:', error);
      alert('Erro ao trocar modelo');
    } finally {
      setRegenerating(false);
    }
  };

  const handleDownload = async () => {
    if (vaga?.arquivoCurriculo) {
      try {
        const response = await api.get(
          `/curriculos/download?path=${encodeURIComponent(vaga.arquivoCurriculo)}`,
          { responseType: 'blob' }
        );

        const blob = new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = vaga.arquivoCurriculo.split('/').pop() || 'curriculo.pdf';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch (error) {
        console.error('Erro ao baixar currículo:', error);
        alert('Erro ao baixar currículo');
      }
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Carregando currículo...</p>
        </div>
      </div>
    );
  }

  if (!conteudo && !loading) {
    return null;
  }

  const nomeModelo = {
    'PROFISSIONAL': 'Profissional',
    'CRIATIVO': 'Criativo',
    'EXECUTIVO': 'Executivo',
    'ACADEMICO': 'Acadêmico'
  }[modeloAtual] || modeloAtual;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => navigate('/dashboard/vagas-externas')}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft size={20} />
          Voltar para Vagas Externas
        </button>

        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Editar Currículo</h1>
            <p className="text-gray-600 mt-1">{vaga?.titulo}</p>
            <p className="text-sm text-gray-500 mt-1">
              Modelo: <span className="font-medium">{nomeModelo}</span>
            </p>
          </div>

          <div className="flex gap-2">
            <button
              onClick={() => setShowModeloModal(true)}
              disabled={regenerating}
              className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
            >
              <RefreshCw size={16} />
              Trocar Modelo
            </button>
            <button
              onClick={handleDownload}
              disabled={!vaga?.arquivoCurriculo}
              className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
            >
              <Download size={16} />
              Download PDF
            </button>
            <button
              onClick={handleSalvar}
              disabled={saving}
              className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              <Save size={16} />
              {saving ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </div>
      </div>

      {/* Editor WYSIWYG */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Conteúdo do Currículo
        </label>
        
        {/* Toolbar */}
        {editor && (
          <div className="border border-gray-300 rounded-t-md bg-gray-50 p-2 flex flex-wrap gap-1">
            <button
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
              onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
              className={`px-3 py-1 rounded text-sm font-medium ${
                editor.isActive('heading', { level: 2 })
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              H2
            </button>
            <button
              onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
              className={`px-3 py-1 rounded text-sm font-medium ${
                editor.isActive('heading', { level: 3 })
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              H3
            </button>
            
            <div className="w-px h-6 bg-gray-300 mx-1"></div>
            
            <button
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
          className="border border-gray-300 border-t-0 rounded-b-md p-4 min-h-[400px] prose prose-sm max-w-none focus:outline-none"
        />
        
        <p className="mt-2 text-sm text-gray-500">
          Use o editor para formatar seu currículo. Ao salvar, um novo PDF será gerado automaticamente.
        </p>
      </div>

      {/* Modal de seleção de modelo */}
      <SelecionarModeloModal
        open={showModeloModal}
        onClose={() => setShowModeloModal(false)}
        onSelect={handleTrocarModelo}
        isGenerating={regenerating}
      />
    </div>
  );
}
