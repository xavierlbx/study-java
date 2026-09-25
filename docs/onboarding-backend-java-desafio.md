> **PARTE 2**
# O desafio: API de Gestão de Ordens de Serviço

**Contexto.** Numa operação de internet, uma *ordem de serviço* (OS) representa um trabalho de campo: uma instalação, um reparo ou uma mudança de endereço. Você vai construir uma API que faz o **CRUD** dessas ordens e controla o **ciclo de vida do status** de cada uma. É um tema simples de propósito — o foco é você aplicar bem a arquitetura e os padrões, não decorar regra de negócio.

> **O que "pronto" significa**
> Uma API Spring Boot que sobe, conecta no nosso **MySQL de testes**, expõe os endpoints abaixo (testáveis pelo **Swagger**), organizada em **arquitetura hexagonal**, com **logs em português**, **código em inglês**, tratamento de erro padronizado e testes. Detalhe de cada item na Parte 3.

## Stack obrigatória

- **Java 21** · **Spring Boot 3.5.x** · **Gradle** (gere o esqueleto em `start.spring.io`).
- Dependências: `Spring Web`, `Spring Data JPA`, `MySQL Driver`, `Validation`, `Lombok`, `springdoc-openapi-starter-webmvc-ui` (Swagger).

## Arquitetura hexagonal (Ports & Adapters) — o padrão da casa

A ideia central: **separar a regra de negócio das tecnologias**. O "miolo" (domínio + casos de uso) não sabe que existe Spring, HTTP ou MySQL. Ele conversa com o mundo por **portas** (interfaces). As tecnologias entram como **adaptadores** que implementam ou chamam essas portas.

| Camada | Responsabilidade |
|---|---|
| `domain` | O modelo de negócio (`ServiceOrder`), os enums, as exceções de negócio e as **portas** (interfaces). **Nunca importa Spring nem JPA.** |
| `application` | Os **casos de uso** — uma classe por operação (criar, buscar, atualizar status...). Orquestram o domínio usando as portas. |
| `infrastructure` | Os **adaptadores**: entrada (controllers REST) e saída (persistência JPA), além das configs (Swagger, datasource). |

**ESTRUTURA DE PACOTES ESPERADA**

```
br.com.desktop.serviceorder.api
├─ domain/serviceorder
│  ├─ ServiceOrder               // modelo de domínio (regras de negócio)
│  ├─ ServiceOrderStatus         // enum: OPEN, SCHEDULED, IN_PROGRESS, DONE, CANCELED
│  ├─ ServiceOrderType           // enum: INSTALLATION, REPAIR, RELOCATION
│  ├─ exception/                 // ServiceOrderNotFoundException, DuplicateProtocolException...
│  └─ repository/
│     └─ ServiceOrderRepository  // ← PORTA (interface, fala em tipos de domínio)
├─ application/usecase/serviceorder
│  ├─ CreateServiceOrderUseCase
│  ├─ GetServiceOrderByIdUseCase
│  ├─ SearchServiceOrdersUseCase
│  ├─ UpdateServiceOrderUseCase
│  ├─ UpdateServiceOrderStatusUseCase
│  └─ DeleteServiceOrderUseCase
└─ infrastructure
   ├─ config/                    // SwaggerConfig
   └─ adapter
      ├─ in/web
      │  ├─ ServiceOrderController
      │  ├─ dto/                 // ServiceOrderRequest, ServiceOrderResponse, ChangeStatusRequest (records)
      │  ├─ mapper/               // ServiceOrderWebMapper (domínio ↔ DTO)
      │  └─ exception/            // GlobalExceptionHandler, ErrorResponse
      └─ out/persistence
         ├─ ServiceOrderEntity              // @Entity (JPA) – separada do domínio!
         ├─ JpaServiceOrderRepository        // extends JpaRepository
         ├─ ServiceOrderPersistenceAdapter   // implements ServiceOrderRepository (a porta)
         └─ ServiceOrderPersistenceMapper    // Entity ↔ domínio
```

