# Tarefas restantes do MVP — Back-end (a partir de 15/11/2026)

Base: requisitos do §6.1.1 do relatório (`docs/relatorio-vigia.txt`, p. 54–62), Quadro 4 (Denuncia reportes), Apêndice D (`denuncia_reporte`) e o código da `main` em `fc6d156` + login (T16).
Entrega: **30/11/2026**. Cada tarefa vira um cartão no Trello.

**Atualização 16/11:** a confirmação de conta (R03) passa a ser por **telefone**, em modo desenvolvimento. A verificação por **CPF** ficou como **funcionalidade futura** (texto para o relatório em `mudancas-docs-relatorio.md` §5).

**Escopo confirmado em 15/11:** entram denúncia de reporte, confirmação de e-mail, recuperação de senha, editar e excluir reporte e desativação de conta. Usuários **não** avaliam reportes de 1 a 5 (não há esse requisito). A "avaliação geral" que aparece no perfil público (Figura 20) é a **reputação automática do R15**.

**Fora do MVP:** denúncia de perfil (R68), painel/ações de admin (R44, R61, R67, R69), punições e recursos (R70–R72), notícias (R31–R32), exclusão permanente de conta (R63), busca textual (R42), agrupamento no mapa (R18).

| # | Tarefa | Requisitos | Quem | Prazo | Depende de |
|---|---|---|---|---|---|
| 1 | ✅ Regras de acesso (feita 15/11) | R49, R50 | Erick | 15/11 | — |
| 2 | ✅ Reporte ligado ao autor (feita 15/11) | R25 | Erick | 15/11 | 1 |
| 3 | ✅ Perfil: controller + histórico + validações (publicada em `271ce38`) | R11, R12, R13 | Murilo | 18/11 | 1, 2 |
| 4 | ✅ Desativar conta (publicada em `a5d220f`) | R62 | Murilo | 18/11 | 3 |
| 5 | Editar reporte (uma vez, só a descrição) — código pronto, falta testar | R29 | Erick | 19/11 | 2 |
| 6 | Excluir reporte (soft delete) — código pronto, falta testar | R30 | Erick | 19/11 | 2 |
| 7 | ✅ Reporte com bairro + filtro + detalhe do bairro (publicada em `e4641da`) | R64, R41 | Murilo | 20/11 | 2 |
| 8 | ✅ Denúncia de reporte (commit `6b99a2c`, 21/11) | R43, R45, R66 | Erick | 20/11 | 2 |
| 9 | Reputação automática (karma permanente) — código pronto, falta testar | R15 | Erick | 22/11 | 8 |
| 10a | Nota do bairro começa em 5, peso ÷ 50, exibida entre 0 e 5 — código pronto, falta testar | R65 | Erick | 21/11 | 7 |
| 10 | Janela de 30 dias (nota e mapa) + reporte com 3+ denúncias fora da nota | R65, R41 | Murilo | 22/11 | 8, 10a |
| 11 | ~~Base de envio de e-mail~~ cortada (21/11): e-mail em modo desenvolvimento = linha no log | — | — | — | — |
| 12 | Confirmação de telefone (modo desenvolvimento, cria o `CodigoService`) | R03 (alterado) | Murilo | 22/11 | — |
| 13 | Recuperação de senha (código no log, modo desenvolvimento) | R07, R08, R09 | Murilo (passada pelo Erick em 21/11) | 22/11 | 12 (reaproveita o `CodigoService`) |
| 14 | Aviso de senha alterada (log, modo desenvolvimento) | R10 | Murilo | 22/11 | 3, 13 |
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

**Onde:** novos `model/Denuncia.java`, `enums/StatusDenuncia.java`, `enums/MotivoDenuncia.java`, `exception/AcessoNegadoException.java`, `repository/DenunciaRepository.java`, `service/DenunciaService.java`, `controller/DenunciaController.java`, `dto/DenunciaRequest.java`, `dto/DenunciaResponse.java`

**Entidade `Denuncia`** (Apêndice D, tabela `denuncia`; nomes definidos pelo Erick em 21/11):
- `idDenuncia` (Long), `usuarioDenunciante` (`@ManyToOne Usuario`), `reporteAlvo` (`@ManyToOne Reporte`), `motivo` (enum `MotivoDenuncia`), `descricao` (TEXT), `dataDenuncia` (`OffsetDateTime`, São Paulo), `statusDenuncia` (enum `StatusDenuncia`: `PENDENTE`, `ARQUIVADA`, `ACEITA`, `REJEITADA` — nasce `PENDENTE`).
- `MotivoDenuncia`: `INFORMACAO_FALSA`, `LOCAL_INCORRETO`, `CONTEUDO_ENGANOSO`, `CONTEUDO_OFENSIVO`, `SPAM`, `OUTRO` (no `OUTRO`, a justificativa obrigatória é o texto livre). Conferir com a tela do Fabrício.
- Repositório: `DenunciaRepository.existsByUsuarioDenunciante_IdAndReporteAlvo_IdReporte` (regra de uma denúncia por usuário e reporte).

