# Especificação Técnica dos Endpoints - Módulo Reporte

Foco: o app cria um reporte, a API recebe, **valida** e **armazena** no banco, e devolve a listagem para os pins do mapa.

> **Status:** base naive v1 concluída; evolução do MVP em andamento. Novas Tarefas 3/4 verificadas por HTTP; Tarefa 7 implementada, compilada e retestada no H2 local recriado, incluindo lista/detalhe públicos em `/api/bairros`, definido por Murilo como caminho canônico. A numeração atual está em `docs/tarefas-mvp-restante.md`.

---

## 1. Contexto Global da API

```
Base URL (dev):  http://localhost:8080/api
Content-Type:    application/json
Datas:           ISO-8601 com offset -03:00 (America/Sao_Paulo)
Identificadores: sem acentos nem cedilha nos nomes dos campos
```

| Código | Quando |
|---|---|
| `200 OK` | Consulta bem-sucedida |
| `201 Created` | Reporte ou usuário criado |
| `400 Bad Request` | Validação falhou ou regra de negócio (sub desalinhada da categoria) |
| `401 Unauthorized` | Login com e-mail ou senha inválidos, ou rota protegida sem token / com token inválido |
| `403 Forbidden` | Login de conta que não está `USUARIO_ATIVO` |
| `404 Not Found` | Recurso inexistente |
| `409 Conflict` | Cadastro com e-mail já existente |

**Formato padrão de erro** (produzido pelo `@RestControllerAdvice` do pacote `exception`):

```json
{
  "timestamp": "2026-09-12T15:00:00-03:00",
  "status": 400,
  "erro": "Erro de validação",
  "mensagem": "titulo: titulo é obrigatório",
  "caminho": "/api/reportes"
}
```

- `400` de **validação** → `"erro": "Erro de validação"`.
- `400` de **regra de negócio** (sub não pertence à categoria) → `"erro": "Regra de negócio"`.
- `401` → `"erro": "Não autorizado"`.
- `403` → `"erro": "Conta inativa"`.
- `404` → `"erro": "Recurso não encontrado"`.
- `409` → `"erro": "Conflito"`.

> Os erros de negócio da própria API (401, 403, 404, 409) herdam de `exception/ApiException`, que carrega o status e o texto de `erro`; um único handler (`handleApi`) monta a resposta. Validação, JSON inválido e `IllegalArgumentException` continuam com handlers próprios.

**Regras de acesso (R49, R50 — `config/SecurityConfig`, desde 15/11/2026):**
- Livres (sem token): `/api/auth/**`, `/h2-console/**`, `/error` e **somente `GET`** em `/api/reportes/**`, `/api/categorias/**`, `/api/subcategorias/**` e `/api/bairros/**`.
- Todo o resto exige `Authorization: Bearer <token>` (o token vem do `POST /api/auth/login`). Rotas novas já nascem protegidas.
- Sem token ou com token inválido numa rota protegida → `401` devolvido pelo Spring Security, **sem corpo JSON** (só o status e o cabeçalho `WWW-Authenticate`). O app deve mandar o usuário para o login.

---

## 2. ENDPOINT: POST /api/reportes — criar reporte

#### Requisitos
- **R19** (Classificação do Reporte), **R20** (Seleção de Subcategoria), **R22** (Preenchimento de Informações do Reporte), **R25** (associação ao autor), **R49/R50** (só logado cria).

**Exige token** (`Authorization: Bearer <token>`). Sem token → `401`. O autor é o usuário do token (`sub` do JWT) — o app **não** envia o autor no corpo.

#### Contrato HTTP — Requisição

```json
{
  "titulo": "Iluminacao ruim",
  "descricao": "Postes apagados na rua de tras da USCS.",
  "latitude": -23.6182,
  "longitude": -46.5645,
  "categoria": "INSEGURANCA",
  "subcategoria": "ILUMINACAO_RUIM",
  "endereco": "Rua de exemplo, Santa Paula",
  "bairro": "Santa Paula"
}
```

> O app envia `categoria` **e** `subcategoria` pelo **nome do enum** (desde 01/11/2026 a chave primária dessas tabelas é o próprio enum, não há mais id numérico). O servidor valida se a sub pertence à categoria.

#### Resposta `201` — reporte criado (formato da listagem)

