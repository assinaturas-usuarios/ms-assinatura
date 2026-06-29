package com.empresa.assinatura.infrastructure.adapter.out.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import com.empresa.assinatura.application.dto.RenovacaoSolicitadaEvent;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssinaturaEventoPublicador")
class AssinaturaEventPublisherTest {

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks
  private AssinaturaProducer assinaturaProducer;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(assinaturaProducer, "topicoRenovacao", "renovacao-solicitada");
    ReflectionTestUtils.setField(assinaturaProducer, "topicoAssinaturaCancelada", "assinatura-cancelada");
    ReflectionTestUtils.setField(assinaturaProducer, "topicoAssinaturaSuspensa", "assinatura-suspensa");
  }

  @Test
  @DisplayName("Deve publicar evento de renovacao solicitada")
  void devePublicarRenovacaoSolicitada() {
    RenovacaoSolicitadaEvent evento = new RenovacaoSolicitadaEvent(
        UUID.randomUUID(), UUID.randomUUID(), "PREMIUM", new BigDecimal("39.90"));

    StepVerifier.create(assinaturaProducer.publicarRenovacaoSolicitada(evento))
        .verifyComplete();

    verify(kafkaTemplate).send(anyString(), anyString(), any());
  }

  @Test
  @DisplayName("Deve publicar evento de assinatura cancelada")
  void devePublicarAssinaturaCancelada() {
    String assinaturaId = UUID.randomUUID().toString();

    StepVerifier.create(assinaturaProducer.publicarAssinaturaCancelada(assinaturaId))
        .verifyComplete();

    verify(kafkaTemplate).send(anyString(), anyString(), any());
  }

  @Test
  @DisplayName("Deve publicar evento de assinatura suspensa")
  void devePublicarAssinaturaSuspensa() {
    String assinaturaId = UUID.randomUUID().toString();

    StepVerifier.create(assinaturaProducer.publicarAssinaturaSuspensa(assinaturaId))
        .verifyComplete();

    verify(kafkaTemplate).send(anyString(), anyString(), any());
  }
}