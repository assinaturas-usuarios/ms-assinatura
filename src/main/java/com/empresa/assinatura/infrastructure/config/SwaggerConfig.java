package com.empresa.assinatura.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 */
@Configuration
public class SwaggerConfig {

  /**
   * Configura as informações do OpenAPI para o ms-assinatura.
   *
   * @return instância configurada do OpenAPI
   */
  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Assinatura API")
            .description("Serviço de gerenciamento de assinaturas")
            .version("v1"));
  }
}
