# Plano de Implementação — Onboarding Backend Java

**Desktop · Time de Payments**
**Onboarding Backend Java — API de Gestão de Ordens de Serviço**

Guia de execução passo a passo, organizado em etapas incrementais e atrelado a commits, construído a partir do documento oficial de onboarding do time de Payments. Cobre o que é obrigatório, os extras selecionados como relevantes, a checklist de validação cruzada com os critérios de avaliação e os pontos de atenção para o desenvolvimento.

| | |
|---|---|
| **Documento-fonte** | Onboarding Backend Java – Desktop (Time de Payments) |
| **Stack obrigatória** | Java 21 · Spring Boot 3.5.x · Gradle · H2 (banco em arquivo local, modo MySQL) |
| **Arquitetura** | Hexagonal (Ports & Adapters) |
| **Duração sugerida** | 6 a 8 semanas (estudo + implementação) |
| **Data de geração** | 22 de setembro de 2026 |

> **Como usar este documento:** siga a Seção 2 na ordem apresentada, sem pular etapas. Cada etapa lista os temas de estudo relacionados (Parte 1 do material original), o que implementar, sugestões de mensagens de commit e um bloco com o MVP a validar antes de seguir adiante. A Seção 3 é a conferência final contra os critérios de avaliação e a Seção 4 reúne os pontos de atenção.

---

## 1. Escopo: o que é obrigatório e o que foi selecionado

O documento original separa claramente o que deve ser seguido à risca do que fica a critério de quem implementa. A tabela abaixo resume o obrigatório; a lista de extras logo em seguida mostra quais itens "desejáveis" citados no material valem a pena entrar no plano — e por quê.

### 1.1 Obrigatório (não é opção)

- Java 21, Spring Boot 3.5.x, Gradle — esqueleto via start.spring.io.
- As 3 camadas da arquitetura hexagonal e a **regra de ouro**: domain nunca importa Spring nem JPA.
- Contrato fixo dos 6 endpoints (rotas, payloads, status HTTP).
- Modelo de erro padronizado `{timestamp, status, message}` + tratador global.
- As 5 regras de negócio (protocolo único, status inicial OPEN, transições válidas, agendamento obrigatório em SCHEDULED, soft delete).
- Logs em português (mensagem) / inglês (código), SLF4J com placeholders `{}`, sem dado sensível.
- Swagger/OpenAPI em todos os endpoints (`@Tag`, `@Operation`, `@ApiResponse`).
- Git limpo: commits pequenos e descritivos, branch de trabalho, README, `.gitignore`, zero credencial versionada.
- Testes: unitários dos casos de uso/regras de domínio (Mockito) + pelo menos 1 teste de integração.
- Códigos HTTP corretos (201 create, 204 delete/no-content, 400/404/409 nos erros).
- SOLID visível, especialmente DIP (caso de uso depende da porta, não do JPA) e SRP.

### 1.2 Ambiente: banco de dados H2 em arquivo local

O documento original pressupõe conexão com o MySQL de testes compartilhado pelo time, usando credenciais fornecidas por um líder. Como esse acesso não está disponível neste contexto, este plano substitui esse banco por um **H2 em modo arquivo, com compatibilidade MySQL** (`jdbc:h2:file:./data/service_orders;MODE=MySQL`), embarcado na própria aplicação — não há nada para instalar nem container para subir, e os dados ficam gravados em disco na pasta `./data` do projeto. Essa troca é uma adaptação de infraestrutura, não uma escolha estética: o README deve deixar explícito que se trata de um banco H2 local e não do banco real do time, para que qualquer avaliador entenda a decisão.

Consequências práticas dessa escolha:

