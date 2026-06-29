package com.empresa.assinatura.infrastructure.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário tenta criar uma nova assinatura enquanto já possui uma
 * assinatura ativa.
 */
public class AssinaturaJaAtivaException extends RuntimeException {

  /**
   * Construtor que recebe o identificador do usuário.
   *
   * @param usuarioId identificador do usuário
   */
  public AssinaturaJaAtivaException(UUID usuarioId) {
    super("Usuário já possui assinatura ativa: " + usuarioId);
  }
}
