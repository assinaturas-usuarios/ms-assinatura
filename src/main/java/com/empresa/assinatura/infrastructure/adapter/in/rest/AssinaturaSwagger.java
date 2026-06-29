package com.empresa.assinatura.infrastructure.adapter.in.rest;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CriarAssinaturaRequest;
import com.empresa.assinatura.application.dto.CursorListaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

/**
 * Interface que define os endpoints REST para gerenciamento de assinaturas, com documentação
 * Swagger.
 */
@Tag(name = "Assinaturas", description = "Gestao de assinaturas de streaming")
public interface AssinaturaSwagger {

  /**
   * Cria uma nova assinatura para o usuário informado.
   *
   * @param request dados da assinatura a ser criada
   * @return assinatura criada
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Criar nova assinatura")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Assinatura criada com sucesso"),
      @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
      @ApiResponse(responseCode = "409", description = "Usuario ja possui assinatura ativa"),
      @ApiResponse(responseCode = "422", description = "Dados invalidos")
  })
  Mono<AssinaturaResponse> criar(@Valid @RequestBody CriarAssinaturaRequest request);

  /**
   * Cancela a assinatura informada, mantendo o acesso até o fim do ciclo.
   *
   * @param id identificador da assinatura a ser cancelada
   * @return assinatura atualizada
   */
  @GetMapping("/{id}")
  @Operation(summary = "Buscar assinatura por ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Assinatura encontrada"),
      @ApiResponse(responseCode = "404", description = "Assinatura nao encontrada")
  })
  Mono<AssinaturaResponse> buscarPorId(
      @Parameter(description = "ID da assinatura", required = true) @PathVariable UUID id);

  /**
   * Busca a assinatura ativa de um usuario.
   *
   * @param usuarioId identificador do usuario
   * @return assinatura ativa do usuario
   */
  @GetMapping("/usuario/{usuarioId}/ativa")
  @Operation(summary = "Buscar assinatura ativa do usuario")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Assinatura ativa encontrada"),
      @ApiResponse(responseCode = "404", description = "Assinatura ativa nao encontrada")
  })
  Mono<AssinaturaResponse> buscarAtivaDoUsuario(
      @Parameter(description = "ID do usuario", required = true) @PathVariable UUID usuarioId);

  /**
   * Lista assinaturas com paginacao cursor-based e filtros.
   *
   * @param cursor  cursor da ultima pagina
   * @param status  filtro por status
   * @param plano   filtro por plano
   * @param tamanho tamanho da pagina
   * @return lista de assinaturas
   */
  @GetMapping
  @Operation(summary = "Listar assinaturas com paginacao cursor-based e filtros")
  Mono<CursorListaResponse<AssinaturaResponse>> listar(
      @Parameter(description = "Cursor da ultima pagina")
      @RequestParam(required = false) String cursor,
      @Parameter(description = "Filtro por status") @RequestParam(required = false) String status,
      @Parameter(description = "Filtro por plano") @RequestParam(required = false) String plano,
      @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "20") int tamanho
  );

  /**
   * Cancela a assinatura informada, mantendo o acesso até o fim do ciclo.
   *
   * @param id identificador da assinatura a ser cancelada
   * @return assinatura atualizada
   */
  @DeleteMapping("/{id}/cancelar")
  @Operation(summary = "Cancelar assinatura")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Assinatura cancelada"),
      @ApiResponse(responseCode = "404", description = "Assinatura nao encontrada"),
      @ApiResponse(responseCode = "409", description = "Assinatura ja cancelada")
  })
  Mono<AssinaturaResponse> cancelar(
      @Parameter(description = "ID da assinatura", required = true) @PathVariable UUID id);

}