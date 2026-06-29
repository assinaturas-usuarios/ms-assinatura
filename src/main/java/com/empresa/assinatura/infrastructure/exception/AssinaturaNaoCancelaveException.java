package com.empresa.assinatura.infrastructure.exception;

import java.util.UUID;

/**
 * Exceção lançada quando uma assinatura não pode ser cancelada porque já se encontra cancelada.
 */
public class AssinaturaNaoCancelaveException extends RuntimeException {

  /**
   * Construtor que recebe o identificador da assinatura.
   *
   * @param id identificador da assinatura
   */
  public AssinaturaNaoCancelaveException(UUID id) {
    super("Assinatura já se encontra cancelada: " + id);
  }
}