**Endpoint:** `POST /api/reportes/{id}/denuncias` (com token) → 201 `DenunciaResponse(id, idReporte, motivo, descricao, dataDenuncia, status)`

**Regras:**
- `motivo` obrigatório; `descricao` obrigatória, 10–500 caracteres (R66 — justificativa obrigatória).
- Reporte inexistente ou `EXCLUIDO` → 404.
- Denunciar o próprio reporte → 403.
- Um usuário só denuncia o mesmo reporte uma vez → 409.
- `ReporteResponse` **não** mostra quem denunciou (o denunciante fica anônimo para o autor).
- A análise das denúncias (admin) fica fora do MVP: todas ficam `PENDENTE`.

**Testes:** denunciar reporte de outro 201 → de novo 409 → o próprio 403 → sem justificativa 400 → sem token 401 → reporte inexistente 404; no H2, linha em `DENUNCIA` com denunciante e reporte.

---

## TAREFA 9 — Reputação automática do usuário (R15)

**O que é:** a "avaliação geral" do perfil é calculada pelo sistema, não dada por outros usuários. "Quanto menos denúncias, maior a avaliação."

**Onde:** `service/UsuarioService.java` (ou novo `ReputacaoService`), `DenunciaRepository`, `ReporteRepository`, `UsuarioResponse`

**Regras:**
- Campo já existe: `Usuario.avaliacao` (`Double`).
- Recalcular sempre que o usuário receber uma denúncia (tarefa 8) e quando publicar ou excluir um reporte.
- **Fórmula implementada (21/11, validar com o grupo):** `avaliacao = 5 × (reportes + 1) / (reportes + 1 + denúncias recebidas)`, 1 casa decimal. Conta nova → 5,0; 1 reporte e 1 denúncia → 3,3; 10 reportes e 1 denúncia → 4,6; 1 reporte e 3 denúncias → 1,7. Nunca negativa. `reportes` conta **todos** os reportes já feitos (inclusive excluídos) e `denúncias` vem do contador permanente, então excluir um reporte não muda a nota.
- `UsuarioResponse` ganha `avaliacao`.

**Implementação:** `service/ReputacaoService` (`registrarDenunciaRecebida`, `recalcular`), `Usuario.totalDenunciasRecebidas`, `ReporteRepository.countByUsuario_Id`; chamado em `DenunciaService.denunciar` (autor do reporte) e `ReporteService.criar`; cadastro começa com `avaliacao = 5.0` e contador 0; `UsuarioResponse.avaliacao` (nulo aparece como 5,0).

**Testes:** conta nova → 5,0 → cria 1 reporte → 5,0 → recebe 1 denúncia → 3,3 → exclui o reporte → continua 3,3 → cria outro reporte → 3,8.


**Karma permanente (decisão de 21/11, igual ao Reddit):** a reputação **nunca se perde** com o tempo, nem quando o reporte sai do mapa (30 dias), é excluído ou (no futuro) arquivado. Implementar com o contador `Usuario.totalDenunciasRecebidas`, somado em `DenunciaService.denunciar` a cada denúncia recebida; a reputação é calculada a partir dele, e não contando as linhas de `denuncia` por status ou data do reporte. Isso fecha a brecha de excluir o reporte para limpar a ficha e deixa a base pronta para o arquivamento futuro.
---

## TAREFA 10a — Nota do bairro começa em 5 (R65) — Erick, 21/11

**Regra do grupo (21/11), igual ao rating da Uber:** o bairro começa com **5,0**; cada reporte soma o peso que já existe em `TipoCategoria.calcularClassificacao`, agora dividido por **50** (ROUBO `(1 − 5)/50 = −0,08`; PRESENÇA POLICIAL `(1 + 5)/50 = +0,12` — cerca de 12 roubos para cair 1 ponto).
```
notaInterna = 5,0 + soma de calcularClassificacao(reporte)  → salva em Bairro.classificacaoSeguranca (pode passar de 5)
notaExibida = notaInterna limitada entre 0 e 5, 1 casa      → o que vai no JSON
```
- Reportes de segurança viram **"gordura"**: no back a nota passa de 5; para o usuário trava em 5.
- Bairro sem reportes → 5,0 (hoje é `null`).
- **Onde:** `TipoCategoria` (`/ 100.0` → `/ 50.0`), `BairroService.recalcularClassificacao` (começa em `5.0`) e a conversão para `BairroResponse`/`BairroDetalheResponse` no `BairroService` (limite 0–5).

---

