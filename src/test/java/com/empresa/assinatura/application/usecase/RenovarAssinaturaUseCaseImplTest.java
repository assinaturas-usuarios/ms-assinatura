package com.empresa.assinatura.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.empresa.assinatura.application.dto.RenovacaoSolicitadaEvent;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import com.empresa.assinatura.domain.port.out.AssinaturaProducerPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("RenovarAssinaturaUseCaseImpl")
class RenovarAssinaturaUseCaseImplTest {

  @Mock
  private AssinaturaRepositoryPort repository;
  @Mock
  private AssinaturaProducerPort eventoPublicador;

  private SimpleMeterRegistry meterRegistry;
  private RenovarAssinaturaUseCaseImpl useCase;
  private Assinatura assinatura;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    useCase = new RenovarAssinaturaUseCaseImpl(repository, eventoPublicador, meterRegistry);
    assinatura = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);
  }

  @Nested
  @DisplayName("renovar")
  class Renovar {

    @Test
    @DisplayName("Deve salvar, publicar evento e completar com sucesso")
    void deveRenovarComSucesso() {
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(eventoPublicador.publicarRenovacaoSolicitada(any())).willReturn(Mono.empty());

      StepVerifier.create(useCase.renovar(assinatura))
          .verifyComplete();

      verify(repository).salvar(assinatura);
      verify(eventoPublicador).publicarRenovacaoSolicitada(any(RenovacaoSolicitadaEvent.class));
    }

    @Test
    @DisplayName("Deve alterar status da assinatura para AGUARDANDO_RENOVACAO")
    void deveAlterarStatusParaAguardandoRenovacao() {
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(eventoPublicador.publicarRenovacaoSolicitada(any())).willReturn(Mono.empty());

      useCase.renovar(assinatura).block();

      assertThat(assinatura.getStatus()).isEqualTo(StatusAssinatura.AGUARDANDO_RENOVACAO);
    }

    @Test
    @DisplayName("Deve incrementar contador de renovacoes iniciadas apos sucesso")
    void deveIncrementarContadorAoRenovarComSucesso() {
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(eventoPublicador.publicarRenovacaoSolicitada(any())).willReturn(Mono.empty());

      useCase.renovar(assinatura).block();

      double total = meterRegistry.counter("assinatura.renovacoes.iniciadas").count();
      assertThat(total).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Deve propagar erro quando repositorio falhar ao salvar")
    void deveEmitirErroQuandoSalvarFalhar() {
      given(repository.salvar(any()))
          .willReturn(Mono.error(new RuntimeException("erro de banco")));

      StepVerifier.create(useCase.renovar(assinatura))
          .expectErrorMessage("erro de banco")
          .verify();

      verify(eventoPublicador, never()).publicarRenovacaoSolicitada(any());
    }

    @Test
    @DisplayName("Deve propagar erro quando publicacao do evento falhar")
    void deveEmitirErroQuandoPublicarFalhar() {
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(eventoPublicador.publicarRenovacaoSolicitada(any()))
          .willReturn(Mono.error(new RuntimeException("erro de kafka")));

      StepVerifier.create(useCase.renovar(assinatura))
          .expectErrorMessage("erro de kafka")
          .verify();
    }

    @Test
    @DisplayName("Nao deve incrementar contador quando publicacao falhar")
    void naoDeveIncrementarContadorQuandoPublicarFalhar() {
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(eventoPublicador.publicarRenovacaoSolicitada(any()))
          .willReturn(Mono.error(new RuntimeException("erro de kafka")));

      useCase.renovar(assinatura).onErrorComplete().block();

      double total = meterRegistry.counter("assinatura.renovacoes.iniciadas").count();
      assertThat(total).isZero();
    }
  }
}
