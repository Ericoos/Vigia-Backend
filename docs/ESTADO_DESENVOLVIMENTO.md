# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: **Fase 1** — primeira `@Entity` de ponta a ponta (Repository → Service → Controller). Passos 0.1 a 0.6 concluídos.
- **Últimas classes finalizadas**: entidades de domínio `Reporte`, `Categoria`, `Subcategoria`, `Bairro` e o enum `StatusReporte` vieram do remoto (commits do Murilo). **Falta criar a entidade `Usuario`** — o `Reporte` já referencia um `Usuario` que ainda não existe (o código não compila até isso existir).
- Estado local de hoje (05/09/2026): `git pull origin main` aplicado (fast-forward `2d661e7..7415eae`).

## 🛠️ 2. Modificações Efetuadas nesta Sessão
**Vindas do remoto — Murilo (`Murilovers06`):**
- `8f26aa1` - `feat: cria entidade Reporte` → cria `src/main/java/com/uscs/vigia/model/Reporte.java` (idReporte, titulo, descricao, latitude, longitude, endereco, status, dataReporte; `@ManyToOne` para `Usuario` [ainda inexistente], `Categoria`, `Subcategoria` e `Bairro`).
- `7415eae` - `feat: adiciona entidades Categoria, Subcategoria, Bairro e StatusReporte` → cria `Categoria.java` (tipoCategoria), `Subcategoria.java` (tipoSubcategoria + `@ManyToOne` categoria), `Bairro.java` (nome, classificacaoSeguranca) e `StatusReporte.java` (enum RASCUNHO/PUBLICADO/EXCLUIDO).

**Locais (minhas, ainda não commitadas):**
- `src/main/resources/application.properties` -> corrigidos typos que impediam o bootstrap correto: `spring.jpa.database-plataform` → `spring.jpa.database-platform` e `spring.jpa.hibernate.ddl-audo=update` → `spring.jpa.hibernate.ddl-auto=update`.
- Rascunhos não rastreados `Pessoa.java` e `UserType.java` no pacote `model` -> criados para modelar usuário, mas **incompletos e fora do padrão** (`protected enum status;` nem compila). Serão substituídos/refeitos ao implementar a entidade `Usuario` (herança `@Inheritance(JOINED)` `Pessoa → Usuario/Admin` conforme AGENTS.md — sem enum `UserType`).

**Reorganização da documentação:**
- `GUIAKAUA.md` (raiz) movido para `docs/guia-desenvolvimento.md` (git mv, renomeado e generalizado).
- Criada a pasta `docs/diagramas/` (aguardando upload dos `.drawio` do TCC).
- `docs/planejamento-desenvolvimento-vigia-backend.md`: removidas as seções 1 e 2 (stack e estrutura de pastas), que duplicavam o `AGENTS.md`; o documento agora aponta para o `AGENTS.md` como fonte das regras e concentra só o plano (modelo de dados, endpoints, fases, protocolo de estado).
- Renomeados: `CLAUDE.md` → `AGENTS.md` (raiz) e `docs/guia-kaua-backend.md` → `docs/guia-desenvolvimento.md` (generalizado para novos integrantes do time). Referências cruzadas atualizadas.

## 📋 3. Estado das Dependências instaladas
- **Nenhuma dependência nova nesta sessão.** Permanecem as do `pom.xml`: `spring-boot-starter-webmvc`, `spring-boot-h2console`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `com.h2database:h2` (runtime), `org.projectlombok:lombok`, mais os starters de teste.
- **Ainda NÃO adicionadas** (fases futuras): `mysql-connector-j` (quando o MySQL existir), `springdoc-openapi-starter-webmvc-ui` (Swagger, Fase 2), Spring Security (fora do escopo).
- ⚠️ H2 é ponte temporária `MODE=MySQL`; o banco do projeto é **MySQL**, de outra pessoa da equipe, ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
1. **Criar a entidade `Usuario`** (Fase 1, Passo 1.1): consultar o `DIAGRAMA_DE_CLASSES.drawio` e a spec antes de escrever; aplicar herança `@Inheritance(JOINED)` (`Pessoa → Usuario/Admin`); descartar/substituir `Pessoa.java` e `UserType.java` incompletos; confirmar que o projeto volta a compilar e que o Hibernate cria as tabelas no h2-console.
2. Definir o que fazer com `avaliacaoGravidade` (mock/Tela 3 exigem, `Reporte` atual não tem).
3. Subir a fatia vertical `Repository` → `Service` → `Controller` (Passos 1.2 a 1.4), com commit a cada camada que funcionar.
4. **PENDÊNCIAS ABERTAS (não bloqueantes)**: (a) professora não respondeu o esperado na entrega; (b) contrato de `vigia-specs-endpoints.md` não validado com o front; (c) fórmula de `calcularClassificacao()` do bairro não definida; (d) credenciais do MySQL desconhecidas; (e) quem manda no schema (`ddl-auto` vs script SQL); (f) CORS não configurado.
5. **Aguardando upload dos diagramas**: assim que os `.drawio` do TCC forem colocados em `docs/diagramas/`, revisar se as entidades criadas correspondem ao diagrama de classes.

## 🧠 5. Contexto Técnico para o Próximo Chat
- **Stack**: Java 21, Spring Boot 4.1.1, Maven (`./mvnw`), Spring Data JPA, Bean Validation, Lombok.
- **Versionamento**: repo `https://github.com/Ericoos/Vigia-Backend.git`, branch `main`. Colaboradores ativos: `Ericoos` (docs/infra) e `Murilovers06` (entidades). Últimos commits: `7415eae` e `8f26aa1` (Murilo, entidades) → `2d661e7` (docs). Local tem `application.properties` corrigido pendente de commit.
- **Banco**: esquema gerado pelo Hibernate a partir das `@Entity` via `ddl-auto=update`; **nunca usar `nativeQuery`**; H2 modo arquivo `MODE=MySQL` até o MySQL da equipe existir.
- **Arquitetura**: `controller` → `service` → `repository` → `model`; integrações externas em `integration`. `VigiaApplication.java` na raiz do pacote (varredura do `@SpringBootApplication`).
- **Regras de tradução (AGENTS.md)**: regras de negócio no `@Service`, nunca na `@Entity`; FK vira `@ManyToOne` com referência de objeto; sem acentos em identificadores; herança com `@Inheritance(JOINED)`.
- **Entidades existentes**: `Reporte` (referencia `Usuario` — **a criar**), `Categoria`, `Subcategoria`, `Bairro`, enum `StatusReporte`. Enums previstos no diagrama: `StatusUsuario` (ATIVO, SUSPENSO, BANIDO, DELETADO, INATIVO) e `StatusReporte` (RASCUNHO, PUBLICADO, EXCLUIDO).
- **Divergências conhecidas**: `avaliacaoGravidade` não consta em `Reporte` (existe no mock/Tela 3); campo `idReporte` usado como nome do id em `Reporte`.
- **Docs**: `docs/` contém `planejamento-desenvolvimento-vigia-backend.md` (plano), `vigia-specs-endpoints.md` (contrato da API), `guia-desenvolvimento.md` (onboarding de novos integrantes), `ESTADO_DESENVOLVIMENTO.md` (este) e `diagramas/` (a aguardar upload). `AGENTS.md` na raiz é a fonte das regras persistentes.
- **Comandos**: `./mvnw spring-boot:run` (rodar), `./mvnw clean install` (build). Commit/push apenas com aprovação explícita do usuário; mensagens em português.