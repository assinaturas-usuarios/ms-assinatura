package com.empresa.assinatura.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import com.empresa.assinatura.domain.port.out.AssinaturaProducerPort;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoCancelaveException;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoEncontradaException;
import com.empresa.assinatura.infrastructure.mapper.AssinaturaMapper;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CancelarAssinaturaUseCaseImplTest {

  private static final LocalDate DATA_FIXA = LocalDate.of(2026, Month.JUNE, 28);
  @Mock
  private AssinaturaRepositoryPort repository;
  @Mock
  private AssinaturaCachePort cache;
  @Mock
  private AssinaturaProducerPort eventPublisher;
  @Mock
  private AssinaturaMapper mapper;
  @InjectMocks
  private CancelarAssinaturaUseCaseImpl useCase;
  private UUID id;
  private Assinatura assinatura;
  private AssinaturaResponse response;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();
    assinatura = Assinatura.nova(UUID.randomUUID(), Plano.BASICO);
    response = new AssinaturaResponse(id, assinatura.getUsuarioId(), Plano.BASICO,
        DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.CANCELADA, 0);
  }

  @Nested
  @DisplayName("Cenários de sucesso")
  class CenariosSuccesso {

    @Test
    @DisplayName("Deve cancelar assinatura ativa")
    void deveCancelarAssinaturaAtiva() {
      given(repository.buscarPorId(id)).willReturn(Mono.just(assinatura));
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(mapper.toResponse(any())).willReturn(response);
      given(cache.invalidar(any())).willReturn(Mono.empty());
      given(eventPublisher.publicarAssinaturaCancelada(any())).willReturn(Mono.empty());

      StepVerifier.create(useCase.cancelar(id))
          .expectNextMatches(r -> StatusAssinatura.CANCELADA.equals(r.status()))
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("Cenários de falha")
  class CenariosFailha {

    @Test
    @DisplayName("Deve dar erro quando assinatura não for encontrada")
    void deveEmitirErroNaoEncontrada() {
      given(repository.buscarPorId(id)).willReturn(Mono.empty());

      StepVerifier.create(useCase.cancelar(id))
          .expectError(AssinaturaNaoEncontradaException.class)
          .verify();
    }

    @Test
    @DisplayName("Deve dar erro quando assinatura já tiver sido cancelada")
    void deveEmitirErroJaCancelada() {
      assinatura.cancelar();
      given(repository.buscarPorId(id)).willReturn(Mono.just(assinatura));

      StepVerifier.create(useCase.cancelar(id))
          .expectError(AssinaturaNaoCancelaveException.class)
          .verify();
    }
  }
}
