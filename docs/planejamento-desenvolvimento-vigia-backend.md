# Plano de Desenvolvimento do Back-End - Foco: Módulo Reporte

Este documento orienta o desenvolvimento do back-end do **Vigia** com **foco apenas no módulo de Reporte**: o app cria um reporte, a API recebe, valida e armazena no banco, e devolve a listagem para os pins do mapa.

> Stack, estrutura de pastas e regras de tradução ficam no `AGENTS.md` da raiz. Este documento trata de *plano*: o que já está pronto, modelo de dados do reporte, escopo dos endpoints, fases de desenvolvimento e protocolo de estado.

---

## 1. O que já está pronto (não mexer)

- **Fase 0** concluída: projeto gerado (Java 21, Spring Boot 4.1.1, Maven), pacotes criados, `docs/` e `AGENTS.md` existem, repositório no GitHub, banco H2 configurado e app rodando em 8080.
- **Fase 1** concluída para o `Reporte` — fatia vertical de ponta a ponta:
  - `model/Reporte` (`@Entity`) e o enum `StatusReporte` (`RASCUNHO`, `PUBLICADO`, `EXCLUIDO`)
  - `repository/ReporteRepository` (`JpaRepository<Reporte, Long>`)
  - `service/ReporteService` com `listar()` e `salvar()`
  - `controller/ReporteController` com `GET /api/reportes` e `POST /api/reportes` (aceita a entidade bruta, sem validação)
- Apoio já modelado no `model`: `Categoria`, `Subcategoria`, `Bairro`.

## 2. Modelo de dados do Reporte

| Entidade | Campos | Observações |
|---|---|---|
| **Reporte** | `idReporte`, `titulo`, `descricao`, `latitude`, `longitude`, `endereco`, `status`, `dataReporte` | `status` inicia em `PUBLICADO` e `dataReporte` = agora, definidos no Service. `usuario` é opcional (módulo de usuário fora de foco). |
| **StatusReporte** | enum `RASCUNHO`, `PUBLICADO`, `EXCLUIDO` | a listagem pública mostra só `PUBLICADO` |
| **Subcategoria** | `id`, `tipoSubCategoria`, `categoria` | referenciada pelo reporte |
| **Categoria** | `id`, `tipoCategoria`, `subcategorias` | derivada da subcategoria no Service |
| **Bairro** | `id`, `nome` | preenchido em fase futura, sem foco agora |

> `avaliacaoGravidade` foi **removida** — não existe mais na classe `Reporte`.

## 3. Escopo dos endpoints (módulo Reporte)

1. **`POST /api/reportes`** — o app envia um novo reporte; a API valida e armazena.
2. **`GET /api/reportes`** — lista os reportes `PUBLICADO` para os pins do mapa.
3. **`GET /api/reportes/{id}`** — detalhe de um reporte.
4. **`GET /api/categorias`** — alimenta os seletores do formulário de reporte (categoria com subcategorias aninhadas).

Contratos detalhados, com JSON de exemplo e prompts prontos, em `docs/vigia-specs-endpoints.md`.

## 4. Fases de Desenvolvimento

### Fase 2: Finalizar a classe e os métodos de `Reporte`
1. `ReporteService.criar()` — recebe os dados, monta a entidade, define `status = PUBLICADO` e `dataReporte = agora`, salva no banco.
2. `POST /api/reportes` — validar entrada: `titulo` e `descricao` obrigatórios, `latitude`/`longitude` obrigatórias.
3. `GET /api/reportes` — retornar apenas reportes com status `PUBLICADO`.
4. `GET /api/reportes/{id}` — detalhe, com `404` quando não existir.

### Fase 3: Organizar DTOs
1. Definir o padrão de DTOs com a equipe: `reporteRequest` de entrada e `ReporteResponse` de saída (a explicação de o que são e como fazer é repassada em conversa, não está na spec).
2. Criar `ReporteRequest` (entrada) e `ReporteResponse` (saída) no pacote `dto`, como `record`.
3. Aplicar Bean Validation nos DTOs de entrada.
4. Criar `@RestControllerAdvice` no pacote `exception` produzindo o formato padrão de erro.

### Fase 4: Conexão com o front-end
1. Alinhar o contrato da API com o app (React Native) — o que o app envia e o que recebe.
2. Receber os reportes enviados pelo app, validar e armazenar.
3. Testar o fluxo ponta a ponta (emulador/celular → API → banco).
4. Combinar com o time o endereço base acessível pelo celular: **não** é `localhost` — o app acessa o IP da máquina na rede local (`192.168.x.x`).

### Fora do foco (não implementar agora)
- Usuário, cadastro e autenticação (autenticação **ainda não** será colocada).
- Classificação de segurança do bairro (lógica quase pronta — foco **depois**).
- Geocodificação reversa (já feita no **front**, não é do back).
- Perfil e histórico do usuário.
- Denúncias, punições, painel administrativo.
- Migração para MySQL (o banco da equipe ainda não existe).

## 5. Protocolo de Registro de Estado (Sincronização Diária)

Este protocolo preserva o contexto entre sessões. Ao final de toda sessão, gere/atualize `docs/ESTADO_DESENVOLVIMENTO.md` no template abaixo:

```markdown
# Estado Atual do Desenvolvimento - Back-End Vigia

## 📍 1. Onde Paramos (Ponto de Controle)
- **Fase/Passo Atual**: [Indicar ex: Fase 2, método criar()]
- **Última classe/endpoint finalizado**: [Descrever o que foi concluído na sessão]

## 🛠️ 2. Modificações Efetuadas nesta Sessão
- **Arquivos Criados**:
  - `caminho/do/Arquivo.java` -> [Função deste arquivo]
- **Arquivos Modificados**:
  - `caminho/do/Arquivo.java` -> [O que foi mudado e por quê]

## 📋 3. Estado das Dependências instaladas
- [Listar as dependências novas adicionadas ao pom.xml, se houver]

## 🚀 4. Próximos Passos Imediatos para a Próxima Sessão
1. [Próxima tarefa 1]
2. [Próxima tarefa 2]

## 🧠 5. Contexto Técnico para o Próximo Chat
*(Resumo compacto da arquitetura atual, entidades mapeadas, endpoints expostos e decisões tomadas).*
```