- **Os dados persistem** entre reinícios da aplicação: o H2 grava o banco no arquivo `./data/service_orders.mv.db`. Para "zerar" o banco, basta parar a aplicação e apagar a pasta `data/`.
- **A pasta `data/` não vai para o Git:** ela é estado local de cada máquina — adicione `data/` (ou `*.mv.db` / `*.trace.db`) ao `.gitignore`.
- **Tabela criada à mão, como no documento original:** sem Flyway e sem `schema.sql` automático. O script oficial (idêntico à Parte 2 do onboarding) fica versionado em `src/main/resources/db/service_order.sql` e é executado **uma única vez** pelo H2 Console; com `ddl-auto: none`, a aplicação nunca mexe no schema. Como o banco é em arquivo, a tabela continua lá entre reinícios.
- **Acesso concorrente ao arquivo:** o H2 em arquivo bloqueia o banco para um único processo. Adicione `AUTO_SERVER=TRUE` à URL se quiser abrir o mesmo arquivo pelo IntelliJ (Database tool) enquanto a aplicação roda.
- **Credenciais por variável de ambiente, sem arquivo `.env`:** como na Parte 2 do onboarding, o datasource fica em `application-local.yaml` (gitignored) lendo `${DB_NAME}`, `${DB_USER}` e `${DB_PASSWORD}` do sistema operacional, sem valor padrão. `DB_HOST`/`DB_PORT` do documento original não se aplicam a um H2 em arquivo. O H2 cria o banco com o usuário/senha da **primeira** conexão — para trocar a senha depois, apague a pasta `data/`. Defina as variáveis no shell (`$env:DB_USER="sa"` no PowerShell, `export DB_USER=sa` no bash) ou na Run Configuration do IntelliJ, junto com `SPRING_PROFILES_ACTIVE=local`.
- **Console web:** o H2 Console (`/h2-console`) permite inspecionar a tabela e rodar `SELECT` durante o desenvolvimento.
- **Diferenças de dialeto:** o modo MySQL do H2 aceita o script oficial sem alteração (`DATETIME`, `UNIQUE` inline, `AUTO_INCREMENT`), mas não é o MySQL real — evite recursos específicos do MySQL no restante do código.

### 1.3 Extras selecionados para este plano

O material cita vários itens "desejáveis" e diz explicitamente que você decide quais valem o esforço. Foram escolhidos os que agregam mais à avaliação com custo controlado, e descartado o que aumentaria demais o escopo para um projeto de entrada.

| Extra | Decisão | Justificativa |
|---|---|---|
| Envelope de paginação (`content/page/size/totalElements`) | **Incluir** | Já faz parte do contrato do GET de listagem no próprio documento — não é realmente opcional na prática, só precisa ser bem-feito. |
| Geração automática de protocolo (`OS-{ano}-{sequencial}`) | **Incluir** | Baixo esforço, resolve uma decisão que o próprio doc deixa em aberto (informado vs. gerado) e é citado como bônus explícito. |
| MapStruct para os mappers | **Incluir** | O próprio exemplo de código do doc já prevê classes `*Mapper` separadas; usar MapStruct é o padrão real de mercado para esse ponto e reduz código repetitivo sem mexer na arquitetura. |
| Teste de integração com banco real (H2) em vez de mocks | **Incluir** | O teste de integração já é obrigatório; rodá-lo com `@SpringBootTest` contra o H2 exercita todas as camadas (HTTP → use case → JPA → banco) sem depender do banco de testes do time nem de Docker. Nos testes, o H2 roda **em memória** (profile de teste), para que cada execução comece limpa e não suje o arquivo local de desenvolvimento. |
| Testcontainers | **Não incluir** | Exige Docker, que este plano não usa. O H2 cumpre o papel de banco real nos testes com custo zero de infraestrutura. |
| Segundo agregado relacionado (ex.: Technician) | **Não incluir** | Aumenta escopo de forma desproporcional para um projeto de entrada com prazo de 6–8 semanas. As 3 camadas já ficam bem demonstradas com um único agregado; melhor investir esse tempo em testes e acabamento. |

---

## 2. Plano de execução por etapas e commits

O plano segue a estratégia sugerida pelo próprio documento: **fazer funcionar simples, depois melhorar**. Em vez de construir camada por camada (o que obrigaria a escrever dezenas de arquivos antes de ver qualquer coisa responder), a Etapa 1 atravessa **todas** as camadas de uma vez para um único endpoint — a chamada **fatia vertical**. Uma vez que esse caminho esteja claro, o resto do CRUD é repetição do mesmo padrão, e as regras de negócio entram sobre uma base que já funciona.

