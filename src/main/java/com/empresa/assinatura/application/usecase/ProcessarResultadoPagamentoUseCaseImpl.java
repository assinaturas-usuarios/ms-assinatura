package com.empresa.assinatura.application.usecase;

import com.empresa.assinatura.application.dto.PagamentoResultadoEvent;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.port.in.ProcessarResultadoPagamentoUseCase;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import com.empresa.assinatura.domain.port.out.AssinaturaProducerPort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Implementação do caso de uso para processar o resultado de um pagamento.
 */
@Service
@Slf4j
public class ProcessarResultadoPagamentoUseCaseImpl implements ProcessarResultadoPagamentoUseCase {

  public static final String APROVADO = "APROVADO";
  public static final String RECUSADO = "RECUSADO";
  private final AssinaturaRepositoryPort repository;
  private final AssinaturaCachePort cache;
  private final AssinaturaProducerPort assinaturaProducer;
  private final Counter contadorMeterRenovacoesAprovadas;
  private final Counter contadorMeterRenovacoesRecusadas;
  private final Counter contadorMeterAssinaturasSuspensas;

  /**
   * Construtor da classe.
   *
   * @param repository    porta de repositório de assinaturas
   * @param cache          porta de cache de assinaturas
   * @param assinaturaProducer porta de publicação de eventos
   * @param meterRegistry  registro de métricas
   */
  public ProcessarResultadoPagamentoUseCaseImpl(AssinaturaRepositoryPort repository,
      AssinaturaCachePort cache,
      AssinaturaProducerPort assinaturaProducer,
      MeterRegistry meterRegistry) {
    this.repository = repository;
    this.cache = cache;
    this.assinaturaProducer = assinaturaProducer;
    this.contadorMeterRenovacoesAprovadas = Counter.builder("assinatura.renovacoes.resultado")
        .tag("resultado", APROVADO)
        .description("Total de renovações aprovadas")
        .register(meterRegistry);
    this.contadorMeterRenovacoesRecusadas = Counter.builder("assinatura.renovacoes.resultado")
        .tag("resultado", RECUSADO)
        .description("Total de renovações recusadas")
        .register(meterRegistry);
    this.contadorMeterAssinaturasSuspensas = Counter.builder("assinatura.suspensas.total")
        .description("Total de assinaturas suspensas por falha de renovação")
        .register(meterRegistry);
  }

  @Override
  @Transactional
  public Mono<Void> processar(PagamentoResultadoEvent evento) {
    log.info("Processando resultado de pagamento: assinaturaId={}, status={}",
        evento.assinaturaId(), evento.status());
    return repository.buscarPorId(evento.assinaturaId())
        .flatMap(assinatura -> renovarAssinatura(evento, assinatura));
  }

  private Mono<Void> renovarAssinatura(PagamentoResultadoEvent evento, Assinatura assinatura) {
    if (APROVADO.equals(evento.status())) {
      return tratarAprovacaoAssinatura(assinatura);
    }
    return registrarFalhaRenovacaoAssinatura(assinatura);
  }

  private Mono<Void> tratarAprovacaoAssinatura(Assinatura assinatura) {
    assinatura.renovar();
    contadorMeterRenovacoesAprovadas.increment();
    log.info("Assinatura renovada com sucesso: id={}", assinatura.getId());
    return salvarAssinaturaInvalidandoCache(assinatura);
  }

  private Mono<Void> registrarFalhaRenovacaoAssinatura(Assinatura assinatura) {
    assinatura.registrarFalhaRenovacao();
    contadorMeterRenovacoesRecusadas.increment();
    log.warn("Falha na renovação: assinaturaId={}, tentativas={}", assinatura.getId(),
        assinatura.getTentativasRenovacao());
    return tratarFalhaRenovacao(assinatura);
  }

  private Mono<Void> tratarFalhaRenovacao(Assinatura assinatura) {
    if (assinatura.atingiuLimiteTentativas()) {
      return suspenderAssinaturaAposTentativasRenovacao(assinatura);
    }
    return salvarAssinaturaInvalidandoCache(assinatura);
  }

  private Mono<Void> salvarAssinaturaInvalidandoCache(Assinatura assinatura) {
    return repository.salvar(assinatura)
        .flatMap(salva -> cache.invalidar(salva.getId()));
  }

  private Mono<Void> suspenderAssinaturaAposTentativasRenovacao(Assinatura assinatura) {
    assinatura.suspender();
    contadorMeterAssinaturasSuspensas.increment();
    log.warn("Assinatura suspensa após limite de tentativas: id={}", assinatura.getId());
    return salvarAssinaturaInvalidandoCache(assinatura)
        .then(assinaturaProducer.publicarAssinaturaSuspensa(assinatura.getId().toString()));
  }
}

