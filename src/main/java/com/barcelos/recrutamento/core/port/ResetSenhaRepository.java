package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.ResetSenha;
import com.barcelos.recrutamento.core.model.StatusResetSenha;

import java.util.Optional;
import java.util.UUID;

public interface ResetSenhaRepository {

    
    ResetSenha save(ResetSenha resetSenha);

    
    Optional<ResetSenha> findByToken(String token);

    
    Optional<ResetSenha> findByUsuarioIdAndStatus(UUID usuarioId, StatusResetSenha status);
}

