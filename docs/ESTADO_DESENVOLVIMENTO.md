# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: Fase 0 — Infraestrutura e Versionamento — Passo 0.4 (em andamento)
- **Última classe/endpoint finalizado**: Nenhuma classe de domínio foi escrita ainda. Concluída a infraestrutura inicial: projeto Spring Boot gerado no Spring Initializr, aberto no IntelliJ, estrutura de pacotes criada, `CLAUDE.md` com regras persistentes e a documentação do back-end em `docs/`. O repositório do back-end ainda não existe no GitHub e nenhum commit foi feito.

## 🛠️ 2. Modificações Efetuadas nesta Sessão
- **Projeto Gerado**:
  - Spring Initializr -> Maven, Java 21, Spring Boot 4.1.1, packaging Jar, `application.properties`.
- **Pacotes Criados** (em `src/main/java/com/uscs/vigia/`):
  - `controller`, `service`, `repository`, `model`, `dto`, `exception`, `integration` -> estrutura da arquitetura em camadas do diagrama de pacotes.
- **Arquivos Criados**:
  - `CLAUDE.md` -> Regras persistentes para IA: aprovação obrigatória de commits, contexto do projeto, divisão de trabalho, stack, estrutura, regras de tradução dos diagramas e protocolo de estado. Fica na raiz, pois é lido automaticamente.
  - `docs/planejamento-desenvolvimento-vigia-backend.md` -> Stack, estrutura, tradução dos diagramas, escopo, ordem de implementação e protocolo.
  - `docs/vigia-specs-endpoints.md` -> Especificação SDD dos 6 endpoints, espelhando `vigia-specs-telas.md`.
  - `docs/ESTADO_DESENVOLVIMENTO.md` -> Este arquivo de transição de contexto.

## 📋 3. Estado das Dependências instaladas
- Declaradas no `pom.xml` pelo Initializr e baixadas pelo Maven na importação do IntelliJ: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `com.h2database:h2` (runtime), `org.projectlombok:lombok`.
- **Ainda NÃO adicionadas** (fases futuras): `mysql-connector-j` (quando o MySQL da equipe existir), `springdoc-openapi-starter-webmvc-ui` (Swagger), Spring Security.
- ⚠️ O H2 é **ponte temporária**, não o banco do projeto. O banco é MySQL, sob responsabilidade de outra pessoa da equipe, e ainda não existe.

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
1. **Passo 0.4**: criar o repositório do back-end no GitHub (**separado** do repositório do front) e fazer o primeiro push com a estrutura, o `CLAUDE.md` e a pasta `docs/`. Copiar os `.drawio` para `docs/diagramas/` antes do commit.
2. **Passo 0.5**: configurar a conexão em `src/main/resources/application.properties` — H2 em modo arquivo com `MODE=MySQL` na URL, driver, dialeto, `spring.jpa.hibernate.ddl-auto=update` e console web habilitado.
3. **Passo 0.6**: rodar `./mvnw spring-boot:run`, confirmar `Tomcat started on port 8080` e validar o acesso a `localhost:8080/h2-console`.
4. **Fase 1 (Passos 1.1 a 1.4)**: escrever a primeira `@Entity`, confirmar que o Hibernate criou a tabela, e subir a fatia vertical `Repository` -> `Service` -> `Controller`, com um commit a cada camada.
5. **PENDÊNCIAS ABERTAS (não bloqueantes)**: (a) professora ainda não respondeu o que é esperado na entrega; (b) primeira entidade não definida — `Usuario` ou `Reporte`; (c) integração front-back nunca foi combinada; (d) contrato de `vigia-specs-endpoints.md` não validado com o front; (e) `avaliacaoGravidade` existe nos mocks e na Tela 3 mas não na classe `Reporte` do diagrama de classes; (f) fórmula de `calcularClassificacao()` do bairro não definida; (g) credenciais do MySQL desconhecidas; (h) não foi definido quem manda no schema — `ddl-auto` ou script SQL; (i) CORS não configurado.

## 🧠 5. Contexto Técnico para o Próximo Chat
- **Stack**: Java 21, Spring Boot 4.1.1, Maven (`./mvnw`), Spring Data JPA, Bean Validation, Lombok.
- **Banco**: o banco do projeto é **MySQL** e é para ele que o código é escrito. Ele é de outra pessoa da equipe e ainda não existe; o H2 é ponte temporária (modo arquivo, `MODE=MySQL`). O esquema é gerado pelo Hibernate a partir das `@Entity` via `ddl-auto=update`, então nenhum SQL é escrito à mão e a troca depois é indolor. **Nunca usar `nativeQuery`.**
- **Ambiente**: Arch Linux + Hyprland, IntelliJ IDEA, git pelo terminal.
- **Arquitetura**: camadas conforme `DIAGRAMA_DE_PACOTES.drawio` — `controller` (REST_CONTROLLERS) -> `service` (REGRAS_NEGOCIO) -> `repository` (REPOSITORIES) -> `model` (MODEL_BACKEND). Integrações externas em `integration`.
- **Ponto de entrada**: `VigiaApplication.java` fica na **raiz do pacote**, acima dos demais, porque `@SpringBootApplication` varre o próprio pacote e os subpacotes procurando beans.
- **Regras de tradução dos diagramas** (detalhadas em `CLAUDE.md` e na Seção 3 do planejamento): métodos do diagrama de classes vão para o `@Service`, nunca para a `@Entity`; chaves estrangeiras viram `@ManyToOne` com referência de objeto, não `int`; sem acentos em identificadores Java; herança com `@Inheritance(JOINED)`.
- **OpenStreetMap é base do sistema**: geocodificação reversa via Nominatim converte latitude e longitude no `endereco` e no bairro do reporte. Fica no pacote `integration`. Exige cabeçalho `User-Agent` e tem limite de ~1 requisição por segundo. **A falha da integração não pode impedir o reporte de ser salvo.**
- **Divisão de trabalho**: back-end é meu; front-end (React Native + Expo, repositório `FabricioBucardi/Vigia`) e banco de dados MySQL são de outras pessoas da equipe.
- **Contrato da API**: derivado de `mockReports.json` e `mockBairros.json` do front. O JSON de reporte é **desnormalizado de propósito** (`autorNome`, `tipoCategoria`, `bairroNome` como texto), para o app desenhar um pin com uma única requisição.
- **Autenticação**: fora do escopo. `POST /api/auth/login` é simplificado, sem Spring Security, sem token e sem hash. Declarar como inseguro na monografia.
- **Documentação**: `docs/` contém `planejamento-desenvolvimento-vigia-backend.md`, `vigia-specs-endpoints.md`, este arquivo e `diagramas/`. `CLAUDE.md` permanece na raiz.
- **Mensagens de commit**: português, concisas.
- **Comandos**: `./mvnw spring-boot:run` (rodar), `./mvnw clean install` (build).
