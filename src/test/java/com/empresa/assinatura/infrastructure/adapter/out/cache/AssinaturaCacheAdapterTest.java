package com.empresa.assinatura.infrastructure.adapter.out.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssinaturaCacheAdapterTest {

  @Mock
  private ReactiveRedisTemplate<String, AssinaturaResponse> redisTemplate;

  @Mock
  private ReactiveValueOperations<String, AssinaturaResponse> valueOps;

  private AssinaturaCacheAdapter cacheAdapter;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    cacheAdapter = new AssinaturaCacheAdapter(redisTemplate);
  }

  @Test
  void deveArmazenarNoCache() {
    UUID id = UUID.randomUUID();
    AssinaturaResponse response = new AssinaturaResponse(
        id, UUID.randomUUID(), Plano.BASICO,
        LocalDate.now(), LocalDate.now().plusMonths(1),
        StatusAssinatura.ATIVA, 0);
    when(valueOps.set(anyString(), any(), any())).thenReturn(Mono.just(true));

    StepVerifier.create(cacheAdapter.armazenar(id, response))
        .verifyComplete();

    verify(valueOps).set(anyString(), any(), any());
  }

  @Test
  void deveBuscarDoCache() {
    UUID id = UUID.randomUUID();
    AssinaturaResponse response = new AssinaturaResponse(
        id, UUID.randomUUID(), Plano.BASICO,
        LocalDate.now(), LocalDate.now().plusMonths(1),
        StatusAssinatura.ATIVA, 0);
    when(valueOps.get(anyString())).thenReturn(Mono.just(response));

    StepVerifier.create(cacheAdapter.buscar(id))
        .expectNext(response)
        .verifyComplete();
  }

  @Test
  void deveRetornarVazioQuandoNaoEncontrado() {
    UUID id = UUID.randomUUID();
    when(valueOps.get(anyString())).thenReturn(Mono.empty());

    StepVerifier.create(cacheAdapter.buscar(id))
        .verifyComplete();
  }

  @Test
  void deveInvalidarCache() {
    UUID id = UUID.randomUUID();
    when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

    StepVerifier.create(cacheAdapter.invalidar(id))
        .verifyComplete();

    verify(redisTemplate).delete(anyString());
  }
}