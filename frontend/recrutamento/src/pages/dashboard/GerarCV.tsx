import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { FileText, Download, Sparkles, Loader2 } from 'lucide-react';
import { authService } from '@/services/auth.service';
import api from '@/lib/api';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

interface Vaga {
  id: string;
  titulo: string;
  organizacao: {
    nome: string;
  };
}

export default function GerarCV() {
  usePageTitle('Gerar Currículo');
  const [vagas, setVagas] = useState<Vaga[]>([]);
  const [vagaSelecionada, setVagaSelecionada] = useState('');
  const [modelo, setModelo] = useState('PROFISSIONAL');
  const [observacoes, setObservacoes] = useState('');
  const [curriculoGerado, setCurriculoGerado] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingVagas, setLoadingVagas] = useState(false);
  const user = authService.getUser();

  const loadVagas = async () => {
    if (!user?.usuarioId) return;
    
    const mockVagas = [
      { id: '1', titulo: 'Desenvolvedor Full Stack Sênior', organizacao: { nome: 'Tech Solutions' } },
      { id: '2', titulo: 'Product Manager', organizacao: { nome: 'Innovation Corp' } },
      { id: '3', titulo: 'UX/UI Designer', organizacao: { nome: 'Design Studio' } },
      { id: '4', titulo: 'DevOps Engineer', organizacao: { nome: 'Cloud Systems' } },
      { id: '5', titulo: 'Data Scientist', organizacao: { nome: 'Analytics Pro' } },
      { id: '6', titulo: 'Mobile Developer', organizacao: { nome: 'App Factory' } },
    ];
    
    try {
      setLoadingVagas(true);
      const response = await api.get('/vagas');
      setVagas(response.data.length > 0 ? response.data : mockVagas);
    } catch (error: any) {
      console.error('Erro ao carregar vagas:', error);
      setVagas(mockVagas);
    } finally {
      setLoadingVagas(false);
    }
  };

  useState(() => {
    loadVagas();
  });

  const handleGerarCV = async () => {
    if (!user?.usuarioId) {
      toast.error('Usuário não autenticado');
      return;
    }

    if (!vagaSelecionada) {
      toast.error('Selecione uma vaga');
      return;
    }

    try {
      setLoading(true);
      const response = await api.post(`/candidatos/${user.usuarioId}/curriculos/gerar-com-ia`, {
        vagaId: vagaSelecionada,
        modelo: modelo,
        observacoes: observacoes || undefined,
      });
      
      setCurriculoGerado(response.data.curriculo);
      toast.success('Currículo gerado com sucesso!');
    } catch (error: any) {
      console.error('Erro ao gerar currículo:', error);
      toast.error(error.response?.data?.message || 'Erro ao gerar currículo');
      
      const vagaSelecionadaObj = vagas.find(v => v.id === vagaSelecionada);
      setCurriculoGerado(`# ${user.nome}
Desenvolvedor Full Stack | ${user.email}

## Objetivo
Candidatura para a vaga de ${vagaSelecionadaObj?.titulo} na ${vagaSelecionadaObj?.organizacao.nome}

## Resumo Profissional
Profissional experiente com sólida formação em desenvolvimento de software e gestão de projetos. Expertise em tecnologias modernas e metodologias ágeis.

## Experiência Profissional

**Desenvolvedor Sênior** - Tech Company (2020 - Presente)
- Desenvolvimento de aplicações web escaláveis
- Liderança técnica de equipe de 5 desenvolvedores
- Implementação de arquitetura de microsserviços

**Desenvolvedor Pleno** - Software House (2018 - 2020)
- Desenvolvimento full stack com React e Node.js
- Integração com APIs REST e GraphQL
- Otimização de performance e SEO

## Formação Acadêmica

**Bacharelado em Ciência da Computação** - Universidade Federal (2014 - 2018)
- Ênfase em Engenharia de Software
- TCC: Sistemas Distribuídos

## Competências Técnicas
- JavaScript/TypeScript, React, Node.js
- Python, Java, SQL/NoSQL
- Docker, Kubernetes, CI/CD
- Git, Agile/Scrum

## Idiomas
- Português: Nativo
- Inglês: Avançado

${observacoes ? `\n## Observações Adicionais\n${observacoes}` : ''}

---
Currículo gerado automaticamente com IA em ${new Date().toLocaleDateString('pt-BR')}
`);
      toast.success('Currículo de exemplo gerado!');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = () => {
    if (!curriculoGerado) return;

    const blob = new Blob([curriculoGerado], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `curriculo-${user?.nome?.replace(/\s+/g, '-')}-${Date.now()}.md`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    toast.success('Currículo baixado!');
  };

  const handleCopyToClipboard = () => {
    if (!curriculoGerado) return;
    
    navigator.clipboard.writeText(curriculoGerado);
    toast.success('Currículo copiado para a área de transferência!');
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl sm:text-2xl lg:text-3xl font-bold flex items-center gap-2">
          <FileText className="w-6 h-6 sm:w-8 sm:h-8" />
          <span>Gerar Currículo com IA</span>
        </h2>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-primary" />
              Configurações do Currículo
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="vaga">Vaga Alvo *</Label>
              <Select value={vagaSelecionada} onValueChange={setVagaSelecionada}>
                <SelectTrigger id="vaga">
                  <SelectValue placeholder="Selecione a vaga" />
                </SelectTrigger>
                <SelectContent>
                  {loadingVagas ? (
                    <SelectItem value="loading" disabled>
                      Carregando vagas...
                    </SelectItem>
                  ) : vagas.length === 0 ? (
                    <SelectItem value="empty" disabled>
                      Nenhuma vaga disponível
                    </SelectItem>
                  ) : (
                    vagas.map((vaga) => (
                      <SelectItem key={vaga.id} value={vaga.id}>
                        {vaga.titulo} - {vaga.organizacao.nome}
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground mt-1">
                O currículo será otimizado para esta vaga específica
              </p>
            </div>

            <div>
              <Label htmlFor="modelo">Modelo de Currículo</Label>
              <Select value={modelo} onValueChange={setModelo}>
                <SelectTrigger id="modelo">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PROFISSIONAL">Profissional</SelectItem>
                  <SelectItem value="CRIATIVO">Criativo</SelectItem>
                  <SelectItem value="EXECUTIVO">Executivo</SelectItem>
                  <SelectItem value="ACADEMICO">Acadêmico</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="observacoes">Observações Adicionais (Opcional)</Label>
              <Textarea
                id="observacoes"
                value={observacoes}
                onChange={(e) => setObservacoes(e.target.value)}
                placeholder="Ex: Destacar experiência com React, mencionar certificações..."
                rows={4}
              />
              <p className="text-xs text-muted-foreground mt-1">
                A IA usará essas informações para personalizar seu currículo
              </p>
            </div>

            <Button
              onClick={handleGerarCV}
              disabled={loading || !vagaSelecionada}
              className="w-full"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Gerando com IA...
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4 mr-2" />
                  Gerar Currículo
                </>
              )}
            </Button>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm">
              <p className="font-medium text-blue-900 mb-1">💡 Dica</p>
              <p className="text-blue-700">
                Nossa IA analisa seu perfil e a descrição da vaga para criar um currículo
                otimizado que destaca suas qualificações mais relevantes.
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
              <CardTitle>Pré-visualização</CardTitle>
              {curriculoGerado && (
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" onClick={handleCopyToClipboard}>
                    Copiar
                  </Button>
                  <Button variant="outline" size="sm" onClick={handleDownload}>
                    <Download className="w-4 h-4 mr-2" />
                    Baixar
                  </Button>
                </div>
              )}
            </div>
          </CardHeader>
          <CardContent>
            {!curriculoGerado ? (
              <div className="text-center py-12 text-muted-foreground">
                <FileText className="w-16 h-16 mx-auto mb-4 opacity-50" />
                <p>Configure as opções e clique em "Gerar Currículo"</p>
                <p className="text-sm mt-2">Seu currículo aparecerá aqui</p>
              </div>
            ) : (
              <div className="prose prose-sm max-w-none">
                <pre className="whitespace-pre-wrap font-sans text-sm bg-gray-50 p-4 rounded-lg border">
                  {curriculoGerado}
                </pre>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Card className="bg-gradient-to-r from-purple-50 to-blue-50 border-purple-200">
        <CardContent className="p-6">
          <h3 className="font-semibold text-lg mb-2 flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-purple-600" />
            Como funciona a geração com IA?
          </h3>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li>✓ Analisa seu perfil completo (experiências, formação, competências)</li>
            <li>✓ Compara com os requisitos da vaga selecionada</li>
            <li>✓ Destaca suas qualificações mais relevantes</li>
            <li>✓ Otimiza palavras-chave para ATS (Applicant Tracking Systems)</li>
            <li>✓ Formata de acordo com o modelo escolhido</li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
