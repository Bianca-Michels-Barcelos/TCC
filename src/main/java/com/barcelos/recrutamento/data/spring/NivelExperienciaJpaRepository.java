package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.NivelExperienciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NivelExperienciaJpaRepository extends JpaRepository<NivelExperienciaEntity, UUID> {
    List<NivelExperienciaEntity> findByOrganizacao_Id(UUID organizacaoId);

    boolean existsByOrganizacao_IdAndDescricaoIgnoreCase(UUID organizacaoId, String descricao);
}