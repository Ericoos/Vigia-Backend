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
  "idCategoria": 2,
  "idSubcategoria": 6
}
```

> O app envia `idCategoria` **e** `idSubcategoria`. O servidor valida se a sub pertence à categoria.

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
1. **`titulo` e `descricao` obrigatórios** — `@NotBlank`.
2. **`latitude` e `longitude` obrigatórias** — `@NotNull`, com `@DecimalMin`/`@DecimalMax` (lat -90..90, lon -180..180).
3. **`idCategoria` e `idSubcategoria` obrigatórios** — `@NotNull`. Ids inexistentes → `404`.
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
2. Filtros opcionais e combináveis (quando o front precisar): `?categoria=Seguranca|Inseguranca`, `?bairro={id}` — **ainda não implementados**.
3. O JSON é desnormalizado de propósito: `categoria` e `subcategoria` saem como **nome do enum** (texto), não como ids — o app desenha o pin com uma única requisição.

---

## 4. ENDPOINT: GET /api/reportes/{id} — detalhe do reporte

- Devolve o mesmo objeto da listagem, sem o array.
- `404` quando o id não existir (usando o `@RestControllerAdvice`).

---

## 5. ENDPOINT: GET /api/categorias — seletores do formulário de reporte

Alimenta os seletores da tela de criação (categoria → subcategorias). **Populado pelo seed no boot** (a tarefa 4 resolveu a lista vazia).

#### Resposta `200`

```json
[
  {
    "id": 1,
    "tipoCategoria": "SEGURANCA",
    "subcategorias": [
      { "id": 1, "tipoSubCategoria": "ILUMINACAO_BOA" },
      { "id": 2, "tipoSubCategoria": "AREA_MOVIMENTADA" },
      { "id": 3, "tipoSubCategoria": "PRESENCA_POLICIAL" },
      { "id": 4, "tipoSubCategoria": "COMERCIO_ATIVO" },
      { "id": 5, "tipoSubCategoria": "CAMERAS" }
    ]
  },
  {
    "id": 2,
    "tipoCategoria": "INSEGURANCA",
    "subcategorias": [
      { "id": 6, "tipoSubCategoria": "ILUMINACAO_RUIM" },
      { "id": 7, "tipoSubCategoria": "ROUBO" },
      { "id": 8, "tipoSubCategoria": "FURTO" },
      { "id": 9, "tipoSubCategoria": "VANDALISMO" },
      { "id": 10, "tipoSubCategoria": "ACIDENTE" }
    ]
  }
]
```

> Os ids exatos vêm do `SELECT` no H2 — o POST usa `idCategoria` + `idSubcategoria`. O campo `categoria` dentro de cada sub não aparece no JSON (marcado `WRITE_ONLY` para evitar recursão).

---

## 6. Pendências do Contrato

1. **Validar com o front-end** o payload do `POST` e o formato de resposta (Fase 4 / Tarefa 10 — nada acordado ainda).
2. **Endereço base acessível pelo celular**: `localhost` não funciona no app — usar o IP da máquina na rede local (`192.168.x.x`) e `server.address=0.0.0.0`.
3. ~~**Seed de `Categoria`/`Subcategoria`**~~ — **resolvido** (tarefa 4).