package com.empresa.assinatura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da aplicação de assinatura. Esta classe é responsável por iniciar a aplicação
 * Spring Boot e habilitar o agendamento de tarefas.
 */
@SpringBootApplication
@EnableScheduling
public class AssinaturaApplication {

  /**
   * Método principal que inicia a aplicação Spring Boot.
   *
   * @param args argumentos de linha de comando
   */
  public static void main(String[] args) {
    SpringApplication.run(AssinaturaApplication.class, args);
  }
}
