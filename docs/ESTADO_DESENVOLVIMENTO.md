# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: **Fases 2 e 3 concluídas** — o **módulo Reporte do backend está funcional de ponta a ponta** (entrega naive v1). As **tarefas 1 a 9** do checklist `C:\Users\teric\Desktop\tarefas-vigia-naive-v1.md` estão **concluídas e testadas**. Falta apenas a **Tarefa 10 — integração com o app** (contrato via IP da rede + teste ponta a ponta), que o time fará depois.
- **Sessão (13/09/2026)**: concluídas as tarefas restantes do backend:
  - **Tarefa 2** — fuso `spring.jackson.time-zone=America/Sao_Paulo` no `application.properties` + `OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))` na criação.
  - **Tarefa 3** — `findAllByStatus(StatusReporte)` no `ReporteRepository`.
  - **Tarefa 4** — seed de `Categoria`/`Subcategoria` no boot (`config/DataSeeder.java`).
  - **Tarefa 5** — regras de negócio no `ReporteService` (`criar`, `listar` só PUBLICADO, `listarPorId`).
  - **Tarefa 9** — erros padronizados (`exception/GlobalExceptionHandler` + `ResourceNotFoundException`).
- **Tarefas 6 e 7** (DTOs `ReporteRequest`/`ReporteResponse`) vieram via commit do time.
- **Decisões de escopo mantidas (10/09/2026)**: módulo **somente de Reporte**. Autenticação fora; geocodificação já feita no front (`endereco`/`bairro` nulos); usuário/perfil/histórico/denúncias/admin fora; `avaliacaoGravidade` removida.
- **Fluxo do Reporte (funcionando)**: app envia `POST /api/reportes` com `titulo`, `descricao`, `latitude`, `longitude`, `idCategoria`, `idSubcategoria`. O servidor valida a subcontra a categoria (desalinhadas → `400`), define `idReporte` (banco), `status=PUBLICADO` e `dataReporte` no horário de São Caetano do Sul (`America/Sao_Paulo`, `-03:00`). Seed já popula as tabelas no boot.

## 🛠️ 2. Modificações Efetuadas nesta Sessão
**Código (módulo Reporte — funcional):**
- `config/DataSeeder.java` → **novo**: `@Component` + `CommandLineRunner`; cria as 2 categorias e as subs no boot se as tabelas estiverem vazias.
- `dto/ReporteRequest.java` e `dto/ReporteResponse.java` → **novos** (records; via time). Request com Bean Validation (`@NotBlank` titulo/descricao, `@NotNull`+`@DecimalMin/Max` nas coordenadas e ids); Response sem expor a entidade.
- `exception/GlobalExceptionHandler.java` e `exception/ResourceNotFoundException.java` → **novos**: `@RestControllerAdvice` devolvendo **sempre o mesmo body** (`timestamp`, `status`, `erro`, `mensagem`, `caminho`). `ResourceNotFoundException` → 404; `IllegalArgumentException` → 400; `MethodArgumentNotValidException` → 400.
- `service/ReporteService.java` → regras: `criar(ReporteRequest)` `@Transactional` (busca cat/sub, valida sub contra cat, `status=PUBLICADO`, `dataReporte` `America/Sao_Paulo`); `listar()` devolve só `PUBLICADO` em DTO; `listarPorId(id)` com 404; conversor `toResponse`.
- `controller/ReporteController.java` → `POST` recebe `@Valid ReporteRequest` e devolve `201`; `GET` devolve `List<ReporteResponse>`; novo `GET /{id}`.
- `repository/ReporteRepository.java` → método novo `findAllByStatus(StatusReporte)`.
- `model/Categoria.java`, `model/Subcategoria.java` → tipos usam **enum** com `@Enumerated(EnumType.STRING)` (antes o H2 guardava ordinal 0/1) e ganharam construtores (`Categoria(TipoCategoria)` / `Subcategoria(TipoSubCategoria, Categoria)`), mantendo `@NoArgsConstructor` (JPA).
- `src/main/resources/application.properties` → adicionado `spring.jackson.time-zone=America/Sao_Paulo`.
- Dados de teste do H2 foram apagados (`data/`) e o seed re-populou as tabelas (categorias id 1-2, subs id 1-10).

**Docs (nesta sessão — 13/09/2026):**
- `docs/ESTADO_DESENVOLVIMENTO.md` → este arquivo, atualizado para o estado final do backend.
- `docs/planejamento-desenvolvimento-vigia-backend.md` → Fases 2 e 3 marcadas como concluídas.
- `docs/vigia-specs-endpoints.md` → contrato corrigido para o que o código realmente faz (payload com `idCategoria`+`idSubcategoria`; seed existe; enums no JSON).
- `docs/guia-desenvolvimento.md` → ponto de controle atualizado.
- `docs/mudancas-docs-relatorio.md` → itens de Fase 2/3 marcados como implementados.
- `C:\Users\teric\Desktop\tarefas-vigia-naive-v1.md` → tarefas 1-9 ✅, tarefa 10 (front) pendente.

