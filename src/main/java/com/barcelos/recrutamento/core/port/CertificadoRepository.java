package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.Certificado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificadoRepository {
    Certificado save(Certificado certificado);
    Optional<Certificado> findById(UUID id);
    List<Certificado> listByPerfilCandidato(UUID perfilCandidatoId);
    void delete(UUID id);
}
