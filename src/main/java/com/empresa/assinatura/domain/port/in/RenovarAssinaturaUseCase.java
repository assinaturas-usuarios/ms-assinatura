package com.empresa.assinatura.domain.port.in;

import com.empresa.assinatura.domain.model.Assinatura;
import reactor.core.publisher.Mono;

/**
 * Interface que define o caso de uso para renovar uma assinatura.
 */
public interface RenovarAssinaturaUseCase {

  /**
   * Inicia a renovação da assinatura informada e publica o evento correspondente.
   *
   * @param assinatura assinatura a ser renovada
   * @return Mono vazio após conclusão
   */
  Mono<Void> renovar(Assinatura assinatura);
}