> **A regra de ouro**
> Se uma classe dentro de `domain` precisar de um `import org.springframework...` ou `jakarta.persistence...`, algo está no lugar errado. O domínio é Java puro. É isso que torna a arquitetura "hexagonal" — e é o principal ponto que vamos olhar.

## Como o fluxo se conecta (exemplo de UMA operação)

Abaixo está o "buscar por ID" atravessando todas as camadas. **Estude este exemplo**, entenda o caminho Controller → UseCase → Porta → Adapter → Banco, e depois replique o padrão para os outros endpoints. Os demais são **com você**.

**1. A PORTA (NO DOMÍNIO) — SÓ INTERFACE, TIPOS DE DOMÍNIO**

```java
public interface ServiceOrderRepository {
    Optional<ServiceOrder> findById(Long id);
    Page<ServiceOrder> search(ServiceOrderFilter filter, Pageable pageable);
    boolean existsByProtocol(String protocol);
    ServiceOrder save(ServiceOrder order);
    void softDelete(Long id);
}
```

**2. O CASO DE USO (APPLICATION) — DEPENDE DA PORTA, NÃO DO JPA**

```java
@Component
@RequiredArgsConstructor
public class GetServiceOrderByIdUseCase {

    private final ServiceOrderRepository repository; // a porta (interface)

    public ServiceOrder execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
    }
}
```

**3. O ADAPTER DE SAÍDA (INFRASTRUCTURE) — IMPLEMENTA A PORTA USANDO JPA**

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceOrderPersistenceAdapter implements ServiceOrderRepository {

    private final JpaServiceOrderRepository jpa;      // Spring Data (fala com o MySQL)
    private final ServiceOrderPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceOrder> findById(Long id) {
        return jpa.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public ServiceOrder save(ServiceOrder order) {
        var entity = mapper.toEntity(order);
        return mapper.toDomain(jpa.save(entity));
    }
    // ... demais métodos: mesmo padrão
}
```

**4. O CONTROLLER (INFRASTRUCTURE/IN) — TRADUZ HTTP E DOCUMENTA NO SWAGGER**

```java
@RestController
@RequestMapping("/v1/service-order-management/service-orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Service Orders", description = "Gestão de Ordens de Serviço")
public class ServiceOrderController {

    private final GetServiceOrderByIdUseCase getById;
    private final ServiceOrderWebMapper webMapper;

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma ordem de serviço por ID")
    @ApiResponse(responseCode = "200", description = "Ordem encontrada")
    @ApiResponse(responseCode = "404", description = "Ordem nao encontrada")
    public ResponseEntity<ServiceOrderResponse> getById(@PathVariable @Min(1) Long id) {
        ServiceOrder order = getById.execute(id);
        return ResponseEntity.ok(webMapper.toResponse(order));
    }
    // ... demais endpoints
}
```

## Tratamento de erro (padrão obrigatório)

Todo erro vira uma resposta JSON **no mesmo formato**. Cada exceção de negócio é traduzida para um status HTTP num **único** ponto: o tratador global. Nada de `try/catch` espalhado nos controllers.

```java
public record ErrorResponse(LocalDateTime timestamp, int status, String message) {}

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ServiceOrderNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateProtocolException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateProtocolException ex) {
        log.warn("Conflito de protocolo: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Erro inesperado ao processar a requisicao.", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(LocalDateTime.now(), status.value(), message));
    }
}
```

## Logs: português na mensagem, inglês no código

```java
// ✓ bom: mensagem em PT, identificadores em inglês, parametrizado, nivel certo
log.info("Ordem de servico criada: protocolo={}", order.getProtocol());
log.warn("Transicao de status invalida: de={} para={}", current, target);
log.error("Falha ao salvar ordem de servico.", ex);

