package com.empresa.assinatura.application.dto;

import java.util.UUID;

/**
 * DTO de evento que representa o resultado de um pagamento.
 *
 * @param assinaturaId identificador da assinatura associada ao pagamento
 * @param pagamentoId  identificador do pagamento
 * @param status       status do pagamento (ex: "SUCESSO", "FALHA")
 */
public record PagamentoResultadoEvent(
    UUID assinaturaId,
    UUID pagamentoId,
    String status
) {

}
