# Mudanças na Documentação do Relatório e no que Vamos Implementar

Documento de controle do que precisa mudar na documentação do relatório (`.docx`, requisitos, diagramas) e do que vamos implementar no código. Os `.drawio` **não são editados aqui** — a intenção de mudança fica registrada para o grupo aplicar nos diagramas.

---

## 1. Mudanças na documentação do relatório

1. **Modelo de dados (§6) / Diagrama de classes**: registrar o enum `StatusReporte` (`RASCUNHO`, `PUBLICADO`, `EXCLUIDO`) usado no `Reporte`. Os tipos de `Categoria`/`Subcategoria` usam **enum** (`TipoCategoria`/`TipoSubcategoria`) — o diagrama deve refletir.
2. **`avaliacaoGravidade`**: o campo foi **removido** da classe `Reporte` no código — o diagrama deve refletir a ausência (ou ser alinhado com o grupo antes de voltar).
3. **Diagrama de classes — associações**: no diagrama aparece `idCategoria: int`/`idSubcategoria: int`; no código isso virou **referência de objeto** (`categoria`, `subcategoria` com `@ManyToOne`), e o `Reporte` só guarda as FKs. Refletir as associações `Reporte → Categoria` e `Reporte → Subcategoria` no diagrama.
4. **Diagrama de pacotes**: adicionar os pacotes `DTO`, `EXCEPTION` e `CONFIG` (seed) na `CAMADA_BACKEND` — existem só no código, ainda não constam no diagrama.
5. **Documentar DTOs**: descrever no relatório que a API usa DTOs (records) no contrato — a entidade não é exposta.
6. **Relatório**: descrever o fluxo do módulo Reporte — o app cria o reporte, a API recebe, valida e armazena, e a listagem alimenta o mapa.

---

## 2. O que vamos implementar (módulo Reporte)

> **Backend concluído (13/09/2026).** Os itens de Fase 2 e 3 abaixo estão **implementados e testados**.

1. **Finalizar a classe e os métodos de `Reporte`** (Fase 2) — ✅:
   - `ReporteService.criar()` — monta a entidade, define `status = PUBLICADO` e `dataReporte = agora` (`America/Sao_Paulo`), salva.
   - `POST /api/reportes` com Bean Validation (`titulo`/`descricao` obrigatórios, coordenadas obrigatórias, `idCategoria`+`idSubcategoria`).
   - `GET /api/reportes` retornando só `PUBLICADO`; `GET /api/reportes/{id}` com `404`.
2. **DTOs** (Fase 3) — ✅:
   - `ReporteRequest` e `ReporteResponse` como `record` no pacote `dto`.
   - `@RestControllerAdvice` no pacote `exception` com o formato padrão de erro.
3. **Integração com o front-end** (Fase 4) — ⏳ pendente:
   - Alinhar o contrato com o app.
   - Receber os reportes do app, validar e armazenar; testar ponta a ponta.

---

## 3. Fora do foco (não implementar agora)

- Usuário/cadastro/autenticação (ainda não será colocada); perfil e histórico; denúncias; painel administrativo.
- Geocodificação (OpenStreetMap): já feita no **front**, não é do back.
- Classificação de segurança do bairro: lógica quase pronta, focar depois.