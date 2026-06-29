package com.empresa.assinatura.infrastructure.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário não é encontrado no sistema.
 */
public class UsuarioNaoEncontradoException extends RuntimeException {

  /**
   * Construtor que recebe o identificador do usuário.
   *
   * @param usuarioId identificador do usuário
   */
  public UsuarioNaoEncontradoException(UUID usuarioId) {
    super("Usuário não encontrado: " + usuarioId);
  }
}
