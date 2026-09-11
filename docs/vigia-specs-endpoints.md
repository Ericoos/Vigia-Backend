# Especificação Técnica dos Endpoints - Módulo Reporte

Foco: o app cria um reporte, a API recebe, **valida** e **armazena** no banco, e devolve a listagem para os pins do mapa.

---

## 1. Contexto Global da API

```
Base URL (dev):  http://localhost:8080/api
Content-Type:    application/json
Datas:           ISO-8601 UTC (2026-08-28T10:30:00Z)
Identificadores: sem acentos nem cedilha nos nomes dos campos
```

| Código | Quando |
|---|---|
| `200 OK` | Consulta bem-sucedida |
| `201 Created` | Reporte criado |
| `400 Bad Request` | Validação falhou |
| `404 Not Found` | Recurso inexistente |

**Formato padrão de erro** (produzido pelo `@RestControllerAdvice` do pacote `exception`):

```json
{
  "timestamp": "2026-09-05T14:22:10Z",
  "status": 400,
  "erro": "Requisicao invalida",
  "mensagem": "O campo titulo e obrigatorio",
  "caminho": "/api/reportes"
}
```

---

## 2. ENDPOINT: POST /api/reportes — criar reporte

#### Requisitos
- **R19** (Classificação do Reporte), **R20** (Seleção de Subcategoria), **R22** (Preenchimento de Informações do Reporte).

#### Contrato HTTP — Requisição

```json
{
  "titulo": "Iluminacao Ruim na Rua Santo Antonio",
  "descricao": "Varios postes apagados na rua de tras da USCS.",
  "latitude": -23.6182,
  "longitude": -46.5645,
  "idSubcategoria": 3
}
```

#### Resposta `201` — reporte criado (formato da listagem)

```json
{
  "idReporte": 1,
  "titulo": "Iluminacao Ruim na Rua Santo Antonio",
  "descricao": "Varios postes apagados na rua de tras da USCS.",
  "latitude": -23.6182,
  "longitude": -46.5645,
  "tipoCategoria": "Inseguranca",
  "tipoSubcategoria": "Iluminacao Publica Ruim",
  "bairroNome": null,
  "dataReporte": "2026-08-28T10:30:00Z"
}
```

#### Regras de negócio e validações
1. **`titulo` e `descricao` obrigatórios** — `@NotBlank`.
2. **`latitude` e `longitude` obrigatórias** — `@NotNull`.
3. **O cliente não envia `status` nem `dataReporte`** — o Service define `status = PUBLICADO` e `dataReporte = agora`.
4. **O cliente não envia a categoria** — ela é derivada da `Subcategoria` (`idSubcategoria`) no Service.
5. **`endereco` e `bairro` ficam nulos por enquanto** — a geocodificação é feita no front, não é responsabilidade do back.

#### 📥 Prompt pronto para copiar
> "Preciso do endpoint de criação de reportes da API do Vigia (Spring Boot 4.1.1, Java 21).
>
> 1. Crie o record `ReporteRequest` no pacote `dto` com titulo, descricao, latitude, longitude e idSubcategoria, aplicando Bean Validation (`@NotBlank` em titulo/descricao, `@NotNull` nas coordenadas e no idSubcategoria).
> 2. Crie o record `ReporteResponse` no pacote `dto` com os campos da resposta (sem expor a entidade).
> 3. Em `ReporteService.criar(ReporteRequest)`, converta o DTO para a entidade `Reporte`, derive a categoria a partir da subcategoria, defina `status = PUBLICADO` e `dataReporte = agora`, e salve.
> 4. No `ReporteController`, troque o `POST /api/reportes` para receber `@Valid ReporteRequest` e devolver `201`.
> 5. Crie `@RestControllerAdvice` no pacote `exception` devolvendo `400` no formato padrão quando a validação falhar.
>
> Lembre: sem `nativeQuery` (o banco final é MySQL). Não exponha a entidade no contrato."

---

## 3. ENDPOINT: GET /api/reportes — listar para o mapa

#### Requisitos
- **R16** (Visualizar Mapa com Ocorrências), **R17** (Filtragem por Categoria/Bairro), **R60** (Visualizar Detalhes do Reporte).

#### Contrato HTTP — Resposta `200`

```json
[
  {
    "idReporte": 1,
    "titulo": "Iluminacao Ruim na Rua Santo Antonio",
    "descricao": "Varios postes apagados na rua de tras da USCS.",
    "latitude": -23.6182,
    "longitude": -46.5645,
    "tipoCategoria": "Inseguranca",
    "tipoSubcategoria": "Iluminacao Publica Ruim",
    "bairroNome": null,
    "dataReporte": "2026-08-28T10:30:00Z"
  }
]
```

#### Regras
1. **Só reportes `PUBLICADO`** aparecem. `RASCUNHO` e `EXCLUIDO` ficam de fora.
2. Filtros opcionais e combináveis (quando o front precisar): `?categoria=Seguranca|Inseguranca`, `?bairro={id}`.
3. O JSON é desnormalizado de propósito: `tipoCategoria` e `tipoSubcategoria` saem como texto, não como ids — o app desenha o pin com uma única requisição.

#### 📥 Prompt pronto para copiar
> "Preciso do endpoint de listagem de reportes da API do Vigia, que alimenta os pins do mapa.
>
> 1. Em `ReporteService`, crie `listar()` que devolve apenas reportes com status `PUBLICADO`, aceitando filtros opcionais por categoria e bairro.
> 2. Use `ReporteResponse` (dto) como formato de resposta, achatando `tipoCategoria` e `tipoSubcategoria` como String.
> 3. Atualize o `GET /api/reportes` no `ReporteController` para devolver a lista de `ReporteResponse`.
> 4. Crie `GET /api/reportes/{id}` devolvendo um único reporte, com `404` quando não existir.
>
> Sem `nativeQuery`. Explicar a conversão entidade → DTO."

---

## 4. ENDPOINT: GET /api/reportes/{id} — detalhe do reporte

- Devolve o mesmo objeto da listagem, sem o array.
- `404` quando o id não existir (usando o `@RestControllerAdvice`).

---

## 5. ENDPOINT: GET /api/categorias — seletores do formulário de reporte

Alimenta os seletores da tela de criação (categoria → subcategorias).

#### Resposta `200`

```json
[
  {
    "id": 1,
    "tipoCategoria": "Inseguranca",
    "subcategorias": [
      { "id": 3, "tipoSubCategoria": "Iluminacao Publica Ruim" },
      { "id": 4, "tipoSubCategoria": "Assalto/Roubo" }
    ]
  }
]
```

> **Atenção**: hoje não existe seed populando `Categoria`/`Subcategoria` — os GETs devolvem lista vazia. Para o `POST /api/reportes` funcionar, essas tabelas precisam de registros.

---

## 6. Pendências do Contrato

1. **Validar com o front-end** o payload do `POST` e o formato de resposta (nada acordado ainda).
2. **Endereço base acessível pelo celular**: `localhost` não funciona no app — usar o IP da máquina na rede local (`192.168.x.x`).
3. **Seed de `Categoria`/`Subcategoria`** para o formulário e para o POST funcionarem.