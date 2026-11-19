# Instruções Persistentes para Assistentes de IA - Projeto Vigia (Back-End)

Este arquivo é lido em toda sessão de desenvolvimento. Leia e siga rigorosamente as regras abaixo em **toda** conversa.

---

## 🚫 REGRA DE OURO: APROVAÇÃO OBRIGATÓRIA PARA COMMITS

> **QUALQUER commit, push, amend, rebase ou criação de Pull Request — SEM EXCEÇÃO — requer a aprovação explícita do usuário ANTES de ser executado.**

- Nunca execute `git commit`, `git push`, `git amend`, `git rebase` ou crie PRs por conta própria.
- Sempre aguarde o usuário dizer explicitamente algo como "pode commitar", "pode dar push", "aprovado".
- Ao terminar uma tarefa que alterou arquivos, **informe ao usuário** que está pronto para commit e aguarde a autorização.
- Antes de commitar: revise `git status`, `git diff` e o histórico recente (`git log --oneline -10`).
- Mensagens de commit devem ser em português e concisas, refletindo o estilo do histórico existente.

### 📦 Workflow de Commits (junto com a regra acima)

- **Commits de código são feitos manualmente pelo usuário**, com o assistente guiando: o assistente prepara `git status`/`git diff` para revisão, indica quais arquivos dar `git add` e sugere a mensagem; o usuário executa os comandos no terminal.
- **Docs e código vão juntos no mesmo commit do bloco**: a cada alteração de código, o assistente atualiza os docs de estado na mesma entrega, e o commit inclui ambos.
- Na mensagem de commit **não é preciso detalhar** o que mudou nos docs — basta indicar que o doc foi atualizado junto (ex.: `docs: atualiza estado do desenvolvimento`).

---

## 📦 Contexto do Projeto

- **App**: Vigia — plataforma colaborativa de segurança urbana de São Caetano do Sul (MVP de TCC).
- **Este repositório**: a API REST consumida pelo app mobile. Só devolve JSON.
- **Foco atual (desde 15/11/2026)**: fechar o **MVP do lado do usuário** até **30/11/2026** — reporte, autenticação (JWT), perfil, bairro, editar/excluir reporte, denúncia de reporte, reputação automática, confirmação de e-mail e recuperação de senha. A lista completa de tarefas, com responsáveis, prazos e passos, está em **`docs/tarefas-mvp-restante.md`** — é o ponto de partida de qualquer sessão.
- **Requisitos**: a fonte é o relatório (`docs/relatorio-vigia.txt`, §6.1.1, p. 54–62). **Antes de propor uma funcionalidade, leia o requisito correspondente** — não adivinhe regra de negócio.
- **Coordenadas de referência**: São Caetano do Sul — `lat -23.6182, lon -46.5645`.

### Divisão de trabalho

| Frente | Responsável | Repositório |
|---|---|---|
| **Back-end (API)** | Erick (dono desta conta) e Murilo | este |
| Front-end (React Native + Expo) | Fabrício | separado (`FabricioBucardi/Vigia`) |
| Banco de dados MySQL | Fernando | ainda não iniciado |

Nunca compartilhamos código entre os repositórios — só o contrato da API (`docs/vigia-specs-endpoints.md`). Mudou o contrato → avisar o Fabrício. Tarefas do quadro "Desenvolvimento Backend" no Trello.

### Fora do MVP (decisões de escopo, 15/11/2026)

- Denúncia de **perfil** (R68), painel e ações de admin (R44, R61, R67, R69), punições e recursos (R70–R72), notícias (R31–R32), exclusão permanente de conta (R63), busca textual (R42), agrupamento no mapa (R18), CAPTCHA, termos/LGPD, testes automáticos.
- **Geocodificação/OpenStreetMap**: feita no **front**; o back só recebe o texto do bairro/endereço.
- **Não existe avaliação de 1 a 5 dada por usuários.** A "avaliação geral" do perfil é a **reputação automática do R15** (campo `Usuario.avaliacao`). `avaliacaoGravidade` foi **removida** do `Reporte` e não deve voltar.
- Rate limiting (T40 no Trello) não é requisito do relatório: só se sobrar tempo.

