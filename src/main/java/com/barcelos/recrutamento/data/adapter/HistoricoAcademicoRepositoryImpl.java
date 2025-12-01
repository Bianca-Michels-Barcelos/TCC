package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.HistoricoAcademico;
import com.barcelos.recrutamento.core.port.HistoricoAcademicoRepository;
import com.barcelos.recrutamento.data.mapper.HistoricoAcademicoMapper;
import com.barcelos.recrutamento.data.spring.HistoricoAcademicoJpaRepository;
import com.barcelos.recrutamento.data.spring.PerfilCandidatoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class HistoricoAcademicoRepositoryImpl implements HistoricoAcademicoRepository {

    private final HistoricoAcademicoJpaRepository jpa;
    private final PerfilCandidatoJpaRepository perfilJpa;
    private final HistoricoAcademicoMapper mapper;

    public HistoricoAcademicoRepositoryImpl(HistoricoAcademicoJpaRepository jpa,
                                            PerfilCandidatoJpaRepository perfilJpa,
                                            HistoricoAcademicoMapper mapper) {
        this.jpa = jpa;
        this.perfilJpa = perfilJpa;
        this.mapper = mapper;
    }

    @Override
    public HistoricoAcademico save(HistoricoAcademico historico) {
        var perfil = perfilJpa.getReferenceById(historico.getUsuarioId());
        var entity = mapper.toEntity(historico, perfil);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<HistoricoAcademico> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<HistoricoAcademico> listByUsuario(UUID usuarioId) {
        return jpa.findByPerfilCandidato_Id(usuarioId)
                .stream().map(mapper::toDomain).toList();
    }
}
