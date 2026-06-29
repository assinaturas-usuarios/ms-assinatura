package com.empresa.assinatura.domain.model;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Enumeração que representa os diferentes planos de assinatura disponíveis, cada um com seu valor
 * associado.
 */
public enum Plano {

  BASICO(new BigDecimal("19.90")),
  PREMIUM(new BigDecimal("39.90")),
  FAMILIA(new BigDecimal("59.90"));

  private final BigDecimal valor;

  Plano(BigDecimal valor) {
    this.valor = valor;
  }

  /**
   * Retorna o plano correspondente ao nome fornecido, ignorando maiúsculas e minúsculas.
   *
   * @param nome o nome do plano
   * @return um Optional contendo o plano correspondente, ou vazio se não houver correspondência
   */
  public static Optional<Plano> fromNome(String nome) {
    try {
      return Optional.of(valueOf(nome.toUpperCase()));
    } catch (IllegalArgumentException _) {
      return Optional.empty();
    }
  }

  public BigDecimal getValor() {
    return valor;
  }
}
