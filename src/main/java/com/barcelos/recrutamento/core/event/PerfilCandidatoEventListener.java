package com.barcelos.recrutamento.core.event;

import com.barcelos.recrutamento.core.service.CompatibilidadeCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PerfilCandidatoEventListener {

    private static final Logger log = LoggerFactory.getLogger(PerfilCandidatoEventListener.class);

    private final CompatibilidadeCacheService compatibilidadeCacheService;

    public PerfilCandidatoEventListener(CompatibilidadeCacheService compatibilidadeCacheService) {
        this.compatibilidadeCacheService = compatibilidadeCacheService;
    }

    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPerfilAtualizado(PerfilCandidatoAtualizadoEvent event) {
        log.info("Evento recebido: Perfil atualizado para candidato {}", event.getCandidatoUsuarioId());
        log.info("Disparando recálculo assíncrono de compatibilidade...");
        

        compatibilidadeCacheService.calcularParaTodasVagas(event.getCandidatoUsuarioId());
    }
}
