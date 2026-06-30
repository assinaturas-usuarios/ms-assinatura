package com.empresa.assinatura.infrastructure.adapter.out.kafka;

import com.empresa.assinatura.application.dto.RenovacaoSolicitadaEvent;
import com.empresa.assinatura.domain.port.out.AssinaturaProducerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Implementação do contrato de publicação de eventos relacionados a assinaturas no Kafka.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssinaturaProducer implements AssinaturaProducerPort {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${kafka.topics.renovacao-solicitada}")
  private String topicoRenovacao;

  @Value("${kafka.topics.assinatura-cancelada}")
  private String topicoAssinaturaCancelada;

  @Value("${kafka.topics.assinatura-suspensa}")
  private String topicoAssinaturaSuspensa;

  @Override
  public Mono<Void> publicarRenovacaoSolicitada(RenovacaoSolicitadaEvent evento) {
    return Mono.fromRunnable(() -> {
          kafkaTemplate.send(topicoRenovacao, evento.assinaturaId().toString(), evento);
          log.info("Enviado evento de renovação de assinatura ao Kafka com sucesso: {}",
          evento.assinaturaId()); }
    ).subscribeOn(Schedulers.boundedElastic()).then();
  }

  @Override
  public Mono<Void> publicarAssinaturaCancelada(String assinaturaId) {
    return Mono.fromRunnable(() -> {
          kafkaTemplate.send(topicoAssinaturaCancelada, assinaturaId, assinaturaId);
          log.info("Enviado evento de assinatura cancelada ao Kafka com sucesso: {}", assinaturaId);
        }
    ).subscribeOn(Schedulers.boundedElastic()).then();
  }

  @Override
  public Mono<Void> publicarAssinaturaSuspensa(String assinaturaId) {
    return Mono.fromRunnable(() -> {
          kafkaTemplate.send(topicoAssinaturaSuspensa, assinaturaId, assinaturaId);
          log.info("Enviado evento de assinatura suspensa ao Kafka com sucesso: {}", assinaturaId);
        }
    ).subscribeOn(Schedulers.boundedElastic()).then();
  }
}
