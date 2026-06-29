package com.empresa.assinatura.domain.port.out;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Interface que define o contrato para operações de cache relacionadas a assinaturas.
 */
public interface AssinaturaCachePort {

  /**
   * Armazena a assinatura no cache.
   *
   * @param assinaturaId identificador da assinatura
   * @param assinatura   dados da assinatura
   * @return Mono vazio após operação
   */
  Mono<Void> armazenar(UUID assinaturaId, AssinaturaResponse assinatura);

  /**
   * Busca assinatura no cache pelo identificador.
   *
   * @param assinaturaId identificador da assinatura
   * @return Mono com a assinatura, se presente no cache
   */
  Mono<AssinaturaResponse> buscar(UUID assinaturaId);

  /**
   * Remove a assinatura do cache.
   *
   * @param assinaturaId identificador da assinatura
   * @return Mono vazio após remoção
   */
  Mono<Void> invalidar(UUID assinaturaId);
}
