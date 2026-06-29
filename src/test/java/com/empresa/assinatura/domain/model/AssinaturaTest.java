package com.empresa.assinatura.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Assinatura - regras de domínio")
class AssinaturaTest {

  @Nested
  @DisplayName("Criação de nova assinatura")
  class CriacaoNovaAssinatura {

    @Test
    @DisplayName("Deve criar assinatura com status ATIVA e zero tentativas")
    void deveCriarAssinaturaAtiva() {
      Assinatura assinatura = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);

      assertThat(assinatura.getStatus()).isEqualTo(StatusAssinatura.ATIVA);
      assertThat(assinatura.getTentativasRenovacao()).isZero();
      assertThat(assinatura.isRenovacaoEmAndamento()).isFalse();
      assertThat(assinatura.getDataExpiracao()).isEqualTo(assinatura.getDataInicio().plusMonths(1));
    }
  }

  @Nested
  @DisplayName("Renovação")
  class Renovacao {

    @Test
    @DisplayName("Deve reiniciar tentativas ao renovar com sucesso")
    void deveReiniciarTentativas() {
      Assinatura assinatura = Assinatura.nova(UUID.randomUUID(), Plano.BASICO);
      assinatura.registrarFalhaRenovacao();
      assinatura.registrarFalhaRenovacao();

      assinatura.renovar();

      assertThat(assinatura.getTentativasRenovacao()).isZero();
      assertThat(assinatura.getStatus()).isEqualTo(StatusAssinatura.ATIVA);
      assertThat(assinatura.isRenovacaoEmAndamento()).isFalse();
    }

    @Test
    @DisplayName("Deve estender data de expiração em 1 mês ao renovar")
    void deveEstenderDataExpiracao() {
      Assinatura assinatura = Assinatura.nova(UUID.randomUUID(), Plano.FAMILIA);
      var dataAntes = assinatura.getDataExpiracao();

      assinatura.renovar();

      assertThat(assinatura.getDataExpiracao()).isEqualTo(dataAntes.plusMonths(1));
    }
  }

  @Nested
  @DisplayName("Tentativas de renovação")
  class TentativasRenovacao {

    @Test
    @DisplayName("Deve atingir limite com 3 tentativas")
    void deveAtingirLimiteComTresTentativas() {
      Assinatura assinatura = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);

      assinatura.registrarFalhaRenovacao();
      assinatura.registrarFalhaRenovacao();
      assinatura.registrarFalhaRenovacao();

      assertThat(assinatura.atingiuLimiteTentativas()).isTrue();
    }

    @Test
    @DisplayName("Não deve atingir limite com menos de 3 tentativas")
    void naoDeveAtingirLimiteComMenosDeTres() {
      Assinatura assinatura = Assinatura.nova(UUID.randomUUID(), Plano.BASICO);

      assinatura.registrarFalhaRenovacao();
      assinatura.registrarFalhaRenovacao();

      assertThat(assinatura.atingiuLimiteTentativas()).isFalse();
    }
  }

  @Nested
  @DisplayName("Cancelamento")
  class Cancelamento {

    @Test
    @DisplayName("Deve alterar status para CANCELADA ao cancelar")
    void deveAlterarStatusParaCancelada() {
      Assinatura assinatura = Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);

      assinatura.cancelar();

      assertThat(assinatura.getStatus()).isEqualTo(StatusAssinatura.CANCELADA);
    }
  }

  @Nested
  @DisplayName("Suspensão")
  class Suspensao {

    @Test
    @DisplayName("Deve alterar status para SUSPENSA ao suspender")
    void deveAlterarStatusParaSuspensa() {
      Assinatura assinatura = Assinatura.nova(UUID.randomUUID(), Plano.FAMILIA);

      assinatura.suspender();

      assertThat(assinatura.getStatus()).isEqualTo(StatusAssinatura.SUSPENSA);
      assertThat(assinatura.isRenovacaoEmAndamento()).isFalse();
    }
  }
}
