package com.barcelos.recrutamento.data.adapter;

import com.barcelos.recrutamento.core.model.Certificado;
import com.barcelos.recrutamento.core.port.CertificadoRepository;
import com.barcelos.recrutamento.data.mapper.CertificadoMapper;
import com.barcelos.recrutamento.data.spring.CertificadoJpaRepository;
import com.barcelos.recrutamento.data.spring.PerfilCandidatoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CertificadoRepositoryImpl implements CertificadoRepository {

    private final CertificadoJpaRepository jpa;
    private final PerfilCandidatoJpaRepository perfilCandidatoJpa;
    private final CertificadoMapper mapper;

    public CertificadoRepositoryImpl(
            CertificadoJpaRepository jpa,
            PerfilCandidatoJpaRepository perfilCandidatoJpa,
            CertificadoMapper mapper) {
        this.jpa = jpa;
        this.perfilCandidatoJpa = perfilCandidatoJpa;
        this.mapper = mapper;
    }

    @Override
    public Certificado save(Certificado certificado) {
        var perfilCandidato = perfilCandidatoJpa.getReferenceById(certificado.getPerfilCandidatoId());
        var entity = mapper.toEntity(certificado, perfilCandidato);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Certificado> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Certificado> listByPerfilCandidato(UUID perfilCandidatoId) {
        return jpa.findByPerfilCandidatoId(perfilCandidatoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }
}
