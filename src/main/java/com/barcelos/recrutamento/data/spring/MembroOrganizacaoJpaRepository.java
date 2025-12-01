package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.MembroOrganizacaoEntity;
import com.barcelos.recrutamento.data.entity.MembroOrganizacaoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembroOrganizacaoJpaRepository extends JpaRepository<MembroOrganizacaoEntity, MembroOrganizacaoId> {
    boolean existsByOrganizacaoIdAndUsuarioId(UUID organizacaoId, UUID usuarioId);
    Optional<MembroOrganizacaoEntity> findByOrganizacaoIdAndUsuarioId(UUID organizacaoId, UUID usuarioId);
    List<MembroOrganizacaoEntity> findByOrganizacaoId(UUID organizacaoId);
    List<MembroOrganizacaoEntity> findByUsuarioId(UUID usuarioId);
    void deleteByOrganizacaoIdAndUsuarioId(UUID organizacaoId, UUID usuarioId);
}