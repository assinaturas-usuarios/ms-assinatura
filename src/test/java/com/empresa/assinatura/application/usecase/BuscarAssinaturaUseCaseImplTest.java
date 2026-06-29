package com.empresa.assinatura.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BuscarAssinaturaUseCaseImpl")
class BuscarAssinaturaUseCaseImplTest {

  private static final LocalDate DATA_FIXA = LocalDate.of(2026, Month.JUNE, 28);
  @Mock
  private AssinaturaRepositoryPort repository;
  @Mock
  private AssinaturaCachePort cache;
  @Mock
  private AssinaturaMapper mapper;
  @InjectMocks
  private BuscarAssinaturaUseCaseImpl useCase;
  private UUID assinaturaId;
  private Assinatura assinatura;
  private AssinaturaResponse response;

  @BeforeEach
  void setUp() {
    assinaturaId = UUID.randomUUID();
    assinatura = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);
    response = new AssinaturaResponse(assinaturaId, assinatura.getUsuarioId(),
        Plano.PREMIUM, DATA_FIXA, DATA_FIXA.plusMonths(1),
        StatusAssinatura.ATIVA, 0);
  }

  @Nested
  @DisplayName("buscarPorId")
  class BuscarPorId {

    @Test
    @DisplayName("Cache hit: retorna do cache sem ir ao banco")
    void deveBuscarDoCacheQuandoHit() {
      given(cache.buscar(assinaturaId)).willReturn(Mono.just(response));
      // switchIfEmpty avalia o argumento de forma eager — precisa de stub mesmo sem usar
      given(repository.buscarPorId(assinaturaId)).willReturn(Mono.empty());

      StepVerifier.create(useCase.buscarPorId(assinaturaId))
          .expectNext(response)
          .verifyComplete();
    }

    @Test
    @DisplayName("Cache miss: busca no banco e armazena no cache")
    void deveBuscarNoBancoQuandoCacheMiss() {
      given(cache.buscar(assinaturaId)).willReturn(Mono.empty());
      given(repository.buscarPorId(assinaturaId)).willReturn(Mono.just(assinatura));
      given(mapper.toResponse(assinatura)).willReturn(response);
      given(cache.armazenar(any(), any())).willReturn(Mono.empty());

      StepVerifier.create(useCase.buscarPorId(assinaturaId))
          .expectNext(response)
          .verifyComplete();
    }

    @Test
    @DisplayName("Cache miss e banco vazio: erro AssinaturaNaoEncontradaException")
    void deveEmitirErroQuandoNaoEncontrada() {
      given(cache.buscar(assinaturaId)).willReturn(Mono.empty());
      given(repository.buscarPorId(assinaturaId)).willReturn(Mono.empty());

      StepVerifier.create(useCase.buscarPorId(assinaturaId))
          .expectError(AssinaturaNaoEncontradaException.class)
          .verify();
    }
  }

  @Nested
  @DisplayName("buscarAtivaDoUsuario")
  class BuscarAtivaDoUsuario {

    @Test
    @DisplayName("Deve retornar assinatura ativa do usuario")
    void deveRetornarAssinaturaAtiva() {
      UUID usuarioId = UUID.randomUUID();
      given(repository.buscarAtivaDoUsuario(usuarioId)).willReturn(Mono.just(assinatura));
      given(mapper.toResponse(assinatura)).willReturn(response);

      StepVerifier.create(useCase.buscarAtivaDoUsuario(usuarioId))
          .expectNext(response)
          .verifyComplete();
    }

    @Test
    @DisplayName("Deve emitir erro quando usuario nao tem assinatura ativa")
    void deveEmitirErroQuandoNaoHaAssinaturaAtiva() {
      UUID usuarioId = UUID.randomUUID();
      given(repository.buscarAtivaDoUsuario(usuarioId)).willReturn(Mono.empty());

      StepVerifier.create(useCase.buscarAtivaDoUsuario(usuarioId))
          .expectError(AssinaturaNaoEncontradaException.class)
          .verify();
    }
  }

  @Nested
  @DisplayName("listar")
  class Listar {

    @Test
    @DisplayName("Listar sem próxima página quando tiver menos itens ou igual ao tamanho")
    void deveListarSemProximaPagina() {
      Assinatura a1 = Assinatura.nova(UUID.randomUUID(), Plano.BASICO);
      AssinaturaResponse r1 = new AssinaturaResponse(UUID.randomUUID(), UUID.randomUUID(),
          Plano.BASICO, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);
      given(repository.listar(any(), any(), any(), anyInt())).willReturn(Flux.just(a1));
      given(mapper.toResponse(a1)).willReturn(r1);

      StepVerifier.create(useCase.listar(null, null, null, 2))
          .assertNext(pagina -> {
            assertThat(pagina.itens()).hasSize(1);
            assertThat(pagina.temMais()).isFalse();
            assertThat(pagina.proximoCursor()).isNull();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("Listar com próxima página tem mais itens do que tamanho atual")
    void deveListarComProximaPagina() {
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();
      UUID id3 = UUID.randomUUID();
      Assinatura a1 = Assinatura.nova(UUID.randomUUID(), Plano.BASICO);
      Assinatura a2 = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);
      Assinatura a3 = Assinatura.nova(UUID.randomUUID(), Plano.FAMILIA);
      AssinaturaResponse r1 = new AssinaturaResponse(id1, UUID.randomUUID(),
          Plano.BASICO, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);
      AssinaturaResponse r2 = new AssinaturaResponse(id2, UUID.randomUUID(),
          Plano.PREMIUM, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);
      AssinaturaResponse r3 = new AssinaturaResponse(id3, UUID.randomUUID(),
          Plano.FAMILIA, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);
      given(repository.listar(any(), any(), any(), anyInt())).willReturn(Flux.just(a1, a2, a3));
      given(mapper.toResponse(a1)).willReturn(r1);
      given(mapper.toResponse(a2)).willReturn(r2);
      given(mapper.toResponse(a3)).willReturn(r3);

      StepVerifier.create(useCase.listar(null, null, null, 2))
          .assertNext(pagina -> {
            assertThat(pagina.itens()).hasSize(2);
            assertThat(pagina.temMais()).isTrue();
            assertThat(pagina.proximoCursor()).isEqualTo(id2.toString());
          })
          .verifyComplete();
    }
  }
}