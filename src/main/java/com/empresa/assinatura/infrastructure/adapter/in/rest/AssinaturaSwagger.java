package com.empresa.assinatura.infrastructure.adapter.in.rest;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CriarAssinaturaRequest;
import com.empresa.assinatura.application.dto.CursorListaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
      @ApiResponse(responseCode = "201", description = "Assinatura criada com sucesso",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = AssinaturaResponse.class))),
      @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Usuário Não Encontrado",
                        "status": 404,
                        "detail": "Usuário com ID 550e8400-e29b-41d4-a716-446655440000 não foi encontrado",
                        "instance": "POST /v1/assinaturas"
                      }
                      """
              ))),
      @ApiResponse(responseCode = "409", description = "Usuário já possui assinatura ativa",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Conflito: Assinatura Já Ativa",
                        "status": 409,
                        "detail": "Usuário com ID 550e8400-e29b-41d4-a716-446655440000 já possui uma assinatura ativa",
                        "instance": "POST /v1/assinaturas"
                      }
                      """
              ))),
      @ApiResponse(responseCode = "422", description = "Dados inválidos",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Erro de Validação",
                        "status": 422,
                        "detail": "Os dados fornecidos contêm erros de validação",
                        "instance": "POST /v1/assinaturas",
                        "campos": {
                          "plano": "Plano inválido",
                          "usuarioId": "Campo obrigatório"
                        }
                      }
                      """
              ))),
      @ApiResponse(responseCode = "503", description = "Serviço temporariamente indisponível",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Serviço Temporariamente Indisponível",
                        "status": 503,
                        "detail": "O banco de dados está temporariamente indisponível. Tente novamente em alguns instantes.",
                        "instance": "POST /v1/assinaturas"
                      }
                      """
              )))
  })
  Mono<AssinaturaResponse> criar(@Valid @RequestBody CriarAssinaturaRequest request);

  /**
   * Busca uma assinatura pelo seu ID.
   *
   * @param id identificador da assinatura
   * @return assinatura encontrada
   */
  @GetMapping("/{id}")
  @Operation(summary = "Buscar assinatura por ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Assinatura encontrada",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = AssinaturaResponse.class))),
      @ApiResponse(responseCode = "404", description = "Assinatura não encontrada",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Assinatura Não Encontrada",
                        "status": 404,
                        "detail": "Assinatura com ID 550e8400-e29b-41d4-a716-446655440000 não foi encontrada",
                        "instance": "GET /v1/assinaturas/550e8400-e29b-41d4-a716-446655440000"
                      }
                      """
              ))),
      @ApiResponse(responseCode = "503", description = "Serviço temporariamente indisponível",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Banco de Dados Indisponível",
                        "status": 503,
                        "detail": "O banco de dados está temporariamente indisponível. Tente novamente em alguns instantes.",
                        "instance": "GET /v1/assinaturas/550e8400-e29b-41d4-a716-446655440000"
                      }
                      """
              )))
  })
  Mono<AssinaturaResponse> buscarPorId(
      @Parameter(description = "ID da assinatura", required = true) @PathVariable UUID id);

  /**
   * Busca a assinatura ativa de um usuário.
   *
   * @param usuarioId identificador do usuário
   * @return assinatura ativa do usuário
   */
  @GetMapping("/usuario/{usuarioId}/ativa")
  @Operation(summary = "Buscar assinatura ativa do usuário")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Assinatura ativa encontrada",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = AssinaturaResponse.class))),
      @ApiResponse(responseCode = "404", description = "Assinatura ativa não encontrada",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Assinatura Não Encontrada",
                        "status": 404,
                        "detail": "Nenhuma assinatura ativa encontrada para o usuário 550e8400-e29b-41d4-a716-446655440000",
                        "instance": "GET /v1/assinaturas/usuario/550e8400-e29b-41d4-a716-446655440000/ativa"
                      }
                      """
              ))),
      @ApiResponse(responseCode = "503", description = "Serviço temporariamente indisponível",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Banco de Dados Indisponível",
                        "status": 503,
                        "detail": "O banco de dados está temporariamente indisponível. Tente novamente em alguns instantes.",
                        "instance": "GET /v1/assinaturas/usuario/550e8400-e29b-41d4-a716-446655440000/ativa"
                      }
                      """
              )))
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
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de assinaturas recuperada com sucesso",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CursorListaResponse.class))),
      @ApiResponse(responseCode = "503", description = "Serviço temporariamente indisponível",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Banco de Dados Indisponível",
                        "status": 503,
                        "detail": "O banco de dados está temporariamente indisponível. Tente novamente em alguns instantes.",
                        "instance": "GET /v1/assinaturas?status=ATIVA&tamanho=20"
                      }
                      """
              )))
  })
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
      @ApiResponse(responseCode = "200", description = "Assinatura cancelada",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = AssinaturaResponse.class))),
      @ApiResponse(responseCode = "404", description = "Assinatura não encontrada",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Assinatura Não Encontrada",
                        "status": 404,
                        "detail": "Assinatura com ID 550e8400-e29b-41d4-a716-446655440000 não foi encontrada",
                        "instance": "DELETE /v1/assinaturas/550e8400-e29b-41d4-a716-446655440000/cancelar"
                      }
                      """
              ))),
      @ApiResponse(responseCode = "409", description = "Assinatura já cancelada",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Conflito: Assinatura Já Cancelada",
                        "status": 409,
                        "detail": "Não é possível cancelar uma assinatura que já foi cancelada",
                        "instance": "DELETE /v1/assinaturas/550e8400-e29b-41d4-a716-446655440000/cancelar"
                      }
                      """
              ))),
      @ApiResponse(responseCode = "503", description = "Serviço temporariamente indisponível",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "type": "ms-assinatura/errors",
                        "title": "Banco de Dados Indisponível",
                        "status": 503,
                        "detail": "O banco de dados está temporariamente indisponível. Tente novamente em alguns instantes.",
                        "instance": "DELETE /v1/assinaturas/550e8400-e29b-41d4-a716-446655440000/cancelar"
                      }
                      """
              )))
  })
  Mono<AssinaturaResponse> cancelar(
      @Parameter(description = "ID da assinatura", required = true) @PathVariable UUID id);

}