# Plano de Tarefas do MVP — Entrega 30/11/2026

Plano de todas as tarefas até a entrega do MVP do Vigia. Cada tarefa vira um cartão no Trello.

**Etiquetas**: `[Back]` API (eu) · `[Front]` app React Native · `[Banco]` MySQL · `[Doc]` relatório/diagramas/docs do repo.

**Ordem combinada**: primeiro o lado do **usuário** completo (banco → login/cadastro → perfil → bairro); depois a integração front ↔ back; o **admin** só começa se sobrar tempo.

> Base: `RELATORIO_VIGIA_4SEM.pdf` (texto em `docs/relatorio-vigia.txt`) — MVP da Figura 9, requisitos do §6.1, banco do §6.3/Apêndice D e próximos passos do cap. 7.

---

## Fase 5 — Banco de dados e chaves (02/11 a 06/11)

| # | Tarefa | Etiqueta |
|---|---|---|
| 5.1 | ✅ `Categoria`: a chave primária passa a ser o próprio enum `TipoCategoria` (`SEGURANCA`, `INSEGURANCA`), sem id numérico | Back |
| 5.2 | ✅ `Subcategoria`: a chave primária passa a ser o enum `TipoSubcategoria` (`ROUBO`, `FURTO`...), com FK para a categoria pelo enum | Back |
| 5.3 | ✅ `Reporte` referencia categoria/subcategoria pelo enum; `ReporteRequest` passa a receber `"categoria": "INSEGURANCA", "subcategoria": "ROUBO"` em vez de `idCategoria`/`idSubcategoria` | Back |
| 5.4 | ✅ Ajustar `DataSeeder`, `GET /api/categorias`, validação "sub pertence à categoria" e apagar o H2 (`data/`) para recriar o esquema | Back |
| 5.5 | Alinhar a lista de subcategorias com o app (o app mostra "atividade suspeita", que não existe no enum) | Back + Front |
| 5.6 | ✅ `Usuario` alinhado ao script do relatório: `senhaHash`, `telefone`, `avatar`, `status`, `avaliacao`, `dataInicio`, `dataFim` (sem `ativo`: o `status` já diz se a conta está ativa) | Back |
| 5.7 | ✅ Seed dos bairros de São Caetano do Sul na tabela `bairro` | Back |
| 5.8 | Decidir se a avaliação de 1 a 5 estrelas (`avaliacaoGravidade`) volta ao `Reporte` ou sai das telas | Back + Front |
| 5.9 | Atualizar `vigia-specs-endpoints.md` com o contrato novo e avisar o front | Doc |

## Fase 6 — Cadastro e login (09/11 a 13/11)

| # | Tarefa | Etiqueta |
|---|---|---|
| 6.1 | Adicionar Spring Security + JWT e hash de senha com bcrypt (RNF10) | Back |
| 6.2 | `POST /api/auth/cadastro` — nome, e-mail único, data de nascimento, senha com as mesmas regras do app (8+ caracteres, maiúscula, minúscula, número, especial) | Back |
| 6.3 | `POST /api/auth/login` — devolve o token JWT | Back |
| 6.4 | Regras de acesso: ver mapa/reportes sem login; criar reporte só logado (R49, R50) | Back |
| 6.5 | Reporte associado ao autor logado (R25) e `autorNome` na resposta | Back |
| 6.6 | Remover o `UsuarioController` antigo, que expõe a entidade e a senha | Back |
| 6.7 | Ligar as telas de login e cadastro à API e guardar o token | Front |

**Regras do cadastro/login decididas em 10/11/2026** (feito em duas etapas: A = cadastro e login puros; B = regras):
validação dos campos (nome, e-mail, data no passado, senha 8–72 com maiúscula, minúscula, número e especial), e-mail único (409), e-mail normalizado (minúsculo, sem espaços), idade mínima (13 anos — ajustável), erro de login genérico (401 "E-mail ou senha inválidos"), conta nova `USUARIO_ATIVO` com `dataInicio`, só conta ativa faz login (403).

