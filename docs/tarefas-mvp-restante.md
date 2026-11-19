# Tarefas restantes do MVP — Back-end (a partir de 15/11/2026)

Base: requisitos do §6.1.1 do relatório (`docs/relatorio-vigia.txt`, p. 54–62), Quadro 4 (Denuncia reportes), Apêndice D (`denuncia_reporte`) e o código da `main` em `fc6d156` + login (T16).
Entrega: **30/11/2026**. Cada tarefa vira um cartão no Trello.

**Escopo confirmado em 15/11:** entram denúncia de reporte, confirmação de e-mail, recuperação de senha, editar e excluir reporte e desativação de conta. Usuários **não** avaliam reportes de 1 a 5 (não há esse requisito). A "avaliação geral" que aparece no perfil público (Figura 20) é a **reputação automática do R15**.

**Fora do MVP:** denúncia de perfil (R68), painel/ações de admin (R44, R61, R67, R69), punições e recursos (R70–R72), notícias (R31–R32), exclusão permanente de conta (R63), busca textual (R42), agrupamento no mapa (R18).

| # | Tarefa | Requisitos | Quem | Prazo | Depende de |
|---|---|---|---|---|---|
| 1 | ✅ Regras de acesso (feita 15/11) | R49, R50 | Erick | 15/11 | — |
| 2 | ✅ Reporte ligado ao autor (feita 15/11) | R25 | Erick | 15/11 | 1 |
| 3 | ✅ Perfil: controller + histórico + validações (publicada em `271ce38`) | R11, R12, R13 | Murilo | 18/11 | 1, 2 |
| 4 | ✅ Desativar conta (publicada em `a5d220f`) | R62 | Murilo | 18/11 | 3 |
| 5 | Editar reporte (uma vez) | R29 | Erick | 19/11 | 2 |
| 6 | Excluir reporte | R30 | Erick | 19/11 | 2 |
| 7 | Reporte com bairro + filtro + detalhe do bairro | R64, R41 | Murilo | 20/11 | 2 |
| 8 | Denúncia de reporte | R43, R45, R66 | Erick | 20/11 | 2 |
| 9 | Reputação automática do usuário | R15 | Murilo | 21/11 | 8 |
| 10 | Classificação do bairro considera denúncias | R65 | Murilo | 21/11 | 7, 8 |
| 11 | Base de envio de e-mail | — | Erick | 21/11 | — |
| 12 | Confirmação de e-mail | R03 | Erick | 22/11 | 11 |
| 13 | Recuperação de senha | R07, R08, R09 | Erick | 22/11 | 11 |
| 14 | Aviso de senha alterada | R10 | Murilo | 22/11 | 3, 11, 13 |
| 15 | Acesso pela rede + MySQL | — | Erick + Fernando | 25/11 | todas |

> Tarefas 5, 6 e 8 mexem no `ReporteService`/`ReporteController`; 7 também mexe no `ReporteService.criar`. Para não dar conflito, **Murilo só começa a 7 depois do push da 2**, e Erick faz 5, 6 e 8 em sequência.

---

## TAREFA 1 — Regras de acesso (R49, R50)

**O que é:** quem não está logado vê mapa, reportes, categorias e bairros, mas não cria nada.

**Onde:** `config/SecurityConfig.java`

**Regras:** trocar o `permitAll` geral por:
- livres: `/api/auth/**`, `/h2-console/**`, e `GET` em `/api/reportes/**`, `/api/categorias/**`, `/api/subcategorias/**`, `/api/bairros/**`;
- todo o resto: `authenticated()`.

Sem token numa rota protegida o Spring devolve `401` sozinho (o `oauth2ResourceServer` já está configurado).

**Testes:**
| Requisição | Sem token | Com token |
|---|---|---|
| `GET /api/reportes` | 200 | 200 |
| `POST /api/reportes` | 401 | 201 |
| `POST /api/auth/login` | 200 | 200 |

**Avisar o front:** a partir daqui `POST /api/reportes` exige `Authorization: Bearer <token>`.

---

## TAREFA 2 — Reporte ligado ao autor (R25)

**O que é:** todo reporte guarda quem o criou. O usuário vem do token, nunca do corpo.

**Onde:** `controller/ReporteController.java`, `service/ReporteService.java`, `dto/ReporteResponse.java`

