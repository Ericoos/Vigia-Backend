# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: **Fase 1, Passo 1.2 a 1.4 concluídos para o `Reporte`** (Repository → Service → Controller de `/api/reportes`). **Passos 1.2 a 1.4 concluídos para `Categoria` e `Subcategoria`** (GETs de `/api/categorias` e `/api/subcategorias`). Pendente: Fase 2 (DTOs/validação), fattia vertical do `Usuario` (responsabilidade de outro integrante) e o restante.
- **Sessão (06/09/2026) — Murilo (Reporte/Categoria/Subcategoria)**: adicionado `ReporteController` (`GET`/`POST` `/api/reportes`); estrutura completa de `Categoria` e `Subcategoria` (Repository + Service + Controller, GETs); `CategoriaService` padronizado no estilo do `ReporteService` (retorna entidade, sem `salvar` — criação por seed); ajustes em `Categoria` (`@OneToMany` EAGER de subcategorias) e em `Subcategoria` (`@JsonProperty WRITE_ONLY` na referência a `Categoria`, para evitar recursão no JSON mantendo a leitura no POST).
- **Fase/Passo Atual (herdado)**: **Fase 1, Passo 1.1** — entidades de usuário modeladas (`UsuarioBase`, `TipoUsuario`, `Usuario`). Pendente: compilar/rodar, confirmar tabelas no `h2-console` e subir Repository → Service → Controller (Passos 1.2 a 1.4) do `Usuario`.
- **Decisão de herança tomada**: `Pessoa` do diagrama virou o molde `UsuarioBase` com **`@MappedSuperclass`** (sem tabela própria), abandonando `@Inheritance(JOINED)`. `Usuario` é `@Entity` que herda os campos do molde. `PerfilPublico` é entidade de fase futura e vai receber `avaliacao` e `avatar` (relação 1-1 com `Usuario`).
- Estado local (05/09/2026): `main` em `b676dbb` + entidades usuário modificadas localmente (commit pendente de aprovação).
- Sessão de **documentação (06/09/2026)**: classificação do bairro definida em parte (base 5,0 / teto 5,0 / piso 0; `(peso × gravidade) ÷ 10`; SEGURANCA soma, INSEGURANCA subtrai; presença policial = positivo), criado `mudancas-docs-relatorio.md` e atualizados os `.md` de plano/spec/guia/AGENTS.

## 🛠️ 2. Modificações Efetuadas nesta Sessão
**Código:**
- `src/main/java/com/uscs/vigia/model/Reporte.java` → `@Entity` do Reporte (`idReporte`, titulo, descricao, latitude, longitude, endereco, `@Enumerated StatusReporte status`, `OffsetDateTime dataReporte`; `@ManyToOne` usuario/categoria/subcategoria/bairro). Commit `8f26aa1`.
- `src/main/java/com/uscs/vigia/model/StatusReporte.java`, `Categoria.java`, `Subcategoria.java`, `Bairro.java` → entidades de apoio do Reporte (Commit `7415eae`).
- `src/main/java/com/uscs/vigia/repository/ReporteRepository.java` → `JpaRepository<Reporte, Long>` (Commit `66d0bf7`).
- `src/main/java/com/uscs/vigia/service/ReporteService.java` → `listar()`/`salvar()` delegando ao repository (Commit `9d66b38`).
- `src/main/java/com/uscs/vigia/controller/ReporteController.java` → `GET`/`POST /api/reportes` (POST devolve `201 Created`).
- `src/main/java/com/uscs/vigia/repository/CategoriaRepository.java` e `SubcategoriaRepository.java` → interfaces `JpaRepository`.
- `src/main/java/com/uscs/vigia/service/CategoriaService.java` e `SubcategoriaService.java` → padrão do `ReporteService` (listar apenas; criação fica com seed no boot).
- `src/main/java/com/uscs/vigia/controller/CategoriaController.java` e `SubcategoriaController.java` → `GET /api/categorias` e `GET /api/subcategorias`.
- `src/main/java/com/uscs/vigia/model/Categoria.java` → adicionada coleção `subcategorias` com `@OneToMany(mappedBy = "categoria", fetch = EAGER)` (aninhamento no `GET /api/categorias`).
- `src/main/java/com/uscs/vigia/model/Subcategoria.java` → `@JsonProperty(access = WRITE_ONLY)` na referência `categoria` (evita recursão/repetição no JSON, ainda aceita `categoria` na entrada).
- `src/main/java/com/uscs/vigia/model/UsuarioBase.java` → molde `@MappedSuperclass` com `Long id`, nome, email, senha, `LocalDate dataNascimento`, `@Enumerated(STRING) TipoUsuario status`. (Rascunhos `Pessoa.java`/`UserType.java` substituídos pelo molde + enum.)
- `src/main/java/com/uscs/vigia/model/TipoUsuario.java` → enum `USUARIO_ATIVO, USUARIO_SUSPENSO, USUARIO_BANIDO, USUARIO_DELETADO, USUARIO_INATIVO` (equivale ao `StatusUsuario` do diagrama).
- `src/main/java/com/uscs/vigia/model/Usuario.java` → `@Entity extends UsuarioBase` com `telefone` e `dataCadastro`.

