# Plano de Desenvolvimento do Back-End (MVP) - Aplicativo Vigia

Esta documentação foi estruturada para orientar assistentes de desenvolvimento de código na criação rápida e eficiente do back-end do aplicativo **Vigia** (plataforma colaborativa de segurança urbana focada em São Caetano do Sul).

O back-end é uma **API REST em Spring Boot** que será consumida pelo app React Native. O app hoje trabalha com **dados locais simulados (mockados)** estruturados de forma idêntica ao banco de dados MySQL oficial — esses mocks são a base do contrato desta API.

> 🚨 **DIRETRIZ DE PERSISTÊNCIA DE CONTEXTO**: Como os assistentes de IA não retêm histórico entre sessões de dias diferentes, siga rigorosamente o **Protocolo de Sincronização Diária (Seção 6)** ao final de cada conversa para gerar um arquivo de transição de contexto.

---

> **Stack, estrutura de pastas e regras de tradução dos diagramas ficam no `AGENTS.md` da raiz** (documento de referência das regras persistentes). Este documento trata de *plano*: modelo de dados, escopo dos endpoints, fases de desenvolvimento e protocolo de estado.
>
> Estrutura da pasta `docs/` atual: `planejamento-desenvolvimento-vigia-backend.md` (este), `vigia-specs-endpoints.md`, `guia-desenvolvimento.md`, `ESTADO_DESENVOLVIMENTO.md` e `diagramas/` (os `.drawio` do TCC).

---

## 3. Modelo de Dados (Entidades a mapear)

As entidades vêm do `DIAGRAMA_DE_CLASSES.drawio` e devem produzir tabelas compatíveis com o schema espelhado em `mockReports.json` e `mockBairros.json`. **Estado atual**: as entidades do MVP existem no pacote `model` — só atributos e construtor padrão (`@NoArgsConstructor`), **sem métodos** (as regras de negócio ficam nos `@Service`, conforme AGENTS.md). O usuário foi modelado com o molde `UsuarioBase` (`@MappedSuperclass`, sem tabela `pessoa`), e `Usuario` (com telefone, dataCadastro) já existe — o `Reporte` tem com o que se relacionar. A tabela abaixo é o planejamento do mapeamento.

### Entidades do MVP

| Entidade | Atributos | Relacionamentos |
|---|---|---|
| **UsuarioBase** (molde, `@MappedSuperclass`) | id, nome, email, senha, dataNascimento, status | — (não vira tabela) |
| **Usuario** | herda os campos de `UsuarioBase` + telefone, dataCadastro | — |
| **Reporte** | id, titulo, descricao, latitude, longitude, endereco, avaliacaoGravidade, dataReporte, status | pertence a um Usuario, uma Subcategoria e um Bairro |
| **Bairro** | id, nome, classificacaoSeguranca | possui vários Reportes |
| **Categoria** | id, tipoCategoria ("Seguranca" ou "Inseguranca") | possui várias Subcategorias |
| **Subcategoria** | id, tipoSubcategoria | pertence a uma Categoria |

### Enums

*   `TipoUsuario`: USUARIO_ATIVO, USUARIO_SUSPENSO, USUARIO_BANIDO, USUARIO_DELETADO, USUARIO_INATIVO (equivalente ao `StatusUsuario` do diagrama).
*   `StatusReporte`: RASCUNHO, PUBLICADO, EXCLUIDO.

> ⚠️ **Decisão desta sessão**: `avaliacao` e `avatar` não pertencem ao `Usuario` — vão para `PerfilPublico` (relação 1-1 com `Usuario`), que é entidade das fases seguintes. Atualizar o diagrama de classes.

### Como o mock vira modelo

| Campo em `mockReports.json` | No back-end |
|---|---|
| `idReporte` | identificador de `Reporte` |
| `idUsuario`, `autorNome` | relacionamento com `Usuario`, achatado no DTO de resposta |
| `tipoCategoria` | vem da `Categoria` da subcategoria, achatado no DTO |
| `tipoSubcategoria` | vem da `Subcategoria`, achatado no DTO |
| `bairroNome` | vem do `Bairro`, achatado no DTO |
| `endereco` | atributo de `Reporte`, resolvido pelo Nominatim |
| demais campos | atributos diretos de `Reporte` |

> ⚠️ **Divergência a resolver**: `avaliacaoGravidade` existe no mock e na Tela 3, mas **não consta** na classe `Reporte` do diagrama de classes. Corrigir o diagrama.

### Entidades das fases seguintes

Ainda não modeladas, presentes no diagrama de classes: `Admin`, `PerfilPublico`, `Historico`, `Denuncia`, `AcaoAdministrativa`, `Recurso`, `Noticia`, `NoticiaRedeSocial`, `NoticiaPortal`, `NoticiaReporte`.

---

## 4. Escopo do MVP e Especificação dos Endpoints

Cada endpoint atende a uma tela de `vigia-specs-telas.md`. A especificação detalhada, com contratos JSON e prompts prontos, está em `docs/vigia-specs-endpoints.md`.

