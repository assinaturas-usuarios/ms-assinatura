package com.empresa.assinatura.infrastructure.adapter.out.persistence;

import com.empresa.assinatura.domain.model.StatusAssinatura;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositório R2DBC para operações de persistência de assinaturas.
 */
public interface AssinaturaR2dbcRepository extends R2dbcRepository<AssinaturaEntity, UUID> {

  /**
   * Busca a assinatura ativa de um usuário pelo ID.
   *
   * @param usuarioId ID do usuário
   * @return assinatura ativa do usuário
   */
  @Query("SELECT * FROM assinaturas WHERE usuario_id = :usuarioId AND status = 'ATIVA' LIMIT 1")
  Mono<AssinaturaEntity> findAtivaByUsuarioId(UUID usuarioId);

  /**
   * Verifica se existe uma assinatura ativa para o usuário.
   *
   * @param usuarioId ID do usuário
   * @param status    status da assinatura
   * @return true se existir, false caso contrário
   */
  Mono<Boolean> existsByUsuarioIdAndStatus(UUID usuarioId, StatusAssinatura status);

  /**
   * Busca assinaturas vencendo na data informada com lock para renovação. 
   * Evitando que múltiplas instâncias do serviço processem a mesma assinatura.
   *
   * @param data data de vencimento
   * @return fluxo de assinaturas para renovar
   */
  @Query("SELECT * FROM assinaturas WHERE data_expiracao = :data AND status = 'ATIVA' "
      + "AND renovacao_em_andamento = false FOR UPDATE SKIP LOCKED LIMIT 50")
  Flux<AssinaturaEntity> findParaRenovar(LocalDate data);

  /**
   * Lista assinaturas com base em um cursor, status e plano.
   *
   * @param cursor  cursor para paginação
   * @param status  status da assinatura
   * @param plano   plano da assinatura
   * @param tamanho tamanho da página
   * @return fluxo de assinaturas
   */
  @Query("SELECT * FROM assinaturas WHERE (:cursor IS NULL OR id::text > :cursor) "
      + "AND (:status IS NULL OR CAST(status AS TEXT) = :status) "
      + "AND (:plano IS NULL OR CAST(plano AS TEXT) = :plano) "
      + "ORDER BY id ASC LIMIT :tamanho")
  Flux<AssinaturaEntity> findWithCursor(String cursor, String status, String plano, int tamanho);
}