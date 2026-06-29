package com.empresa.assinatura.infrastructure.exception;

import java.util.UUID;

/**
 * Exceção lançada quando uma assinatura não é encontrada no sistema.
 */
public class AssinaturaNaoEncontradaException extends RuntimeException {

  /**
   * Construtor que recebe o identificador da assinatura.
   *
   * @param id identificador da assinatura
   */
  public AssinaturaNaoEncontradaException(UUID id) {
    super("Assinatura não encontrada: " + id);
  }
}