**Regras:**
- Controller: parâmetro `@AuthenticationPrincipal Jwt jwt` no `criar`; `Long idUsuario = Long.valueOf(jwt.getSubject())`.
- Service: `criar(ReporteRequest, Long idUsuario)` busca o `Usuario` (404 se não existir) e faz `reporte.setUsuario(usuario)`.
- Response: acrescentar `Long autorId` e `String autorNome` (`null` para reportes antigos sem autor).

**Testes:** login → POST com token → resposta traz `autorNome`; no H2, `USUARIO_ID` preenchido.

---

## TAREFA 3 — Perfil: controller, histórico e validações (R11, R12, R13)

**O que é:** o Service e os DTOs já existem (`UsuarioService.buscarPorId/atualizar/trocarSenha`). Falta expor por HTTP, criar o histórico e corrigir validações.

**Onde:** novo `controller/UsuarioController.java` (`/api/usuarios`), `service/UsuarioService.java`, `repository/ReporteRepository.java`, `dto/AtualizarUsuarioRequest.java`, `dto/TrocarSenhaRequest.java`

**Endpoints (todos com token; id sempre do `jwt.getSubject()`):**
| Método | Rota | Resposta |
|---|---|---|
| GET | `/api/usuarios/me` | 200 `UsuarioResponse` |
| PUT | `/api/usuarios/me` | 200 `UsuarioResponse` |
| PUT | `/api/usuarios/me/senha` | 204 |
| GET | `/api/usuarios/me/reportes` | 200 `List<ReporteResponse>` |

**Regras:**
- `AtualizarUsuarioRequest`: acrescentar `avatar` (String, opcional — URL/identificador da foto, R11); telefone com a **mesma regex** do cadastro; campos `null` não apagam o valor salvo.
- `TrocarSenhaRequest.novaSenha`: **mesma regra de força** do cadastro (8–30, maiúscula, minúscula, número, especial — R09/R12). Nova senha igual à atual → 400.
- Histórico: método derivado no `ReporteRepository` buscando por `usuario.id` e status diferente de `EXCLUIDO`, mais recente primeiro. Mostra também os reportes editados.

**Testes:** login → GET me 200 → PUT me altera nome → PUT senha com atual errada 400 / fraca 400 / certa 204 → login com a nova senha 200 → criar reporte → GET me/reportes traz o reporte → qualquer rota sem token 401.

---

## TAREFA 4 — Desativar conta (R62)

**O que é:** o usuário desativa a própria conta com confirmação de senha. Os dados ficam; a conta não loga mais.

**Onde:** `UsuarioController`, `UsuarioService`, novo `dto/DesativarContaRequest.java` (`senha` obrigatória)

**Endpoint:** `POST /api/usuarios/me/desativar` → 204

**Regras:**
- Senha errada → 400 `"Senha incorreta"`.
- `status = USUARIO_INATIVO`, `dataFim = agora (America/Sao_Paulo)`.
- Os reportes do usuário continuam no mapa (os dados não são excluídos).
- O login já devolve 403 para conta não ativa.

**Testes:** desativar com senha errada 400 → certa 204 → login 403.

---

## TAREFA 5 — Editar reporte uma única vez (R29)

**O que é:** o autor pode editar um reporte publicado **apenas uma vez**.

**Onde:** `model/Reporte.java`, `ReporteController`, `ReporteService`, novo `dto/EditarReporteRequest.java`, `ReporteResponse`

**Endpoint:** `PUT /api/reportes/{id}` (com token) → 200 `ReporteResponse`

**Regras:**
- Campos editáveis: `titulo` (5–30), `descricao` (20–500), `categoria`, `subcategoria` — mesmas validações do `ReporteRequest`; sub tem que pertencer à categoria (400).
- Localização, data e autor **não** mudam.
- Novo campo no `Reporte`: `boolean editado` (default `false`) e `OffsetDateTime dataEdicao`. Depois da edição, `editado = true`.
- Reporte não encontrado ou `EXCLUIDO` → 404. Não é o autor → 403. Já editado → 409 `"Este reporte já foi editado"`.
- `ReporteResponse` ganha `editado` para o app esconder o botão.

**Exceções novas:** `AcessoNegadoException` (403) — filha de `ApiException`, reaproveitada nas tarefas 6 e 8.