Cada etapa termina em um estado executável e verificável — um **MVP**. O bloco ao final de cada uma diz exatamente o que você deve conseguir fazer antes de seguir adiante; se não passar, não comece a etapa seguinte. Cada etapa também indica o tema de estudo correspondente da Parte 1 do material original.

| Etapa | Foco | Resultado ao final |
|---|---|---|
| 0 | Ambiente | App sobe e conecta no banco |
| 1 | Fatia vertical (GET por ID) | Um endpoint responde de ponta a ponta |
| 2 | CRUD completo | POST, GET lista, PUT, DELETE funcionando |
| 3 | Regras e status | PATCH de status, transições, protocolo único |
| 4 | Acabamento | Erros padronizados, validação, logs, Swagger |
| 5 | Testes e README | Suíte verde e checklist da Parte 3 fechada |

---

### Etapa 0 — Setup do ambiente e do esqueleto

**Estudo relacionado (Parte 1):** Temas 1 e 6 (JVM/ferramentas, Spring Boot fundamentos)

| # | O que fazer | Commit sugerido |
|---|---|---|
| 1 | Gerar o esqueleto em start.spring.io com Java 21, Gradle, e as dependências: Web, Data JPA, H2 Database, Validation, Lombok, springdoc-openapi-starter-webmvc-ui e MapStruct. | `chore: inicializa projeto spring boot via start.spring.io` |
| 2 | Criar `src/main/resources/db/service_order.sql` com o `CREATE TABLE service_order` **exatamente** como na Parte 2 do onboarding. Ele não roda sozinho: é executado à mão no H2 Console (o documento original manda criar a tabela direto no banco, sem Flyway). Os testes reaproveitam o mesmo arquivo via `spring.sql.init.schema-locations`. | `chore: adiciona script oficial de criacao da tabela service_order` |
| 3 | Trocar `mysql-connector-j` por `runtimeOnly 'com.h2database:h2'` no `build.gradle` e criar `application-local.yaml` no formato da Parte 2 do onboarding: `url: jdbc:h2:file:./data/${DB_NAME};MODE=MySQL;AUTO_SERVER=TRUE`, `username: ${DB_USER}`, `password: ${DB_PASSWORD}`, `ddl-auto: none`, `hibernate.format_sql: true` e o H2 Console habilitado. Em `src/test/resources/application.yaml`, manter o H2 em memória (`jdbc:h2:mem:...`) para os testes. | `chore: configura datasource h2 em arquivo no profile local` |
| 4 | Adicionar `data/`, `*.mv.db`, `*.trace.db` e `application-local.yaml` ao `.gitignore`. | `chore: ignora arquivos do banco h2 e configuracao local` |

> **Atenção:** o arquivo do banco (`data/service_orders.mv.db`) nunca deve ser commitado — confira o `.gitignore` antes do primeiro push. Não há arquivo `.env`: mesmo sem credenciais reais, usuário/senha vêm de variáveis de ambiente do sistema (com default para o H2). Deixe explícito no README que o banco é H2 em arquivo local, substituindo o banco compartilhado do time citado no documento original.

**MVP desta etapa — como validar:** com `SPRING_PROFILES_ACTIVE=local`, `DB_NAME`, `DB_USER` e `DB_PASSWORD` definidas, `./gradlew bootRun` sobe com o profile `local` e o arquivo `data/<DB_NAME>.mv.db` aparece. Abra `localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/<DB_NAME>;MODE=MySQL;AUTO_SERVER=TRUE`, mesmo usuário/senha das variáveis), rode o conteúdo de `db/service_order.sql`, insira uma linha e confira com `SELECT * FROM service_order`. Reinicie a aplicação e confirme que a linha continua lá.

---

### Etapa 1 — Uma fatia vertical: GET por ID de ponta a ponta

**Estudo relacionado (Parte 1):** Temas 2, 3 e 4 (Java essencial, Collections/Streams/Optional, POO + SOLID) + Tema 8 (JPA) + Tema 7 (REST)

