package com.empresa.assinatura.domain.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Interface que define o contrato para comunicação com o ms-usuario.
 */
public interface UsuarioClientPort {

  /**
   * Verifica se o usuário existe no ms-usuario.
   *
   * @param usuarioId identificador do usuário
   * @return Mono com true se existir, false caso contrário
   */
  Mono<Boolean> existePorId(UUID usuarioId);
}
