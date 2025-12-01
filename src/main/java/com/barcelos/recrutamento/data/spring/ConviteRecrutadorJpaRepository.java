package com.barcelos.recrutamento.data.spring;

import com.barcelos.recrutamento.data.entity.ConviteRecrutadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConviteRecrutadorJpaRepository extends JpaRepository<ConviteRecrutadorEntity, UUID> {

    Optional<ConviteRecrutadorEntity> findByToken(String token);

    List<ConviteRecrutadorEntity> findByEmail(String email);

    List<ConviteRecrutadorEntity> findByOrganizacaoId(UUID organizacaoId);

    @Query("SELECT c FROM ConviteRecrutadorEntity c WHERE c.organizacaoId = :organizacaoId AND CAST(c.status AS string) = :status")
    List<ConviteRecrutadorEntity> findByOrganizacaoIdAndStatus(@Param("organizacaoId") UUID organizacaoId, @Param("status") String status);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ConviteRecrutadorEntity c " +
           "WHERE c.email = :email AND c.organizacaoId = :organizacaoId AND CAST(c.status AS string) = :status")
    boolean existsByEmailAndOrganizacaoIdAndStatus(@Param("email") String email, @Param("organizacaoId") UUID organizacaoId, @Param("status") String status);
}
