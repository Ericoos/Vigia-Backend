# Mudanças na Documentação do Relatório e no que Vamos Implementar

Documento de controle do que precisa mudar na documentação do relatório (`.docx`, requisitos, diagramas) e do que vamos implementar no código. Os `.drawio` **não são editados aqui** — a intenção de mudança fica registrada para o grupo aplicar nos diagramas.

---

## 1. Mudanças na documentação do relatório (requisitos, diagramas, etc.)

1. **Requisitos (§3.2)**: a classificação de segurança está descrita como "calculada automaticamente a partir dos **reportes e denúncias** registrados na região". Trocar para "calculada automaticamente a partir das **categorias e subcategorias dos reportes** registrados na região" — denúncia é outro conceito e a fórmula usa só reportes.
2. **Modelo de dados (§6) / Diagrama de classes**: registrar os enums `TipoUsuario`, `StatusReporte`, `TipoCategoria` e `TipoSubcategoria`.
3. **Diagrama de classes**: incluir `avaliacaoGravidade` na classe `Reporte` (existe no mock, na Tela 3 e no contrato, mas não na classe do diagrama).
4. **Diagrama de pacotes**: adicionar `DTO` e `EXCEPTION` na `CAMADA_BACKEND`. `REPOSITORIES` já existe no diagrama e representa o pacote `repository` do código (camada de acesso a dados) — **não** é o banco, que está representado como `BANCO_MYSQL` na `CAMADA_DADOS`.
5. **Relatório**: descrever que `Categoria`/`Subcategoria` são tabelas alimentadas por seed dos enums, e documentar a fórmula da classificação do bairro (Seção 2).

---

## 2. O que vamos implementar

1. **Enums no pacote `model`**:
   - `TipoCategoria`: `SEGURANCA`, `INSEGURANCA`.
   - `TipoSubcategoria`: cada constante tem um **peso** e um método que recebe a `avaliacaoGravidade` do reporte.
   - Subcategorias previstas (proposta, a confirmar): `ASSALTO_ROUBO`, `FURTO`, `VANDALISMO`, `ILUMINACAO_PUBLICA`, `OUTROS`, `PRESENCA_POLICIAL`, `AREA_MOVIMENTADA`, `CAMERAS`, `FATOR_POSITIVO`.

2. **`Categoria` e `Subcategoria` continuam como `@Entity`/tabelas**, alimentadas por **seed a partir dos enums** no boot do app — o Hibernate continua dono do schema.

3. **`calcularClassificacao()` no `BairroService`** (regra de negócio, nunca na `@Entity`):
   - Nota parte de **5,0**, com teto **5,0** e piso **0**, recalculada do zero (não acumula).
   - Contribuição por reporte = `(peso da subcategoria × avaliacaoGravidade) ÷ 10` (uma casa decimal).
   - `SEGURANCA` soma na nota; `INSEGURANCA` subtrai. Presença policial é fator **positivo**.

4. **`avaliacaoGravidade`**: já presente em `mockReports.json`, na Tela 3 e nos contratos; deve existir no `Reporte` do código (alteração de código somente com autorização).

---

## 3. Pendências para fechar (próxima sessão)

- **Tabela de pesos** das subcategorias — ainda não definida.
- Confirmar a lista final de subcategorias de `SEGURANCA`.