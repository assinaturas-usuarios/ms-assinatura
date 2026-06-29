package com.empresa.assinatura.application.dto;

import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de resposta que representa os detalhes de uma assinatura.
 *
 * @param id                  identificador da assinatura
 * @param usuarioId           identificador do usuário associado à assinatura
 * @param plano               plano da assinatura
 * @param dataInicio          data de início da assinatura
 * @param dataExpiracao       data de expiração da assinatura
 * @param status              status atual da assinatura
 * @param tentativasRenovacao número de tentativas de renovação realizadas
 */
public record AssinaturaResponse(
    UUID id,
    UUID usuarioId,
    Plano plano,
    LocalDate dataInicio,
    LocalDate dataExpiracao,
    StatusAssinatura status,
    int tentativasRenovacao
) {

}