// ✗ evite: concatenar string, logar em ingles, logar dado sensivel do cliente
log.info("created order " + order);
log.info("Documento do cliente: " + customerDocument);
```

> **Regra dos logs**
> Sempre use `@Slf4j` (do Lombok) e placeholders `{}` (nunca `"texto " + variavel`). Níveis: `info` para eventos de negócio, `warn` para situações esperadas mas atípicas (ex.: conflito), `error` só para falhas inesperadas (sempre com a exceção). Nunca logar senha, documento ou dado pessoal.

---
> **PARTE 2**
## Modelo de dados e conexão com o banco

Nós **não usamos Flyway** neste desafio: você mesmo cria a tabela direto no banco de testes, rodando o SQL abaixo no seu cliente MySQL (Workbench, DBeaver, etc.). Crie num **schema/tabela isolado** pra não encostar em dado de ninguém.

```sql
CREATE TABLE service_order (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  protocol          VARCHAR(20)  NOT NULL UNIQUE,
  customer_name     VARCHAR(120) NOT NULL,
  customer_document VARCHAR(14)  NOT NULL,
  type              VARCHAR(20)  NOT NULL,   -- INSTALLATION | REPAIR | RELOCATION
  status            VARCHAR(20)  NOT NULL,   -- OPEN | SCHEDULED | IN_PROGRESS | DONE | CANCELED
  scheduled_date    DATE         NULL,
  notes             VARCHAR(500) NULL,
  created_at        DATETIME     NOT NULL,
  updated_at        DATETIME     NOT NULL,
  deleted_at        DATETIME     NULL
);
```

### Conexão (o ponto que mais confunde)

Seu líder vai te passar os dados de acesso ao **MySQL de testes** (as mesmas credenciais da `product-catalog-api`). Configure num profile local e **por variável de ambiente** — nunca escreva a senha no código nem suba pro Git.

**SRC/MAIN/RESOURCES/APPLICATION-LOCAL.YAML**

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: none        # voce cria a tabela na mao; o app nao mexe no schema
    properties:
      hibernate.format_sql: true
```

Rode com o profile `local` ativo (ex.: variável `SPRING_PROFILES_ACTIVE=local`) e as variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` definidas no seu ambiente / na configuração de execução do IntelliJ.

> **Segurança & Git**
> Coloque `application-local.yaml` (e qualquer arquivo com segredo) no `.gitignore`. Se uma credencial vazar num commit, ela é considerada comprometida — avise o time imediatamente. Isso faz parte da avaliação.

## Os endpoints esperados

Todos sob o prefixo `/v1/service-order-management/service-orders`. Este **contrato é fixo** (rotas, formatos e status) porque simula uma API real que outros sistemas consumiriam.

| Método | Rota | O que faz | Status |
|---|---|---|---|
| **POST** | `/` | Cria uma OS | 201 / 400 / 409 |
| **GET** | `/` | Busca paginada com filtros | 200 / 204 |
| **GET** | `/{id}` | Busca por ID | 200 / 404 |
| **PUT** | `/{id}` | Atualiza dados da OS | 200 / 400 / 404 |
| **PATCH** | `/{id}/status` | Muda o status da OS | 200 / 400 / 404 / 409 |
| **DELETE** | `/{id}` | Remove a OS (soft delete) | 204 / 404 |

**O OBJETO DE RESPOSTA (SERVICEORDERRESPONSE) — USADO EM TODOS OS RETORNOS DE SUCESSO COM CORPO**

```json
{
  "id": 1,
  "protocol": "OS-2026-0001",
  "customerName": "Maria Souza",
  "customerDocument": "12345678901",
  "type": "INSTALLATION",
  "status": "OPEN",
  "scheduledDate": "2026-09-20",
  "notes": "Cliente prefere periodo da manha",
  "createdAt": "2026-09-03T10:15:30",
  "updatedAt": "2026-09-03T10:15:30"
}
```

### `POST` `/v1/service-order-management/service-orders`

Cria uma nova OS. O `status` **não** vem no corpo — toda OS nasce como `OPEN`. `createdAt`/`updatedAt` são preenchidos pelo servidor.

**REQUEST**

```json
{
  "protocol": "OS-2026-0001",
  "customerName": "Maria Souza",
  "customerDocument": "12345678901",
  "type": "INSTALLATION",
  "scheduledDate": "2026-09-20",
  "notes": "Cliente prefere periodo da manha"
}
```

**201 CREATED** → devolve o `ServiceOrderResponse` completo (com `id` e `status: "OPEN"`).

**409 CONFLICT** → protocolo já existe

```json
{ "timestamp": "2026-09-03T10:16:00", "status": 409,
  "message": "Ja existe uma ordem de servico com o protocolo OS-2026-0001." }
