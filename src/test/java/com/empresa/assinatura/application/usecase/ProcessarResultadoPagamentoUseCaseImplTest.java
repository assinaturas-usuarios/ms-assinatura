package com.empresa.assinatura.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.empresa.assinatura.application.dto.PagamentoResultadoEvent;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
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
class ProcessarResultadoPagamentoUseCaseImplTest {

  @Mock
  private AssinaturaRepositoryPort repository;
  @Mock
  private AssinaturaCachePort cache;
  @Mock
  private AssinaturaProducerPort assinaturaProducer;

  private ProcessarResultadoPagamentoUseCaseImpl useCase;

  private UUID assinaturaId;
  private Assinatura assinatura;

  @BeforeEach
  void setUp() {
    useCase = new ProcessarResultadoPagamentoUseCaseImpl(repository, cache, assinaturaProducer,
        new SimpleMeterRegistry());
    assinaturaId = UUID.randomUUID();
    assinatura = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);
  }

  @Nested
  @DisplayName("Pagamento aprovado")
  class PagamentoAprovado {

    @Test
    @DisplayName("Deve renovar assinatura quando pagamento aprovado")
    void deveRenovarAssinatura() {
      PagamentoResultadoEvent evento = new PagamentoResultadoEvent(assinaturaId, UUID.randomUUID(),
          "APROVADO");
      given(repository.buscarPorId(assinaturaId)).willReturn(Mono.just(assinatura));
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(cache.invalidar(any())).willReturn(Mono.empty());

      StepVerifier.create(useCase.processar(evento))
          .verifyComplete();

      verify(repository).salvar(any());
    }
  }

  @Nested
  @DisplayName("Pagamento recusado")
  class PagamentoRecusado {

    @Test
    @DisplayName("Deve suspender assinatura apos 3 tentativas falhas")
    void deveSuspenderAposTresFalhas() {
      for (int i = 0; i < 3; i++) {
        assinatura.registrarFalhaRenovacao();
      }
      PagamentoResultadoEvent evento = new PagamentoResultadoEvent(assinaturaId, UUID.randomUUID(),
          "RECUSADO");
      given(repository.buscarPorId(assinaturaId)).willReturn(Mono.just(assinatura));
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(cache.invalidar(any())).willReturn(Mono.empty());
      given(assinaturaProducer.publicarAssinaturaSuspensa(any())).willReturn(Mono.empty());

      StepVerifier.create(useCase.processar(evento))
          .verifyComplete();

      verify(assinaturaProducer).publicarAssinaturaSuspensa(any());
    }
  }
}