| # | O que fazer | Commit sugerido |
|---|---|---|
| 1 | Criar a estrutura de pacotes da hexagonal (`domain/model`, `domain/port`, `application/usecase`, `infrastructure/adapter/in`, `infrastructure/adapter/out`) e os enums `ServiceOrderStatus` e `ServiceOrderType`. | `feat: adiciona estrutura de pacotes hexagonal e enums de dominio` |
| 2 | Criar o modelo de domínio `ServiceOrder` (sem nenhum import Spring/JPA) apenas com os campos, e a exceção `ServiceOrderNotFoundException`. | `feat: adiciona modelo de dominio ServiceOrder e excecao de nao encontrado` |
| 3 | Criar a porta `ServiceOrderRepository` com um único método por enquanto: `findById(Long): Optional<ServiceOrder>`. A interface vai crescer nas próximas etapas. | `feat: adiciona porta ServiceOrderRepository com busca por id` |
| 4 | Criar `ServiceOrderEntity` (`@Entity`) espelhando a tabela e `JpaServiceOrderRepository` (extends `JpaRepository`) com `findByIdAndDeletedAtIsNull`, já respeitando o soft delete. | `feat: adiciona entidade JPA e repository JPA da ordem` |
| 5 | Criar `ServiceOrderPersistenceMapper` com MapStruct (entity -> domínio) e `ServiceOrderPersistenceAdapter` implementando a porta. | `feat: adiciona mapper e persistence adapter da ordem de servico` |
| 6 | Criar `GetServiceOrderByIdUseCase`, que recebe a porta por construtor e lança `ServiceOrderNotFoundException` quando o Optional vier vazio. | `feat: adiciona caso de uso de busca de ordem por id` |
| 7 | Criar `ServiceOrderResponse` (record), `ServiceOrderWebMapper` (domínio -> DTO) e o `ServiceOrderController` com o endpoint `GET /v1/service-order-management/service-orders/{id}` devolvendo 200. | `feat: adiciona endpoint de busca de ordem de servico por id` |

> **Atenção:** esta é a etapa que mais ensina. Tudo a partir daqui é repetição deste mesmo caminho. O modelo de domínio fica anêmico por ora (só dados) e ganha comportamento na Etapa 3 — isso é deliberado, não o destino final.

**MVP desta etapa — como validar:** Insira uma linha na tabela `service_order` via SQL (pelo H2 Console) e chame `GET /v1/service-order-management/service-orders/1`: deve voltar 200 com o JSON no formato do contrato. Um id inexistente ainda vai estourar 500 — isso é esperado, o tratamento vem na Etapa 4. Antes de seguir, saiba apontar no código por onde a requisição passou: controller → use case → porta → adapter → JPA → mapper → DTO.

---

### Etapa 2 — CRUD completo (repetindo o padrão)

**Estudo relacionado (Parte 1):** Tema 4 (DIP), Tema 7 (REST) e Tema 3 (Streams/Optional)

| # | O que fazer | Commit sugerido |
|---|---|---|
| 1 | POST: criar `ServiceOrderRequest` (record), estender a porta com `save`, implementar `CreateServiceOrderUseCase` forçando status inicial OPEN (Regra 2) e o endpoint `POST /v1/service-order-management/service-orders` devolvendo 201 com header `Location`. | `feat: adiciona endpoint de criacao de ordem de servico` |
| 2 | GET lista: estender a porta com `search(filter, pageable)`, criar `ServiceOrderFilter`, `SearchServiceOrdersUseCase` e o endpoint `GET /v1/service-order-management/service-orders` com filtros `protocol/status/type` e o envelope `content/page/size/totalElements`. | `feat: adiciona endpoint de busca paginada com filtros` |
| 3 | PUT: `UpdateServiceOrderUseCase` alterando apenas os dados da OS (protocolo imutável, status inalterado aqui) e o endpoint `PUT /v1/service-order-management/service-orders/{id}`. | `feat: adiciona endpoint de atualizacao de dados da ordem` |
| 4 | DELETE: `DeleteServiceOrderUseCase` preenchendo `deletedAt` (Regra 5 — soft delete) e o endpoint `DELETE /v1/service-order-management/service-orders/{id}` devolvendo 204. | `feat: adiciona endpoint de remocao logica da ordem` |

