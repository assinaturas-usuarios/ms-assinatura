package com.empresa.assinatura.infrastructure.adapter.in.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.assinatura.application.dto.PagamentoResultadoEvent;
import com.empresa.assinatura.domain.port.in.ProcessarResultadoPagamentoUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PagamentoResultadoConsumerTest {

  @Mock
  private ProcessarResultadoPagamentoUseCase useCase;

  @Mock
  private Acknowledgment ack;

  @InjectMocks
  private PagamentoResultadoConsumer consumer;

  @Test
  void deveProcessarEventoEConfirmarAck() {
    PagamentoResultadoEvent evento = new PagamentoResultadoEvent(
        UUID.randomUUID(), UUID.randomUUID(), "APROVADO");
    when(useCase.processar(evento)).thenReturn(Mono.empty());

    consumer.consumir(evento, ack);

    verify(useCase, timeout(1000)).processar(evento);
    verify(ack, timeout(1000)).acknowledge();
  }

  @Test
  void deveRegistrarErroSemAckQuandoUseCaseFalha() {
    PagamentoResultadoEvent evento = new PagamentoResultadoEvent(
        UUID.randomUUID(), UUID.randomUUID(), "RECUSADO");
    when(useCase.processar(any())).thenReturn(Mono.error(new RuntimeException("erro simulado")));

    consumer.consumir(evento, ack);

    verify(useCase, timeout(1000)).processar(evento);
  }
}
