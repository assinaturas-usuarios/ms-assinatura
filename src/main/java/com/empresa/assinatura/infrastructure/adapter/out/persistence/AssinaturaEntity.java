package com.empresa.assinatura.infrastructure.adapter.out.persistence;

import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidade que representa uma assinatura no banco de dados.
 */
@Table("assinaturas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssinaturaEntity {

  @Id
  private UUID id;

  @Column("usuario_id")
  private UUID usuarioId;

  @Column("plano")
  private Plano plano;

  @Column("data_inicio")
  private LocalDate dataInicio;

  @Column("data_expiracao")
  private LocalDate dataExpiracao;

  @Column("status")
  private StatusAssinatura status;

  @Column("tentativas_renovacao")
  private int tentativasRenovacao;

  @Column("renovacao_em_andamento")
  private boolean renovacaoEmAndamento;

  @Column("data_inicio_renovacao")
  private LocalDateTime dataInicioRenovacao;

  @Version
  private Long versao;
}
