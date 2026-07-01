package com.empresa.assinatura.infrastructure.config;

import com.empresa.assinatura.application.dto.PagamentoResultadoEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Configuração do Kafka para produção e consumo de eventos.
 */
@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  /**
   * Configura o ProducerFactory para publicação de eventos.
   *
   * @return fábrica de produtores Kafka
   */
  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    config.put(ProducerConfig.ACKS_CONFIG, "all");
    config.put(ProducerConfig.RETRIES_CONFIG, 3);
    config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    return new DefaultKafkaProducerFactory<>(config);
  }

  /**
   * Configura o KafkaTemplate para envio de mensagens.
   *
   * @return template Kafka
   */
  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(
        producerFactory());
    return kafkaTemplate;
  }


  /**
   * Configura o ConsumerFactory para consumo de eventos genéricos, mantendo a desserialização correta.
   *
   * @return fábrica de consumidores Kafka
   */
  @Bean
  public ConsumerFactory<Object, Object> consumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

    JsonDeserializer<PagamentoResultadoEvent> deserializer = new JsonDeserializer<>(
        PagamentoResultadoEvent.class, false);

    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), (JsonDeserializer) deserializer);
  }

  /**
   * Configura o container factory com confirmação manual e backoff exponencial.
   *
   * @return container factory para listeners
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      ConsumerFactory<Object, Object> consumerFactory) {

    ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    configurer.configure(factory, consumerFactory);

    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

    ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
    backOff.setMaxInterval(30000L);
    backOff.setMaxElapsedTime(120000L);

    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate());
    factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backOff));
    return factory;
  }
}
