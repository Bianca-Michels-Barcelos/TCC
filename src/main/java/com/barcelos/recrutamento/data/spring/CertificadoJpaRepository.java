package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.CertificadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CertificadoJpaRepository extends JpaRepository<CertificadoEntity, UUID> {

    @Query("SELECT c FROM CertificadoEntity c WHERE c.perfilCandidato.id = :perfilCandidatoId")
    List<CertificadoEntity> findByPerfilCandidatoId(@Param("perfilCandidatoId") UUID perfilCandidatoId);
}