**Testes:** editar o próprio 200 → editar de novo 409 → editar de outro usuário 403 → sem token 401 → id inexistente 404.

---

## TAREFA 6 — Excluir reporte (R30)

**O que é:** o autor exclui o próprio reporte.

**Onde:** `ReporteController`, `ReporteService`

**Endpoint:** `DELETE /api/reportes/{id}` (com token) → 204

**Regras:**
- Exclusão lógica: `status = EXCLUIDO` (o enum já existe). O reporte some do mapa, do detalhe (404), do histórico e da classificação do bairro, mas as denúncias ligadas a ele continuam registradas (R45).
- Não é o autor → 403; já excluído ou inexistente → 404.
- Depois de excluir, recalcular a classificação do bairro do reporte (se tiver bairro).

**Testes:** excluir o próprio 204 → GET detalhe 404 → some de `GET /api/reportes` → excluir de outro 403.

---

## TAREFA 7 — Reporte com bairro, filtro e detalhe do bairro (R64, R41)

**Estado em 19/11/2026:** implementação e testes concluídos, inclusive reteste no H2 local recriado e GETs públicos no plural. Aguardando revisão/autorização de commit e publicação; comunicar o contrato ao Fabrício.

**O que é:** o app manda o bairro que a geocodificação devolveu; a API associa. O detalhe do bairro passa a listar os reportes.

**Onde:** `model/TipoBairro.java`, `dto/ReporteRequest.java`, `dto/ReporteResponse.java`, `ReporteService`, `ReporteController`, `ReporteRepository`, `BairroService`, novo `dto/BairroDetalheResponse.java`

**Regras:**
- `ReporteRequest`: `endereco` e `bairro` (Strings opcionais).
- `TipoBairro.porNome(String)`: compara ignorando acento e maiúscula; tratar "São Caetano" → `JARDIM_SAO_CAETANO`. Bairro fora dos 15 → 400.
- `ReporteResponse`: `endereco` e `bairro` (`TipoBairro`).
- Ao criar reporte com bairro: recalcular a classificação daquele bairro (assim a lista de bairros não fica desatualizada).
- Filtro: `GET /api/reportes?bairro=SANTA_PAULA` (combinável com `?categoria=`).
- `GET /api/bairros/{tipoBairro}` → `BairroDetalheResponse(tipoBairro, nome, classificacaoSeguranca, List<ReporteResponse> reportes)` — só reportes `PUBLICADO`.
- **Caminho definido por Murilo em 19/11/2026:** `/api/bairros` (plural), para lista e detalhe. Comunicar o contrato ao Fabrício.

**Testes:** POST com `"bairro": "Santa Paula"` e com `"santa paula"` → associa `SANTA_PAULA`; `"Inexistente"` → 400; filtro por bairro; detalhe traz os reportes e a nota atualizada.

---

## TAREFA 8 — Denúncia de reporte (R43, R45, R66)

**O que é:** um usuário logado denuncia o reporte de **outro** usuário, com motivo e justificativa. A denúncia fica registrada e alimenta a reputação do autor (tarefa 9) e a classificação do bairro (tarefa 10).

**Onde:** novos `model/DenunciaReporte.java`, `model/StatusDenuncia.java`, `model/MotivoDenuncia.java`, `repository/DenunciaReporteRepository.java`, `service/DenunciaService.java`, `controller/DenunciaController.java`, `dto/DenunciaRequest.java`, `dto/DenunciaResponse.java`

**Entidade `DenunciaReporte`** (Apêndice D, `denuncia_reporte`):
- `id` (Long), `denunciante` (`@ManyToOne Usuario`), `reporte` (`@ManyToOne Reporte`), `motivo` (enum `MotivoDenuncia`), `descricao` (TEXT), `dataDenuncia` (`OffsetDateTime`, São Paulo), `status` (enum `StatusDenuncia`: `PENDENTE`, `ARQUIVADA`, `ACEITA`, `REJEITADA` — nasce `PENDENTE`).
- `MotivoDenuncia`: conferir com a tela "Qual motivo da denúncia" do Fabrício antes de fechar a lista (sugestão: `INFORMACAO_FALSA`, `LOCAL_INCORRETO`, `CONTEUDO_OFENSIVO`, `SPAM`, `OUTRO`).