## TAREFA 10 — Janela de 30 dias e denúncias na nota (R65, R41) — Murilo

**O que é:** a Figura 12 do relatório diz que a nota vem "dos reportes **recentes**", e o R65 fala em "reportes **e denúncias**". Depende da 10a (Erick), que já deixa a nota começando em 5.

**Regras:**
- **Janela móvel de 30 dias**: só contam reportes `VISIVEIS` com `dataReporte` nos últimos 30 dias. Vale **para a nota e para o mapa** (`GET /api/reportes`, filtros e detalhe do bairro). Sem rotina agendada: é só filtro por data; o reporte antigo **não é apagado**, continua no banco e no histórico do perfil.
- Consultas novas no `ReporteRepository` com `...AndDataReporteAfter(OffsetDateTime)`. Centralizar o "agora menos 30 dias" num lugar só (ex.: constante `DIAS_NO_MAPA = 30`).
- Reporte com **3 ou mais denúncias** não entra na nota (`DenunciaRepository`, ex.: `countByReporteAlvo_IdReporte`).
- Recalcular o bairro do reporte quando ele receber uma denúncia (`DenunciaService.denunciar`).
- Como a nota muda com o tempo, `GET /api/bairros` também recalcula ao listar (são 15 bairros).
- Só muda **por dentro** de `recalcularClassificacao`: `criar` e `excluir` do `ReporteService` já chamam ele.

**Testes:** reporte com `dataReporte` de 31 dias atrás (ajustar no H2) some do mapa e da nota, mas continua em `/api/usuarios/me/reportes` → reporte com 3 denúncias sai da nota → excluir um reporte recalcula.

---

## TAREFA 11 — Base de envio de e-mail

**O que é:** infraestrutura usada pelas tarefas 13 e 14 (a 12 passou a ser por telefone).

**Onde:** `pom.xml` (`spring-boot-starter-mail`), `application.properties`, `application-local.properties.example`, novo `service/EmailService.java`

**Regras:**
- Configuração SMTP com variáveis de ambiente, **fora do Git** como a chave do JWT: `spring.mail.host`, `port`, `username=${MAIL_USUARIO}`, `password=${MAIL_SENHA}`, TLS ligado.
- **Decidir o provedor** (Gmail do grupo com senha de app envia de verdade; Mailtrap só para testes).
- `EmailService.enviar(String para, String assunto, String texto)` com `JavaMailSender`.
- Se o envio falhar, registrar no log e **não** derrubar a requisição.
- Atualizar o `guia-desenvolvimento.md` com como configurar o e-mail local.

**Testes:** subir a API com as variáveis; um envio de teste chega na caixa configurada.

---

## TAREFA 12 — Confirmação de telefone (R03, alterado em 16/11)

> **Mudou em 16/11:** a confirmação passou de e-mail para **telefone**, e o relatório será atualizado (ver `mudancas-docs-relatorio.md` §4). O SMS roda em **modo desenvolvimento**: o código aparece no console da API em vez de ser enviado. Esta tarefa **não depende mais da 11**.

**O que é:** a conta só loga depois de confirmar o telefone com o código de 6 dígitos gerado no cadastro.

**Onde:** `model/Usuario.java`, `dto/CadastroRequest`, `AuthService`, `AuthController`, `repository/UsuarioRepository`, novo `service/SmsService.java` (versão de desenvolvimento que só escreve no log), novos DTOs `ConfirmarTelefoneRequest` (`email`, `codigo`) e `ReenviarCodigoRequest` (`email`)

**Regras:**
- `Usuario` ganha `Boolean telefoneConfirmado`, `codigoVerificacao`, `codigoExpiraEm` e `tentativasCodigo`. Os campos de código têm nome genérico porque a tarefa 13 (recuperação de senha) usa os mesmos. Usuários antigos ficam com `telefoneConfirmado = null` e continuam entrando (só `false` bloqueia).
- **Telefone único:** cadastro com telefone já usado → 409 "Telefone já cadastrado" (`existsByTelefone` no repositório).
- Cadastro: gera código aleatório de 6 dígitos (`SecureRandom`), validade de **10 min**, e chama `smsService.enviar(telefone, codigo)`. No modo desenvolvimento, isso só faz `log.info` com o telefone e o código.
- `POST /api/auth/confirmar-telefone` `{ email, codigo }` → 204. Código errado ou vencido → 400. Depois de **5 erros**, o código é invalidado e é preciso reenviar.
- Ao confirmar: `telefoneConfirmado = true` e apaga código, validade e tentativas.
- Login com telefone não confirmado → 403 "Confirme seu telefone antes de entrar" (checado depois da senha, como o status).
- `POST /api/auth/reenviar-codigo` `{ email }` → 204 sempre (não revela se o e-mail existe); gera código novo. No máximo 1 reenvio por minuto.
- Usuários já existentes no H2: marcar como confirmados ao migrar, ou recadastrar.
- O `SmsService` fica isolado: trocar para um provedor real de SMS depois é só trocar a implementação.

