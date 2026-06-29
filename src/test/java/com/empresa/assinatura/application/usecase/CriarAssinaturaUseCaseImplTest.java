package com.empresa.assinatura.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CriarAssinaturaRequest;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import com.empresa.assinatura.domain.port.out.UsuarioClientPort;
import com.empresa.assinatura.infrastructure.exception.AssinaturaJaAtivaException;
import com.empresa.assinatura.infrastructure.exception.UsuarioNaoEncontradoException;
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
class CriarAssinaturaUseCaseImplTest {

  private static final LocalDate DATA_FIXA = LocalDate.of(2026, Month.JUNE, 28);
  @Mock
  private AssinaturaRepositoryPort repository;
  @Mock
  private UsuarioClientPort usuarioClient;
  @Mock
  private AssinaturaCachePort cache;
  @Mock
  private AssinaturaMapper mapper;
  @InjectMocks
  private CriarAssinaturaUseCaseImpl useCase;
  private UUID usuarioId;
  private CriarAssinaturaRequest request;
  private Assinatura assinatura;
  private AssinaturaResponse response;

  @BeforeEach
  void setUp() {
    usuarioId = UUID.randomUUID();
    request = new CriarAssinaturaRequest(usuarioId, "PREMIUM");
    assinatura = Assinatura.nova(usuarioId, Plano.PREMIUM);
    response = new AssinaturaResponse(assinatura.getId(), usuarioId, Plano.PREMIUM,
        DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);
  }

  @Nested
  @DisplayName("Cenários de sucesso")
  class CenariosSuccesso {

    @Test
    @DisplayName("Deve criar assinatura quando usuário existe e não tem assinatura ativa")
    void deveCriarAssinatura() {
      given(usuarioClient.existePorId(usuarioId)).willReturn(Mono.just(true));
      given(repository.existeAssinaturaAtiva(usuarioId)).willReturn(Mono.just(false));
      given(repository.salvar(any())).willReturn(Mono.just(assinatura));
      given(mapper.toResponse(assinatura)).willReturn(response);
      given(cache.armazenar(any(), any())).willReturn(Mono.empty());

      StepVerifier.create(useCase.criar(request))
          .expectNextMatches(r -> r.usuarioId().equals(usuarioId) && r.plano() == Plano.PREMIUM)
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("Cenários de falha")
  class CenariosFailha {

    @Test
    @DisplayName("Deve dar erro se usuário não existir")
    void deveEmitirErroUsuarioNaoExistente() {
      given(usuarioClient.existePorId(usuarioId)).willReturn(Mono.just(false));

      StepVerifier.create(useCase.criar(request))
          .expectError(UsuarioNaoEncontradoException.class)
          .verify();
    }

    @Test
    @DisplayName("Deve dar erro se usuário já tiver assinatura ativa")
    void deveEmitirErroAssinaturaJaAtiva() {
      given(usuarioClient.existePorId(usuarioId)).willReturn(Mono.just(true));
      given(repository.existeAssinaturaAtiva(usuarioId)).willReturn(Mono.just(true));

      StepVerifier.create(useCase.criar(request))
          .expectError(AssinaturaJaAtivaException.class)
          .verify();
    }
  }
}
