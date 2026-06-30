package com.empresa.assinatura.infrastructure.adapter.in.kafka;

import com.empresa.assinatura.application.dto.PagamentoResultadoEvent;
import com.empresa.assinatura.domain.port.in.ProcessarResultadoPagamentoUseCase;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Componente que consome eventos de resultado de pagamento do Kafka e delega o processamento para o
 * caso de uso apropriado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PagamentoResultadoConsumer {

  private final ProcessarResultadoPagamentoUseCase useCase;

  /**
   * Método que consome eventos de resultado de pagamento do Kafka.
   *
   * @param evento evento com o resultado do pagamento
   * @param ack    objeto para confirmação de processamento
   */
  @KafkaListener(
      topics = "${kafka.topics.pagamento-resultado}",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void consumir(@Payload PagamentoResultadoEvent evento, Acknowledgment ack) {
    log.info("Recebendo resultado de pagamento via Consumer Kafka: assinaturaId={}", evento.assinaturaId());
    useCase.processar(evento)
        .doOnSuccess(_ -> {
          ack.acknowledge();
          log.info("Resultado processado e ack confirmado: assinaturaId={}", evento.assinaturaId());
        })
        .doOnError(
            erro -> log.error("Erro ao processar resultado: assinaturaId={}", evento.assinaturaId(),
                erro))
        .contextWrite(context -> ContextSnapshotFactory.builder().build().captureAll().updateContext(context))
        .subscribe();
  }
}