**Docs:**
- `AGENTS.md` → regra 4 de tradução atualizada: herança usa `@MappedSuperclass`, não mais `@Inheritance(JOINED)`.
- `docs/planejamento-desenvolvimento-vigia-backend.md` → modelo de dados reflete `UsuarioBase`/`Usuario` sem `avaliacao`; enum `TipoUsuario`; nota de decisão `avaliacao`/`avatar` → `PerfilPublico`.
- `docs/guia-desenvolvimento.md` → regra 5 de herança e ponto de controle atualizados.
- `docs/vigia-specs-endpoints.md` → Endpoint 1 (cadastro/login) sem `avaliacao`/`avatar`; camadas descrevem `UsuarioBase` (`@MappedSuperclass`) + `Usuario` com telefone/dataCadastro.
- `docs/diagramas/` → **7 `.drawio` do TCC carregados** (antes a pasta estava vazia). O `DIAGRAMA DE COMPONENTES E CONTEINERS (C4 MODEL).drawio` veio vazio (10 linhas) — a baixar a versão corrigida.
- `docs/relatorio-vigia.txt` → texto extraído do `RELATÓRIO VIGIA - CORREÇÃO.docx` (script SQL do Apêndice D: sem tabela `pessoa`; `usuario` com `avaliacao`; `perfil_publico` sem `avatar`).
- **Simplificação de escopo (09/2026)**: CORS, PUT/DELETE, `avatar`/`avaliacao` e as "entidades das fases seguintes" foram marcados como **fora do MVP** (CORS não se aplica a React Native; `avatar`/`avaliacao` dependem de `PerfilPublico`; o app não edita nem apaga reportes). Coordenadas fora de SCS: decidido aceitar. Docs tocados: `planejamento-...`, `vigia-specs-endpoints.md`, `guia-desenvolvimento.md`.
- **Classificação do bairro (06/09/2026)**: fórmula decidida em parte — `calcularClassificacao()` no Service, nota parte de **5,0** (teto 5,0, piso 0), contribuição por reporte = `(peso da subcategoria × avaliacaoGravidade) ÷ 10`, `SEGURANCA` soma / `INSEGURANCA` subtrai, presença policial é fator positivo. `Categoria`/`Subcategoria` continuam como `@Entity` alimentadas por seed dos enums (`TipoCategoria`, `TipoSubcategoria` com peso). Diagrama de pacotes: só faltam `DTO` e `EXCEPTION` (`REPOSITORIES` já existe). **Criado** `docs/mudancas-docs-relatorio.md` (lista de mudanças no relatório/diagramas + o que implementar) e **atualizados** `planejamento-...`, `vigia-specs-endpoints.md`, `guia-desenvolvimento.md`, `AGENTS.md` e este arquivo. **Pendente para a próxima sessão: tabela de pesos das subcategorias.**

## 📋 3. Estado das Dependências instaladas
- **Nenhuma dependência nova nesta sessão.** Permanecem as do `pom.xml`: `spring-boot-starter-webmvc`, `spring-boot-h2console`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `com.h2database:h2` (runtime), `org.projectlombok:lombok`, mais os starters de teste.
- **Ainda NÃO adicionadas** (fases futuras): `mysql-connector-j` (quando o MySQL existir), `springdoc-openapi-starter-webmvc-ui` (Swagger, Fase 2), Spring Security (fora do escopo).
- ⚠️ H2 é ponte temporária `MODE=MySQL`; o banco do projeto é **MySQL**, de outra pessoa da equipe, ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
0. **Fase 1 de `Reporte`/`Categoria`/`Subcategoria` concluída.** Próximo (Murilo): **Fase 2** — DTOs do Reporte (`ReporteRequest`, `ReporteResponse`) com Bean Validation, `@RestControllerAdvice` para erros, e **seed** de `Categoria`/`Subcategoria` a partir dos enums no boot (hoje os GETs devolvem lista vazia até existir seed).
1. **Definir a tabela de pesos** das subcategorias e confirmar a lista final de subs de `SEGURANCA` (`PRESENCA_POLICIAL`, `AREA_MOVIMENTADA`, `CAMERAS`, `FATOR_POSITIVO`...) — registrado como pendência em `mudancas-docs-relatorio.md`.
2. **Revisar `Usuario.java`** (digitação do usuário): campo `dataCadastro` está como `double` — o esperado é `LocalDateTime` (padrão do `Reporte.dataReporte` usa `OffsetDateTime`). Decidir o tipo junto com o usuário.
3. **Compilar e validar o modelo de usuário**: rodar `./mvnw clean install`, conferir no `h2-console` que o schema tem as tabelas + enum (`USUARIO_ATIVO` etc.) e que **não** existe tabela `pessoa`.
4. **Confirmar o diagrama C4** (veio vazio): pedir a versão corrigida ou reconstruir pelo `COMPONENTES UML.drawio`.
5. **Subir a fatia vertical** `Repository` → `Service` → `Controller` do `Usuario` (Passos 1.2 a 1.4), com commit a cada camada que funcionar.
6. **PENDÊNCIAS ABERTAS (não bloqueantes)**: (a) validar contrato de `vigia-specs-endpoints.md` com o front; (b) criar `PerfilPublico` e mover `avaliacao`/`avatar` para lá (atualizar diagrama); (c) **tabela de pesos** da classificação (demais parâmetros já definidos); (d) credenciais do MySQL desconhecidas; (e) quem manda no schema (`ddl-auto` vs script SQL do relatório); (f) CORS não configurado; (g) aplicar no `RELATÓRIO...docx` e nos `.drawio` as mudanças listadas em `mudancas-docs-relatorio.md` (parceria com o grupo).

