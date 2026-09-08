# Java Parser Patterns Module

A **self-contained, removable module** containing comprehensive Java code samples for testing the CodeScout Java parser.

## Purpose

This module provides examples of Java patterns and constructs that the CodeScout parser (`ParserService.Grammar\LanguageParser\java`) handles, which were not previously represented in the main LendWise mock repository.

**To remove:** Simply delete the `java-parser-patterns/` folder - the main LendWise project remains unaffected.

## Structure

```
java-parser-patterns/
├── integration-producer-service/    # PRODUCER/SENDER/CLIENT patterns
│   ├── soap/                        # SOAP clients (@WebService, SOAPConnection, WebServiceTemplate)
│   ├── jms/                         # JMS producers (JNDI, createProducer, JmsTemplate)
│   ├── http/                        # HTTP clients (Java 11, Apache, OkHttp, URLConnection)
│   ├── database/                    # DB write patterns (JDBC, JPA, Hibernate, Spring Data, MyBatis)
│   ├── ejb/                         # EJB patterns (@Stateless, @Singleton, @EJB)
│   └── async/                       # Async patterns (CompletableFuture, Streams, Lambdas)
│
└── integration-consumer-service/    # CONSUMER/LISTENER/RECEIVER patterns
    ├── jms/                         # JMS consumers (@JmsListener, createConsumer, MDB)
    ├── soap/                        # SOAP endpoints (@Endpoint, @WebServiceProvider)
    ├── kafka/                       # Kafka listeners (@KafkaListener, @KafkaHandler)
    ├── database/                    # DB read patterns (ResultSet, TypedQuery, Criteria API)
    ├── model/                       # Inheritance patterns (extends, @Override, initializers)
    └── processing/                  # Loop patterns (for, while, do-while, iterators)
```

## Patterns Covered

### Producer Service (18 files)

| Category | Patterns |
|----------|----------|
| SOAP Clients | `@WebService`, `SOAPConnectionFactory`, `WebServiceTemplate.marshalSendAndReceive()` |
| JMS Producers | `InitialContext.lookup()`, `session.createProducer()`, `producer.send()`, `JmsTemplate.convertAndSend()` |
| HTTP Clients | `HttpClient.send()`, `CloseableHttpClient.execute()`, `OkHttpClient.newCall()`, `HttpURLConnection` |
| Database Write | `DriverManager.getConnection()`, `PreparedStatement.executeUpdate()`, `EntityManager.persist()`, `session.save()`, `JpaRepository`, `@Mapper/@Select/@Insert` |
| EJB | `@Stateless`, `@Stateful`, `@Singleton`, `@EJB` injection, `@Schedule` |
| Async | `CompletableFuture.supplyAsync()`, `.thenApply()`, `.thenCombine()`, `Stream.filter().map().collect()` |

### Consumer Service (14 files)

| Category | Patterns |
|----------|----------|
| JMS Consumers | `@JmsListener`, `session.createConsumer()`, `consumer.receive()`, `@MessageDriven` (MDB) |
| SOAP Endpoints | `@Endpoint`, `@PayloadRoot`, `@WebServiceProvider`, `Provider<Source>` |
| Kafka Listeners | `@KafkaListener`, `@KafkaHandler`, `ConsumerRecord`, batch processing |
| Database Read | `ResultSet.next()`, `rs.getString()`, `TypedQuery.getResultList()`, `CriteriaBuilder`, `Subquery` |
| Inheritance | `extends`, `abstract class`, `@Override`, `super.method()` |
| Initializers | `static {}` blocks, instance `{}` blocks |
| Loops | `for`, `while`, `do-while`, enhanced for-each, `Iterator`, labeled break/continue |

## Running the Parser

Test parsing with the .NET parser:

```bash
cd Parser3.0/parser-service-v2

# Parse entire module
dotnet run --project ParserService.Runner -- \
  "C:\Users\2000173735\Desktop\Repos\lendwise-mortgage-system\java-parser-patterns" java

# Parse single file
dotnet run --project ParserService.Runner -- \
  "path\to\file.java" java --single > output.json
```

## Expected Parser Detection

After parsing, verify these patterns are detected:

- **Callees:**
  - `JAVA_JMS_PRODUCE` type for `producer.send()`, `jmsTemplate.send()`
  - `JAVA_DB_*` types for database operations
  - HTTP callees for client requests

- **Snippets:**
  - `InheritsFrom` populated for classes extending base classes
  - `@Override` methods linked to parent

- **Decision Nodes:**
  - Loop constructs (for, while, do-while)
  - Control flow (if-else, switch)

- **Annotations:**
  - `@WebService`, `@JmsListener`, `@KafkaListener`, `@Endpoint`
  - `@Stateless`, `@EJB`, `@PersistenceContext`
  - `@Query`, `@Select`, `@Insert`

## File Count

| Module | Files |
|--------|-------|
| integration-producer-service | 18 |
| integration-consumer-service | 14 |
| **Total** | **32** |
