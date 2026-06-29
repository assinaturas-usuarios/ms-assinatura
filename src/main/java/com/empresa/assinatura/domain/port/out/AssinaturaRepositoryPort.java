package com.empresa.assinatura.domain.port.out;

import com.empresa.assinatura.domain.model.Assinatura;
import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface que define o contrato para operações de persistência relacionadas a assinaturas.
 */
public interface AssinaturaRepositoryPort {

  /**
   * Persiste uma assinatura.
   *
   * @param assinatura entidade de domínio a ser salva
   * @return assinatura persistida
   */
  Mono<Assinatura> salvar(Assinatura assinatura);

  /**
   * Busca assinatura pelo identificador.
   *
   * @param id identificador da assinatura
   * @return assinatura encontrada, se existir
   */
  Mono<Assinatura> buscarPorId(UUID id);

  /**
   * Busca a assinatura ativa de um usuário.
   *
   * @param usuarioId identificador do usuário
   * @return assinatura ativa do usuário, se existir
   */
  Mono<Assinatura> buscarAtivaDoUsuario(UUID usuarioId);

  /**
   * Verifica se o usuário já possui assinatura ativa.
   *
   * @param usuarioId identificador do usuário
   * @return true se já possuir
   */
  Mono<Boolean> existeAssinaturaAtiva(UUID usuarioId);

  /**
   * Busca assinaturas vencendo na data informada com lock para renovação.
   *
   * @param data data de vencimento
   * @return fluxo de assinaturas para renovar
   */
  Flux<Assinatura> buscarParaRenovar(LocalDate data);

  /**
   * Lista assinaturas com filtros e paginação.
   *
   * @param cursor  cursor da última página (UUID string), null para primeira página
   * @param status  filtro por status (opcional)
   * @param plano   filtro por plano (opcional)
   * @param tamanho tamanho da página
   * @return fluxo de assinaturas
   */
  Flux<Assinatura> listar(String cursor, String status, String plano, int tamanho);
}
