package com.empresa.assinatura.infrastructure.scheduler;

import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.port.in.RenovarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Agendador de renovações de assinatura.
 *
 * <p><b>StructuredTaskScope + Virtual Threads:</b>
 * cada assinatura é processada em uma virtual thread separada via
 * {@link StructuredTaskScope#open()}, permitindo paralelismo de I/O sem
 * bloquear threads de plataforma. O método principal aguarda TODAS as tarefas concluírem antes
 * de retornar, eliminando sobreposição entre execuções consecutivas do cron.
 *
 * <p><b>FOR UPDATE SKIP LOCKED:</b> a query
 * {@code buscarParaRenovar} usa {@code FOR UPDATE SKIP LOCKED} no banco, permitindo que múltiplas
 * instâncias do scheduler rodem em paralelo sem processar a mesma assinatura duas vezes.
 *
 */
@Component
@Slf4j
public class RenovacaoAgendadaScheduler {

  private final AssinaturaRepositoryPort repository;
  private final RenovarAssinaturaUseCase renovarUseCase;
  private final Counter contadorErrosMeterRenovacao;

  /**
   * Construtor da classe RenovacaoAgendada.
   *
   * @param repository      porta de repositório de assinaturas
   * @param renovarUseCase   caso de uso para renovar assinaturas
   * @param meterRegistry    registro de métricas do Micrometer
   */
  public RenovacaoAgendadaScheduler(AssinaturaRepositoryPort repository,
      RenovarAssinaturaUseCase renovarUseCase,
      MeterRegistry meterRegistry) {
    this.repository = repository;
    this.renovarUseCase = renovarUseCase;
    this.contadorErrosMeterRenovacao = Counter.builder("assinatura.renovacoes.erros")
        .description("Total de erros ao iniciar renovações")
        .register(meterRegistry);
  }

  /**
   * Método agendado para processar as renovações de assinaturas vencendo na data atual. Executa
   * diariamente à meia-noite, buscando assinaturas para renovar e processando cada uma em uma
   * virtual thread separada, garantindo que todas as renovações sejam concluídas antes de
   * retornar.
   */
  @Scheduled(cron = "0 */3 * * * *")
  public void processarRenovacoes() {
    LocalDate hoje = LocalDate.now(ZoneId.systemDefault());
    log.info("Iniciando renovações para: {}", hoje);

    List<Assinatura> assinaturas = buscarAssinaturasParaRenovar(hoje);
    if (CollectionUtils.isEmpty(assinaturas)) {
      log.info("Nenhuma assinatura para renovar em {}", hoje);
      return;
    }

    log.info("Processando {} renovações via StructuredTaskScope + virtual threads",
        assinaturas.size());

    if (!threadsDeRenovacaoExecutadasComSucesso(assinaturas)) {
      return;
    }

    log.info("Processamento de renovações concluído para {}", hoje);
  }

  private List<Assinatura> buscarAssinaturasParaRenovar(LocalDate hoje) {
    return repository.buscarParaRenovar(hoje)
        .collectList()
        .block(Duration.ofSeconds(30));
  }

  private boolean threadsDeRenovacaoExecutadasComSucesso(List<Assinatura> assinaturas) {
    try (var scope = StructuredTaskScope.open()) {
      renovarAssinaturasPorThread(assinaturas, scope);
    } catch (InterruptedException e) {
      interromperThreadPorExcecao(e);
      return false;
    }
    return true;
  }

  private static void interromperThreadPorExcecao(InterruptedException e) {
    Thread.currentThread().interrupt();
    log.error("Processamento de renovações interrompido", e);
  }

  private void renovarAssinaturasPorThread(List<Assinatura> assinaturas,
      StructuredTaskScope<Object, Void> scope) throws InterruptedException {
    var snapshotFactory = ContextSnapshotFactory.builder().build();
    var snapshot = snapshotFactory.captureAll();
    assinaturas
        .forEach(assinatura ->
            scope.fork(snapshot.wrap(() -> iniciarRenovacaoSync(assinatura))));
    scope.join();
  }

  private void iniciarRenovacaoSync(Assinatura assinatura) {
    try {
      renovarUseCase.renovar(assinatura).block(Duration.ofSeconds(10));
    } catch (Exception e) {
      contadorErrosMeterRenovacao.increment();
      log.error("Erro ao iniciar renovação: assinaturaId={}", assinatura.getId(), e);
    }
  }
}