> **Atenção:** não se preocupe ainda com payload inválido, protocolo duplicado nem mensagens de erro bonitas — isso é Etapa 4. Aqui o objetivo é o caminho feliz dos 5 endpoints. Cada caso de uso depende apenas da porta (DIP), nunca do `JpaRepository`.

**MVP desta etapa — como validar:** Pelo Postman/curl, execute o ciclo completo: POST cria (201) → GET lista mostra o registro no envelope paginado → GET por id devolve 200 → PUT altera os dados → DELETE devolve 204 e, depois dele, o GET por id **não** encontra mais o registro. Teste também um filtro na listagem.

---

### Etapa 3 — Regras de negócio e transições de status

**Estudo relacionado (Parte 1):** Tema 4 (POO/SOLID) e Tema 5 (exceções)

| # | O que fazer | Commit sugerido |
|---|---|---|
| 1 | Mover o comportamento para o domínio: criar o mapa de transições válidas e o método `ServiceOrder.changeStatusTo(novoStatus, data)` que rejeita transição inválida (Regra 3), lançando `InvalidStatusTransitionException`. | `feat: adiciona regra de transicao de status no dominio` |
| 2 | Dentro do mesmo `changeStatusTo`, exigir `scheduledDate` ao passar para SCHEDULED e recusar data retroativa (Regra 4). | `feat: adiciona regra de agendamento obrigatorio no dominio` |
| 3 | Criar `UpdateServiceOrderStatusUseCase` (que apenas delega ao método de domínio), o DTO `ChangeStatusRequest` e o endpoint `PATCH /v1/service-order-management/service-orders/{id}/status`. | `feat: adiciona endpoint de mudanca de status da ordem` |
| 4 | Protocolo único (Regra 1): estender a porta com `existsByProtocol`, criar `DuplicateProtocolException` e aplicar a checagem no `CreateServiceOrderUseCase`. | `feat: adiciona validacao de protocolo unico na criacao` |
| 5 | Geração automática de protocolo (extra selecionado): quando o campo `protocol` não vier no request, gerar no formato `OS-{ano}-{sequencial}`. | `feat: adiciona geracao automatica de protocolo quando omitido` |

> **Atenção:** a regra de transição deve morar dentro do `ServiceOrder`, nunca no controller nem espalhada no use case — é o ponto que o time mais observa. Ao final desta etapa o domínio deixa de ser anêmico.

**MVP desta etapa — como validar:** Teste os caminhos infelizes: PATCH de OPEN para DONE deve ser recusado; PATCH para SCHEDULED sem data deve ser recusado; PATCH para SCHEDULED com data no passado deve ser recusado; POST com protocolo já existente deve ser recusado; POST sem protocolo deve gerar um. As respostas ainda serão 500 feias — o que importa aqui é que a regra **impeça** a operação.

---

### Etapa 4 — Acabamento: erros, validação, logs e Swagger

**Estudo relacionado (Parte 1):** Tema 5 (validação/exceções) e Tema 10 (qualidade)

| # | O que fazer | Commit sugerido |
|---|---|---|
| 1 | Criar `ErrorResponse` (record com `timestamp`, `status`, `message`) e o `GlobalExceptionHandler` (`@RestControllerAdvice`) mapeando: NotFound → 404, DuplicateProtocol e InvalidStatusTransition → 409, erro inesperado → 500. | `feat: adiciona error response e tratador global de excecoes` |
| 2 | Adicionar Bean Validation nos DTOs de entrada (`@NotBlank`, `@NotNull`, `@Min` etc.) e tratar `MethodArgumentNotValidException` no handler → 400 com as mensagens de campo. | `feat: adiciona bean validation nos dtos e tratamento de 400` |
| 3 | Revisar os códigos HTTP de todos os endpoints contra o contrato do documento: 201 no POST, 204 no DELETE, 200/204 na listagem conforme houver resultado. | `refactor: ajusta codigos http conforme contrato da api` |
| 4 | Adicionar logs SLF4J (`@Slf4j`) nos casos de uso: `info` para eventos de negócio, `warn` para conflitos esperados, `error` para falhas inesperadas — mensagens em português, placeholders `{}` e sem dado sensível. | `feat: adiciona logs de negocio nos casos de uso` |
| 5 | Anotar todos os endpoints com `@Tag`, `@Operation` e `@ApiResponse` e conferir no Swagger UI que os 6 endpoints aparecem com os status documentados. | `docs: adiciona anotacoes swagger em todos os endpoints` |

