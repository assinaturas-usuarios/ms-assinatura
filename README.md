# ms-assinatura

Microsserviço responsável pela gestão do ciclo de vida de assinaturas da plataforma de streaming.

## Tecnologias

- Java 25 + Spring Boot 3.5.3
- Spring WebFlux (reativo) + R2DBC + PostgreSQL 16
- Spring Data Redis Reactive (cache)
- Spring Kafka (consumidor e produtor)
- Flyway (migração via JDBC separado)
- Resilience4j (Circuit Breaker)
- MapStruct + Lombok
- Swagger/OpenAPI 3 (springdoc-webflux 2.6.0)
- Micrometer Tracing + Zipkin
- JaCoCo (90% de cobertura)

## Arquitetura

Arquitetura hexagonal com programação reativa (Project Reactor):

- `domain/` — modelos, enums e portas
- `application/` — casos de uso reativos e DTOs
- `infrastructure/` — adapters REST (WebFlux), Kafka, R2DBC, Redis, client HTTP

## Endpoints

| Método | Path                                 | Descrição                          |
|--------|--------------------------------------|------------------------------------|
| POST   | `/v1/assinaturas`                    | Criar nova assinatura              |
| GET    | `/v1/assinaturas/{id}`               | Buscar assinatura por ID           |
| GET    | `/v1/assinaturas/usuario/{id}/ativa` | Buscar assinatura ativa do usuário |
| GET    | `/v1/assinaturas`                    | Listar assinaturas (cursor-based)  |
| DELETE | `/v1/assinaturas/{id}/cancelar`      | Cancelar assinatura                |

### Paginação cursor-based

```
GET /v1/assinaturas?cursor=&status=ATIVA&plano=BASICO&tamanho=20
```

Resposta inclui `proximoCursor` e `hasNext` para navegar nas páginas.

### Exemplo de requisição

```bash
curl -X POST http://localhost:8082/v1/assinaturas \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": "550e8400-e29b-41d4-a716-446655440000", "plano": "BASICO"}'
```

## Planos disponíveis

| Plano    | Valor mensal |
|----------|--------------|
| BASICO   | R$ 19,90     |
| PREMIUM  | R$ 39,90     |
| FAMILIA  | R$ 59,90     |

## Tópicos Kafka

| Tópico                            | Produção | Consumo | Descrição                               |
|-----------------------------------|----------|---------|-----------------------------------------|
| `assinatura.renovacao.solicitada` | X        |         | Solicitar renovação a ms-pagamento      |
| `pagamento.resultado`             |          | X       | Receber resultado do pagamento          |
| `assinatura.cancelada`            | X        |         | Notificar cancelamento                  |
| `assinatura.suspensa`             | X        |         | Notificar suspensão                     |

Tópicos DLT (Dead Letter): `pagamento.resultado.DLT`

## Variáveis de Ambiente

| Variável                  | Descrição               | Padrão      |
|---------------------------|-------------------------|-------------|
| `DB_URL`                  | URL R2DBC do PostgreSQL | obrigatório |
| `DB_FLYWAY_URL`           | JDBC URL (Flyway)       | obrigatório |
| `DB_USERNAME`             | Usuário do banco        | obrigatório |
| `DB_PASSWORD`             | Senha do banco          | obrigatório |
| `REDIS_HOST`              | Host do Redis           | localhost   |
| `REDIS_PORT`              | Porta do Redis          | 6379        |
| `KAFKA_BOOTSTRAP_SERVERS` | Servidores Kafka        | obrigatório |
| `MS_USUARIO_URL`          | URL base do ms-usuario  | obrigatório |
| `ZIPKIN_URL`              | URL do Zipkin           | opcional    |

## Circuit Breaker

Protege a chamada HTTP ao ms-usuario:

- Janela deslizante: 10 chamadas
- Taxa de falha para abertura: 50%
- Tempo de espera no estado aberto: 10s
- Fallback: retorna `false` (usuário não encontrado)

