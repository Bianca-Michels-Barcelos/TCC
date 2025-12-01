package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.HistoricoEtapaProcesso;
import com.barcelos.recrutamento.core.port.HistoricoEtapaProcessoRepository;
import com.barcelos.recrutamento.data.mapper.HistoricoEtapaProcessoMapper;
import com.barcelos.recrutamento.data.spring.EtapaProcessoJpaRepository;
import com.barcelos.recrutamento.data.spring.HistoricoEtapaProcessoJpaRepository;
import com.barcelos.recrutamento.data.spring.ProcessoSeletivoJpaRepository;
import com.barcelos.recrutamento.data.spring.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class HistoricoEtapaProcessoRepositoryImpl implements HistoricoEtapaProcessoRepository {

    private final HistoricoEtapaProcessoJpaRepository jpaRepository;
    private final ProcessoSeletivoJpaRepository processoJpaRepository;
    private final EtapaProcessoJpaRepository etapaProcessoJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final HistoricoEtapaProcessoMapper mapper;

    public HistoricoEtapaProcessoRepositoryImpl(HistoricoEtapaProcessoJpaRepository jpaRepository,
                                                ProcessoSeletivoJpaRepository processoJpaRepository,
                                                EtapaProcessoJpaRepository etapaProcessoJpaRepository,
                                                UsuarioJpaRepository usuarioJpaRepository,
                                                HistoricoEtapaProcessoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.processoJpaRepository = processoJpaRepository;
        this.etapaProcessoJpaRepository = etapaProcessoJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public HistoricoEtapaProcesso save(HistoricoEtapaProcesso historico) {
        var processo = processoJpaRepository.getReferenceById(historico.getProcessoId());
        var etapaAnterior = historico.getEtapaAnteriorId() != null
            ? etapaProcessoJpaRepository.getReferenceById(historico.getEtapaAnteriorId())
            : null;
        var etapaNova = etapaProcessoJpaRepository.getReferenceById(historico.getEtapaNovaId());
        var usuario = usuarioJpaRepository.getReferenceById(historico.getUsuarioId());

        var entity = mapper.toEntity(historico, processo, etapaAnterior, etapaNova, usuario);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<HistoricoEtapaProcesso> findByProcessoIdOrderByDataMudancaDesc(UUID processoId) {
        return jpaRepository.findByProcesso_IdOrderByDataMudancaDesc(processoId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
