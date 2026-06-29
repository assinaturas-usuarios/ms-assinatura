package com.empresa.assinatura.application.usecase;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.domain.port.in.CancelarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import com.empresa.assinatura.domain.port.out.AssinaturaProducerPort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoCancelaveException;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoEncontradaException;
import com.empresa.assinatura.infrastructure.mapper.AssinaturaMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Implementação do caso de uso para cancelar uma assinatura.
 *
 * <b>Event-Driven Architecture:</b> após o cancelamento, o evento {@code assinatura.cancelada} é publicado no Kafka via
 * {@code EventPublisherPort}, possibilitando notificação de outros microsserviços interessados.
 *
 * <p><b>Cache Reativo:</b> o cancelamento invalida a entrada no Redis, garantindo consistência entre banco e cache.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelarAssinaturaUseCaseImpl implements CancelarAssinaturaUseCase {

  private final AssinaturaRepositoryPort repository;
  private final AssinaturaCachePort cache;
  private final AssinaturaProducerPort eventPublish;
  private final AssinaturaMapper mapper;

  @Override
  @Transactional
  public Mono<AssinaturaResponse> cancelar(UUID id) {
    log.info("Cancelando assinatura: id={}", id);
    return repository.buscarPorId(id)
        .switchIfEmpty(assinaturaNaoEncontrada(id))
        .flatMap(this::cancelarConvertendoParaResponse)
        .flatMap(this::invalidarCacheComPublisher)
        .doOnSuccess(this::logCancelamento);
  }

  private Mono<AssinaturaResponse> cancelarConvertendoParaResponse(Assinatura assinatura) {
    return salvarCancelamentoAssinatura(assinatura).map(mapper::toResponse);
  }

  private Mono<AssinaturaResponse> invalidarCacheComPublisher(AssinaturaResponse response) {
    return cache.invalidar(response.id())
        .then(eventPublish.publicarAssinaturaCancelada(response.id().toString()))
        .thenReturn(response);
  }

  private void logCancelamento(AssinaturaResponse response) {
    log.info("Assinatura cancelada: id={}", response.id());
  }

  private <T> Mono<T> assinaturaNaoEncontrada(UUID id) {
    return Mono.error(new AssinaturaNaoEncontradaException(id));
  }

  private Mono<Assinatura> salvarCancelamentoAssinatura(Assinatura assinatura) {
    if (StatusAssinatura.CANCELADA.equals(assinatura.getStatus())) {
      return Mono.error(new AssinaturaNaoCancelaveException(assinatura.getId()));
    }
    assinatura.cancelar();
    return repository.salvar(assinatura);
  }
}