**Endpoint:** `POST /api/reportes/{id}/denuncias` (com token) → 201 `DenunciaResponse(id, idReporte, motivo, descricao, dataDenuncia, status)`

**Regras:**
- `motivo` obrigatório; `descricao` obrigatória, 10–500 caracteres (R66 — justificativa obrigatória).
- Reporte inexistente ou `EXCLUIDO` → 404.
- Denunciar o próprio reporte → 403.
- Um usuário só denuncia o mesmo reporte uma vez → 409.
- `ReporteResponse` **não** mostra quem denunciou (o denunciante fica anônimo para o autor).
- A análise das denúncias (admin) fica fora do MVP: todas ficam `PENDENTE`.

**Testes:** denunciar reporte de outro 201 → de novo 409 → o próprio 403 → sem justificativa 400 → sem token 401 → reporte inexistente 404; no H2, linha em `DENUNCIA_REPORTE` com denunciante e reporte.

---

## TAREFA 9 — Reputação automática do usuário (R15)

**O que é:** a "avaliação geral" do perfil é calculada pelo sistema, não dada por outros usuários. "Quanto menos denúncias, maior a avaliação."

**Onde:** `service/UsuarioService.java` (ou novo `ReputacaoService`), `DenunciaReporteRepository`, `ReporteRepository`, `UsuarioResponse`

**Regras:**
- Campo já existe: `Usuario.avaliacao` (`Double`).
- Recalcular sempre que o usuário receber uma denúncia (tarefa 8) e quando publicar ou excluir um reporte.
- **Fórmula — decidir com o grupo antes de codar** (o relatório não fixa números). Proposta: escala 0 a 5; usuário sem reportes → `null`; caso contrário `5 − 5 × (denúncias recebidas ÷ reportes publicados)`, limitado entre 0 e 5. Exemplo: 4 reportes e 1 denúncia → 3,75.
- `UsuarioResponse` ganha `avaliacao`.

**Testes:** usuário novo → `null`; 2 reportes sem denúncia → 5,0; recebe 1 denúncia → 2,5.

---

## TAREFA 10 — Classificação do bairro considera denúncias (R65)

**O que é:** o R65 diz que a classificação usa "reportes **e denúncias**". Reporte muito denunciado deve pesar menos.

**Onde:** `service/BairroService.recalcularClassificacao`

**Regras:**
- **Decidir com o grupo a regra** (sugestão: reporte com 3 ou mais denúncias não entra na soma).
- Recalcular o bairro do reporte quando ele receber uma denúncia.

**Testes:** bairro com um reporte de ROUBO → nota negativa; o reporte recebe 3 denúncias → sai da soma.

---

## TAREFA 11 — Base de envio de e-mail

**O que é:** infraestrutura usada pelas tarefas 12, 13 e 14.

**Onde:** `pom.xml` (`spring-boot-starter-mail`), `application.properties`, `application-local.properties.example`, novo `service/EmailService.java`

**Regras:**
- Configuração SMTP com variáveis de ambiente, **fora do Git** como a chave do JWT: `spring.mail.host`, `port`, `username=${MAIL_USUARIO}`, `password=${MAIL_SENHA}`, TLS ligado.
- **Decidir o provedor** (Gmail do grupo com senha de app envia de verdade; Mailtrap só para testes).
- `EmailService.enviar(String para, String assunto, String texto)` com `JavaMailSender`.
- Se o envio falhar, registrar no log e **não** derrubar a requisição.
- Atualizar o `guia-desenvolvimento.md` com como configurar o e-mail local.

**Testes:** subir a API com as variáveis; um envio de teste chega na caixa configurada.

---

## TAREFA 12 — Confirmação de e-mail (R03)

**O que é:** a conta só loga depois de confirmar o e-mail pelo link enviado no cadastro.

**Onde:** `model/Usuario.java`, novos `model/TokenVerificacao.java`, `model/TipoToken.java`, `repository/TokenVerificacaoRepository.java`, `AuthService`, `AuthController`

**Entidade `TokenVerificacao`:** `id`, `token` (UUID aleatório, único), `usuario` (`@ManyToOne`), `tipo` (`CONFIRMACAO_EMAIL`, `RECUPERACAO_SENHA`), `expiraEm`, `usadoEm`. Reaproveitada na tarefa 13.

