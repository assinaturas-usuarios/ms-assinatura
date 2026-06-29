package com.empresa.assinatura.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de evento que representa a solicitação de renovação de uma assinatura.
 *
 * @param assinaturaId identificador da assinatura a ser renovada
 * @param usuarioId    identificador do usuário associado à assinatura
 * @param plano        plano da assinatura a ser renovada
 * @param valor        valor da renovação
 */
public record RenovacaoSolicitadaEvent(
    UUID assinaturaId,
    UUID usuarioId,
    String plano,
    BigDecimal valor
) {

}
