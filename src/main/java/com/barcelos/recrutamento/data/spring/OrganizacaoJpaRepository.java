package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.OrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrganizacaoJpaRepository extends JpaRepository<OrganizacaoEntity, UUID> {
}