> **Atenção:** o contrato de rotas, payloads e status HTTP é fixo no documento — compare cada resposta com o exemplo de JSON antes de fechar o commit.

**MVP desta etapa — como validar:** Repita todos os testes infelizes da Etapa 3 — agora cada um deve devolver o status correto (400/404/409) com o JSON `{timestamp, status, message}`, nunca um stacktrace. Abra `/swagger-ui.html` e execute os 6 endpoints direto de lá. Confira no console que os logs saíram em português e sem dado do cliente.

---

### Etapa 5 — Testes, README e fechamento

**Estudo relacionado (Parte 1):** Tema 10 (JUnit 5 + Mockito, qualidade e entrega)

| # | O que fazer | Commit sugerido |
|---|---|---|
| 1 | Testes unitários do domínio: `ServiceOrder.changeStatusTo` cobrindo transições válidas, inválidas (Regra 3), SCHEDULED sem data e com data retroativa (Regra 4). Sem Spring, sem mock — é Java puro. | `test: adiciona testes das regras de transicao de status` |
| 2 | Testes unitários dos casos de uso (create, update status, delete) com Mockito mockando a porta `ServiceOrderRepository` — aqui o DIP mostra o seu valor. | `test: adiciona testes unitarios dos casos de uso` |
| 3 | Escrever o teste de integração com `@SpringBootTest` + `@AutoConfigureMockMvc` rodando contra o H2 em memória (profile de teste, separado do arquivo local de desenvolvimento), cobrindo criar → buscar → mudar status (e o 404 após o DELETE). Use `@Transactional` ou `@Sql` para isolar o estado entre os testes. | `test: adiciona teste de integracao com h2` |
| 4 | Escrever o README: como rodar (`./gradlew bootRun`), como acessar o H2 Console, onde fica o arquivo do banco e como zerá-lo (apagar `data/`), como criar a tabela com `db/service_order.sql`, um modelo do `application-local.yaml` (ele é gitignored), as variáveis de ambiente obrigatórias (`SPRING_PROFILES_ACTIVE`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) e como defini-las no PowerShell/bash/IntelliJ, nota explícita de que o banco é H2 em arquivo local substituindo o banco compartilhado do time citado no documento original, e as decisões de arquitetura (por que MapStruct, onde ficou cada regra, geração de protocolo). | `docs: adiciona readme com instrucoes e decisoes de arquitetura` |
| 5 | Passar a checklist da Seção 3 deste plano item por item, revisar o histórico de commits e conferir que nenhuma credencial foi versionada. | — (revisão final, sem commit de código) |

> **Atenção:** prepare a explicação de cada decisão — o bate-papo final de ~15 min avalia isso tanto quanto o código funcionar.

**MVP desta etapa — como validar:** `./gradlew test` passa inteiro, sem nenhuma dependência externa. Em seguida, a validação definitiva: clone o próprio repositório em uma pasta nova, siga **apenas** o que o seu README manda e veja se a aplicação sobe. Se não subir, o README está incompleto.

---

## 3. Validação cruzada com os critérios do documento

Conferência de que o plano acima cobre, item por item, tudo o que a Parte 3 (checklist de avaliação) e a Parte 2 (endpoints e regras de negócio) do documento original exigem. Use esta seção como checklist final antes de considerar o desafio concluído.

### 3.1 Os 6 endpoints

| Endpoint | Status esperados | Implementado em |
|---|---|---|
| `POST /v1/service-order-management/service-orders` | 201 / 400 / 409 | Etapa 2, item 1 |
| `GET /v1/service-order-management/service-orders` | 200 / 204 | Etapa 2, item 2 |
| `GET /v1/service-order-management/service-orders/{id}` | 200 / 404 | Etapa 1, item 7 |
| `PUT /v1/service-order-management/service-orders/{id}` | 200 / 400 / 404 | Etapa 2, item 3 |
| `PATCH /v1/service-order-management/service-orders/{id}/status` | 200 / 400 / 404 / 409 | Etapa 3, item 3 |
| `DELETE /v1/service-order-management/service-orders/{id}` | 204 / 404 | Etapa 2, item 4 |

