# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: **Fase 1 completa** — fatia vertical (Repository → Service → Controller) existente para **todas as 5 entidades do MVP**: `Reporte` (`GET`/`POST /api/reportes`, POST `201`), `Categoria` (`GET /api/categorias`), `Subcategoria` (`GET /api/subcategorias`), `Bairro` (`GET /api/bairros`) e `Usuario` (`GET`/`POST /api/usuarios`, POST `201`). **Foco atual (o único que importa agora)**: módulo **Reporte** — que vai ser finalizado, receber DTOs e se conectar ao front (Fases 2, 3 e 4). Pendente: rodar/validar e iniciar a Fase 2.
- **Sessão (06/09/2026) — Erico**: subiu a fatia vertical de `Bairro` e `Usuario` (Repository + Service + Controller; POST com `@ResponseStatus(HttpStatus.CREATED)`). `dataCadastro` do `Usuario` já em `LocalDateTime`. Compilando sem erros.
- **Sessão (06/09/2026) — Murilo (Reporte/Categoria/Subcategoria)**: `ReporteController` (`GET`/`POST /api/reportes`); estrutura de `Categoria`/`Subcategoria` (Repository + Service + Controller, GETs); `Categoria` com `@OneToMany` EAGER de subcategorias; `Subcategoria` com `@JsonProperty WRITE_ONLY` na referência a `Categoria` (evita recursão no JSON mantendo a leitura no POST).
- **Decisões de escopo (10/09/2026)**: módulo **somente de Reporte**. **Autenticação ainda não será colocada**; **geocodificação/OpenStreetMap já feita no front** (não é do back — `endereco`/`bairro` ficam nulos); **classificação de bairro quase pronta** (pesos definidos, focar depois); usuário/perfil/histórico/denúncias/admin fora; `avaliacaoGravidade` foi **removida**.
- **Decisões do fluxo do Reporte (10/09/2026)**: o app envia `POST /api/reportes` com `titulo`, `descricao`, `latitude`, `longitude`, `idCategoria`, `idSubcategoria`. A **subcategoria é validada contra a categoria** (a categoria define as subs válidas; desalinhadas → 400). **O servidor controla**: `idReporte`, `status=PUBLICADO` e `dataReporte` (**horário de São Caetano do Sul** — `America/Sao_Paulo`, `OffsetDateTime.now(ZoneId...)`). `usuario`, `endereco` e `bairro` ficam nulos. Seed de `Categoria`/`Subcategoria` **ainda não existe** (só as classes e enums). `StatusReporte` fica `RASCUNHO/PUBLICADO/EXCLUIDO` (novo valor de enum fica para depois).
- **Decisão de herança tomada**: `Pessoa` do diagrama virou o molde `UsuarioBase` com `@MappedSuperclass` (sem tabela própria). `Usuario` é `@Entity` que herda os campos do molde.
- **Classificação do bairro (definida)**: enums `TipoCategoria`/`TipoSubcategoria` (com pesos) finalizados e tabela de pesos definida — ver `docs/mudancas-docs-relatorio.md`. `calcularClassificacao()` no `BairroService` fica para depois.

## 🛠️ 2. Modificações Efetuadas nesta Sessão
**Código (sessões anteriores — não mexer):**
- `model/Reporte.java` → `@Entity` do Reporte (`idReporte`, titulo, descricao, latitude, longitude, endereco, `@Enumerated StatusReporte status`, `OffsetDateTime dataReporte`; `@ManyToOne` usuario/categoria/subcategoria/bairro).
- `model/StatusReporte.java`, `Categoria.java`, `Subcategoria.java`, `Bairro.java` → entidades de apoio do Reporte.
- `model/TipoSubcategoria.java` → enum com os pesos das subcategorias (no momento **não usado** em cálculo).
- `repository/ReporteRepository.java` → `JpaRepository<Reporte, Long>` (sem métodos customizados).
- `service/ReporteService.java` → `listar()`/`salvar()` delegando ao repository.
- `controller/ReporteController.java` → `GET`/`POST /api/reportes` (aceita a **entidade bruta**, sem validação; `status`/`dataReporte` ficam nulos).
- `repository/CategoriaRepository.java`/`SubcategoriaRepository.java`/`BairroRepository.java`/`UsuarioRepository.java` e os `Service`/`Controller` correspondentes → fatias de Categoria, Subcategoria, Bairro e Usuario.
- `model/UsuarioBase.java` (molde `@MappedSuperclass`), `TipoUsuario.java`, `Usuario.java` (com `telefone` e `dataCadastro` em `LocalDateTime`).

**Docs (nesta sessão — 10/09/2026):**
- `AGENTS.md` → enxugado para o foco em Reporte (removidos OpenStreetMap/Autenticação/Classificação de Bairro).
- `docs/planejamento-desenvolvimento-vigia-backend.md` → reescrito: só o módulo Reporte, fases 2/3/4 futuras.
- `docs/vigia-specs-endpoints.md` → reescrito: só endpoints de reporte (contratos + prompts prontos). O guia de DTOs foi removido — explicado em conversa.
- `docs/guia-desenvolvimento.md` → regras e ponto de controle atualizados.
- `docs/mudancas-docs-relatorio.md` → enxugado para mudanças relacionadas a reporte.
- `docs/ESTADO_DESENVOLVIMENTO.md` → este arquivo, mesclado com o estado do remoto (Fase 1 das 5 entidades) e atualizado com as decisões de fluxo/payload/data de hoje.
- `C:\Users\teric\Desktop\tarefas-vigia-naive-v1.md` → checklist das **11 tarefas** da entrega naive v1 (fora do repo, só para transcrição no Trello).