### Endpoint 1: Cadastro e Autenticação (`/api/usuarios`, `/api/auth`)
*   **Atende**: Tela 1 (Login e Cadastro) — R01, R04.
*   `POST /api/usuarios` recebe nome, email, senha, dataNascimento e telefone. E-mail duplicado devolve `400`.
*   `POST /api/auth/login` recebe email e senha, devolve os dados do usuário.
*   *Comportamento*: login simplificado, **sem Spring Security, sem hash e sem token**. Compatível com o `AuthContext` mockado do app. Declarar como inseguro na monografia.

### Endpoint 2: Listagem de Reportes (`/api/reportes`)
*   **Atende**: Tela 2 (Mapa Interativo) — R16, R17, R60.
*   `GET /api/reportes` com filtros opcionais `?bairro={id}` e `?categoria=Seguranca|Inseguranca`.
*   Devolve o array no **formato exato de `mockReports.json`**, com os relacionamentos achatados em texto (`autorNome`, `tipoCategoria`, `bairroNome`), para o app desenhar um pin com uma única requisição.
*   Reportes com status `RASCUNHO` ou `EXCLUIDO` não aparecem.

### Endpoint 3: Criação de Reporte (`/api/reportes`, `/api/categorias`)
*   **Atende**: Tela 3 (Criar Reporte) — R19, R20, R22.
*   `POST /api/reportes` recebe idUsuario, titulo, descricao, latitude, longitude, idSubcategoria e avaliacaoGravidade.
*   `GET /api/categorias` devolve as categorias com suas subcategorias aninhadas, alimentando os seletores da tela.
*   *Regras*: título e descrição obrigatórios; gravidade entre 1 e 5; a **categoria é derivada da subcategoria** no Service, impedindo combinações inválidas; `endereco` e `bairro` são resolvidos no servidor pelo Nominatim.

### Endpoint 4: Detalhe do Bairro (`/api/bairros`)
*   **Atende**: Tela 4 (Detalhe do Bairro) — R64, R17.
*   `GET /api/bairros` devolve o formato de `mockBairros.json`.
*   `GET /api/bairros/{id}` acrescenta contadores de segurança e insegurança e a lista de reportes do bairro.
*   *Regra*: `calcularClassificacao()` é regra de negócio e mora no **Service**, nunca na `@Entity`. **A fórmula ainda precisa ser definida pela equipe.**

### Endpoint 5: Perfil e Histórico (`/api/usuarios/{id}`)
*   **Atende**: Tela 5 (Perfil do Cidadão) — R11, R13.
*   `GET /api/usuarios/{id}` devolve nome, email, avatar, avaliacao, dataCadastro e totalReportes.
*   `GET /api/usuarios/{id}/reportes` devolve o histórico do próprio usuário, **incluindo** rascunhos e reportes sob moderação.
*   *Regra*: a senha nunca aparece em nenhuma resposta.

### Endpoint 6: Geocodificação Reversa (`integration/NominatimClient`)
*   **Atende**: base do sistema — caso de uso "API Integração (Open Source Map)".
*   Consome `https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}`.
*   *Regras*: exige cabeçalho `User-Agent`; limite de ~1 requisição por segundo; **a falha da integração nunca pode impedir o reporte de ser salvo** — grava `endereco` nulo e registra em log.

### Fora do escopo do MVP
Spring Security e tokens JWT; integração com Instagram/Meta API e portais de notícia; sistema de punição automática e recursos; painel administrativo.

---

## 5. Passos Recomendados para Desenvolvimento

