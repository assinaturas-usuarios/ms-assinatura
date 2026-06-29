package com.empresa.assinatura.infrastructure.adapter.out.cache;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Implementação do contrato de cache para assinaturas utilizando Redis.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssinaturaCacheAdapter implements AssinaturaCachePort {

  private static final Duration TTL = Duration.ofMinutes(5);
  private static final String PREFIXO = "assinatura:";

  private final ReactiveRedisTemplate<String, AssinaturaResponse> redisTemplate;

  @Override
  public Mono<Void> armazenar(UUID assinaturaId, AssinaturaResponse assinatura) {
    String chave = PREFIXO + assinaturaId;
    return redisTemplate.opsForValue().set(chave, assinatura, TTL)
        .doOnSuccess(r -> log.info("Assinatura armazenada no cache: {}", assinaturaId))
        .then();
  }

  @Override
  public Mono<AssinaturaResponse> buscar(UUID assinaturaId) {
    String chave = PREFIXO + assinaturaId;
    return redisTemplate.opsForValue().get(chave)
        .doOnNext(r -> log.info("Cache hit: {}", assinaturaId));
  }

  @Override
  public Mono<Void> invalidar(UUID assinaturaId) {
    String chave = PREFIXO + assinaturaId;
    return redisTemplate.delete(chave)
        .doOnSuccess(r -> log.info("Cache invalidado: {}", assinaturaId))
        .then();
  }
}