## 📋 3. Estado das Dependências instaladas
- Permanecem as do `pom.xml`: `spring-boot-starter-webmvc`, `spring-boot-h2console`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `com.h2database:h2` (runtime), `org.projectlombok:lombok`, mais os starters de teste.
- **Não adicionadas** (fora do foco agora): `mysql-connector-j`, Swagger, Spring Security.
- ⚠️ H2 é ponte temporária `MODE=MySQL`; o banco do projeto é **MySQL**, de outra pessoa da equipe, ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
Seguir as **11 tarefas** do checklist na área de trabalho (`tarefas-vigia-naive-v1.md`) — uma por classe/camada:

1. Validar o estado atual (rodar, testar endpoints, h2-console).
2. `model/Reporte` — `usuario` opcional (`@ManyToOne(optional = true)`), sem métodos.
3. Configurar data/hora: `dataReporte` com `OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))`.
4. `repository/ReporteRepository` — `findAllByStatus(StatusReporte)`.
5. Seed de `Categoria`/`Subcategoria` no boot (hoje as tabelas estão vazias).
6. `service/ReporteService` — `criar()` (buscar cat/sub pelos ids, validar sub contra cat, `status=PUBLICADO`, `dataReporte=agora`, `@Transactional`), `listar()` (só PUBLICADO), `buscarPorId()` (404).
7. `dto/ReporteRequest` — record + Bean Validation (`@NotBlank`, `@NotNull`, `@Min`/`@Max` nas coordenadas).
8. `dto/ReporteResponse` — record de saída.
9. `controller/ReporteController` — `POST` (201), `GET` (só PUBLICADO), `GET /{id}` (404).
10. `exception/GlobalExceptionHandler` (`@RestControllerAdvice`) — formato padrão de erro, 400/404.
11. Integração com o front — contrato, URL base (IP da rede), teste ponta a ponta com pin no mapa.

**Cadastro/login do `Usuario`**: fora de foco (autenticação ainda não será colocada).

## 🧠 5. Contexto Técnico para o Próximo Chat
- **Stack**: Java 21, Spring Boot 4.1.1, Maven (`./mvnw`), Spring Data JPA, Bean Validation, Lombok.
- **Versionamento**: repo `https://github.com/Ericoos/Vigia-Backend.git`, branch `main`. **Commit/push só com aprovação explícita do usuário; mensagens em português.**
- **Banco**: esquema gerado pelo Hibernate a partir das `@Entity` via `ddl-auto=update`; **nunca `nativeQuery`**; H2 modo arquivo `MODE=MySQL` até o MySQL da equipe existir.
- **Arquitetura**: `controller` (recebe DTO) → `service` (regras + conversão DTO ↔ entidade) → `repository` → `model`. `VigiaApplication.java` na raiz do pacote. **A entidade não é exposta no contrato da API.**
- **Fluxo-alvo do reporte**: app envia `POST /api/reportes` (JSON) → Controller recebe `ReporteRequest` → Service valida/deriva/armazena → banco → `GET /api/reportes` devolve só `PUBLICADO` para os pins do mapa.
- **Entidades existentes no `model`**: `Reporte`, `StatusReporte` (RASCUNHO/PUBLICADO/EXCLUIDO), `Categoria`, `Subcategoria`, `Bairro`, `TipoSubcategoria` (com pesos). Módulo usuário (fora de foco): `UsuarioBase` (molde `@MappedSuperclass`), `TipoUsuario`, `Usuario`.
- **Fatias prontas (Fase 1)**: `Reporte`, `Categoria`, `Subcategoria`, `Bairro`, `Usuario`.
- **Decisões**: `avaliacaoGravidade` removida; pesos de `TipoSubcategoria` definidos mas **não** usados em cálculo por enquanto; geocodificação já feita no front (`endereco`/`bairro` ficam nulos no back); `usuario` do reporte é opcional para o POST; **`dataReporte` no horário de São Caetano do Sul** (`America/Sao_Paulo`); **subcategoria validada contra a categoria** (app envia `idCategoria` + `idSubcategoria`).
- **Pacotes ainda a criar**: `dto`, `exception`. Sem seed de `Categoria`/`Subcategoria`.
- **Docs**: `docs/` contém `planejamento-desenvolvimento-vigia-backend.md`, `vigia-specs-endpoints.md`, `guia-desenvolvimento.md`, `mudancas-docs-relatorio.md`, este `ESTADO_DESENVOLVIMENTO.md`, `diagramas/` (7 `.drawio`) e `relatorio-vigia.txt`. `AGENTS.md` na raiz é a fonte das regras persistentes.
- **Comandos**: `./mvnw spring-boot:run` (rodar), `./mvnw clean install` (build).