**Decisões de escopo (10/11/2026)**: **CAPTCHA fora** (o rate limiting + a confirmação de e-mail já seguram robôs). **Confirmação de e-mail por código** será feita, mas **depois do MVP se não houver tempo**. **Termos de uso / LGPD ainda não.**

## Fase 7 — Perfil do usuário (16/11 a 18/11)

| # | Tarefa | Etiqueta |
|---|---|---|
| 7.1 | `GET /api/usuarios/me` — dados do usuário logado | Back |
| 7.2 | `PUT /api/usuarios/me` — editar nome, telefone e foto (R11) | Back |
| 7.3 | `PUT /api/usuarios/me/senha` — trocar senha exigindo a atual (R12) | Back |
| 7.4 | `GET /api/usuarios/me/reportes` — histórico de reportes (R13) | Back |
| 7.5 | Desativar conta com confirmação de senha (R62) — se der tempo | Back |
| 7.6 | Tela de perfil ligada à API | Front |

## Fase 8 — Bairro (18/11 a 22/11)

| # | Tarefa | Etiqueta |
|---|---|---|
| 8.1 | Reporte recebe o bairro (o app já descobre pelo endereço) e grava a associação | Back + Front |
| 8.2 | Classificação automática de segurança do bairro com os pesos dos enums (R65) | Back |
| 8.3 | `GET /api/bairros` (lista com classificação) e `GET /api/bairros/{id}` (classificação + reportes do bairro) (R64) | Back |
| 8.4 | Filtros em `GET /api/reportes`: `?categoria=` e `?bairro=` (R41) | Back |
| 8.5 | Tela de bairro e escolha do local do reporte no mapa/GPS | Front |

## Fase 9 — Integração front ↔ back e MySQL (23/11 a 25/11)

| # | Tarefa | Etiqueta |
|---|---|---|
| 9.1 | Liberar acesso pela rede (`server.address=0.0.0.0`) e testar pelo IP do PC no celular | Back |
| 9.2 | Teste ponta a ponta: cadastro → login → criar reporte → mapa → bairro → perfil | Back + Front |
| 9.3 | Migrar do H2 para o MySQL (`mysql-connector-j` + `application.properties`) | Back + Banco |
| 9.4 | Testes automáticos dos endpoints principais | Back |

## Fase 10 — Admin e denúncias (só se sobrar tempo)

| # | Tarefa | Etiqueta |
|---|---|---|
| 10.1 | Denunciar reporte com justificativa (R43) | Back + Front |
| 10.2 | Denunciar perfil de outro usuário com justificativa (R68) | Back + Front |
| 10.3 | Admin: login, listar denúncias pendentes, arquivar ou punir (R69, R44) | Back + Front |

Ficam como **trabalho futuro** no relatório: confirmação de e-mail por código (se não couber no MVP), termos de uso/LGPD, CAPTCHA, recuperação de senha por e-mail, exclusão de conta, editar/excluir reporte, recursos de punição e integração com notícias.

## Fase 11 — Relatório e apresentação (26/11 a 30/11)

| # | Tarefa | Etiqueta |
|---|---|---|
| 11.1 | Gerar o script SQL a partir das entidades e substituir o Apêndice D | Doc + Banco |
| 11.2 | Refazer MER e DER (§6.3) a partir do banco real | Doc + Banco |
| 11.3 | Atualizar diagramas de classes, pacotes (`dto`, `exception`, `config`, `security`) e sequência (login) | Doc |
| 11.4 | Atualizar o cap. 7 com login, perfil e bairro funcionando | Doc |
| 11.5 | Atualizar `AGENTS.md`, planejamento, guia e `mudancas-docs-relatorio.md` para o escopo novo | Doc |
| 11.6 | Dados de exemplo para a demonstração e roteiro da apresentação | Back + Front |
| 11.7 | Ensaio da apresentação | Todos |

**Congelamento**: nenhuma funcionalidade nova depois de **27/11**.
