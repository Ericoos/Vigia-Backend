# Especificação Técnica dos Endpoints - Módulo Reporte

Foco: o app cria um reporte, a API recebe, **valida** e **armazena** no banco, e devolve a listagem para os pins do mapa.

> **Status: implementado no backend (tarefas 1 a 9 concluídas).** Contrato refletindo o que o código realmente faz. Falta só alinhar o contrato com o front (Fase 4 / Tarefa 10).

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
| `201 Created` | Reporte criado |
| `400 Bad Request` | Validação falhou ou regra de negócio (sub desalinhada da categoria) |
| `404 Not Found` | Recurso inexistente |

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
- `404` → `"erro": "Recurso não encontrado"`.

---

## 2. ENDPOINT: POST /api/reportes — criar reporte

#### Requisitos
- **R19** (Classificação do Reporte), **R20** (Seleção de Subcategoria), **R22** (Preenchimento de Informações do Reporte).

#### Contrato HTTP — Requisição

```json
{
  "titulo": "Iluminacao ruim",
  "descricao": "Postes apagados na rua de tras da USCS.",
  "latitude": -23.6182,
  "longitude": -46.5645,
  "categoria": "INSEGURANCA",
  "subcategoria": "ILUMINACAO_RUIM"
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
  "subcategoria": "ILUMINACAO_RUIM"
}
```

#### Regras de negócio e validações
1. **`titulo` obrigatório, de 5 a 30 caracteres; `descricao` obrigatória, de 20 a 500 caracteres** — `@NotBlank` + `@Size`. Fora do tamanho → `400` "Erro de validação", com `mensagem` indicando o campo (ex.: `descricao: Descrição deve ter entre 20 e 500 caracteres.`). A coluna `descricao` é `TEXT` (sem o limite de 255 caracteres).
2. **`latitude` e `longitude` obrigatórias** — `@NotNull`, com `@DecimalMin`/`@DecimalMax` (lat -90..90, lon -180..180).
3. **`categoria` e `subcategoria` obrigatórias** — `@NotNull`. Nome que não existe no enum (ex.: `"ASSALTO"`) → `400` "Erro de validação".
4. **Sub desalinhada da categoria** → `400` (regra de negócio).
5. **O cliente não envia `status` nem `dataReporte`** — o Service define `status = PUBLICADO` e `dataReporte = agora` (`America/Sao_Paulo`).
6. **`endereco` e `bairro` ficam nulos por enquanto** — a geocodificação é feita no front, não é responsabilidade do back.

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
    "subcategoria": "ILUMINACAO_RUIM"
  }
]
```

#### Regras
1. **Só reportes `PUBLICADO`** aparecem. `RASCUNHO` e `EXCLUIDO` ficam de fora.
2. **Filtro por categoria implementado** (Tarefa 3 semanal, 10/11/2026): parâmetro opcional `?categoria=SEGURANCA` ou `?categoria=INSEGURANCA`. Sem o parâmetro, retorna todos os publicados; com ele, somente os publicados da categoria informada. Valor inexistente no enum, como `XYZ`, retorna `400`. O filtro por bairro continua pendente.
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

> Não há ids numéricos: o nome do enum é a chave e é o que o POST usa em `categoria` + `subcategoria`. A resposta sai de DTOs (`CategoriaResponse`/`SubcategoriaResponse`), não da entidade; cada sub traz o nome da sua `categoria`. `GET /api/subcategorias` devolve a mesma lista de subs sem agrupar e `GET /api/bairro` devolve os 15 bairros de São Caetano do Sul (seed no boot) como `[{ "tipoBairro": "SANTA_PAULA", "nome": "Santa Paula", "classificacaoSeguranca": null }]` (`BairroResponse`). A chave do bairro é o enum `TipoBairro`, sem id numérico.

---

## 5.1. ENDPOINT: GET /api/bairro/{tipoBairro} — detalhe do bairro

Implementado em 10/11/2026 conforme a Tarefa 1 de `tarefas-murilo-semana.md`.

- Recebe a chave enum `TipoBairro` na URL, por exemplo `GET /api/bairro/CENTRO`.
- Busca o bairro pelo repositório e devolve `BairroResponse`, usando a conversão do Service.
- `200` para bairro cadastrado. Exemplo sem reportes publicados associados:

```json
{
  "tipoBairro": "CENTRO",
  "nome": "Centro",
  "classificacaoSeguranca": null
}
```

- `400` para texto que não existe no enum, como `NAO_EXISTE` (verificado via HTTP).
- `404` via `ResourceNotFoundException` se a chave enum for válida, mas o bairro não estiver cadastrado.
- Ao buscar o detalhe, o Service recalcula e salva a classificação somando as notas dos reportes `PUBLICADO` daquele bairro, com a fórmula existente em `TipoCategoria.calculaClassificacao`.
- Sem reportes publicados associados, a classificação é `null` (não zero).
- Verificado em 10/11/2026: três reportes publicados de `CENTRO` (`PRESENCA_POLICIAL`, `ROUBO`, `CAMERAS`) resultaram em `classificacaoSeguranca: 0.06` (`0.06 - 0.04 + 0.04`). O valor foi confirmado no banco. Um bairro sem reportes (`BARCELONA`) retornou e persistiu `null`.

---

## 6. Pendências do Contrato

1. **Validar com o front-end** o payload do `POST` e o formato de resposta (Fase 4 / Tarefa 10 — nada acordado ainda).
2. **Endereço base acessível pelo celular**: `localhost` não funciona no app — usar o IP da máquina na rede local (`192.168.x.x`) e `server.address=0.0.0.0`.
3. ~~**Seed de `Categoria`/`Subcategoria`**~~ — **resolvido** (tarefa 4).
