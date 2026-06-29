package com.empresa.assinatura.infrastructure.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.port.in.RenovarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RenovacaoAgendada")
class RenovacaoAgendadaSchedulerTest {

  @Mock
  private AssinaturaRepositoryPort repository;

  @Mock
  private RenovarAssinaturaUseCase renovarUseCase;

  private RenovacaoAgendadaScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new RenovacaoAgendadaScheduler(repository, renovarUseCase, new SimpleMeterRegistry());
  }

  @Nested
  @DisplayName("processarRenovacoes")
  class ProcessarRenovacoes {

    @Test
    @DisplayName("Deve delegar renovação ao use case para cada assinatura encontrada")
    void deveDelegarRenovacaoAoUseCase() {
      var assinatura = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);
      given(repository.buscarParaRenovar(any())).willReturn(Flux.just(assinatura));
      given(renovarUseCase.renovar(any())).willReturn(Mono.empty());

      scheduler.processarRenovacoes();

      verify(repository).buscarParaRenovar(any());
      verify(renovarUseCase).renovar(assinatura);
    }

    @Test
    @DisplayName("Deve concluir sem chamar use case quando não há assinaturas")
    void deveEncerrarSemProcessarQuandoListaVazia() {
      given(repository.buscarParaRenovar(any())).willReturn(Flux.empty());

      scheduler.processarRenovacoes();

      verify(repository).buscarParaRenovar(any());
      verify(renovarUseCase, never()).renovar(any());
    }

    @Test
    @DisplayName("Deve continuar processando demais assinaturas quando uma falha")
    void deveContinuarQuandoUmaRenovacaoFalha() {
      var a1 = Assinatura.nova(UUID.randomUUID(), Plano.BASICO);
      var a2 = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);
      given(repository.buscarParaRenovar(any())).willReturn(Flux.just(a1, a2));
      given(renovarUseCase.renovar(any()))
          .willReturn(Mono.error(new RuntimeException("falha simulada")))
          .willReturn(Mono.empty());

      scheduler.processarRenovacoes();

      verify(renovarUseCase, times(2)).renovar(any());
    }
  }
}