## Cache Redis

- Prefixo da chave: `assinatura:`
- TTL: 5 minutos
- Invalidado em: cancelamento, renovação, suspensão

## Como executar

```bash
# Com toda a infraestrutura via Docker Compose (a partir da raiz)
docker compose up --build

# Apenas a infraestrutura, servico rodando localmente
cd ..
docker compose up postgres redis kafka zookeeper -d
cd ms-assinatura
./mvnw spring-boot:run
```

## Documentação da API

Com o serviço em execução:

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs
- Actuator: http://localhost:8082/actuator/health

## Testes

```bash
./mvnw test      # Executar testes
./mvnw verify    # Testes + verificação de cobertura JaCoCo
```

Cobertura mínima configurada: **90% de linhas**.

---

### Arquitetura Hexagonal (Ports & Adapters)

Todo o domínio fica isolado em `domain/` e `application/usecase/`. Controllers, R2DBC, Redis e Kafka são adapters em `infrastructure/`. Nenhum use case importa classes Spring Data, R2DBC ou Kafka — apenas interfaces de porta.

```
domain/port/in/    → CriarAssinaturaUseCase, BuscarAssinaturaUseCase, ...
domain/port/out/   → AssinaturaRepositoryPort, AssinaturaCachePort, EventPublisherPort, ...
application/usecase/ → implementações dos use cases (regras de negócio puras)
infrastructure/    → adapters: REST, R2DBC, Redis, Kafka, WebClient
```

### Spring WebFlux + R2DBC (Reativo de ponta a ponta)

Todos os use cases retornam `Mono<T>` ou `Flux<T>`. O pipeline reativo não bloqueia nenhuma thread de I/O. O único `block()` está no `RenovacaoAgendadaScheduler`, dentro de virtual threads gerenciadas pelo `StructuredTaskScope`. Também foi usado R2DBC, que define uma API totalmente assíncrona para comunicação com bancos relacionais como o PostgreSQL.

### Cursor-based Pagination

A listagem de assinaturas usa paginação por cursor em vez de offset. A lógica de construção da página está encapsulada no próprio record `CursorListaResponse` como método estático `fromListaItens`, sem vazar lógica de paginação para os use cases.

### Cache Reativo com Redis

`BuscarAssinaturaUseCaseImpl` consulta o cache antes de ir ao banco (`cache.buscar → switchIfEmpty → banco`). O cache é invalidado no cancelamento, renovação e suspensão via `AssinaturaCachePort`.

### Kafka + Event-Driven + Manual ACK + DLQ

O consumidor `pagamento.resultado` usa `AckMode.MANUAL` para confirmar o offset apenas após processamento bem-sucedido. Mensagens que falham após as tentativas configuradas vão para `pagamento.resultado.DLT`. O produtor é idempotente (`enable.idempotence=true`).

### Circuit Breaker (Resilience4j)

A chamada HTTP ao ms-usuario via `UsuarioWebClient` é protegida por Circuit Breaker. Se ms-usuario estiver indisponível, o fallback retorna `false`, e a criação de assinatura falha com erro de usuário não encontrado sem travar threads.

### StructuredTaskScope + Virtual Threads (Java 21/25)

`RenovacaoAgendadaScheduler` processa cada assinatura vencendo em uma virtual thread separada via `StructuredTaskScope.open()`. O método principal bloqueia até TODAS as tarefas concluírem, eliminando sobreposição de execuções de cron consecutivas. A query usa `FOR UPDATE SKIP LOCKED` para suportar múltiplas instâncias do scheduler sem conflito.

### Micrometer

Contadores granulares para renovações aprovadas, recusadas, assinaturas suspensas e erros de agendamento, expostos via `/actuator/prometheus` para o Prometheus coletar.

### ProblemDetail (RFC 7807)

Erros de domínio retornam respostas estruturadas no padrão RFC 7807 via `GlobalExceptionHandler`, com `type`, `title`, `status` e `detail`.