### 3.2 As 5 regras de negócio

| Regra | Onde fica implementada |
|---|---|
| 1. Protocolo único (409 se duplicado) | `CreateServiceOrderUseCase`, checando `existsByProtocol` via a porta (Etapa 3, item 4) |
| 2. Toda OS nasce OPEN | `CreateServiceOrderUseCase` ignora status vindo do request (Etapa 2, item 1) |
| 3. Transições válidas de status | `ServiceOrder.changeStatusTo`, no domínio (Etapa 3, item 1) |
| 4. `scheduledDate` obrigatório e não retroativo em SCHEDULED | `ServiceOrder.changeStatusTo`, no domínio (Etapa 3, item 2) |
| 5. Soft delete (nunca delete físico) | `findByIdAndDeletedAtIsNull` (Etapa 1, item 4) + `DeleteServiceOrderUseCase` (Etapa 2, item 4) |

### 3.3 Checklist obrigatória (Parte 3 do documento)

| Item | Coberto por |
|---|---|
| Projeto compila, sobe (`./gradlew bootRun`) e conecta no banco H2 em arquivo local | Etapa 0 completa |
| 6 endpoints funcionam e são testáveis pelo Swagger UI | Etapas 1 a 3 + Etapa 4, item 5 |
| Arquitetura hexagonal respeitada (domínio limpo, porta, adapter, Entity separada) | Etapa 1 estabelece o padrão; Etapas 2 e 3 o repetem |
| Validação nos requests + tratador global com `ErrorResponse` padronizado | Etapa 4, itens 1 e 2 |
| As 5 regras de negócio implementadas | Ver tabela 3.2 acima |
| Logs em PT, código em EN, SLF4J parametrizado, sem dado sensível | Etapa 4, item 4 |
| Swagger com `@Tag`/`@Operation`/`@ApiResponse` em todos os endpoints | Etapa 4, item 5 |
| Git: commits pequenos, branch de trabalho, README, `.gitignore`, zero credencial | Etapa 0 + Etapa 5, itens 4 e 5 |
| Testes unitários + pelo menos 1 teste de integração | Etapa 5, itens 1 a 3 |
| Códigos HTTP corretos em todos os fluxos | Etapa 4, item 3 |
| SOLID visível (DIP e SRP) | Etapa 1 (porta) + Etapas 2 e 3 (1 responsabilidade por use case) |
| Nomes claros, métodos curtos, sem código morto | Revisão contínua a cada commit |

### 3.4 Desejáveis incluídos

| Item | Coberto por |
|---|---|
| Paginação e filtros com envelope `content/page/size/totalElements` | Etapa 2, item 2 |
| Mapeamento com MapStruct | Etapa 1, itens 5 e 7 |
| Teste de integração ponta a ponta contra banco real (H2) | Etapa 5, item 3 |
| Geração automática de protocolo | Etapa 3, item 5 |
| README explicando as decisões (por que MapStruct, onde ficou cada regra, adaptação de ambiente) | Etapa 5, item 4 |

> Não incluídos por decisão de escopo: segundo agregado relacionado (ex.: Technician) e Testcontainers — ver justificativa na Seção 1.3. O H2 em arquivo local não está nesta lista porque não é um extra opcional, e sim a estratégia de ambiente do projeto (ver Seção 1.2 e Etapa 0).

---

## 4. Pontos de atenção para o desenvolvimento

Resumo das armadilhas e decisões que mais pesam na avaliação, segundo o próprio documento do time. Releia esta seção antes do bate-papo final de ~15 minutos.

1. **Resista a construir camada por camada.** A tentação é escrever todos os casos de uso, depois todos os adapters, depois todos os controllers. O problema é que nada funciona até o último arquivo, e um erro de concepção na primeira camada só aparece dias depois. A fatia vertical da Etapa 1 inverte isso: você erra cedo e barato, em um único endpoint, e só replica o padrão depois que ele está comprovado funcionando.

