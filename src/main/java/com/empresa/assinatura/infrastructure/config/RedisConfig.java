package com.empresa.assinatura.infrastructure.config;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuração do Redis para cache de assinaturas.
 */
@Configuration
public class RedisConfig {

  /**
   * Configura o template reativo do Redis para serialização de AssinaturaResponse.
   *
   * @param factory      fábrica de conexões reativas do Redis
   * @param objectMapper ObjectMapper configurado pelo Spring Boot (com JavaTimeModule)
   * @return template reativo configurado
   */
  @Bean
  public ReactiveRedisTemplate<String, AssinaturaResponse> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {

    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    Jackson2JsonRedisSerializer<AssinaturaResponse> jsonSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper.copy(), AssinaturaResponse.class);

    RedisSerializationContext<String, AssinaturaResponse> context =
        RedisSerializationContext.<String, AssinaturaResponse>newSerializationContext(
                stringSerializer)
            .value(jsonSerializer)
            .hashValue(jsonSerializer)
            .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }
}
