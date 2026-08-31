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

---

## 📦 Contexto do Projeto

- **App**: Vigia — plataforma colaborativa de segurança urbana de São Caetano do Sul (MVP de TCC).
- **Este repositório**: a API REST consumida pelo app mobile. Só devolve JSON.
- **Domínio**: usuários publicam relatos georreferenciados (reportes) classificados por categoria e bairro, avaliam reportes e perfis de outros usuários, e podem denunciar conteúdo. Administradores moderam, aplicam punições e registram ações. Usuários punidos podem entrar com recurso.
- **Coordenadas de referência**: São Caetano do Sul — `lat -23.6182, lon -46.5645`.

### Divisão de trabalho

| Frente | Responsável | Repositório |
|---|---|---|
| **Back-end (API)** | eu | este |
| Front-end (React Native + Expo) | outra pessoa | separado (`FabricioBucardi/Vigia`) |
| Banco de dados MySQL | outra pessoa | ainda não iniciado |

Nunca compartilhamos código entre os repositórios — só o contrato da API.

## 🛠️ Stack

- **Java 21**, **Spring Boot 4.1.1**, **Maven** (`./mvnw`)
- Spring Web, Spring Data JPA, Validation, Lombok
- **OpenStreetMap / Nominatim** para geocodificação reversa

### Banco de dados

> **O banco do projeto é MySQL.** Todo o código é escrito pensando nele.

O MySQL é responsabilidade de outra pessoa da equipe e ainda não existe. Enquanto isso, o **H2 é uma ponte temporária** para a aplicação conseguir subir — não é o banco do projeto.

Isso não afeta o código, porque o esquema é gerado pelo Hibernate a partir das `@Entity` (`spring.jpa.hibernate.ddl-auto=update`). Nenhum SQL é escrito à mão. Quando o MySQL existir, a troca é: substituir a dependência no `pom.xml` e ajustar a conexão no `application.properties`.

**Para que essa troca continue indolor:**

- **Nunca use `nativeQuery`.** Use métodos derivados do Spring Data ou JPQL.
- Use `MODE=MySQL` na URL do H2, para desenvolver contra o mesmo dialeto.
- Prefira o H2 em modo arquivo ao modo memória, para os dados não sumirem a cada reinício.

## 🗺️ OpenStreetMap

**É a base do sistema — sem mapa não há aplicativo.** No back-end, o papel dele é a geocodificação reversa: converter latitude e longitude no `endereco` legível e no bairro do reporte, via Nominatim.

- Fica no pacote `integration` (`INTEGRAÇÕES EXTERNAS` do diagrama de pacotes).
- O Nominatim **exige cabeçalho `User-Agent`** identificando a aplicação; sem ele, bloqueia.
- Limite de aproximadamente **1 requisição por segundo** na instância pública.
- **A falha da integração nunca pode impedir o reporte de ser salvo.** Em caso de erro, gravar `endereco` como nulo e registrar em log.
- O bairro vem como texto livre e precisa ser casado com a tabela `Bairro`. Bairro não encontrado cai num registro genérico, nunca quebra a requisição.

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
│   │   │   └── integration/            # OpenStreetMap, notícias, redes sociais
│   │   └── resources/
│   │       └── application.properties  # Configuração do banco
│   └── test/
├── CLAUDE.md                    # Este arquivo
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
| `INTEGRAÇÕES EXTERNAS` | `integration` |

## 📐 Regras de Tradução do Diagrama de Classes para o Código

**1. Os métodos das classes do diagrama NÃO vão para as `@Entity`.**
`criarReporte()`, `suspenderUsuario()`, `calcularClassificacao()` e os demais viram métodos no `@Service`. As `@Entity` ficam só com atributos, getters e setters. Isso é a camada `REGRAS_NEGOCIO` do diagrama de pacotes fazendo o trabalho dela.

**2. Chaves estrangeiras viram referências de objeto, não `int`.**
O diagrama mostra `idBairro: int`; no código isso vira um atributo do tipo `Bairro` anotado com `@ManyToOne`, e o Hibernate cria a coluna e a foreign key sozinho. Vale para `Reporte`, `Subcategoria`, `Denuncia`, `AcaoAdministrativa`, `Recurso` e `NoticiaReporte`.

**3. Sem acentos nem cedilha em identificadores Java.**
`endereço` no diagrama vira `endereco` no código. Vale para atributos, métodos, classes e pacotes.

**4. Herança (`Pessoa → Usuario/Admin`, `Noticia → NoticiaRedeSocial/NoticiaPortal`)** usa `@Inheritance(strategy = InheritanceType.JOINED)`, que gera uma tabela por classe — o mapeamento mais fiel ao diagrama.

## 🔐 Autenticação

Fora do escopo do protótipo: sem Spring Security, sem hash de senha, sem token. O login previsto é `POST /api/auth/login`, que valida e-mail e senha contra o banco e devolve o usuário. **Não é seguro para produção** e deve ser declarado como tal na monografia.

## 📂 Documentos de Referência (em `docs/`)

- `planejamento-desenvolvimento-vigia-backend.md` → stack, estrutura, tradução dos diagramas, escopo, ordem de implementação
- `vigia-specs-endpoints.md` → especificação SDD de cada endpoint, com prompts prontos
- `ESTADO_DESENVOLVIMENTO.md` → onde paramos na última sessão
- `diagramas/` → os `.drawio` do TCC

**Ao implementar qualquer entidade ou endpoint, consulte o diagrama e a spec correspondentes antes de sugerir código.** Se o código precisar divergir do diagrama, avise e explique o porquê.

## 📋 Protocolo de Sincronização de Estado

- Gere/atualize `docs/ESTADO_DESENVOLVIMENTO.md` **a cada interação que adicione, altere ou remova algo do projeto** — não apenas no fim do dia.
- Siga o template da Seção 6 do planejamento: ponto de controle, modificações, dependências, próximos passos, contexto técnico.
- Reforce neste `CLAUDE.md` qualquer nova regra ou convenção persistente criada durante a sessão.
- Ao encerrar a sessão do dia, garanta que `docs/ESTADO_DESENVOLVIMENTO.md` esteja atualizado com o último estado.
