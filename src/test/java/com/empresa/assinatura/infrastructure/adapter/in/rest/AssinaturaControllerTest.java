package com.empresa.assinatura.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CriarAssinaturaRequest;
import com.empresa.assinatura.application.dto.CursorListaResponse;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.domain.port.in.BuscarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.in.CancelarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.in.CriarAssinaturaUseCase;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
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
@DisplayName("AssinaturaController")
class AssinaturaControllerTest {

  @Mock
  private CriarAssinaturaUseCase criarUseCase;

  @Mock
  private BuscarAssinaturaUseCase buscarUseCase;

  @Mock
  private CancelarAssinaturaUseCase cancelarUseCase;

  private AssinaturaController controller;
  private static final LocalDate DATA_FIXA = LocalDate.of(2026, Month.JUNE, 28);

  @BeforeEach
  void setUp() {
    controller = new AssinaturaController(criarUseCase, buscarUseCase, cancelarUseCase);
  }

  @Nested
  @DisplayName("criar")
  class Criar {

    @Test
    @DisplayName("Deve criar assinatura com sucesso")
    void deveCriarComSucesso() {
      UUID usuarioId = UUID.randomUUID();
      UUID assinaturaId = UUID.randomUUID();
      CriarAssinaturaRequest request = new CriarAssinaturaRequest(usuarioId, "PREMIUM");
      AssinaturaResponse response = new AssinaturaResponse(assinaturaId, usuarioId,
          Plano.PREMIUM, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);

      given(criarUseCase.criar(request)).willReturn(Mono.just(response));

      StepVerifier.create(controller.criar(request))
          .expectNext(response)
          .verifyComplete();

      verify(criarUseCase).criar(request);
    }
  }

  @Nested
  @DisplayName("buscarPorId")
  class BuscarPorId {

    @Test
    @DisplayName("Deve buscar assinatura por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
      UUID assinaturaId = UUID.randomUUID();
      UUID usuarioId = UUID.randomUUID();
      AssinaturaResponse response = new AssinaturaResponse(assinaturaId, usuarioId,
          Plano.BASICO, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);

      given(buscarUseCase.buscarPorId(assinaturaId)).willReturn(Mono.just(response));

      StepVerifier.create(controller.buscarPorId(assinaturaId))
          .expectNext(response)
          .verifyComplete();

      verify(buscarUseCase).buscarPorId(assinaturaId);
    }
  }

  @Nested
  @DisplayName("buscarAtivaDoUsuario")
  class BuscarAtivaDoUsuario {

    @Test
    @DisplayName("Deve buscar assinatura ativa do usuário com sucesso")
    void deveBuscarAtivaDoUsuarioComSucesso() {
      UUID usuarioId = UUID.randomUUID();
      UUID assinaturaId = UUID.randomUUID();
      AssinaturaResponse response = new AssinaturaResponse(assinaturaId, usuarioId,
          Plano.FAMILIA, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);

      given(buscarUseCase.buscarAtivaDoUsuario(usuarioId)).willReturn(Mono.just(response));

      StepVerifier.create(controller.buscarAtivaDoUsuario(usuarioId))
          .expectNext(response)
          .verifyComplete();

      verify(buscarUseCase).buscarAtivaDoUsuario(usuarioId);
    }
  }

  @Nested
  @DisplayName("listar")
  class Listar {

    @Test
    @DisplayName("Deve listar assinaturas com paginação por cursor")
    void deveListarComCursor() {
      UUID assinaturaId1 = UUID.randomUUID();
      UUID usuarioId1 = UUID.randomUUID();
      UUID assinaturaId2 = UUID.randomUUID();
      UUID usuarioId2 = UUID.randomUUID();

      AssinaturaResponse response1 = new AssinaturaResponse(assinaturaId1, usuarioId1,
          Plano.BASICO, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);
      AssinaturaResponse response2 = new AssinaturaResponse(assinaturaId2, usuarioId2,
          Plano.PREMIUM, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);

      CursorListaResponse<AssinaturaResponse> cursorResponse = new CursorListaResponse<>(
          List.of(response1, response2), "next-cursor", true);

      given(buscarUseCase.listar("cursor", "ATIVA", "BASICO", 20))
          .willReturn(Mono.just(cursorResponse));

      StepVerifier.create(controller.listar("cursor", "ATIVA", "BASICO", 20))
          .expectNext(cursorResponse)
          .verifyComplete();

      verify(buscarUseCase).listar("cursor", "ATIVA", "BASICO", 20);
    }

    @Test
    @DisplayName("Deve listar assinaturas com tamanho padrão")
    void deveListarComTamanhoPadrao() {
      UUID assinaturaId = UUID.randomUUID();
      UUID usuarioId = UUID.randomUUID();
      AssinaturaResponse response = new AssinaturaResponse(assinaturaId, usuarioId,
          Plano.PREMIUM, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.ATIVA, 0);

      CursorListaResponse<AssinaturaResponse> cursorResponse = new CursorListaResponse<>(
          List.of(response), null, false);

      given(buscarUseCase.listar(null, null, null, 20))
          .willReturn(Mono.just(cursorResponse));

      StepVerifier.create(controller.listar(null, null, null, 20))
          .expectNext(cursorResponse)
          .verifyComplete();

      verify(buscarUseCase).listar(null, null, null, 20);
    }
  }

  @Nested
  @DisplayName("cancelar")
  class Cancelar {

    @Test
    @DisplayName("Deve cancelar assinatura com sucesso")
    void deveCancelarComSucesso() {
      UUID assinaturaId = UUID.randomUUID();
      UUID usuarioId = UUID.randomUUID();
      AssinaturaResponse response = new AssinaturaResponse(assinaturaId, usuarioId,
          Plano.PREMIUM, DATA_FIXA, DATA_FIXA.plusMonths(1), StatusAssinatura.CANCELADA, 0);

      given(cancelarUseCase.cancelar(assinaturaId)).willReturn(Mono.just(response));

      StepVerifier.create(controller.cancelar(assinaturaId))
          .expectNext(response)
          .verifyComplete();

      verify(cancelarUseCase).cancelar(assinaturaId);
    }
  }
}