### 🧑‍🏫 Como trabalhar com o Erick

- **O assistente guia; o Erick escreve as classes.** Não editar código Java por conta própria — só ler, revisar, compilar e testar. Escrever código apenas quando ele pedir explicitamente ("arrume o código", "pode corrigir").
- **Explicações em passos curtos e numerados**: arquivo → o que digitar (código) → "O que faz" em poucas linhas. Separar claramente o passo da explicação; evitar texto longo e enrolado.
- **Uma tarefa por vez**, dizendo sempre onde estamos ("tarefa X feita, agora a Y") — não perder o fio da meada.
- **Subir a API para testes no terminal do app** (para o Erick poder parar com Ctrl+C). Rodar `mvnw spring-boot:run` em segundo plano deixa o processo Java preso na porta 8080 e segurando o H2; se acontecer, pedir para ele fechar o PID (`Stop-Process -Id <pid>`).

## 🛠️ Stack

- **Java 21**, **Spring Boot 4.1.1**, **Maven** (`./mvnw`)
- Spring Web, Spring Data JPA, Validation, Lombok
- Spring Security + OAuth2 Resource Server (JWT HS256) + BCrypt. A chave do JWT fica em `src/main/resources/application-local.properties`, **fora do Git** (modelo em `application-local.properties.example`); sem ela a API não sobe.
- **Erros**: exceções de negócio herdam de `exception/ApiException` (carrega `HttpStatus` + texto de `erro`) e são tratadas por um único handler no `GlobalExceptionHandler`. Erro novo = nova filha de `ApiException`.
- **Usuário logado**: `@AuthenticationPrincipal Jwt jwt` → `Long.valueOf(jwt.getSubject())`. O id vem sempre do token, nunca da URL ou do corpo.
- **Acesso**: no `SecurityConfig`, só `/api/auth/**`, `/h2-console/**`, `/error` e os `GET` de leitura são livres; todo o resto exige token.
- **Bairros**: caminho canônico no plural, `/api/bairros` e `/api/bairros/{tipoBairro}` (definido por Murilo em 19/11/2026). O contrato deve ser comunicado ao Fabrício; não manter um segundo endpoint no singular.

### Banco de dados

> **O banco do projeto é MySQL.** Todo o código é escrito pensando nele.

O MySQL é responsabilidade de outra pessoa da equipe e ainda não existe. Enquanto isso, o **H2 é uma ponte temporária** para a aplicação conseguir subir — não é o banco do projeto.

Isso não afeta o código, porque o esquema é gerado pelo Hibernate a partir das `@Entity` (`spring.jpa.hibernate.ddl-auto=update`). Nenhum SQL é escrito à mão. Quando o MySQL existir, a troca é: substituir a dependência no `pom.xml` e ajustar a conexão no `application.properties`.

**Para que essa troca continue indolor:**

- **Nunca use `nativeQuery`.** Use métodos derivados do Spring Data ou JPQL.
- Use `MODE=MySQL` na URL do H2, para desenvolver contra o mesmo dialeto.
- Prefira o H2 em modo arquivo ao modo memória, para os dados não sumirem a cada reinício.

## 📁 Estrutura de Pastas

```
vigia-api/
├── docs/                        # Documentação e diagramas UML
├── src/
│   ├── main/
│   │   ├── java/com/uscs/vigia/
│   │   │   ├── VigiaApplication.java   # Ponto de entrada — raiz do pacote
│   │   │   ├── controller/             # Recebe HTTP, devolve JSON
│   │   │   ├── service/                # Regras de negócio
│   │   │   ├── repository/             # Interfaces do Spring Data
│   │   │   ├── model/                  # As @Entity
│   │   │   ├── dto/                    # Entrada e saída da API (usar record)
│   │   │   ├── exception/              # Tratamento de erros
│   │   │   └── config/                 # Configurações sob demanda (ex.: seed, DataSeeder)
│   │   └── resources/
│   │       └── application.properties  # Configuração do banco
│   └── test/
├── AGENTS.md                   # Este arquivo
├── README.md
└── pom.xml
```