**Regras:**
- `Usuario` ganha `boolean emailConfirmado` (default `false`).
- Cadastro: cria token `CONFIRMACAO_EMAIL` com validade de 24 h e envia o link `http://<servidor>/api/auth/confirmar-email?token=...`.
- `GET /api/auth/confirmar-email?token=` → 200 `{ "mensagem": "E-mail confirmado" }`; token inexistente, vencido ou já usado → 400.
- Login com e-mail não confirmado → 403 `"Confirme seu e-mail antes de entrar"` (checado depois da senha, como o status).
- `POST /api/auth/reenviar-confirmacao` `{ email }` → 204 sempre (não revela se o e-mail existe); gera token novo.
- Usuários já existentes no H2: marcar como confirmados ao migrar, ou recadastrar.

**Testes:** cadastro → login 403 → abrir o link 200 → login 200 → mesmo link de novo 400 → link vencido 400.

---

## TAREFA 13 — Recuperação de senha (R07, R08, R09)

**O que é:** o usuário pede a recuperação, recebe um link único com prazo e define uma senha nova.

**Onde:** `AuthService`, `AuthController`, novos `dto/EsqueciSenhaRequest.java` (`email`), `dto/RedefinirSenhaRequest.java` (`token`, `novaSenha`)

**Regras:**
- `POST /api/auth/esqueci-senha` `{ email }` → 204 **sempre** (não revela se o e-mail existe). Se existir: token `RECUPERACAO_SENHA` com validade de **30 min** e e-mail com o link (R07, R08).
- O link abre o app: `vigia://redefinir-senha?token=...` (deep link do Expo — combinar com o Fabrício).
- `POST /api/auth/redefinir-senha` `{ token, novaSenha }` → 204. `novaSenha` com a **mesma regra de força** do cadastro (R09). Token inexistente, vencido ou usado → 400.
- Ao redefinir: marca o token como usado e invalida os outros tokens de recuperação do usuário.

**Testes:** pedir com e-mail existente e inexistente → ambos 204 → redefinir com token válido 204 → login com a nova 200 → mesmo token de novo 400 → senha fraca 400.

---

## TAREFA 14 — Aviso de senha alterada (R10)

**O que é:** e-mail avisando o usuário sempre que a senha mudar.

**Onde:** `UsuarioService.trocarSenha` (tarefa 3) e `AuthService` redefinir senha (tarefa 13)

**Regras:** depois de salvar a senha nova, `emailService.enviar(...)` com data/hora e o aviso "se não foi você, recupere a senha". Falha no envio não desfaz a troca.

**Testes:** trocar senha pelo perfil e pela recuperação → e-mail chega nos dois casos.

---

## TAREFA 15 — Acesso pela rede e MySQL

**O que é:** o app no celular precisa alcançar a API, e o banco do projeto é MySQL.

**Onde:** `application.properties`, `pom.xml`

**Regras:**
- `server.address=0.0.0.0`; testar `http://<IP do PC>:8080/api/reportes` no celular (liberar a porta 8080 no Firewall se precisar).
- `mysql-connector-j` + conexão com senha em variável de ambiente (`DB_SENHA`), fora do Git.
- Repetir o roteiro completo do Postman: cadastro → confirmar e-mail → login → criar/editar/excluir reporte → denunciar → bairro → perfil → desativar.

---

## Do lado do front (Fabrício) — para combinar

Logout (R06) é só apagar o token no app — a API não precisa de rota. Telas e chamadas novas: confirmar e-mail/reenviar, esqueci senha + deep link, editar/excluir reporte (botões só para o autor e editar só se `editado = false`), denunciar reporte, perfil com avaliação, desativar conta, detalhe do bairro com reportes.

## Decisões abertas antes de codar

1. Fórmula da reputação (tarefa 9).
2. Regra das denúncias na classificação do bairro (tarefa 10).
3. Lista de motivos de denúncia (tarefa 8) — conferir com a tela do app.
4. Provedor de e-mail (tarefa 11).
5. ~~Caminho de bairros (tarefa 7)~~ — definido por Murilo em 19/11/2026: `/api/bairros` (plural); avisar Fabrício.
6. Rate limiting (T40 do Trello) não é requisito do relatório: fazer só se sobrar tempo.
