package com.empresa.assinatura.domain.port.in;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CursorListaResponse;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Interface que define o caso de uso para buscar assinaturas.
 */
public interface BuscarAssinaturaUseCase {

  /**
   * Busca uma assinatura pelo seu identificador.
   *
   * @param id identificador da assinatura
   * @return assinatura encontrada
   */
  Mono<AssinaturaResponse> buscarPorId(UUID id);

  /**
   * Busca a assinatura ativa de um usuario.
   *
   * @param usuarioId identificador do usuario
   * @return assinatura ativa do usuario
   */
  Mono<AssinaturaResponse> buscarAtivaDoUsuario(UUID usuarioId);

  /**
   * Lista assinaturas com paginacao cursor-based e filtros opcionais.
   *
   * @param cursor  cursor da ultima pagina (UUID string), null para primeira pagina
   * @param status  filtro por status (opcional)
   * @param plano   filtro por plano (opcional)
   * @param tamanho quantidade de itens por pagina
   * @return pagina com itens e cursor para proxima pagina
   */
  Mono<CursorListaResponse<AssinaturaResponse>> listar(String cursor, String status, String plano,
      int tamanho);
}
