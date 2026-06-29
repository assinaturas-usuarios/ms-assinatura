package com.empresa.assinatura.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

/**
 * Configura o TaskScheduler para usar virtual threads (Java 25 / Project Loom). Cada execucao de
 *
 * @Scheduled corre numa virtual thread dedicada, eliminando o custo de bloqueio de plataforma
 *        threads e permitindo .block() seguro fora do event loop do Netty.
 */
@Configuration
public class SchedulerConfig {

  /**
   * Configura o TaskScheduler para usar virtual threads.
   *
   * @return TaskScheduler configurado
   */
  @Bean
  public TaskScheduler taskScheduler() {
    SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
    scheduler.setVirtualThreads(true);
    scheduler.setThreadNamePrefix("scheduler-vt-");
    return scheduler;
  }
}