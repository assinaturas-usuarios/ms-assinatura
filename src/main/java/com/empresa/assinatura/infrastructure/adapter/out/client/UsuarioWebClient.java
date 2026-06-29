package com.empresa.assinatura.infrastructure.adapter.out.client;

import com.empresa.assinatura.domain.port.out.UsuarioClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implementação do contrato de comunicação com o ms-usuario utilizando WebClient.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsuarioWebClient implements UsuarioClientPort {

  private final WebClient usuarioWebClient;

  @Override
  @CircuitBreaker(name = "ms-usuario", fallbackMethod = "fallbackExistePorId")
  public Mono<Boolean> existePorId(UUID usuarioId) {
    log.info("Verificando existência do usuário: {}", usuarioId);
    return usuarioWebClient.get()
        .uri("/v1/usuarios/{id}", usuarioId)
        .retrieve()
        .toBodilessEntity()
        .map(response -> response.getStatusCode().is2xxSuccessful())
        .onErrorReturn(false);
  }

  private Mono<Boolean> fallbackExistePorId(UUID usuarioId, Throwable throwable) {
    log.error("Fallback ativado para verificação de usuário: {}", usuarioId, throwable);
    return Mono.just(false);
  }
}