```json
{
  "id": 1,
  "titulo": "Iluminacao ruim",
  "descricao": "Postes apagados na rua de tras da USCS.",
  "latitude": -23.6182,
  "longitude": -46.5645,
  "dataReporte": "2026-09-12T15:00:00-03:00",
  "categoria": "INSEGURANCA",
  "subcategoria": "ILUMINACAO_RUIM",
  "autorId": 2,
  "autorNome": "Maria Silva",
  "endereco": "Rua de exemplo, Santa Paula",
  "bairro": "SANTA_PAULA"
}
```

> `autorId` e `autorNome` aparecem em todas as respostas de reporte (criação, lista e detalhe). Reportes criados antes de 15/11/2026 não têm autor e devolvem os dois como `null`.

#### Regras de negócio e validações
1. **`titulo` obrigatório, de 5 a 30 caracteres; `descricao` obrigatória, de 20 a 500 caracteres** — `@NotBlank` + `@Size`. Fora do tamanho → `400` "Erro de validação", com `mensagem` indicando o campo (ex.: `descricao: Descrição deve ter entre 20 e 500 caracteres.`). A coluna `descricao` é `TEXT` (sem o limite de 255 caracteres).
2. **`latitude` e `longitude` obrigatórias** — `@NotNull`, com `@DecimalMin`/`@DecimalMax` (lat -90..90, lon -180..180).
3. **`categoria` e `subcategoria` obrigatórias** — `@NotNull`. Nome que não existe no enum (ex.: `"ASSALTO"`) → `400` "Erro de validação".
4. **Sub desalinhada da categoria** → `400` (regra de negócio).
5. **O cliente não envia `status` nem `dataReporte`** — o Service define `status = PUBLICADO` e `dataReporte = agora` (`America/Sao_Paulo`).
6. **`endereco` e `bairro` são Strings opcionais na entrada** — a geocodificação é feita no front. O endereço é salvo como recebido; o nome do bairro é comparado com os 15 bairros ignorando acentos, maiúsculas e espaços nas pontas. "São Caetano" corresponde a `JARDIM_SAO_CAETANO`. Bairro informado que não corresponde a nenhum deles → `400` (`BairroInvalidoException`, filha de `ApiException`). Sem bairro informado, a associação permanece nula.
7. Ao criar um reporte com bairro, o Service recalcula a classificação daquele bairro depois de salvar o reporte, dentro da transação de criação. As respostas de criação/lista/detalhe/histórico incluem `endereco` e `bairro` (`TipoBairro`, nome do enum; `null` sem associação).

---

## 3. ENDPOINT: GET /api/reportes — listar para o mapa

#### Requisitos
- **R16** (Visualizar Mapa com Ocorrências), **R17** (Filtragem por Categoria/Bairro), **R60** (Visualizar Detalhes do Reporte).

#### Contrato HTTP — Resposta `200`

```json
[
  {
    "id": 1,
    "titulo": "Iluminacao ruim",
    "descricao": "Postes apagados na rua de tras da USCS.",
    "latitude": -23.6182,
    "longitude": -46.5645,
    "dataReporte": "2026-09-12T15:00:00-03:00",
    "categoria": "INSEGURANCA",
    "subcategoria": "ILUMINACAO_RUIM",
    "autorId": 2,
    "autorNome": "Maria Silva",
    "endereco": "Rua de exemplo, Santa Paula",
    "bairro": "SANTA_PAULA"
  }
]
```

Não exige token (R49).

#### Regras
1. **Só reportes `PUBLICADO`** aparecem. `RASCUNHO` e `EXCLUIDO` ficam de fora.
2. **Filtros opcionais de categoria e bairro:** `?categoria=SEGURANCA` ou `?categoria=INSEGURANCA`; `?bairro=SANTA_PAULA`. São combináveis, por exemplo `?categoria=INSEGURANCA&bairro=SANTA_PAULA`. Sem filtros, retorna todos os publicados; com filtros, somente os publicados correspondentes. Valor inexistente em um dos enums, como `XYZ`, retorna `400`. Filtros isolados e combinados verificados por HTTP em 19/11/2026.
3. O JSON é desnormalizado de propósito: `categoria` e `subcategoria` saem como **nome do enum** (texto), não como ids — o app desenha o pin com uma única requisição.

---

## 4. ENDPOINT: GET /api/reportes/{id} — detalhe do reporte

