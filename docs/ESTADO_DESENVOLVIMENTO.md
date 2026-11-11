# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Retomada (10/11/2026 — `tarefas-murilo-semana.md`)**: preparação da seção 0 concluída na ordem do documento: pull na `main` (base `4c056a2`), arquivo local JWT criado a partir do exemplo (ignorado pelo Git), inicialização confirmada e leitura de `AGENTS.md`/guia concluída. A chave não deve ser registrada na documentação nem versionada. Trabalho em etapas, explicando e aguardando autorização de Murilo para avançar.
- **Tarefa 1 concluída (10/11/2026)**: após autorizações por etapa, adicionados `BairroService.buscarPorTipo(TipoBairro)` e `GET /api/bairro/{tipoBairro}` no `BairroController`. Murilo parou a API com `Ctrl+C` antes de cada alteração Java. Compilação `BUILD SUCCESS`; API reiniciada com `Started VigiaApplication in 3.197 seconds` às 15:27:00, porta 8080, PID 13432. Testes HTTP via curl: `CENTRO` → 200 com `BairroResponse` e classificação `null`; `NAO_EXISTE` → 400. Tarefa pronta para revisão e commit, sem commit/push executados. Tarefa 2 aguarda nova autorização.
- **Fase/Passo Atual**: **Fases 2 e 3 concluídas** — o **módulo Reporte do backend está funcional de ponta a ponta** (entrega naive v1). As **tarefas 1 a 9** do checklist `C:\Users\teric\Desktop\tarefas-vigia-naive-v1.md` estão **concluídas e testadas**. Falta apenas a **Tarefa 10 — integração com o app** (contrato via IP da rede + teste ponta a ponta), que o time fará depois.
- **Sessão (01/11/2026)**: `docs/relatorio-vigia.txt` substituído pela versão do 4º semestre (`RELATORIO_VIGIA_4SEM.pdf`, 131 páginas), que inclui o capítulo 7 (Desenvolvimento do Sistema) e o Apêndice D (script de criação do banco). Marcadores `===== Página N =====` indicam a página do PDF. Criado `docs/plano-mvp-novembro.md` com todas as tarefas até a entrega de 30/11 (Fases 5 a 11). **Tarefas 5.1 a 5.4 concluídas**: a chave primária de `Categoria` e `Subcategoria` agora é o próprio enum (`TipoCategoria`/`TipoSubcategoria`), sem id numérico; repositórios usam o enum como tipo do id; `ReporteRequest` recebe `categoria` e `subcategoria` pelo nome (ex.: `"INSEGURANCA"`, `"ROUBO"`); `GlobalExceptionHandler` trata `HttpMessageNotReadableException` (nome inexistente → 400 padronizado); `vigia-specs-endpoints.md` atualizado. O H2 antigo foi movido para `data/backup-2026-11-01/` e o esquema recriado pelo seed. Testado: GET categorias/subcategorias/reportes, POST 201, sub desalinhada 400, nome inválido 400, campos faltando 400; `mvnw test` passa. **Contrato mudou — avisar o front.** Próxima tarefa: ~~5.6~~ (feita, ver acima).
- **Fase 6 / passo 3 — cadastro e login, em andamento (10/11/2026)**: **Cadastro (A1) concluído**: `POST /api/auth/cadastro` (`AuthController` → `AuthService.cadastrar`) recebe `CadastroRequest` (nome 3–100, e-mail, telefone só dígitos com DDD — regex `^[1-9]{2}(9\d{8}|[2-5]\d{7})$`, data de nascimento no passado, senha 8–30 com maiúscula, minúscula, número e especial), grava a senha com BCrypt (`senhaHash`) e devolve `201` com `UsuarioResponse(id, nome, email, telefone, dataNascimento, avatar)` — sem senha. Testado: 201 válido (hash `$2a$10$...` no H2), 400 senha fraca, 400 telefone inválido. `status` e `dataInicio` ainda `null` (entram no B5); e-mail repetido ainda dá erro do banco (B2). **Avisar o Fabrício**: o cadastro exige `telefone` (só números). Próximo: login (A2). plano em duas etapas — **A** (cadastro e login puros: `CadastroRequest`, `UsuarioResponse`, `LoginRequest`, `LoginResponse`, `AuthService`, `AuthController` em `/api/auth`) e **B** (regras: validação, e-mail único 409, e-mail normalizado, idade mínima 13, login errado 401 genérico, conta `USUARIO_ATIVO` + `dataInicio`, só ativa loga 403). Escopo: CAPTCHA fora; confirmação de e-mail depois do MVP se não houver tempo; termos/LGPD ainda não. Detalhes em `docs/plano-mvp-novembro.md` (Fase 6). Pull do commit do Murilo `913bf22` (detalhe do bairro).
- **Fase 6 / passo 2 — JWT concluído (08/11/2026)**: `spring-boot-starter-security-oauth2-resource-server` no `pom.xml` (traz o Nimbus para gerar e conferir JWT); `application.properties` com `vigia.jwt.secret=${JWT_SECRET}` sem valor padrão (HS256, mínimo 32 caracteres). A chave fica em `src/main/resources/application-local.properties`, **fora do Git** (`.gitignore`), lido via `spring.config.import=optional:classpath:...`; modelo em `application-local.properties.example`; em produção vem da variável de ambiente `JWT_SECRET`. Sem chave, a API não sobe e `vigia.jwt.expiracao-horas=24`. `SecurityConfig` ganhou os beans `JwtEncoder`/`JwtDecoder` (Nimbus, HS256, mesma chave) e `oauth2ResourceServer(jwt)` — toda requisição com `Authorization: Bearer` tem o token conferido; rotas continuam `permitAll`. Novo `service/TokenService.gerarToken(Usuario)` com `iss=vigia-api`, `sub`=id do usuário, `iat` e `exp` (24h); ainda não é chamado (entra no login, passo 4). Testado: API sobe lendo a chave local, a linha "Using generated security password" sumiu, GET sem token 200, GET com token falso 401. Próximo: passo 3 — cadastro (`POST /api/auth/cadastro`).
- **Fase 6 / passo 1 — base do Spring Security (08/11/2026)**: `spring-boot-starter-security` (+ `-test`) no `pom.xml`; novo `config/SecurityConfig.java` com `SecurityFilterChain` (CSRF desligado, sessão `STATELESS`, `frameOptions(sameOrigin)` para o console do H2, **todas as rotas liberadas por enquanto**) e bean `PasswordEncoder` (BCrypt). Testado: GET 200 com os cabeçalhos do Security, POST 201, validação 400. O log ainda mostra "Using generated security password" (usuário padrão do Boot) — some quando o JWT for configurado no passo 2.
- **Validação de tamanho no reporte (08/11/2026)**: `ReporteRequest` com `@Size` — `titulo` de 5 a 30 caracteres e `descricao` de 20 a 500 (mesma regra do app). Fora do tamanho → `400` padronizado. Testado no Postman (curta 400, válida 201, longa 400). **Avisar o front da regra do título.**
- **Tarefa 12 / plano 5.7 concluída (01/11/2026)**: novo enum `TipoBairro` com os 15 bairros de São Caetano do Sul (constante sem acento + `nome` com acento para exibir). `Bairro` passa a ter o enum como `@Id` (sai `Long id` e `String nome`), `BairroRepository` usa `TipoBairro` como chave e `BairroResponse` devolve `tipoBairro`, `nome` e `classificacaoSeguranca`. `DataSeeder` separado em `seedCategorias()` e `seedBairros()`, cada um só roda com a tabela vazia. DTOs de resposta com os parâmetros numa linha. **Apagar `data/vigia.mv.db` antes de subir** (chave do bairro mudou). Compila; ainda não testado subindo a API.
- **Tarefa 11 / plano 5.6 concluída (01/11/2026)**: `Usuario` alinhado ao script do relatório — `UsuarioBase` com `id`, `nome`, `email` (único), `senhaHash`, `dataNascimento`, `status` (`TipoUsuario`: ATIVO, SUSPENSO, BANIDO, DELETADO, INATIVO — substitui o `ativo` do script); `Usuario` com `telefone`, `avaliacao` (`Double`), `avatar`, `dataInicio`, `dataFim`. `UsuarioRepository` ganhou `findByEmail` e `existsByEmail` (usados no cadastro/login). H2 antigo movido para `data/backup-2026-11-01/vigia-antes-descricao-text.mv.db` (vazio). Próxima tarefa: **Fase 6 — Spring Security + JWT, cadastro e login**.
- **Revisão de código (01/11/2026)**: campos `private` em todo o model (`UsuarioBase` saiu de `protected`; Lombok gera os acessos); `BairroController` com dependência `private final`; imports e o método `salvar` sem uso removidos; `TipoCategoria.calculaClassificacao` usa o próprio `getPeso()`; `ReporteService.criar` devolve o reporte salvo; `GET /api/reportes/{id}` dá `404` se o reporte não for `PUBLICADO`; `Reporte.descricao` virou `TEXT`; categorias, subcategorias e bairros saem por DTO (`CategoriaResponse`, `SubcategoriaResponse`, `BairroResponse`) e cada sub passa a trazer `categoria` no JSON; `UsuarioController` removido (expunha a entidade com `senhaHash`) — volta como `/api/auth` e `/api/usuarios/me` nas Fases 6 e 7. **Apagar `data/vigia.mv.db` antes de subir** (coluna `descricao` e colunas do `Usuario` mudaram). Testado em banco temporário; `mvnw test` passa.
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
**Etapa autorizada em 10/11/2026:**
- `service/BairroService.java` → `buscarPorTipo(TipoBairro)` usa `bairroRepository.findById`, lança `ResourceNotFoundException` se ausente e devolve o DTO pelo `toResponse` existente.
- `controller/BairroController.java` → nova rota `GET /{tipoBairro}`, recebendo `@PathVariable TipoBairro` e delegando ao Service.
- `docs/vigia-specs-endpoints.md` → documentada a rota de detalhe do bairro e os retornos esperados.
- `application-local.properties` → configuração JWT local ignorada pelo Git; não incluir em commit.
- `docs/ESTADO_DESENVOLVIMENTO.md` → preparação, verificação e progresso parcial registrados.

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
- **10/11/2026**: nenhuma dependência nova nesta etapa. A base atual já inclui Spring Security, OAuth2 Resource Server/JWT e BCrypt; os itens históricos abaixo descrevem a entrega anterior.
- Permanecem as do `pom.xml`: Spring Web, Spring Data JPA, Bean Validation, H2 (runtime), Lombok, starters de teste.
- **Não adicionadas**: `mysql-connector-j`, Swagger, Spring Security.
- ⚠️ H2 é ponte temporária `MODE=MySQL`; o banco do projeto é **MySQL**, de outra pessoa da equipe, ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
**Próximos passos (tarefas semanais de Murilo):** revisar a Tarefa 1 concluída; commit somente mediante autorização e conforme o workflow do projeto; avisar Erick antes do push e mover o cartão para Revisão. Depois, mediante nova autorização, iniciar a Tarefa 2 (classificação por soma dos reportes publicados). Tarefas 2, 3 e 4 ainda não iniciadas. Preservar os arquivos de autenticação e os arquivos/método reservados ao Erick no documento semanal.

**Histórico da entrega naive v1:**
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