**`VigiaApplication.java` fica na raiz do pacote, acima de todos os outros.** A anotação `@SpringBootApplication` varre o próprio pacote e os subpacotes procurando beans — se essa classe for movida para dentro de um subpacote, o Spring deixa de encontrar as demais.

## 🔄 Correspondência com o Diagrama de Pacotes

| Diagrama de pacotes | Pacote no código |
|---|---|
| `REST_CONTROLLERS` | `controller` |
| `REGRAS_NEGOCIO` | `service` |
| `REPOSITORIES` | `repository` |
| `MODEL_BACKEND` | `model` |
| `DTO` / `EXCEPTION` / `CONFIG` | `dto` / `exception` / `config` (adicionar ao diagrama — já existem no código) |

## 📐 Regras de Tradução do Diagrama de Classes para o Código

**1. Os métodos das classes do diagrama NÃO vão para as `@Entity`.**
`criarReporte()` e os demais viram métodos no `@Service`. As `@Entity` ficam só com atributos, getters e setters. Isso é a camada `REGRAS_NEGOCIO` do diagrama de pacotes fazendo o trabalho dela.

**2. Chaves estrangeiras viram referências de objeto, não `int`.**
O diagrama mostra `idSubcategoria: int`; no código isso vira um atributo do tipo `Subcategoria` anotado com `@ManyToOne`, e o Hibernate cria a coluna e a foreign key sozinho. Vale para `Reporte` e as entidades que ele referencia.

**3. Sem acentos nem cedilha em identificadores Java.**
`endereço` no diagrama vira `endereco` no código. Vale para atributos, métodos, classes e pacotes.

**4. Herança (`Pessoa → Usuario/Admin`) vira `@MappedSuperclass`.**
A `Pessoa` do diagrama é o molde `UsuarioBase` no código, sem tabela própria. Já está implementado; o módulo de usuário está **fora do foco atual**, mas o molde permanece no código.

**5. A entidade não aparece no contrato da API.**
Entrada e saída usam **DTOs (records)** no pacote `dto`; a conversão DTO ↔ entidade acontece no `@Service`.

## 📂 Documentos de Referência (em `docs/`)

- `planejamento-desenvolvimento-vigia-backend.md` → escopo, fases atuais e futuras, modelo de dados do reporte, protocolo de estado
- `vigia-specs-endpoints.md` → contratos dos endpoints de reporte (padrão de DTOs + prompts prontos)
- `mudancas-docs-relatorio.md` → mudanças na documentação do relatório e no que vamos implementar
- `guia-desenvolvimento.md` → onboarding de novos integrantes (ambiente, como rodar, regras)
- `ESTADO_DESENVOLVIMENTO.md` → onde paramos na última sessão
- `diagramas/` → os `.drawio` do TCC

**Ao implementar qualquer entidade ou endpoint, consulte o diagrama e a spec correspondentes antes de sugerir código.** Se o código precisar divergir do diagrama, avise e explique o porquê.

## 📋 Protocolo de Sincronização de Estado

- Gere/atualize `docs/ESTADO_DESENVOLVIMENTO.md` **a cada interação que adicione, altere ou remova algo do projeto** — não apenas no fim do dia.
- Siga o template da Seção 5 do planejamento: ponto de controle, modificações, dependências, próximos passos, contexto técnico.
- Reforce neste `AGENTS.md` qualquer nova regra ou convenção persistente criada durante a sessão.
- Ao encerrar a sessão do dia, garanta que `docs/ESTADO_DESENVOLVIMENTO.md` esteja atualizado com o último estado.
