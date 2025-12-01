import { useEffect, useState } from 'react';
import {
  User,
  Mail,
  MapPin,
  Briefcase,
  GraduationCap,
  Calendar,
  Building2,
  Loader2,
  Award,
  FileText,
  Link as LinkIcon
} from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { candidatoService, type ExperienciaProfissional, type HistoricoAcademico, type Competencia, type Certificado, type Portfolio } from '@/services/candidato.service';
import type { BuscarCandidatoResponse } from '@/services/recrutador.service';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

interface CandidatoPerfilPanelProps {
  candidato: BuscarCandidatoResponse | null;
  isOpen: boolean;
  onClose: () => void;
}

export default function CandidatoPerfilPanel({
  candidato,
  isOpen,
  onClose,
}: CandidatoPerfilPanelProps) {
  const [experiencias, setExperiencias] = useState<ExperienciaProfissional[]>([]);
  const [historicos, setHistoricos] = useState<HistoricoAcademico[]>([]);
  const [competencias, setCompetencias] = useState<Competencia[]>([]);
  const [certificados, setCertificados] = useState<Certificado[]>([]);
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (candidato && isOpen) {
      fetchAdditionalData();
    }
  }, [candidato, isOpen]);

  const fetchAdditionalData = async () => {
    if (!candidato) return;

    setLoading(true);

    try {
      const [experienciasData, historicosData, competenciasData, certificadosData, portfoliosData] = await Promise.all([
        candidatoService.listarExperiencias(candidato.usuarioId),
        candidatoService.listarHistoricos(candidato.usuarioId),
        candidatoService.listarCompetencias(candidato.usuarioId),
        candidatoService.listarCertificados(candidato.usuarioId),
        candidatoService.listarPortfolios(candidato.usuarioId),
      ]);

      setExperiencias(experienciasData);
      setHistoricos(historicosData);
      setCompetencias(competenciasData);
      setCertificados(certificadosData);
      setPortfolios(portfoliosData);
    } catch (err) {
      console.error('Error fetching additional candidate data:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string | null | undefined) => {
    if (!dateString) return 'Presente';
    try {
      return format(new Date(dateString), 'MMM yyyy', { locale: ptBR });
    } catch {
      return dateString;
    }
  };

  const formatFullDate = (dateString: string) => {
    try {
      return format(new Date(dateString), 'dd/MM/yyyy', { locale: ptBR });
    } catch {
      return dateString;
    }
  };

  return (
    <Sheet open={isOpen} onOpenChange={onClose}>
      <SheetContent side="right" className="overflow-y-auto">
        {!candidato ? null : (
          <>
            <SheetHeader>
              <SheetTitle className="text-2xl">{candidato.nome}</SheetTitle>
              <SheetDescription>Perfil do candidato</SheetDescription>
            </SheetHeader>

            <div className="mt-6 space-y-6">
              {/* Dados Pessoais */}
              <section>
                <div className="flex items-center gap-2 mb-3">
                  <User className="w-5 h-5 text-primary" />
                  <h3 className="text-lg font-semibold">Dados Pessoais</h3>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex items-center gap-2">
                    <Mail className="w-4 h-4 text-muted-foreground" />
                    <span>{candidato.email}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-muted-foreground" />
                    <span>Nascimento: {formatFullDate(candidato.dataNascimento)}</span>
                  </div>
                  {(candidato.cidade || candidato.uf) && (
                    <div className="flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-muted-foreground" />
                      <span>
                        {candidato.cidade && candidato.cidade}
                        {candidato.uf && ` - ${candidato.uf}`}
                      </span>
                    </div>
                  )}
                </div>
              </section>

              <Separator />

              {/* Competências */}
              {candidato.competencias && candidato.competencias.length > 0 && (
                <>
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <Briefcase className="w-5 h-5 text-primary" />
                      <h3 className="text-lg font-semibold">Competências</h3>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {candidato.competencias.map((comp, idx) => (
                        <Badge key={idx} variant="secondary">
                          {comp}
                        </Badge>
                      ))}
                    </div>
                  </section>

                  <Separator />
                </>
              )}

              {/* Experiências Profissionais - from search or fetched */}
              {(candidato.experiencias.length > 0 || experiencias.length > 0) && (
                <>
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <Briefcase className="w-5 h-5 text-primary" />
                      <h3 className="text-lg font-semibold">Experiências Profissionais</h3>
                    </div>

                    {loading ? (
                      <div className="flex items-center justify-center py-4">
                        <Loader2 className="w-6 h-6 animate-spin text-primary" />
                      </div>
                    ) : experiencias.length > 0 ? (
                      <div className="space-y-4">
                        {experiencias.map((exp) => (
                          <div key={exp.id} className="border-l-2 border-primary pl-4">
                            <h4 className="font-semibold">{exp.cargo}</h4>
                            <div className="flex items-center gap-2 text-sm text-muted-foreground mb-1">
                              <Building2 className="w-3 h-3" />
                              <span>{exp.empresa}</span>
                            </div>
                            <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
                              <Calendar className="w-3 h-3" />
                              <span>
                                {formatDate(exp.dataInicio)} - {formatDate(exp.dataFim)}
                              </span>
                            </div>
                            {exp.descricao && (
                              <p className="text-sm text-muted-foreground">{exp.descricao}</p>
                            )}
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="space-y-2">
                        {candidato.experiencias.map((exp, idx) => (
                          <div key={idx} className="border-l-2 border-primary pl-4">
                            <h4 className="font-semibold text-sm">{exp}</h4>
                          </div>
                        ))}
                      </div>
                    )}
                  </section>

                  <Separator />
                </>
              )}

              {/* Formação Acadêmica */}
              {historicos.length > 0 && (
                <>
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <GraduationCap className="w-5 h-5 text-primary" />
                      <h3 className="text-lg font-semibold">Formação Acadêmica</h3>
                    </div>
                    {loading ? (
                      <div className="flex items-center justify-center py-4">
                        <Loader2 className="w-6 h-6 animate-spin text-primary" />
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {historicos.map((hist) => (
                          <div key={hist.id} className="border-l-2 border-primary pl-4">
                            <h4 className="font-semibold">{hist.titulo}</h4>
                            <div className="flex items-center gap-2 text-sm text-muted-foreground mb-1">
                              <Building2 className="w-3 h-3" />
                              <span>{hist.instituicao}</span>
                            </div>
                            <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
                              <Calendar className="w-3 h-3" />
                              <span>
                                {formatDate(hist.dataInicio)} - {formatDate(hist.dataFim)}
                              </span>
                            </div>
                            {hist.descricao && (
                              <p className="text-sm text-muted-foreground">{hist.descricao}</p>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </section>

                  <Separator />
                </>
              )}

              {/* Competências */}
              {competencias.length > 0 && (
                <>
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <Award className="w-5 h-5 text-primary" />
                      <h3 className="text-lg font-semibold">Competências</h3>
                    </div>
                    {loading ? (
                      <div className="flex items-center justify-center py-4">
                        <Loader2 className="w-6 h-6 animate-spin text-primary" />
                      </div>
                    ) : (
                      <div className="space-y-3">
                        {competencias.map((comp) => (
                          <div key={comp.id} className="border rounded-lg p-3">
                            <div className="flex items-center gap-2 mb-1">
                              <h4 className="font-semibold">{comp.titulo}</h4>
                              <Badge variant="secondary" className="text-xs">
                                {comp.nivel === 'BASICO' ? 'Básico' : comp.nivel === 'INTERMEDIARIO' ? 'Intermediário' : 'Avançado'}
                              </Badge>
                            </div>
                            <p className="text-sm text-muted-foreground">{comp.descricao}</p>
                          </div>
                        ))}
                      </div>
                    )}
                  </section>

                  <Separator />
                </>
              )}

              {/* Certificados */}
              {certificados.length > 0 && (
                <>
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <FileText className="w-5 h-5 text-primary" />
                      <h3 className="text-lg font-semibold">Certificados</h3>
                    </div>
                    {loading ? (
                      <div className="flex items-center justify-center py-4">
                        <Loader2 className="w-6 h-6 animate-spin text-primary" />
                      </div>
                    ) : (
                      <div className="space-y-3">
                        {certificados.map((cert) => (
                          <div key={cert.id} className="border rounded-lg p-3">
                            <h4 className="font-semibold">{cert.titulo}</h4>
                            <div className="flex items-center gap-2 text-sm text-muted-foreground mb-1">
                              <Building2 className="w-3 h-3" />
                              <span>{cert.instituicao}</span>
                            </div>
                            <p className="text-xs text-muted-foreground">
                              Emitido em: {formatFullDate(cert.dataEmissao)}
                              {cert.dataValidade && ` • Válido até: ${formatFullDate(cert.dataValidade)}`}
                            </p>
                            {cert.descricao && (
                              <p className="text-sm text-muted-foreground mt-2">{cert.descricao}</p>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </section>

                  <Separator />
                </>
              )}

              {/* Portfólio */}
              {portfolios.length > 0 && (
                <section>
                  <div className="flex items-center gap-2 mb-3">
                    <LinkIcon className="w-5 h-5 text-primary" />
                    <h3 className="text-lg font-semibold">Portfólio</h3>
                  </div>
                  {loading ? (
                    <div className="flex items-center justify-center py-4">
                      <Loader2 className="w-6 h-6 animate-spin text-primary" />
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {portfolios.map((portfolio) => (
                        <div key={portfolio.id} className="border rounded-lg p-3">
                          <h4 className="font-semibold">{portfolio.titulo}</h4>
                          <a
                            href={portfolio.link}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-sm text-blue-600 hover:text-blue-800 hover:underline flex items-center gap-1 mt-1"
                          >
                            <LinkIcon className="w-3 h-3" />
                            {portfolio.link}
                          </a>
                        </div>
                      ))}
                    </div>
                  )}
                </section>
              )}
            </div>
          </>
        )}
      </SheetContent>
    </Sheet>
  );
}
