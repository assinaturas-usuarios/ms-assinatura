package com.empresa.assinatura.domain.port.in;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Interface que define o caso de uso para cancelar uma assinatura.
 */
public interface CancelarAssinaturaUseCase {

  /**
   * Cancela a assinatura informada, mantendo o acesso até o fim do ciclo.
   *
   * @param id identificador da assinatura a ser cancelada
   * @return assinatura atualizada
   */
  Mono<AssinaturaResponse> cancelar(UUID id);
}