```

**400 BAD REQUEST** → validação (ex.: `customerName` em branco, `type` inválido).

### `GET` `/v1/service-order-management/service-orders`

Busca paginada. Filtros opcionais via query string: `protocol`, `status`, `type`. Paginação: `page` (padrão 0) e `size` (padrão 20). Registros com `deletedAt` preenchido **não** aparecem.

**EXEMPLO**

```
GET /v1/service-order-management/service-orders?status=OPEN&page=0&size=20
```

**200 OK**

```json
{
  "content": [ { "id": 1, "protocol": "OS-2026-0001", "status": "OPEN", ... } ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

**204 NO CONTENT** → nenhuma OS encontrada (sem corpo).

### `GET` `/v1/service-order-management/service-orders/{id}`

**200 OK** → o `ServiceOrderResponse`.
**404 NOT FOUND** → id inexistente ou já removido

```json
{ "timestamp": "2026-09-03T10:17:10", "status": 404,
  "message": "Ordem de servico nao encontrada: id=99." }
```

### `PUT` `/v1/service-order-management/service-orders/{id}`

Atualiza os **dados** da OS. O `protocol` é imutável e o `status` **não** muda aqui (isso é no PATCH).

**REQUEST**

```json
{
  "customerName": "Maria Souza Lima",
  "customerDocument": "12345678901",
  "type": "REPAIR",
  "scheduledDate": "2026-09-22",
  "notes": "Reagendado a pedido do cliente"
}
```

**200 OK** → o `ServiceOrderResponse` atualizado. **404** → não encontrada. **400** → validação.

### `PATCH` `/v1/service-order-management/service-orders/{id}/status`

Muda o status respeitando as transições válidas (ver regras de negócio). Ao mover para `SCHEDULED`, o `scheduledDate` é obrigatório e não pode ser no passado.

**REQUEST**

```json
{ "status": "SCHEDULED", "scheduledDate": "2026-09-20" }
```

**200 OK** → o `ServiceOrderResponse` com o novo status.
**409 CONFLICT** → transição não permitida

```json
{ "timestamp": "2026-09-03T10:18:00", "status": 409,
  "message": "Transicao de status invalida: de DONE para OPEN." }
```

**400 BAD REQUEST** → `scheduledDate` ausente ou no passado. **404** → não encontrada.

### `DELETE` `/v1/service-order-management/service-orders/{id}`

**Soft delete**: não apaga a linha — preenche `deleted_at`. A partir daí a OS some das buscas e responde 404 em novas operações.

**204 NO CONTENT** → removida (sem corpo). **404** → não encontrada.

## Regras de negócio

1. **Protocolo único:** não pode haver duas OS com o mesmo `protocol` → `409`.
2. **Status inicial:** toda OS criada nasce `OPEN` (ignore/rejeite `status` no POST).
3. **Transições válidas de status:**
   `OPEN → SCHEDULED | CANCELED` · `SCHEDULED → IN_PROGRESS | CANCELED` · `IN_PROGRESS → DONE | CANCELED`.
   `DONE` e `CANCELED` são finais (não saem deles). Qualquer outra transição → `409`.
4. **Agendamento:** ao entrar em `SCHEDULED`, `scheduledDate` é obrigatório e não pode ser data passada → `400`.
5. **Soft delete:** nunca faça `DELETE` físico; buscas ignoram registros com `deletedAt`.

> **Onde essa lógica mora?**
> A validação de **transição de status** é regra de negócio pura — o lugar natural dela é **no domínio** (ex.: um método `ServiceOrder.changeStatusTo(novoStatus)` que decide se pode). Já a validação de formato do request (`@NotBlank`, `@Min`) mora no DTO de entrada. Pensar sobre "o que vai onde" é exatamente o que estamos avaliando.

## O que seguir à risca × onde você tem liberdade

| Siga à risca (padrão da casa) | Você decide (mostre seu critério) |
|---|---|
| Java 21, Spring Boot 3.5.x, Gradle. | Como implementa cada caso de uso por dentro. |
| As 3 camadas e a **regra de ouro** do domínio limpo. | Mapear camadas com **MapStruct** ou na mão — sua escolha, saiba justificar. |
| O **contrato dos endpoints** (rotas, request/response, status). | Domínio como `class` ou `record`. |
| Modelo de erro `{timestamp, status, message}` + tratador global. | Estratégia e cobertura de **testes** (mas tem que ter). |
| Logs em PT, código em inglês, SLF4J com `{}`. | Validações e mensagens extras que julgar úteis. |
| Swagger em todos os endpoints. | Protocolo informado no request **ou** gerado automático (bônus). |
| Nomes: `*UseCase`, `*Controller`, `*Repository` (porta), `*PersistenceAdapter`, `*Entity`, `*Request`/`*Response`. | Organização fina dos subpacotes. |
| Git limpo e sem credenciais. | Os extras abaixo. |

### Extras desejáveis (se sobrar fôlego)

Não são obrigatórios — são pra você mostrar o quanto consegue ir além. Priorize entregar o obrigatório bem-feito antes de partir pra cá.

- Paginação e filtros bem-feitos (envelope `content/page/size/totalElements`).
- Mapeamento com **MapStruct**.
- **Docker**: um `Dockerfile` e/ou `docker-compose` subindo app + MySQL local.
- Testes de integração com **Testcontainers** (sobe um MySQL real no teste).
- Geração automática do `protocol` (ex.: `OS-{ano}-{sequencial}`).
- Um segundo agregado relacionado (ex.: `Technician` alocado à OS).

---
> **PARTE 3**
# Como você será avaliado

Esta é a lista que usaremos pra te dar feedback. Use como seu próprio checklist antes de dizer "terminei". Não precisa marcar tudo de primeira — ela mostra onde está a régua.

### Obrigatório

- [ ] O projeto **compila e sobe** (`./gradlew bootRun`) e **conecta no MySQL de testes**.
- [ ] Os **6 endpoints** funcionam e são testáveis pelo **Swagger UI**.
- [ ] **Arquitetura hexagonal** respeitada: domínio sem Spring/JPA; porta no domínio; adapter na infra; `Entity` separada do modelo de domínio.
- [ ] **Validação** nos requests + **tratador global** devolvendo o `ErrorResponse` padronizado.
- [ ] As **5 regras de negócio** implementadas (protocolo único, status inicial, transições, agendamento, soft delete).
- [ ] **Logs** em português, código em inglês, SLF4J parametrizado, níveis corretos, sem dado sensível.
- [ ] **Swagger** com `@Tag`/`@Operation`/`@ApiResponse` em todos os endpoints.
- [ ] **Git**: histórico de commits pequenos e descritivos, uma branch de trabalho, `README` (como rodar + variáveis), `.gitignore`, **zero credencial** no repositório.
- [ ] **Testes**: unitários dos casos de uso / regras do domínio (com Mockito) + ao menos 1 teste de integração.
- [ ] Códigos HTTP corretos (201 no create, 204 no delete/no-content, 400/404/409 nos erros).

### Desejável (conta pontos)

- [ ] **SOLID** visível — especialmente **DIP** (caso de uso depende da porta, não do JPA) e **SRP** (uma responsabilidade por classe).
- [ ] Nomes claros, métodos curtos, ausência de código morto.
- [ ] Um ou mais dos **extras** da Parte 2.
- [ ] Um `README` que explique suas **decisões** (por que MapStruct ou não, onde colocou cada regra).

> **No fim, um bate-papo de ~15 min**
> Vamos te pedir pra **explicar seu código**: onde estão as fronteiras da hexagonal, por que cada regra ficou onde ficou, o que você mudaria. Saber explicar vale tanto quanto o código funcionar — é assim que a gente vê que você **entendeu**, e não apenas seguiu uma receita.

---
> **PARTE 4**
# Como se organizar

Sugestão de ataque em etapas. **Não tente fazer tudo de uma vez.** Faça funcionar simples, depois melhore.

- **Etapa 0 — Ambiente.** JDK 21, IntelliJ, projeto gerado no `start.spring.io`, tabela criada no banco, app subindo e conectando.
- **Etapa 1 — Uma fatia vertical.** Faça **só o GET por ID** funcionar de ponta a ponta, passando por todas as camadas. Entendeu esse caminho? O resto é repetição do padrão.
- **Etapa 2 — CRUD completo.** POST, GET lista, PUT, DELETE (soft). Sem se preocupar ainda com todos os detalhes de erro.
- **Etapa 3 — Regras e status.** PATCH de status com transições válidas + agendamento + protocolo único.
- **Etapa 4 — Acabamento.** Swagger, tratador global de erros, validação, logs em PT.
- **Etapa 5 — Testes e README.** Testes dos casos de uso + integração, README, e passar o checklist da Parte 3.

> **Ritmo saudável**
> Commit pequeno e frequente (um por etapa/funcionalidade, mensagem no imperativo: "adiciona criacao de OS"). Fez uma etapa e funcionou? Chame o time pra um checkpoint rápido — feedback cedo evita retrabalho grande.

## Armadilhas comuns de quem vem do JavaScript

- **Tipagem:** não existe "qualquer coisa". Todo valor tem um tipo; abrace o compilador — ele te avisa dos erros antes de rodar.
- **`null`:** prefira `Optional` nas buscas em vez de retornar `null`. Compare objetos com `.equals()`, não `==`.
- **Não é `console.log`:** use o `log` do SLF4J, não `System.out.println`.
- **Injeção de dependência:** você não dá `new` nas suas dependências — declara no construtor e o Spring injeta (é o que o `@RequiredArgsConstructor` faz).
- **Camadas separadas:** o objeto que entra pela web (`Request`), o do domínio (`ServiceOrder`) e o do banco (`Entity`) são **três coisas diferentes** de propósito. Não use a Entity como resposta da API.

## Glossário rápido

| Termo | O que é |
|---|---|
| **Porta (Port)** | Uma **interface** no domínio que diz "preciso de alguém que saiba salvar/buscar OS", sem dizer como. |
| **Adapter** | A **implementação** de uma porta usando uma tecnologia (ex.: o adapter JPA que fala com o MySQL). |
| **Caso de uso (Use Case)** | Uma classe que executa **uma** operação de negócio (criar OS, mudar status...). |
| **DTO** | Objeto simples só para transportar dados na fronteira (ex.: `Request`/`Response` da API). |
| **Entity** | Classe anotada com `@Entity` que o JPA mapeia para uma tabela do banco. |
| **DIP** | Princípio do SOLID: dependa de **abstrações** (a porta), não de implementações (o JPA). |
| **Soft delete** | "Apagar" marcando uma data em `deleted_at`, sem remover a linha de fato. |

Dúvida trava? Pesquisou uns 30 minutos e não saiu? Chame alguém do time — pedir ajuda no momento certo é sinal de maturidade, não de fraqueza. Boa jornada. 🚀