**Testes:** cadastro (código aparece no console) → login 403 → código errado 400 → código certo 200 → login 200 → mesmo código de novo 400 → código vencido 400 → 5 erros invalidam o código → telefone repetido no cadastro 409.

---

## TAREFA 13 — Recuperação de senha (R07, R08, R09) — Murilo

**O que é:** o usuário esquece a senha, recebe um **código de 6 dígitos** com prazo e define uma senha nova. **Modo desenvolvimento** (decisão de 21/11): o "e-mail" é uma linha no log da API com o código, sem envio real — igual à confirmação de telefone.

**Depende de:** tarefa 12 (Murilo), que cria o `CodigoService` (gerar, expirar, contar tentativas e validar código). A 13 reaproveita ele; não criar outro.

**Onde:** `AuthService`, `AuthController`, novos `dto/EsqueciSenhaRequest.java` (`email`), `dto/RedefinirSenhaRequest.java` (`email`, `codigo`, `novaSenha`)

**Regras:**
- `POST /api/auth/esqueci-senha` `{ email }` → 204 **sempre** (não revela se o e-mail existe). Se existir: gera o código pelo `CodigoService` (validade **30 min**, R08) e escreve no log: `[EMAIL DEV] Para <email>: seu código de recuperação é 123456`.
- `POST /api/auth/redefinir-senha` `{ email, codigo, novaSenha }` → 204. `novaSenha` com a **mesma regra de força** do cadastro (R09). Código errado, vencido ou já usado → 400; 5 erros invalidam o código.
- Depois de redefinir, o código é descartado (não pode ser usado de novo).

**Testes:** pedir com e-mail existente e inexistente → ambos 204 (só o existente gera log) → redefinir com o código do log 204 → login com a senha nova 200 → mesmo código de novo 400 → senha fraca 400 → código errado 400.

---

## TAREFA 14 — Aviso de senha alterada (R10) — Murilo

**O que é:** avisar o usuário sempre que a senha mudar. **Modo desenvolvimento:** o aviso é uma linha no log (`[EMAIL DEV] Para <email>: sua senha foi alterada em <data/hora>. Se não foi você, recupere sua senha.`).

**Onde:** `UsuarioService.trocarSenha` (tarefa 3) e `AuthService` redefinir senha (tarefa 13)

**Regras:** depois de salvar a senha nova, escrever o aviso no log com data/hora (São Paulo). Uma falha no aviso não desfaz a troca.

**Testes:** trocar a senha pelo perfil e pela recuperação → o aviso aparece no log nos dois casos.

---

## TAREFA 15 — Acesso pela rede e MySQL

**O que é:** o app no celular precisa alcançar a API, e o banco do projeto é MySQL.

**Onde:** `application.properties`, `pom.xml`

**Regras:**
- `server.address=0.0.0.0`; testar `http://<IP do PC>:8080/api/reportes` no celular (liberar a porta 8080 no Firewall se precisar).
- `mysql-connector-j` + conexão com senha em variável de ambiente (`DB_SENHA`), fora do Git.
- Repetir o roteiro completo do Postman: cadastro → confirmar telefone → login → criar/editar/excluir reporte → denunciar → bairro → perfil → desativar.


**Índice (decisão de 21/11):** na migração, adicionar no `Reporte` um índice nas colunas `status`, `data_reporte` e `bairro_id` (`@Table(indexes = @Index(columnList = "status, data_reporte, bairro_id"))`), usadas pelo mapa e pela nota. **Docker:** a API inteira + MySQL em `docker-compose`, para o Fabrício rodar no servidor dele; senhas num `.env` fora do Git.
---

## Do lado do front (Fabrício) — para combinar

Logout (R06) é só apagar o token no app — a API não precisa de rota. Telas e chamadas novas: confirmar telefone (código de 6 dígitos)/reenviar, esqueci senha + deep link, editar/excluir reporte (botões só para o autor e editar só se `editado = false`), denunciar reporte, perfil com avaliação, desativar conta, detalhe do bairro com reportes.

## Decisões abertas antes de codar

1. Fórmula da reputação (tarefa 9).
2. Regra das denúncias na classificação do bairro (tarefa 10).
3. Lista de motivos de denúncia (tarefa 8) — conferir com a tela do app.
4. Provedor de e-mail (tarefa 11).
5. ~~Caminho de bairros (tarefa 7)~~ — definido por Murilo em 19/11/2026: `/api/bairros` (plural); avisar Fabrício.
6. Rate limiting (T40 do Trello) não é requisito do relatório: fazer só se sobrar tempo.
