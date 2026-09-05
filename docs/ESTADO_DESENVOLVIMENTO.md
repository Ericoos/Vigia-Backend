# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: **Fase 1** — fatia vertical de uma `@Entity` de ponta a ponta (Repository → Service → Controller). Fase 0 (passos 0.1 a 0.6) concluída. As entidades esqueleto já existem, mas a fatia vertical ainda não subiu.
- **Últimas classes finalizadas**: entidades esqueleto do Murilo (`Reporte`, `Categoria`, `Subcategoria`, `Bairro` e o enum `StatusReporte`) vieram do remoto (commits `8f26aa1` e `7415eae`). **São só esqueleto**: atributos + `@NoArgsConstructor`, **sem métodos** — as regras de negócio vão para os `@Service`, conforme AGENTS.md.
- **Bloqueio de compilação**: falta criar a entidade `Usuario`. O `Reporte` já referencia um `Usuario` que não existe — o código não compila até isso existir.
- Estado local (05/09/2026): `main` em `b676dbb` (docs reorganizados + `application.properties` **já commitado e corrigido**).

## 🛠️ 2. Modificações Efetuadas nesta Sessão
**Vindas do remoto — Murilo (`Murilovers06`), esqueleto:**
- `8f26aa1` - `feat: cria entidade Reporte` → `src/main/java/com/uscs/vigia/model/Reporte.java` (idReporte, titulo, descricao, latitude, longitude, endereco, status, dataReporte; `@ManyToOne` para `Usuario` [ainda inexistente], `Categoria`, `Subcategoria` e `Bairro`).
- `7415eae` - `feat: adiciona entidades Categoria, Subcategoria, Bairro e StatusReporte` → `Categoria.java` (tipoCategoria), `Subcategoria.java` (tipoSubcategoria + `@ManyToOne` categoria), `Bairro.java` (nome, classificacaoSeguranca) e `StatusReporte.java` (enum RASCUNHO/PUBLICADO/EXCLUIDO).

**Locais (não commitadas):**
- `src/main/java/com/uscs/vigia/model/Pessoa.java` e `UserType.java` → rascunhos incompletos (ex.: `protected enum status;` nem compila). Serão **descartados** ao implementar `Usuario` com herança `@Inheritance(JOINED)` (`Pessoa → Usuario/Admin`) — sem enum `UserType`.

**Nesta rodada (docs):**
- `docs/planejamento-desenvolvimento-vigia-backend.md` e `docs/guia-desenvolvimento.md` → seção de modelo de dados e ponto de controle corrigidos para refletirem que as entidades MVP já existem como esqueleto.
- Este `ESTADO_DESENVOLVIMENTO.md` → atualizado com o ponto de controle acima.

## 📋 3. Estado das Dependências instaladas
- **Nenhuma dependência nova nesta sessão.** Permanecem as do `pom.xml`: `spring-boot-starter-webmvc`, `spring-boot-h2console`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `com.h2database:h2` (runtime), `org.projectlombok:lombok`, mais os starters de teste.
- **Ainda NÃO adicionadas** (fases futuras): `mysql-connector-j` (quando o MySQL existir), `springdoc-openapi-starter-webmvc-ui` (Swagger, Fase 2), Spring Security (fora do escopo).
- ⚠️ H2 é ponte temporária `MODE=MySQL`; o banco do projeto é **MySQL**, de outra pessoa da equipe, ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
1. **Aguardando upload dos diagramas do TCC** em `docs/diagramas/` (pasta ainda vazia). Assim que os `.drawio` forem colocados: revisar se as entidades esqueleto correspondem ao diagrama de classes antes de evoluir.
2. **Criar a entidade `Usuario`** (Fase 1, Passo 1.1): aplicar herança `@Inheritance(JOINED)` (`Pessoa → Usuario/Admin`); descartar `Pessoa.java` e `UserType.java`; confirmar que o projeto volta a compilar e que as tabelas aparecem no h2-console.
3. Definir o que fazer com `avaliacaoGravidade` (mock/Tela 3 exigem, `Reporte` atual não tem — corrigir o diagrama).
4. Subir a fatia vertical `Repository` → `Service` → `Controller` (Passos 1.2 a 1.4), com commit a cada camada que funcionar.
5. **PENDÊNCIAS ABERTAS (não bloqueantes)**: (a) validar contrato de `vigia-specs-endpoints.md` com o front; (b) fórmula de `calcularClassificacao()` do bairro não definida; (c) credenciais do MySQL desconhecidas; (d) quem manda no schema (`ddl-auto` vs script SQL); (e) CORS não configurado.

## 🧠 5. Contexto Técnico para o Próximo Chat
- **Stack**: Java 21, Spring Boot 4.1.1, Maven (`./mvnw`), Spring Data JPA, Bean Validation, Lombok.
- **Versionamento**: repo `https://github.com/Ericoos/Vigia-Backend.git`, branch `main`. Último commit local `b676dbb`. Colaboradores: `Ericoos` (docs/infra) e `Murilovers06` (entidades). **Commit/push só com aprovação explícita do usuário; mensagens em português.**
- **Banco**: esquema gerado pelo Hibernate a partir das `@Entity` via `ddl-auto=update`; **nunca usar `nativeQuery`**; H2 modo arquivo `MODE=MySQL` até o MySQL da equipe existir.
- **Arquitetura**: `controller` → `service` → `repository` → `model`; integrações externas em `integration`. `VigiaApplication.java` na raiz do pacote (varredura do `@SpringBootApplication`).
- **Regras de tradução (AGENTS.md)**: regras de negócio no `@Service`, nunca na `@Entity`; FK vira `@ManyToOne` com referência de objeto; sem acentos em identificadores; herança com `@Inheritance(JOINED)`.
- **Entidades esqueleto existentes**: `Reporte` (referencia `Usuario` — **a criar**), `Categoria`, `Subcategoria`, `Bairro`, enum `StatusReporte`. Enums previstos no diagrama: `StatusUsuario` (ATIVO, SUSPENSO, BANIDO, DELETADO, INATIVO) e `StatusReporte` (RASCUNHO, PUBLICADO, EXCLUIDO).
- **Divergências conhecidas**: `avaliacaoGravidade` não consta em `Reporte` (existe no mock/Tela 3); campo `idReporte` usado como id; `Pessoa.java` e `UserType.java` são rascunhos inválidos a descartar; diagramas ainda não carregados em `docs/diagramas/`.
- **Docs**: `docs/` contém `planejamento-desenvolvimento-vigia-backend.md` (plano), `vigia-specs-endpoints.md` (contrato da API), `guia-desenvolvimento.md` (onboarding), este `ESTADO_DESENVOLVIMENTO.md` e `diagramas/` (aguardando upload). `AGENTS.md` na raiz é a fonte das regras persistentes.
- **Comandos**: `./mvnw spring-boot:run` (rodar), `./mvnw clean install` (build).