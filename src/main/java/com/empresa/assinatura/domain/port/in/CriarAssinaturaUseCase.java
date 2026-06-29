package com.empresa.assinatura.domain.port.in;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CriarAssinaturaRequest;
import reactor.core.publisher.Mono;

/**
 * Interface que define o caso de uso para criar uma nova assinatura.
 */
public interface CriarAssinaturaUseCase {

  /**
   * Cria uma nova assinatura para o usuário informado.
   *
   * @param request dados da assinatura a ser criada
   * @return assinatura criada
   */
  Mono<AssinaturaResponse> criar(CriarAssinaturaRequest request);
}
