package com.empresa.assinatura.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração do WebClient para comunicação com outros microsserviços.
 */
@Configuration
public class WebClientConfig {

  /**
   * Cria o WebClient configurado para comunicação com o ms-usuario.
   *
   * @param usuarioUrl URL base do ms-usuario
   * @return instância configurada do WebClient
   */
  @Bean
  public WebClient msUsuarioWebClient(@Value("${ms-usuario.url}") String usuarioUrl) {
    return WebClient.builder()
        .baseUrl(usuarioUrl)
        .build();
  }
}