2. **Regra de ouro do domínio.** Nenhuma classe dentro de `domain` pode importar `org.springframework.*` ou `jakarta.persistence.*`. É o principal ponto que o time olha para avaliar a arquitetura hexagonal.

3. **Onde mora a regra de transição de status.** A validação de transição (Regra 3) e de agendamento (Regra 4) é regra de negócio pura: deve viver em um método do próprio `ServiceOrder` (ex.: `changeStatusTo`), não no controller nem espalhada no use case. Já a validação de formato (`@NotBlank`, `@Min`) fica no DTO.

4. **Três objetos, três propósitos.** Request (entra pela web), `ServiceOrder` (domínio) e Entity (banco) são classes diferentes. Nunca devolva a Entity como resposta da API — isso vaza detalhe de persistência para o cliente HTTP.

5. **Soft delete consistente.** Toda busca (`findById`, `search`) precisa ignorar registros com `deletedAt` preenchido. Após o DELETE, qualquer operação subsequente sobre aquele id deve responder 404, não 200.

6. **Logs: forma tanto quanto conteúdo.** Sempre `@Slf4j` + placeholders `{}` (nunca concatenação de string). Mensagem em português, identificadores em inglês. `info` para eventos de negócio, `warn` para conflitos esperados (ex.: protocolo duplicado), `error` só para falha inesperada, sempre passando a exceção. Nunca logar documento ou dado pessoal do cliente.

7. **Injeção de dependência via construtor.** Nada de `new` nas dependências dentro das classes. Declare campos `final` e use `@RequiredArgsConstructor` (Lombok) — é assim que o Spring injeta e é o que viabiliza o DIP nos testes com Mockito.

8. **`null` não é um valor de retorno.** Buscas que podem não encontrar nada devolvem `Optional`, nunca `null`. Compare objetos com `.equals()`, nunca com `==`. Isso é diferente do hábito comum em JavaScript.

9. **Credenciais fora do Git.** O H2 usa `sa` sem senha, mas o hábito vale do mesmo jeito: nenhuma senha real vai para o repositório, e a configuração lê usuário/senha de variáveis de ambiente. Se uma credencial vazar em um commit, ela é considerada comprometida e o time deve ser avisado imediatamente — isso entra na avaliação.

10. **H2 substituindo o banco do time.** O documento original pressupõe credenciais fornecidas por um líder para um MySQL compartilhado. Este plano usa um H2 em arquivo local (modo MySQL, dados persistidos em `./data`) para viabilizar o desafio sem esse acesso e sem Docker. Deixe isso explícito logo nas primeiras linhas do README, para que quem avaliar entenda que é uma adaptação consciente de ambiente, não um desvio do enunciado — e saiba explicar no bate-papo o que mudaria para apontar ao MySQL real (driver, URL e variáveis de ambiente; nenhuma mudança no domínio nem nos use cases, graças à hexagonal).

11. **MapStruct não substitui pensar nas camadas.** Ao adotar MapStruct para os mappers, garanta que o `@Mapper` de persistência (domínio ↔ entity) e o `@Mapper` web (domínio ↔ DTO) continuem em módulos/pacotes separados, coerentes com `infrastructure/adapter/out` e `infrastructure/adapter/in` respectivamente.

12. **Protocolo automático não quebra a Regra 1.** Se optar por gerar o protocolo quando não informado, a checagem de unicidade continua necessária (o gerador pode colidir em cenários concorrentes) — a constraint UNIQUE do banco é o backstop final, mas o 409 tratado pela aplicação é o que o contrato exige.

13. **Prepare a explicação, não só o código.** O bate-papo final de ~15 min pede para você explicar onde estão as fronteiras da hexagonal, por que cada regra ficou onde ficou e o que mudaria com mais tempo. Isso vale tanto quanto o código funcionar — vá documentando essas decisões no README à medida que avança, em vez de tentar lembrar tudo no final.

> Dúvida travou por mais de 30 minutos de pesquisa? Chame o time — pedir ajuda no momento certo é sinal de maturidade, não de fraqueza. Boa jornada.
