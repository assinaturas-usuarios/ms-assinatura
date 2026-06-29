package com.empresa.assinatura.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Representa o domínio rico da assinatura, onde somente ela pode alterar seu estado e gerenciar
 * suas regras de negócio e sem referência a framework específico.
 */
public class Assinatura {

  private UUID id;
  private UUID usuarioId;
  private Plano plano;
  private LocalDate dataInicio;
  private LocalDate dataExpiracao;
  private StatusAssinatura status;
  private int tentativasRenovacao;
  private boolean renovacaoEmAndamento;
  private LocalDateTime dataInicioRenovacao;
  private Long versao;

  /**
   * Construtor privado para uso interno da classe.
   */
  public Assinatura(UUID id, UUID usuarioId, Plano plano, LocalDate dataInicio,
      LocalDate dataExpiracao, StatusAssinatura status,
      int tentativasRenovacao, boolean renovacaoEmAndamento,
      LocalDateTime dataInicioRenovacao, Long versao) {
    this.id = id;
    this.usuarioId = usuarioId;
    this.plano = plano;
    this.dataInicio = dataInicio;
    this.dataExpiracao = dataExpiracao;
    this.status = status;
    this.tentativasRenovacao = tentativasRenovacao;
    this.renovacaoEmAndamento = renovacaoEmAndamento;
    this.dataInicioRenovacao = dataInicioRenovacao;
    this.versao = versao;
  }

  /**
   * Cria uma nova assinatura para um usuário com um plano específico.
   *
   * @param usuarioId O ID do usuário que está criando a assinatura.
   * @param plano     O plano ao qual o usuário está se inscrevendo.
   * @return Uma nova instância de Assinatura.
   */
  public static Assinatura nova(UUID usuarioId, Plano plano) {
    LocalDate hoje = LocalDate.now(ZoneId.systemDefault());
    return new Assinatura(
        UUID.randomUUID(), usuarioId, plano,
        hoje, hoje.plusMonths(1),
        StatusAssinatura.ATIVA, 0, false, null, null
    );
  }

  /**
   * Cancela a assinatura, alterando seu status para CANCELADA.
   */
  public void cancelar() {
    this.status = StatusAssinatura.CANCELADA;
    this.renovacaoEmAndamento = false;
  }

  /**
   * Suspende a assinatura, alterando seu status para SUSPENSA.
   */
  public void suspender() {
    this.status = StatusAssinatura.SUSPENSA;
    this.renovacaoEmAndamento = false;
  }

  /**
   * Renova a assinatura, estendendo sua data de expiração em um mês e resetando o status e
   * tentativas de renovação.
   */
  public void renovar() {
    this.dataExpiracao = this.dataExpiracao.plusMonths(1);
    this.status = StatusAssinatura.ATIVA;
    this.tentativasRenovacao = 0;
    this.renovacaoEmAndamento = false;
    this.dataInicioRenovacao = null;
  }

  /**
   * Inicia o processo de renovação da assinatura, alterando seu status para AGUARDANDO_RENOVACAO e
   * registrando o momento de início.
   */
  public void iniciarRenovacao() {
    this.status = StatusAssinatura.AGUARDANDO_RENOVACAO;
    this.renovacaoEmAndamento = true;
    this.dataInicioRenovacao = LocalDateTime.now(ZoneId.systemDefault());
  }

  /**
   * Registra uma falha na tentativa de renovação da assinatura, incrementando o contador de
   * tentativas e marcando a renovação como não em andamento.
   */
  public void registrarFalhaRenovacao() {
    this.tentativasRenovacao++;
    this.renovacaoEmAndamento = false;
  }

  /**
   * Verifica se a assinatura atingiu o limite de tentativas de renovação.
   *
   * @return true se o número de tentativas de renovação for maior ou igual a 3, caso contrário
   *        false.
   */
  public boolean atingiuLimiteTentativas() {
    return this.tentativasRenovacao >= 3;
  }

  /**
   * Verifica se a assinatura está ativa.
   *
   * @return true se o status da assinatura for ATIVA, caso contrário false.
   */
  public boolean estaAtiva() {
    return StatusAssinatura.ATIVA.equals(this.status);
  }

  public UUID getId() {
    return id;
  }

  public UUID getUsuarioId() {
    return usuarioId;
  }

  public Plano getPlano() {
    return plano;
  }

  public LocalDate getDataInicio() {
    return dataInicio;
  }

  public LocalDate getDataExpiracao() {
    return dataExpiracao;
  }

  public StatusAssinatura getStatus() {
    return status;
  }

  public int getTentativasRenovacao() {
    return tentativasRenovacao;
  }

  public boolean isRenovacaoEmAndamento() {
    return renovacaoEmAndamento;
  }

  public LocalDateTime getDataInicioRenovacao() {
    return dataInicioRenovacao;
  }

  public Long getVersao() {
    return versao;
  }
}