## 📋 3. Estado das Dependências instaladas
- Permanecem as do `pom.xml`: Spring Web, Spring Data JPA, Bean Validation, H2 (runtime), Lombok, starters de teste.
- **Não adicionadas**: `mysql-connector-j`, Swagger, Spring Security.
- ⚠️ H2 é ponte temporária `MODE=MySQL`; o banco do projeto é **MySQL**, de outra pessoa da equipe, ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
1. **Tarefa 10 — integração com o app (ponta a ponta)**:
   - Alinhar o contrato com o front (payload do POST + JSON da lista/reporte).
   - Liberar conexões de fora: `server.address=0.0.0.0` no `application.properties`; descobrir o IP do PC (`ipconfig`) e testar `http://IP:8080/api/reportes` no celular.
   - Emular o app pelo Postman enquanto o front não estiver pronto (GET categorias → POST reporte → GET lista → GET detalhe).
2. **Depois (fora da entrega naive v1)**: classificação de segurança do bairro (pesos já definidos nos enums), autenticação, editar/excluir reporte, denúncias, migração H2 → MySQL.

**Cadastro/login do `Usuario`**: fora de foco (autenticação ainda não será colocada).

## 🧠 5. Contexto Técnico para o Próximo Chat
- **Stack**: Java 21, Spring Boot 4.1.1, Maven (`.\mvnw.cmd` no PowerShell), Spring Data JPA, Bean Validation, Lombok.
- **Versionamento**: repo `https://github.com/Ericoos/Vigia-Backend.git`, branch `main`. **Commit/push só com aprovação explícita do usuário; mensagens em português.**
- **Banco**: esquema gerado pelo Hibernate a partir das `@Entity` via `ddl-auto=update`; **nunca `nativeQuery`**; H2 modo arquivo `jdbc:h2:file:./data/vigia;MODE=MySQL` até o MySQL existir. Console: `http://localhost:8080/h2-console`, user `sa`, senha vazia.
- **Arquitetura**: `controller` (recebe DTO) → `service` (regras + conversão DTO ↔ entidade) → `repository` → `model`. **Entidade nunca aparece no contrato da API** (regra do `AGENTS.md`).
- **Módulo Reporte — endpoints prontos**:
  - `GET /api/reportes` → lista só `PUBLICADO` em `ReporteResponse`.
  - `GET /api/reportes/{id}` → detalhe; `404` se não existir.
  - `POST /api/reportes` → recebe `ReporteRequest` validado; `201`; sub desalinhada da categoria → `400`; ids inexistentes → `404`.
  - `GET /api/categorias` → 2 categorias com subs aninhadas (seed no boot).
- **Seed (`config/DataSeeder.java`)**: IDFICA somente se as tabelas estiverem vazias. **SEGURANCA** → ILUMINACAO_BOA, AREA_MOVIMENTADA, PRESENCA_POLICIAL, COMERCIO_ATIVO, CAMERAS. **INSEGURANCA** → ILUMINACAO_RUIM, ROUBO, FURTO, VANDALISMO, ACIDENTE. Total: 2 categorias, 10 subs.
- **Modelo**: `Reporte` (`@ManyToOne` para usuario/categoria/subcategoria/bairro — usuário não é tocado); `Categoria.tipoCategoria` = enum `TipoCategoria`; `Subcategoria.tipoSubCategoria` = enum `TipoSubcategoria` (com `peso`); `StatusReporte` (RASCUNHO/PUBLICADO/EXCLUIDO). `TipoCategoria` tem `calculaClassificacao(sub)` (pesos definidos, uso futuro).
- **Erros padronizados** (`exception/`): `ResourceNotFoundException` → 404; `IllegalArgumentException` → 400; `MethodArgumentNotValidException` → 400. Body: `timestamp`, `status`, `erro`, `mensagem`, `caminho`.
- **Comandos**: `.\mvnw.cmd spring-boot:run` (rodar — sobe e roda o seed), `.\mvnw.cmd compile` (compilar).
- **Docs**: `docs/` contém `planejamento-desenvolvimento-vigia-backend.md`, `vigia-specs-endpoints.md`, `guia-desenvolvimento.md`, `mudancas-docs-relatorio.md`, este `ESTADO_DESENVOLVIMENTO.md` e `diagramas/`. `AGENTS.md` na raiz é a fonte das regras persistentes. Checklist da entrega: `C:\Users\teric\Desktop\tarefas-vigia-naive-v1.md`.