- Devolve o mesmo objeto da listagem, sem o array.
- `404` quando o id não existir ou o reporte não estiver `PUBLICADO` (usando o `@RestControllerAdvice`).

---

## 5. ENDPOINT: GET /api/categorias — seletores do formulário de reporte

Alimenta os seletores da tela de criação (categoria → subcategorias). **Populado pelo seed no boot** (a tarefa 4 resolveu a lista vazia).

#### Resposta `200`

```json
[
  {
    "tipoCategoria": "SEGURANCA",
    "subcategorias": [
      { "tipoSubCategoria": "ILUMINACAO_BOA", "categoria": "SEGURANCA" },
      { "tipoSubCategoria": "AREA_MOVIMENTADA", "categoria": "SEGURANCA" },
      { "tipoSubCategoria": "PRESENCA_POLICIAL", "categoria": "SEGURANCA" },
      { "tipoSubCategoria": "COMERCIO_ATIVO", "categoria": "SEGURANCA" },
      { "tipoSubCategoria": "CAMERAS", "categoria": "SEGURANCA" }
    ]
  },
  {
    "tipoCategoria": "INSEGURANCA",
    "subcategorias": [
      { "tipoSubCategoria": "ILUMINACAO_RUIM", "categoria": "INSEGURANCA" },
      { "tipoSubCategoria": "ROUBO", "categoria": "INSEGURANCA" },
      { "tipoSubCategoria": "FURTO", "categoria": "INSEGURANCA" },
      { "tipoSubCategoria": "VANDALISMO", "categoria": "INSEGURANCA" },
      { "tipoSubCategoria": "ACIDENTE", "categoria": "INSEGURANCA" }
    ]
  }
]
```

> Não há ids numéricos: o nome do enum é a chave e é o que o POST usa em `categoria` + `subcategoria`. A resposta sai de DTOs (`CategoriaResponse`/`SubcategoriaResponse`), não da entidade; cada sub traz o nome da sua `categoria`. `GET /api/subcategorias` devolve a mesma lista de subs sem agrupar e `GET /api/bairros` devolve os 15 bairros de São Caetano do Sul (seed no boot) como `[{ "tipoBairro": "SANTA_PAULA", "nome": "Santa Paula", "classificacaoSeguranca": null }]` (`BairroResponse`). A chave do bairro é o enum `TipoBairro`, sem id numérico.

---

## 5.1. ENDPOINT: GET /api/bairros/{tipoBairro} — detalhe do bairro

Criado em 10/11/2026 e ampliado em 19/11/2026 conforme a Tarefa 7 de `docs/tarefas-mvp-restante.md`. Plural definido por Murilo, compilado e verificado por HTTP sem token no H2 local recriado.

- Recebe a chave enum `TipoBairro` na URL, por exemplo `GET /api/bairros/CENTRO`.
- Busca o bairro pelo repositório e devolve `BairroDetalheResponse`, com tipo, nome, classificação e lista de reportes `PUBLICADO` convertidos para `ReporteResponse` no Service. A listagem `GET /api/bairros` continua usando `BairroResponse`.
- `200` para bairro cadastrado. Exemplo sem reportes publicados associados:

```json
{
  "tipoBairro": "CENTRO",
  "nome": "Centro",
  "classificacaoSeguranca": null,
  "reportes": []
}
```

- `400` para texto que não existe no enum, como `NAO_EXISTE` (verificado via HTTP).
- `404` via `RecursoNaoEncontradoException` se a chave enum for válida, mas o bairro não estiver cadastrado.
- Ao buscar o detalhe, o Service recalcula e salva a classificação somando as notas dos reportes `PUBLICADO` daquele bairro, com a fórmula existente em `TipoCategoria.calculaClassificacao`.
- Sem reportes publicados associados, a classificação é `null` (não zero).
- Verificado em 10/11/2026: três reportes publicados de `CENTRO` (`PRESENCA_POLICIAL`, `ROUBO`, `CAMERAS`) resultaram em `classificacaoSeguranca: 0.06` (`0.06 - 0.04 + 0.04`). O valor foi confirmado no banco. Um bairro sem reportes (`BARCELONA`) retornou e persistiu `null`.

---

## 5.2. ENDPOINT: POST /api/auth/cadastro — criar conta

