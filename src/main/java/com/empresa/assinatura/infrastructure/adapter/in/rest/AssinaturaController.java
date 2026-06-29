package com.empresa.assinatura.infrastructure.adapter.in.rest;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CriarAssinaturaRequest;
import com.empresa.assinatura.application.dto.CursorListaResponse;
import com.empresa.assinatura.domain.port.in.BuscarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.in.CancelarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.in.CriarAssinaturaUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para gerenciar assinaturas.
 */
@Slf4j
@RestController
@RequestMapping("/v1/assinaturas")
@RequiredArgsConstructor
public class AssinaturaController implements AssinaturaSwagger {

  private final CriarAssinaturaUseCase criarUseCase;
  private final BuscarAssinaturaUseCase buscarUseCase;
  private final CancelarAssinaturaUseCase cancelarUseCase;

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<AssinaturaResponse> criar(@Valid @RequestBody CriarAssinaturaRequest request) {
    log.info("Recebendo POST request de criação de assinatura, usuário ID: {}", request.usuarioId());
    return criarUseCase.criar(request);
  }

  @Override
  @GetMapping("/{id}")
  public Mono<AssinaturaResponse> buscarPorId(@PathVariable UUID id) {
    log.info("Recebendo GET request de busca de assinatura, ID: {}", id);
    return buscarUseCase.buscarPorId(id);
  }

  @Override
  @GetMapping("/usuario/{usuarioId}/ativa")
  public Mono<AssinaturaResponse> buscarAtivaDoUsuario(@PathVariable UUID usuarioId) {
    log.info("Recebendo GET request de busca de assinatura por usuário ID: {}", usuarioId);
    return buscarUseCase.buscarAtivaDoUsuario(usuarioId);
  }

  @Override
  @GetMapping
  public Mono<CursorListaResponse<AssinaturaResponse>> listar(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String plano,
      @RequestParam(defaultValue = "20") int tamanho) {
    log.info("Recebendo GET request de busca de assinaturas paginadas por status {} ou plano {}", status, plano);
    return buscarUseCase.listar(cursor, status, plano, tamanho);
  }

  @Override
  @DeleteMapping("/{id}/cancelar")
  public Mono<AssinaturaResponse> cancelar(@PathVariable UUID id) {
    log.info("Recebendo DELETE request de cancelamento de assinatura, ID: {}", id);
    return cancelarUseCase.cancelar(id);
  }
}
