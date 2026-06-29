package com.empresa.assinatura.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO de requisição para criar uma nova assinatura.
 *
 * @param usuarioId identificador do usuário que está criando a assinatura
 * @param plano     plano da assinatura a ser criada
 */
public record CriarAssinaturaRequest(

    @NotNull(message = "ID do usuário é obrigatório")
    UUID usuarioId,

    @NotNull(message = "Plano é obrigatório")
    String plano
) {

}