Requisição (`CadastroRequest`):

```json
{
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "telefone": "11987654321",
  "dataNascimento": "2000-05-20",
  "senha": "Senha@123"
}
```

- Validação: nome 3–100; e-mail válido; telefone só dígitos com DDD; data no passado; senha 8–30 com maiúscula, minúscula, número e especial → `400` se falhar.
- O e-mail é normalizado (`trim` + minúsculas) antes de gravar.
- E-mail já cadastrado (comparação já normalizada) → `409` `"Conflito"`.
- A senha é gravada com BCrypt; a conta nasce `USUARIO_ATIVO` com `dataInicio` no horário de São Caetano do Sul.
- `201` com `UsuarioResponse` (sem senha): `{ "id", "nome", "email", "telefone", "dataNascimento", "avatar" }`.

## 5.3. ENDPOINT: POST /api/auth/login — entrar

Requisição (`LoginRequest`): `{ "email": "maria@email.com", "senha": "Senha@123" }` — e-mail obrigatório e válido, senha obrigatória (`400` se faltar).

Resposta `200` (`LoginResponse`):

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": { "id": 2, "nome": "Maria Silva", "email": "maria@email.com", "telefone": "11987654321", "dataNascimento": "2000-05-20", "avatar": null }
}
```

- O e-mail é normalizado antes da busca (maiúsculas não importam).
- E-mail inexistente **e** senha errada devolvem o mesmo `401` com a mesma mensagem, para não revelar quais e-mails têm conta.
- Conta com senha certa mas `status` diferente de `USUARIO_ATIVO` → `403`. A checagem vem depois da senha.
- Token JWT HS256: `iss=vigia-api`, `sub`=id do usuário, `iat`, `exp` (24 h). O app manda nas próximas requisições como `Authorization: Bearer <token>`. Desde 15/11/2026 o token é exigido em toda rota que não seja `GET` de leitura ou `/api/auth/**` (ver "Regras de acesso" na seção 1).

---

## 5.4. Perfil do usuário — Tarefa 3 implementada e revisada

Implementado e testado por HTTP em 18/11/2026, conforme `docs/tarefas-mvp-restante.md` (R11, R12 e R13). Compilação verificada com `BUILD SUCCESS`.

| Método | Rota | Resposta verificada |
|---|---|---|
| GET | `/api/usuarios/me` | 200 `UsuarioResponse`, sem senha |
| PUT | `/api/usuarios/me` | 200 `UsuarioResponse` atualizado |
| PUT | `/api/usuarios/me/senha` | 204, sem corpo |
| GET | `/api/usuarios/me/reportes` | 200 `List<ReporteResponse>` |

Todas exigem Bearer Token. O Controller obtém o id exclusivamente de `jwt.getSubject()`; os PUTs validam os DTOs com `@Valid`.

- `AtualizarUsuarioRequest` contém `nome`, `telefone` e `avatar` (String opcional, URL/identificador da foto).
- Nome é opcional na atualização: ausente/`null` preserva o valor salvo; quando informado, deve ter 3–100 caracteres e não pode ficar em branco. Telefone usa a mesma regex do cadastro: `^[1-9]{2}(9\d{8}|[2-5]\d{7})$`.
- `TrocarSenhaRequest.novaSenha` usa as mesmas validações de força do cadastro: obrigatória, 8–30 caracteres, maiúscula, minúscula, número e caractere especial. `senhaAtual` continua obrigatória.
- `UsuarioService.atualizar` já salva avatar e só altera nome/telefone/avatar quando o campo correspondente não é `null`, preservando os valores salvos nos demais.
- `UsuarioService.trocarSenha` já rejeita nova senha igual à salva, após conferir a senha atual, lançando `SenhaIgualException` (`ApiException`, status `400`, erro "Regra de negócio").
- Histórico integrado internamente em `UsuarioService.listarReportes(Long idUsuario)`: busca por `usuario.id`, exclui `EXCLUIDO`, ordena por `dataReporte` decrescente e converte para `List<ReporteResponse>` no Service, com os campos atuais de autor.

Exemplo de requisição para `PUT /api/usuarios/me`:

```json
{
  "nome": "Maria Silva",
  "telefone": "11987654321",
  "avatar": "https://example.com/avatar.png"
}
```

Exemplo de requisição para `PUT /api/usuarios/me/senha`:

```json
{
  "senhaAtual": "Atual@123",
  "novaSenha": "NovaSenha@456"
}
```

Verificações do roteiro:
- Login → `200`; GET perfil → `200`, com o usuário do token e sem `senha`/`senhaHash`.
- PUT nome/avatar → `200`; telefone/avatar enviados como `null` mantiveram os valores salvos; telefone inválido → `400`.
- Senha atual errada, nova fraca (`123`) ou nova igual à atual → `400`.
- Troca válida → `204` sem corpo; login com a nova senha → `200`.
- Histórico → `200`, somente os reportes do usuário, em ordem decrescente; reporte de teste marcado `EXCLUIDO` no H2 não apareceu.
- Cada uma das quatro rotas sem token → `401`.

**Correção de revisão verificada (18/11/2026):** nome ausente/`null` é aceito e preserva o valor salvo. Repetição HTTP após reiniciar a API: PUT somente avatar → 200; somente telefone → 200; nome/telefone/avatar explícitos `null` → 200, sem apagar os valores salvos. Nome vazio, somente espaços, com 2 ou 101 caracteres → 400, sem alterar o dado salvo. Compilação e testes focados passaram.

---

## 5.5. POST /api/usuarios/me/desativar — Tarefa 4 implementada e testada

Implementado e verificado em 19/11/2026, conforme `docs/tarefas-mvp-restante.md` (R62):

- Criado `DesativarContaRequest` com `String senha` obrigatória (`@NotBlank`).
- Service implementado: confere a senha; se incorreta, lança `SenhaIncorretaException` (filha de `ApiException`, status 400, mensagem "Senha incorreta"). Com senha correta, define `USUARIO_INATIVO`, grava `dataFim` no horário de São Paulo e salva, preservando os dados e reportes.
- Implementado `POST /api/usuarios/me/desativar`, com Bearer Token. O id vem exclusivamente do JWT, e a senha recebida é validada com `@Valid`.
- Resposta verificada: `204`, sem corpo. Exemplo de requisição: `{ "senha": "SuaSenha@123" }`.
- DTO, Service e Controller compilados com sucesso; roteiro HTTP concluído.

| Teste | Resultado verificado |
|---|---|
| Senha errada | 400, mensagem "Senha incorreta" |
| Senha ausente ou em branco | 400 |
| Senha correta | 204, sem corpo |
| Login depois da desativação | 403 |
| Reporte da conta desativada no mapa/detalhe público | 200, dados e autor preservados |
| Desativação sem token | 401 |

Conferido no H2: usuário preservado com `USUARIO_INATIVO` e `dataFim` preenchida; reporte permanece `PUBLICADO` e associado ao mesmo usuário. A desativação não apaga os dados.

---

## 5.6. Tarefa 7 — verificação e alinhamento

- `TipoBairro.porNome` conferido manualmente no JShell: Santa Paula/caixa variada/espaços → `SANTA_PAULA`; Cerâmica/Ceramica → `CERAMICA`; São Caetano → `JARDIM_SAO_CAETANO`; inexistente → `Optional.empty`.
- Compilação passou. O teste de contexto já existente passou com um H2 temporário, sem adicionar testes ao projeto.
- Roteiro HTTP verificado em 19/11/2026 inicialmente com H2 isolado e repetido com sucesso no H2 local recriado após o reset autorizado:

| Caso | Resultado verificado |
|---|---|
| Santa Paula / santa paula | 201, associado a `SANTA_PAULA` |
| Cerâmica / alias São Caetano | 201, `CERAMICA` / `JARDIM_SAO_CAETANO` |
| Sem endereço/bairro | 201, campos nulos; consulta posterior compatível |
| Bairro inexistente | 400, sem criar reporte |
| Bairro isolado ou combinado com categoria | 200, somente reportes correspondentes |
| Recálculo na criação | Nota já atualizada na lista antes de abrir detalhe |
| Detalhe do bairro | 200 com classificação e lista de reportes publicados |
| Sem reportes no bairro | Nota nula e lista vazia |
| Reporte RASCUNHO / EXCLUIDO | Fora de filtros, detalhe e cálculo do bairro |
| Histórico do usuário | Endereço/bairro preservados; excluídos continuam de fora |
| Bairro inválido no filtro / POST sem token | 400 / 401 |

- Caminho canônico definido por Murilo em 19/11/2026: `/api/bairros` e `/api/bairros/{tipoBairro}`. Controller e matcher GET do Security atualizados, sem alias no singular. Lista e detalhe sem token → 200; caminho antigo no singular com token → 404. Comunicar ao Fabrício. O banco local foi recriado pelo Hibernate/seed após reset autorizado, e os testes funcionais passaram nesse banco.

---

## 5.7. ENDPOINT: POST /api/reportes/{idReporte}/denuncias — denunciar reporte (Tarefa 8)

**Requisitos:** R43, R45, R66. **Exige token** (`Authorization: Bearer <token>`); o denunciante vem do token.

#### Requisição
```json
{
  "motivo": "INFORMACAO_FALSA",
  "descricao": "Esse reporte nao aconteceu nesse local"
}
```
- `motivo` (obrigatório): `INFORMACAO_FALSA`, `LOCAL_INCORRETO`, `CONTEUDO_ENGANOSO`, `CONTEUDO_OFENSIVO`, `SPAM`, `OUTRO`.
- `descricao` (obrigatória, 10 a 500 caracteres): a justificativa. No `OUTRO`, é o texto livre do motivo.

#### Resposta `201`
```json
{
  "idDenuncia": 1,
  "idReporte": 65,
  "motivo": "INFORMACAO_FALSA",
  "descricao": "Esse reporte nao aconteceu nesse local",
  "dataDenuncia": "2026-11-21T21:27:45.341-03:00",
  "statusDenuncia": "PENDENTE"
}
```
A resposta não mostra quem denunciou. Toda denúncia nasce `PENDENTE` (análise do admin fora do MVP).

#### Erros
| Situação | Status |
|---|---|
| Sem motivo, motivo inexistente, sem justificativa ou fora de 10–500 | 400 |
| Sem token ou token inválido | 401 |
| Denunciar o próprio reporte | 403 (`"erro": "Acesso negado"`) |
| Reporte inexistente, `RASCUNHO` ou `EXCLUIDO` | 404 |
| Já denunciou esse reporte | 409 |

Testado via HTTP em 21/11/2026 (todos os casos acima, com reporte id 65 e contas de teste ids 34/35 no H2 local).

---

## 5.8. Editar, excluir, nota do bairro e avaliação (21/11/2026) — código pronto, testes pendentes

**`PUT /api/reportes/{idReporte}`** (token; só o autor; uma vez) — corpo `{ "descricao": "..." }` (20 a 500 caracteres). Só a descrição muda. Resposta 200 com o `ReporteResponse` (`editado: true`). Erros: 400 descrição inválida, 401 sem token, 403 não é o autor, 404 inexistente/excluído, 409 já editado.

**`DELETE /api/reportes/{idReporte}`** (token; só o autor) — 204 sem corpo. É soft delete: o status vira `EXCLUIDO`, o reporte some do mapa, filtros, bairro e histórico, e a nota do bairro é recalculada. Erros: 401, 403, 404 (inexistente ou já excluído).

**`ReporteResponse`** ganhou `editado` (boolean) no fim. Reportes editados (status `EDITADO`) continuam no mapa e podem ser denunciados.

**Nota do bairro** (`classificacaoSeguranca` em `/api/bairros` e `/api/bairros/{tipoBairro}`): agora sempre entre **0 e 5**, com 1 casa decimal; bairro sem reportes = 5,0 (antes era `null`).

**`UsuarioResponse`** (login, cadastro, `/api/usuarios/me`) ganhou `avaliacao` (0 a 5; usuários sem nota aparecem com 5,0). Calculada automaticamente (tarefa 9): `5 × (reportes + 1) / (reportes + 1 + denúncias recebidas)`; cai quando o autor recebe denúncia e sobe com novos reportes. Permanente: excluir reporte não altera.

---

## 6. Pendências do Contrato

1. **Validar com o front-end** o payload do `POST` e o formato de resposta (Fase 4 / Tarefa 10 — nada acordado ainda).
2. **Endereço base acessível pelo celular**: `localhost` não funciona no app — usar o IP da máquina na rede local (`192.168.x.x`) e `server.address=0.0.0.0`.
3. ~~**Seed de `Categoria`/`Subcategoria`**~~ — **resolvido** (tarefa 4).
