import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, MapPin, Building2, DollarSign, Bookmark, BookmarkCheck, Sparkles, Filter, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Slider } from '@/components/ui/slider';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { vagaService, type BuscaVagaResponse } from '@/services/vaga.service';
import { vagaSalvaService } from '@/services/vagaSalva.service';
import { toast } from 'sonner';
import { usePageTitle } from '@/hooks/usePageTitle';

function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

export default function BuscarVagas() {
  usePageTitle('Buscar Vagas');
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearchTerm = useDebounce(searchTerm, 500);
  const [vagas, setVagas] = useState<BuscaVagaResponse[]>([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [vagasSalvasIds, setVagasSalvasIds] = useState<Set<string>>(new Set());
  const navigate = useNavigate();

  const [showFilters, setShowFilters] = useState(false);
  const [filtroModalidade, setFiltroModalidade] = useState<string>('TODAS');
  const [filtroSalarioMin, setFiltroSalarioMin] = useState<number>(0);
  const [filtroSalarioMax, setFiltroSalarioMax] = useState<number>(30000);
  const [filtroCidade, setFiltroCidade] = useState<string>('');

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    loadVagasSalvas();
  }, []);

  useEffect(() => {
    if (debouncedSearchTerm.trim() && searched) {
      handleSearch();
    }
  }, [debouncedSearchTerm]);

  const loadVagasSalvas = async () => {
    try {
      const salvas = await vagaSalvaService.listar();
      const ids = new Set(salvas.map(s => s.vagaId));
      setVagasSalvasIds(ids);
    } catch (error) {
      console.error('Erro ao carregar vagas salvas:', error);
    }
  };

  const handleToggleSalvar = async (vagaId: string) => {
    const isSalva = vagasSalvasIds.has(vagaId);

    try {
      if (isSalva) {
        await vagaSalvaService.remover(vagaId);
        setVagasSalvasIds(prev => {
          const newSet = new Set(prev);
          newSet.delete(vagaId);
          return newSet;
        });
        toast.success('Vaga removida dos favoritos!');
      } else {
        await vagaSalvaService.salvar(vagaId);
        setVagasSalvasIds(prev => new Set(prev).add(vagaId));
        toast.success('Vaga salva nos favoritos!');
      }
    } catch (error: any) {
      console.error('Erro ao salvar/remover vaga:', error);
      toast.error(error.response?.data?.message || 'Erro ao processar ação');
    }
  };

  const handleSearch = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();

    setSearched(true);
    setLoading(true);
    setCurrentPage(1);

    try {
      const consultaBusca = searchTerm.trim();
      if (!consultaBusca) {
        toast.info('Mostrando todas as vagas disponíveis');
      }

      const results = await vagaService.buscarInteligente({
        consulta: consultaBusca,
        limite: 50
      });

      setVagas(results);

      if (results.length === 0) {
        toast.info('Nenhuma vaga encontrada para sua busca');
      }
    } catch (error: any) {
      console.error('Erro ao buscar vagas:', error);

      if (error.response?.status === 401) {
        toast.error('Sessão expirada. Redirecionando para login...');
        setTimeout(() => navigate('/login'), 2000);
      } else {
        toast.error('Erro ao buscar vagas. Tente novamente.');
      }
      setVagas([]);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickSearch = async (tag: string) => {
    setSearchTerm(tag);
    setSearched(true);
    setLoading(true);
    setCurrentPage(1);

    try {
      const results = await vagaService.buscarInteligente({
        consulta: tag,
        limite: 50
      });
      setVagas(results);
    } catch (error: any) {
      console.error('Erro ao buscar vagas:', error);
      if (error.response?.status === 401) {
        toast.error('Sessão expirada. Redirecionando para login...');
        setTimeout(() => navigate('/login'), 2000);
      } else {
        toast.error('Erro ao buscar vagas');
      }
      setVagas([]);
    } finally {
      setLoading(false);
    }
  };

  const vagasFiltradas = useMemo(() => {
    let filtered = [...vagas];

    if (filtroModalidade !== 'TODAS') {
      filtered = filtered.filter(v => v.modalidade === filtroModalidade);
    }

    filtered = filtered.filter(v => {
      if (!v.salario) return true;
      return v.salario >= filtroSalarioMin && v.salario <= filtroSalarioMax;
    });

    if (filtroCidade.trim()) {
      filtered = filtered.filter(v =>
        v.cidade?.toLowerCase().includes(filtroCidade.toLowerCase())
      );
    }

    filtered.sort((a, b) => {
      if (a.percentualCompatibilidade !== null && a.percentualCompatibilidade !== undefined &&
          b.percentualCompatibilidade !== null && b.percentualCompatibilidade !== undefined) {
        return b.percentualCompatibilidade - a.percentualCompatibilidade;
      }
      if (a.percentualCompatibilidade !== null && a.percentualCompatibilidade !== undefined) return -1;
      if (b.percentualCompatibilidade !== null && b.percentualCompatibilidade !== undefined) return 1;
      return b.scoreRelevancia - a.scoreRelevancia;
    });

    return filtered;
  }, [vagas, filtroModalidade, filtroSalarioMin, filtroSalarioMax, filtroCidade]);

  const totalPages = Math.ceil(vagasFiltradas.length / itemsPerPage);
  const vagasPaginadas = vagasFiltradas.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const formatSalario = (salario?: number) => {
    if (!salario) return 'A combinar';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(salario);
  };

  const getModalidadeBadge = (modalidade?: string) => {
    const colors: Record<string, string> = {
      REMOTO: 'bg-green-100 text-green-800',
      PRESENCIAL: 'bg-blue-100 text-blue-800',
      HIBRIDO: 'bg-purple-100 text-purple-800',
    };
    return colors[modalidade || ''] || 'bg-gray-100 text-gray-800';
  };

  const getScoreColor = (score: number) => {
    if (score >= 70) return 'text-green-600';
    if (score >= 40) return 'text-yellow-600';
    return 'text-red-600';
  };

  const limparFiltros = () => {
    setFiltroModalidade('TODAS');
    setFiltroSalarioMin(0);
    setFiltroSalarioMax(30000);
    setFiltroCidade('');
  };

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      <h2 className="text-2xl sm:text-3xl font-bold">Buscar Vagas</h2>

      {/* Search Form */}
      <Card>
        <CardContent className="pt-4 sm:pt-6">
          <form onSubmit={handleSearch} className="space-y-3 sm:space-y-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 sm:w-5 sm:h-5 text-muted-foreground" />
              <Input
                type="text"
                placeholder="Buscar por cargo, empresa, tecnologia..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9 sm:pl-10 h-11 sm:h-12 text-sm sm:text-base"
              />
            </div>
            <div className="flex flex-col gap-2">
              <Button type="submit" className="w-full" size="lg" disabled={loading}>
                <Search className="w-4 h-4 mr-2" />
                {loading ? 'Buscando...' : 'Pesquisar'}
              </Button>
              <Button
                type="button"
                variant="outline"
                size="lg"
                onClick={() => navigate('/dashboard/vagas-externas/cadastrar')}
                className="w-full"
              >
                Cadastrar Vaga Externa
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Quick Filters */}
      {!searched && (
        <div className="space-y-2 sm:space-y-3">
          <p className="text-xs sm:text-sm font-medium text-muted-foreground">
            Pesquisas populares:
          </p>
          <div className="flex flex-wrap gap-2">
            {['Desenvolvedor', 'Designer', 'Gerente', 'Analista', 'DevOps', 'Product Manager'].map((tag) => (
              <Button
                key={tag}
                variant="outline"
                size="sm"
                onClick={() => handleQuickSearch(tag)}
                disabled={loading}
                className="text-xs sm:text-sm"
              >
                {tag}
              </Button>
            ))}
          </div>
        </div>
      )}

      {/* Results */}
      {searched && (
        <div className="space-y-4">
          {/* Header com filtros */}
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 sm:gap-4">
            <h3 className="text-lg sm:text-xl font-semibold">
              {vagasFiltradas.length} {vagasFiltradas.length === 1 ? 'vaga encontrada' : 'vagas encontradas'}
            </h3>

            <div className="flex gap-2 w-full sm:w-auto">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowFilters(!showFilters)}
                className="flex-1 sm:flex-none"
              >
                <Filter className="w-4 h-4 mr-2" />
                Filtros
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSearched(false);
                  setSearchTerm('');
                  setVagas([]);
                  setCurrentPage(1);
                  limparFiltros();
                }}
                className="flex-1 sm:flex-none"
              >
                Limpar
              </Button>
            </div>
          </div>

          {/* Painel de Filtros */}
          {showFilters && (
            <Card>
              <CardContent className="pt-4 sm:pt-6">
                <div className="space-y-4">
                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2">
                    <h4 className="text-base sm:text-lg font-semibold">Filtros Avançados</h4>
                    <Button variant="ghost" size="sm" onClick={limparFiltros}>
                      <X className="w-4 h-4 mr-2" />
                      Limpar
                    </Button>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {/* Modalidade */}
                    <div className="space-y-2">
                      <Label>Modalidade</Label>
                      <Select value={filtroModalidade} onValueChange={setFiltroModalidade}>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="TODAS">Todas</SelectItem>
                          <SelectItem value="REMOTO">Remoto</SelectItem>
                          <SelectItem value="PRESENCIAL">Presencial</SelectItem>
                          <SelectItem value="HIBRIDO">Híbrido</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    {/* Cidade */}
                    <div className="space-y-2">
                      <Label>Cidade</Label>
                      <Input
                        placeholder="Ex: São Paulo"
                        value={filtroCidade}
                        onChange={(e) => setFiltroCidade(e.target.value)}
                      />
                    </div>

                    {/* Faixa Salarial */}
                    <div className="space-y-2 sm:col-span-2 lg:col-span-1">
                      <Label className="text-sm">
                        Faixa Salarial: <span className="text-xs">{formatSalario(filtroSalarioMin)} - {formatSalario(filtroSalarioMax)}</span>
                      </Label>
                      <div className="pt-2">
                        <Slider
                          min={0}
                          max={30000}
                          step={1000}
                          value={[filtroSalarioMin, filtroSalarioMax]}
                          onValueChange={([min, max]) => {
                            setFiltroSalarioMin(min);
                            setFiltroSalarioMax(max);
                          }}
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Loading State */}
          {loading && (
            <div className="grid gap-4">
              {[1, 2, 3].map((i) => (
                <Card key={i}>
                  <CardHeader>
                    <Skeleton className="h-6 w-3/4 mb-2" />
                    <Skeleton className="h-4 w-1/2" />
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                    <div className="flex gap-2">
                      <Skeleton className="h-6 w-20" />
                      <Skeleton className="h-6 w-24" />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {/* Empty State */}
          {!loading && vagasPaginadas.length === 0 && (
            <Card>
              <CardContent className="p-12 text-center">
                <Search className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
                <h3 className="text-xl font-semibold mb-2">Nenhuma vaga encontrada</h3>
                <p className="text-muted-foreground">
                  Tente ajustar seus filtros ou buscar por outros termos
                </p>
              </CardContent>
            </Card>
          )}

          {/* Results List */}
          {!loading && vagasPaginadas.length > 0 && (
            <>
              <div className="grid gap-3 sm:gap-4">
                {vagasPaginadas.map((vaga) => (
                  <Card key={vaga.vagaId} className="hover:shadow-lg transition-shadow">
                    <CardHeader className="pb-3 sm:pb-6">
                      <div className="flex flex-col sm:flex-row justify-between items-start gap-3 sm:gap-4">
                        <div className="flex-1 min-w-0 w-full">
                          <div className="flex flex-col sm:flex-row items-start gap-2 sm:gap-3">
                            <CardTitle className="text-lg sm:text-xl break-words">{vaga.titulo}</CardTitle>
                            {vaga.percentualCompatibilidade && vaga.percentualCompatibilidade >= 80 && (
                              <Badge variant="secondary" className="bg-green-100 text-green-700 flex items-center gap-1 text-xs whitespace-nowrap">
                                <Sparkles className="w-3 h-3" />
                                <span className="hidden sm:inline">Altamente compatível</span>
                                <span className="sm:hidden">Alta compat.</span>
                              </Badge>
                            )}
                          </div>
                          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-2 sm:gap-4 text-xs sm:text-sm text-muted-foreground mt-2">
                            <span className="flex items-center gap-1 truncate">
                              <Building2 className="w-3 h-3 sm:w-4 sm:h-4 flex-shrink-0" />
                              <span className="truncate">{vaga.nomeOrganizacao}</span>
                            </span>
                            {vaga.cidade && vaga.uf && (
                              <span className="flex items-center gap-1 truncate">
                                <MapPin className="w-3 h-3 sm:w-4 sm:h-4 flex-shrink-0" />
                                {vaga.cidade}, {vaga.uf}
                              </span>
                            )}
                          </div>
                        </div>

                        {/* Score de Compatibilidade and Save Button */}
                        <div className="flex sm:flex-col items-center gap-3 sm:gap-2">
                          {vaga.percentualCompatibilidade !== null && vaga.percentualCompatibilidade !== undefined && (
                            <div className="text-center">
                              <div className={`text-2xl sm:text-3xl font-bold ${getScoreColor(vaga.percentualCompatibilidade)}`}>
                                {vaga.percentualCompatibilidade}%
                              </div>
                              <div className="text-xs text-muted-foreground hidden sm:block">compatibilidade</div>
                            </div>
                          )}

                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleToggleSalvar(vaga.vagaId)}
                            className={vagasSalvasIds.has(vaga.vagaId) ? 'text-yellow-600 hover:text-yellow-700' : ''}
                          >
                            {vagasSalvasIds.has(vaga.vagaId) ? (
                              <BookmarkCheck className="w-5 h-5 fill-current" />
                            ) : (
                              <Bookmark className="w-5 h-5" />
                            )}
                          </Button>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-3 sm:space-y-4 pt-0">
                      <p className="text-xs sm:text-sm text-muted-foreground line-clamp-2">
                        {vaga.descricao}
                      </p>

                      {/* Resumo IA */}
                      {vaga.justificativa && vaga.usouIA && (
                        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-2 sm:p-3">
                          <div className="flex items-center gap-2 mb-1 sm:mb-2">
                            <Sparkles className="w-3 h-3 sm:w-4 sm:h-4 text-blue-600" />
                            <Badge variant="secondary" className="text-xs bg-blue-100 text-blue-700">
                              Análise por IA
                            </Badge>
                          </div>
                          <p className="text-xs sm:text-sm text-gray-700 leading-relaxed">
                            {vaga.justificativa}
                          </p>
                        </div>
                      )}

                      <div className="flex flex-wrap gap-2">
                        {vaga.modalidade && (
                          <Badge className={`${getModalidadeBadge(vaga.modalidade)} text-xs`}>
                            {vaga.modalidade}
                          </Badge>
                        )}
                        <Badge variant="outline" className="flex items-center gap-1 text-xs">
                          <DollarSign className="w-3 h-3" />
                          <span className="truncate">{formatSalario(vaga.salario)}</span>
                        </Badge>
                      </div>

                      <div className="flex flex-col sm:flex-row gap-2 pt-2">
                        <Button
                          className="flex-1"
                          onClick={() => navigate(`/dashboard/vagas/${vaga.vagaId}`)}
                        >
                          Ver Detalhes
                        </Button>
                        <Button
                          variant="outline"
                          className="flex-1"
                          onClick={() => navigate(`/dashboard/vagas/${vaga.vagaId}`)}
                        >
                          Candidatar-se
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>

              {/* Paginação */}
              {totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-4 sm:mt-6">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                    disabled={currentPage === 1}
                  >
                    <span className="hidden sm:inline">Anterior</span>
                    <span className="sm:hidden">←</span>
                  </Button>
                  <span className="text-xs sm:text-sm text-muted-foreground px-2 sm:px-4">
                    {currentPage}/{totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                    disabled={currentPage === totalPages}
                  >
                    <span className="hidden sm:inline">Próxima</span>
                    <span className="sm:hidden">→</span>
                  </Button>
                </div>
              )}

              {/* Indicador de resultados */}
              <div className="text-center text-xs sm:text-sm text-muted-foreground">
                Mostrando {(currentPage - 1) * itemsPerPage + 1}-{Math.min(currentPage * itemsPerPage, vagasFiltradas.length)} de {vagasFiltradas.length} vagas
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
}
