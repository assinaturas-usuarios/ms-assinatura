package com.empresa.assinatura.domain.port.in;

import com.empresa.assinatura.application.dto.PagamentoResultadoEvent;
import reactor.core.publisher.Mono;

/**
 * Interface que define o caso de uso para processar o resultado de um pagamento.
 */
public interface ProcessarResultadoPagamentoUseCase {

  /**
   * Processa o resultado de um pagamento recebido do ms-pagamento.
   *
   * @param evento evento com o resultado do pagamento
   * @return Mono vazio após o processamento
   */
  Mono<Void> processar(PagamentoResultadoEvent evento);
}
