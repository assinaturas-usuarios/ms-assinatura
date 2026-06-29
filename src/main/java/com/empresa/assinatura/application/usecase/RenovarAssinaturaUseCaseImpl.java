package com.empresa.assinatura.application.usecase;

import com.empresa.assinatura.application.dto.RenovacaoSolicitadaEvent;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.port.in.RenovarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.out.AssinaturaProducerPort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementação do caso de uso para renovar uma assinatura, cuja arquitetura hexagonal desconhece
 * detalhes de infraestrutura como R2DBC ou Kafka.
 *
 * <p><b> WebFlux Reativo:</b> permitindo encadear persistência e publicação de evento sem bloquear 
 * threads. O único {@code block()} legítimo está no {@code RenovacaoAgendadaScheduler}, 
 * dentro de virtual threads.
 *
 * <p><b>Micrometer:</b> o contador {@code assinatura.renovacoes.iniciadas} é incrementado via 
 * {@code doOnSuccess}, garantindo a contagem apenas de renovações efetivamente concluídas para 
 * métricas de monitoramento.
 */
@Service
@Slf4j
public class RenovarAssinaturaUseCaseImpl implements RenovarAssinaturaUseCase {

  private final AssinaturaRepositoryPort repository;
  private final AssinaturaProducerPort assinaturaProducer;
  private final Counter contadorMeterRenovacoesIniciadas;

  /**
   * Construtor da classe.
   *
   * @param repository        porta de repositório de assinaturas
   * @param assinaturaProducer porta de publicação de eventos
   * @param meterRegistry      registro de métricas
   */
  public RenovarAssinaturaUseCaseImpl(AssinaturaRepositoryPort repository,
      AssinaturaProducerPort assinaturaProducer,
      MeterRegistry meterRegistry) {
    this.repository = repository;
    this.assinaturaProducer = assinaturaProducer;
    this.contadorMeterRenovacoesIniciadas = Counter.builder("assinatura.renovacoes.iniciadas")
        .description("Total de renovações iniciadas pelo agendador")
        .register(meterRegistry);
  }

  @Override
  public Mono<Void> renovar(Assinatura assinatura) {
    assinatura.iniciarRenovacao();
    return repository.salvar(assinatura)
        .flatMap(this::publicarContabilizandoRenovacao);
  }

  private Mono<Void> publicarContabilizandoRenovacao(Assinatura salva) {
    var evento = new RenovacaoSolicitadaEvent(
        salva.getId(), salva.getUsuarioId(),
        salva.getPlano().name(), salva.getPlano().getValor());
    return assinaturaProducer.publicarRenovacaoSolicitada(evento)
        .doOnSuccess(_ -> {
          contadorMeterRenovacoesIniciadas.increment();
          log.info("Renovação iniciada: assinaturaId={}", salva.getId());
        });
  }
}