## 🧠 5. Contexto Técnico para o Próximo Chat
- **Stack**: Java 21, Spring Boot 4.1.1, Maven (`./mvnw`), Spring Data JPA, Bean Validation, Lombok.
- **Versionamento**: repo `https://github.com/Ericoos/Vigia-Backend.git`, branch `main`. Último commit local `b676dbb`. Colaboradores: `Ericoos` (docs/infra) e `Murilovers06` (entidades). **Commit/push só com aprovação explícita do usuário; mensagens em português.**
- **Banco**: esquema gerado pelo Hibernate a partir das `@Entity` via `ddl-auto=update`; **nunca usar `nativeQuery`**; H2 modo arquivo `MODE=MySQL` até o MySQL da equipe existir.
- **Arquitetura**: `controller` → `service` → `repository` → `model`; integrações externas em `integration`. `VigiaApplication.java` na raiz do pacote (varredura do `@SpringBootApplication`).
- **Herança (regra 4 do AGENTS.md)**: `Pessoa` (diagrama) = `UsuarioBase` (molde `@MappedSuperclass`, sem tabela própria); `Usuario` e `Admin` são `@Entity` que copiam os campos. Nada de `@Inheritance(JOINED)`.
- **Entidades existentes no `model`**: `Reporte` (referencia `Usuario`), `Categoria`, `Subcategoria`, `Bairro`, `StatusReporte`, `UsuarioBase` (molde), `TipoUsuario`, `Usuario`. Enums no diagrama: `StatusUsuario`/`TipoUsuario` (ATIVO/SUSPENSO/BANIDO/DELETADO/INATIVO) e `StatusReporte` (RASCUNHO/PUBLICADO/EXCLUIDO).
- **Divergências conhecidas**: `avaliacaoGravidade` não consta em `Reporte` (existe no mock/Tela 3); campo `idReporte` usado como id; `avaliacao` no script SQL fica no `usuario`, mas o diagrama prevê `PerfilPublico` com `avatar` e o relatório traz `bio` — alinhar; `admin` no script SQL tem FK `idUsuario`, divergente do modelo de herança.
- **Classificação do bairro**: `calcularClassificacao()` no `BairroService`; base 5,0 (teto 5,0, piso 0), contribuição `(peso × gravidade) ÷ 10`; peso na enum `TipoSubcategoria`, direção na enum `TipoCategoria` (SEGURANCA soma / INSEGURANCA subtrai); `Categoria`/`Subcategoria` com seed dos enums. **Diagrama de pacotes**: falta só `DTO` e `EXCEPTION` (e aplicar as mudanças listadas em `mudancas-docs-relatorio.md`).
- **Docs**: `docs/` contém `planejamento-desenvolvimento-vigia-backend.md` (plano), `vigia-specs-endpoints.md` (contrato da API), `guia-desenvolvimento.md` (onboarding), `mudancas-docs-relatorio.md` (mudanças no relatório + implementação — **novo**), este `ESTADO_DESENVOLVIMENTO.md`, `diagramas/` (7 `.drawio`, C4 ainda vazio) e `relatorio-vigia.txt`. `AGENTS.md` na raiz é a fonte das regras persistentes (nova seção `⏳ Classificação de Bairro`).
- **Comandos**: `./mvnw spring-boot:run` (rodar), `./mvnw clean install` (build).