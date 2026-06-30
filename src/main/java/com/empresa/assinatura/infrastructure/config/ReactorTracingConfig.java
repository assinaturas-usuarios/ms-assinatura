package com.empresa.assinatura.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class ReactorTracingConfig {

  @PostConstruct
  public void init() {
    Hooks.enableAutomaticContextPropagation();
  }
}