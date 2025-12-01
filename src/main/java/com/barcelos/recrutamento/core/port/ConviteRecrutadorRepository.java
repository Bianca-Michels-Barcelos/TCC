package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.ConviteRecrutador;
import com.barcelos.recrutamento.core.model.StatusConvite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConviteRecrutadorRepository {

    
    ConviteRecrutador save(ConviteRecrutador convite);

    
    Optional<ConviteRecrutador> findById(UUID id);

    
    Optional<ConviteRecrutador> findByToken(String token);

    
    boolean existsByEmailAndOrganizacaoAndStatus(String email, UUID organizacaoId, StatusConvite status);
}
