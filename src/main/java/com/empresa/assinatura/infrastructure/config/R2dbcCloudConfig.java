package com.empresa.assinatura.infrastructure.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configura a ConnectionFactory do R2DBC para o perfil cloud, usando Unix domain socket
 * do Cloud SQL Proxy em vez de conexão TCP.
 *
 * <p>O driver r2dbc-postgresql não suporta Unix socket via URL query param.
 * Esta config programática define o socket diretamente no builder.
 */
@Configuration
@Profile("cloud")
public class R2dbcCloudConfig {

  public static final String SEP = "/";
  public static final String SUFIXO_SOCKET_CLOUD = "/.s.PGSQL.5432";
  @Value("${cloud.sql.socket-path:/cloudsql}")
  private String socketBasePath;

  @Value("${cloud.sql.instance}")
  private String cloudSqlInstance;

  @Value("${spring.r2dbc.username}")
  private String username;

  @Value("${spring.r2dbc.password}")
  private String password;

  /**
   * Cria a ConnectionFactory usando Unix socket do Cloud SQL Proxy.
   *
   * @return ConnectionPool configurado para Cloud SQL via Unix socket
   */
  @Bean
  public ConnectionFactory connectionFactory() {
    String socketPath = socketBasePath + SEP + cloudSqlInstance + SUFIXO_SOCKET_CLOUD;

    PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
        .socket(socketPath)
        .database("assinatura_db")
        .username(username)
        .password(password)
        .build();

    PostgresqlConnectionFactory factory = new PostgresqlConnectionFactory(config);

    ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(factory)
        .maxSize(10)
        .initialSize(2)
        .maxIdleTime(Duration.ofMinutes(30))
        .build();

    return new ConnectionPool(poolConfig);
  }
}
