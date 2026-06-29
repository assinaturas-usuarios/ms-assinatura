package com.empresa.assinatura.domain.port.out;

import com.empresa.assinatura.application.dto.RenovacaoSolicitadaEvent;
import reactor.core.publisher.Mono;

/**
 * Interface que define o contrato para publicação de eventos relacionados a assinaturas.
 */
public interface AssinaturaProducerPort {

  /**
   * Publica evento de renovação solicitada no Kafka.
   *
   * @param evento dados da renovação
   * @return Mono vazio após publicação
   */
  Mono<Void> publicarRenovacaoSolicitada(RenovacaoSolicitadaEvent evento);

  /**
   * Publica evento de assinatura cancelada no Kafka.
   *
   * @param assinaturaId identificador da assinatura cancelada
   * @return Mono vazio após publicação
   */
  Mono<Void> publicarAssinaturaCancelada(String assinaturaId);

  /**
   * Publica evento de assinatura suspensa no Kafka.
   *
   * @param assinaturaId identificador da assinatura suspensa
   * @return Mono vazio após publicação
   */
  Mono<Void> publicarAssinaturaSuspensa(String assinaturaId);
}