### **Fase 0: Infraestrutura e Versionamento**
*   **Passo 0.1**: Gerar o projeto em [start.spring.io](https://start.spring.io) — Maven, Java 21, Spring Boot 4.1.1, Jar, com Spring Web, Spring Data JPA, H2 Database, Lombok e Validation. ✅
*   **Passo 0.2**: Criar os pacotes `controller`, `service`, `repository`, `model`, `dto`, `exception` e `integration`. ✅
*   **Passo 0.3**: Criar `AGENTS.md` na raiz e a pasta `docs/`. ✅
*   **Passo 0.4**: Criar o repositório no GitHub (**separado** do front) e fazer o primeiro push. ✅
    - Repositório: `https://github.com/Ericoos/Vigia-Backend.git` (branch `main`).
    - O primeiro push foi feito localmente e depois houve merge da `LICENSE` criada no GitHub (`0abf4e0` Initial commit -> `1ddcac8` Estrutura Inicial do Backend -> `847da80` Merge da licensa do repositorio remoto).
*   **Passo 0.5**: Configurar a conexão em `src/main/resources/application.properties`:
    ```properties
    spring.datasource.url=jdbc:h2:file:./data/vigia;MODE=MySQL
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=
    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
    spring.jpa.hibernate.ddl-auto=update
    spring.h2.console.enabled=true
    ```
*   **Passo 0.6**: Rodar e validar:
    ```bash
    ./mvnw spring-boot:run
    ```
    Confirmar `Tomcat started on port 8080` e o acesso a `localhost:8080/h2-console`.

### **Fase 1: Fatia Vertical (uma entidade de ponta a ponta)**
*   **Passo 1.1**: Criar a entidade `Usuario` que falta para o projeto compilar (as demais do MVP já existem como esqueleto, Seção 3), rodar e confirmar as tabelas no `h2-console`.
*   **Passo 1.2**: Criar o `Repository` — interface de uma linha estendendo `JpaRepository`. Rodar; nada muda visualmente, mas tem que continuar subindo.
*   **Passo 1.3**: Criar o `Service` — classe `@Service` recebendo o Repository no construtor, com listar e salvar.
*   **Passo 1.4**: Criar o `Controller` — `@RestController` com `GET` e `POST`. Testar o `GET` no navegador e o `POST` no Postman ou Insomnia.

> **Commit a cada camada que funcionar.** Três commits pequenos valem mais que um gigante.

### **Fase 2: Consolidação da API**
*   **Passo 2.1**: Criar os DTOs como `record` no pacote `dto` e aplicar Bean Validation nas entradas.
*   **Passo 2.2**: Criar o `@RestControllerAdvice` no pacote `exception`, produzindo o formato padrão de erro.
*   **Passo 2.3**: Adicionar o Swagger ao `pom.xml` e validar em `localhost:8080/swagger-ui.html`:
    ```xml
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
    ```
*   **Passo 2.4**: Configurar CORS liberando a origem do app.
*   **Passo 2.5**: Completar o CRUD com `PUT` e `DELETE`.

### **Fase 3: Domínio Completo e Integrações**
*   **Passo 3.1**: Implementar as demais entidades da Seção 3 e seus endpoints (Seção 4).
*   **Passo 3.2**: Implementar a geocodificação reversa com o Nominatim no pacote `integration`.
*   **Passo 3.3**: Implementar `POST /api/auth/login`.
*   **Passo 3.4 (depende de terceiro)**: Migrar do H2 para o MySQL quando o banco existir — trocar a dependência no `pom.xml` e ajustar o `application.properties`:
    ```xml
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
    ```

### **Fase 4: Entrega**
*   **Passo 4.1**: Escrever o `README.md` com descrição, stack, como rodar e a lista de endpoints.
*   **Passo 4.2**: Revisar o histórico de commits.
*   **Passo 4.3**: Gravar o vídeo de demonstração da API em funcionamento (Swagger ou Postman).

### Pendências abertas

| # | Pendência | Responsável |
|---|---|---|
| 1 | Confirmar com a professora o que é esperado na entrega | back |
| 2 | Definir a primeira entidade da Fase 1 | back |
| 3 | Combinar se e quando a integração front-back acontece (o plano do front marca como "Passo 3.3 — Opcional") | back + front |
| 4 | Validar `vigia-specs-endpoints.md` com o front | back + front |
| 5 | `avaliacaoGravidade` existe no mock e na Tela 3, mas não na classe `Reporte` do diagrama | back + front |
| 6 | Definir a fórmula de `calcularClassificacao()` do bairro | equipe |
| 7 | Obter host, porta, nome do banco, usuário e senha do MySQL | back + banco |
| 8 | Definir quem manda no schema: as `@Entity` via `ddl-auto` ou um script SQL. **Duas fontes de verdade vão conflitar** | back + banco |

---

## 6. Protocolo de Registro de Estado (Sincronização Diária)

Como o desenvolvedor utiliza assistentes de IA sem memória persistente de longo prazo, a IA **deve atuar ativamente na preservação do contexto do projeto** seguindo a instrução abaixo ao final de cada conversa.

### 📝 Instrução para a IA ao Final de Toda Sessão:
> *"Sempre que o usuário informar que está encerrando a sessão de desenvolvimento por hoje, você deve gerar um arquivo em formato Markdown chamado `ESTADO_DESENVOLVIMENTO.md` em `docs/` contendo as seções descritas no template abaixo. O desenvolvedor usará este arquivo para restaurar seu contexto no início da próxima conversa."*

### 📁 Template do arquivo `ESTADO_DESENVOLVIMENTO.md`:
```markdown
# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: [Indicar ex: Fase 1, Passo 1.3]
- **Última classe/endpoint finalizado**: [Descrever o que foi concluído na sessão]

## 🛠️ 2. Modificações Efetuadas nesta Sessão
- **Arquivos Criados**:
  - `caminho/do/Arquivo.java` -> [Função deste arquivo]
- **Arquivos Modificados**:
  - `caminho/do/Arquivo.java` -> [O que foi mudado e por quê]

## 📋 3. Estado das Dependências instaladas
- [Listar as dependências novas adicionadas ao pom.xml, se houver]

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
1. [Próxima tarefa 1 baseada nos passos recomendados]
2. [Próxima tarefa 2]

## 🧠 5. Contexto Técnico para o Próximo Chat
*(Resumo compacto da arquitetura atual, entidades mapeadas, endpoints expostos e decisões tomadas, para que a IA da próxima sessão compreenda a lógica sem precisar ler todo o projeto).*
```

---

*Com este documento base e o protocolo de sincronização diária ativo, o desenvolvedor do back-end do Vigia garante consistência com o front-end, velocidade no desenvolvimento do MVP e uma documentação limpa do processo!*
