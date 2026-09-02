# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: Fase 0 — Infraestrutura e Versionamento — Passo 0.4 concluído. Próximo: Passo 0.5 (configuração do banco, em aberto).
- **Última classe/endpoint finalizado**: Nenhuma classe de domínio foi escrita ainda. Concluída a infraestrutura inicial: projeto Spring Boot gerado no Spring Initializr, estrutura de pacotes criada, `CLAUDE.md` com regras persistentes, documentação do back-end em `docs/` e **repositório do GitHub criado com o primeiro push feito (Passo 0.4)**.

## 🛠️ 2. Modificações Efetuadas nesta Sessão
- **Arquivos Modificados** (somente documentação, nenhum código alterado):
  - `docs/ESTADO_DESENVOLVIMENTO.md` -> Corrigido para refletir o estado real do versionamento (Passo 0.4 já concluído) e atualizar a seção 3 com as dependências reais do `pom.xml`.
  - `docs/planejamento-desenvolvimento-vigia-backend.md` -> Passo 0.4 marcado como concluído, com a URL real do repositório.
- **Contexto da sessão anterior (não documentado)**: o repositório do back-end foi criado em `https://github.com/Ericoos/Vigia-Backend.git` e o primeiro push já foi realizado (Passo 0.4). A config de banco ainda não existe.

## 📋 3. Estado das Dependências instaladas
- Declaradas no `pom.xml` e baixadas pelo Maven:
  - `spring-boot-starter-webmvc` (REST — no Spring Boot 4.1 o artifact do web antigo virou este)
  - `spring-boot-h2console` (console web do H2, artifact próprio no Boot 4.1)
  - `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`
  - `com.h2database:h2` (runtime), `org.projectlombok:lombok`
  - Starters de teste: `spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test`
- **Ainda NÃO adicionadas** (fases futuras): `mysql-connector-j` (quando o MySQL da equipe existir), `springdoc-openapi-starter-webmvc-ui` (Swagger), Spring Security.
- ⚠️ O H2 é **ponte temporária**, não o banco do projeto. O banco é MySQL, sob responsabilidade de outra pessoa da equipe, e ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
1. **Passo 0.5**: configurar a conexão em `src/main/resources/application.properties` — H2 em modo arquivo com `MODE=MySQL` na URL, driver, dialeto, `spring.jpa.hibernate.ddl-auto=update` e console web habilitado. **O desenvolvedor fará junto, aprendendo cada propriedade.**
2. **Passo 0.6**: rodar `./mvnw spring-boot:run`, confirmar `Tomcat started on port 8080` e validar o acesso a `localhost:8080/h2-console`.
3. **Fase 1 (Passos 1.1 a 1.4)**: escrever a primeira `@Entity`, confirmar que o Hibernate criou a tabela, e subir a fatia vertical `Repository` -> `Service` -> `Controller`, com um commit a cada camada.
4. **PENDÊNCIAS ABERTAS (não bloqueantes)**: (a) professora ainda não respondeu o que é esperado na entrega; (b) primeira entidade não definida — `Usuario` ou `Reporte`; (c) integração front-back nunca foi combinada; (d) contrato de `vigia-specs-endpoints.md` não validado com o front; (e) `avaliacaoGravidade` existe nos mocks e na Tela 3 mas não na classe `Reporte` do diagrama de classes; (f) fórmula de `calcularClassificacao()` do bairro não definida; (g) credenciais do MySQL desconhecidas; (h) não foi definido quem manda no schema — `ddl-auto` ou script SQL; (i) CORS não configurado.

## 🧠 5. Contexto Técnico para o Próximo Chat
- **Stack**: Java 21, Spring Boot 4.1.1, Maven (`./mvnw`), Spring Data JPA, Bean Validation, Lombok.
- **Versionamento**: repo `https://github.com/Ericoos/Vigia-Backend.git`, branch `main`. Histórico: `0abf4e0` Initial commit (LICENSE criado no GitHub) -> `1ddcac8` Estrutura Inicial do Backend -> `847da80` Merge da licensa do repositorio remoto (HEAD atual). Working tree limpo.
- **Banco**: o banco do projeto é **MySQL** e é para ele que o código é escrito. Ele é de outra pessoa da equipe e ainda não existe; o H2 é ponte temporária (modo arquivo, `MODE=MySQL`). O esquema é gerado pelo Hibernate a partir das `@Entity` via `ddl-auto=update`, então nenhum SQL é escrito à mão e a troca depois é indolor. **Nunca usar `nativeQuery`.**
- **Ambiente**: Arch Linux + Hyprland, IntelliJ IDEA, git pelo terminal.
- **Arquitetura**: camadas conforme `DIAGRAMA_DE_PACOTES.drawio` — `controller` (REST_CONTROLLERS) -> `service` (REGRAS_NEGOCIO) -> `repository` (REPOSITORIES) -> `model` (MODEL_BACKEND). Integrações externas em `integration`.
- **Ponto de entrada**: `VigiaApplication.java` fica na **raiz do pacote**, acima dos demais, porque `@SpringBootApplication` varre o próprio pacote e os subpacotes procurando beans.
- **Regras de tradução dos diagramas** (detalhadas em `CLAUDE.md` e na Seção 3 do planejamento): métodos do diagrama de classes vão para o `@Service`, nunca para a `@Entity`; chaves estrangeiras viram `@ManyToOne` com referência de objeto, não `int`; sem acentos em identificadores Java; herança com `@Inheritance(JOINED)`.
- **Nota do Spring Boot 4.1**: o Initializr gerou artifacts novos (`spring-boot-starter-webmvc` e `spring-boot-h2console` no lugar de `spring-boot-starter-web` + `h2-console` manual). Conferir o `pom.xml` se algum passo assumir o nome antigo.
- **OpenStreetMap é base do sistema**: geocodificação reversa via Nominatim converte latitude e longitude no `endereco` e no bairro do reporte. Fica no pacote `integration`. Exige cabeçalho `User-Agent` e tem limite de ~1 requisição por segundo. **A falha da integração não pode impedir o reporte de ser salvo.**
- **Divisão de trabalho**: back-end é meu; front-end (React Native + Expo, repositório `FabricioBucardi/Vigia`) e banco de dados MySQL são de outras pessoas da equipe.
- **Contrato da API**: derivado de `mockReports.json` e `mockBairros.json` do front. O JSON de reporte é **desnormalizado de propósito** (`autorNome`, `tipoCategoria`, `bairroNome` como texto), para o app desenhar um pin com uma única requisição.
- **Autenticação**: fora do escopo. `POST /api/auth/login` é simplificado, sem Spring Security, sem token e sem hash. Declarar como inseguro na monografia.
- **Documentação**: `docs/` contém `planejamento-desenvolvimento-vigia-backend.md`, `vigia-specs-endpoints.md`, este arquivo e `diagramas/`. `CLAUDE.md` permanece na raiz.
- **Mensagens de commit**: português, concisas.
- **Comandos**: `./mvnw spring-boot:run` (rodar), `./mvnw clean install` (build).