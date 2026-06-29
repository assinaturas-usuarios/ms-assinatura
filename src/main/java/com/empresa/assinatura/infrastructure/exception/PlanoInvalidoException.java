package com.empresa.assinatura.infrastructure.exception;

/**
 * Exceção lançada quando um plano inválido é fornecido.
 */
public class PlanoInvalidoException extends RuntimeException {

  /**
   * Construtor que recebe o nome do plano inválido.
   *
   * @param plano nome do plano inválido
   */
  public PlanoInvalidoException(String plano) {
    super("Plano inválido: " + plano + ". Valores aceitos: BASICO, PREMIUM, FAMILIA");
  }
}
