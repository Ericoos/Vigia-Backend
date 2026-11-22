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

> As seções 2 e 3 acima são de 13/09/2026. O escopo atual do MVP está em `tarefas-mvp-restante.md`.

---

## 4. Confirmação por telefone no lugar do e-mail (decisão de 16/11/2026)

**O que muda no relatório (§6.1.1, Cadastro de Usuário):**

- **R03** passa de "Confirmação de e-mail" para **"Confirmação de telefone"**. Texto sugerido:

  > **R03 – Confirmação de telefone:** o sistema deve exigir a confirmação do número de telefone por meio de um código numérico de 6 dígitos, com tempo de expiração, enviado ao número cadastrado. Prioridade: Alta. Rastreabilidade: UC001, CN001.

- **Novo requisito** (sugestão de número: R02.1, ou o próximo livre na lista). Texto sugerido:

  > **R02.1 – Validação de telefone único:** o sistema deve garantir que cada número de telefone esteja vinculado a uma única conta, impedindo que uma mesma pessoa crie várias contas. Prioridade: Alta. Rastreabilidade: UC001, CN001.

- **R07 a R10** (recuperação de senha e aviso de alteração) **continuam por e-mail**: não mudam.

**Como fica no MVP:**

- O envio de SMS é pago, então o MVP usa o **modo desenvolvimento**: em vez de enviar o SMS, a API escreve o código no console (log). Para a apresentação, o código é lido no console.
- A troca para um provedor real de SMS (Twilio, Zenvia ou similar) é só trocar a implementação do envio, sem mexer no fluxo.
- Vale citar no relatório como **limitação conhecida do protótipo**.

---

## 5. Funcionalidade futura: verificação de identidade em 2 níveis (para "Trabalhos Futuros" do relatório)

Texto pronto para adaptar no relatório:

### 5.1 Motivação

Em uma plataforma colaborativa, a confiança nos reportes depende de cada pessoa ter uma única conta. Sem essa garantia, um usuário mal-intencionado pode criar várias contas para publicar reportes falsos, inflar ou derrubar a classificação de um bairro e burlar a reputação automática (R15) e o sistema de denúncias. A confirmação por telefone, adotada no protótipo, reduz esse risco; a verificação por CPF o reduz ainda mais, porque cada CPF corresponde a uma única pessoa.

### 5.2 Níveis de usuário

| Nível | Como é obtido | Permissões |
|---|---|---|
| **Usuário base** | Confirmou o número de telefone | Utiliza o aplicativo normalmente (mapa, reportes, bairros, perfil), mas **não pode realizar reportes nem denunciar** reportes ou usuários |
| **Usuário verificado** | Confirmou o CPF e a data de nascimento | Utiliza **todas** as funcionalidades disponíveis ao usuário comum |

A verificação por CPF é feita **uma única vez**. Depois dela, o usuário realiza reportes normalmente, apenas autenticado; o CPF não é solicitado novamente.

### 5.3 Fluxo 1: tornar-se usuário base (confirmação de telefone)

1. O usuário se cadastra com nome, e-mail, telefone, data de nascimento e senha.
2. O sistema recusa o cadastro se o telefone já estiver vinculado a outra conta.
3. O sistema gera um código de 6 dígitos com validade de 10 minutos e o envia por SMS.
4. O usuário informa o código no aplicativo.
5. Se o código estiver correto e dentro do prazo, a conta passa a ser **usuário base**; o código é descartado.
6. O sistema limita as tentativas erradas (ex.: 5) e os reenvios (ex.: 1 por minuto).

### 5.4 Fluxo 2: tornar-se usuário verificado (confirmação de CPF)

1. O usuário base acessa "Verificar conta", lê o aviso sobre o uso do CPF e concede o consentimento.
2. Informa o CPF e a data de nascimento. O aplicativo valida os dígitos verificadores antes do envio, para dar retorno imediato.
3. Os dados são enviados à API **por HTTPS**.
4. A API, em ordem:
   1. recusa se a conta já for verificada;
   2. valida os dígitos verificadores do CPF (a validação do aplicativo não substitui esta);
   3. confere a data de nascimento com a informada no cadastro;
   4. calcula o identificador `cpfHash = HMAC-SHA256(chave secreta, CPF)`;
   5. recusa se o `cpfHash` já estiver vinculado a outra conta;
   6. grava o `cpfHash` e muda a conta para **usuário verificado**.
5. **O CPF é descartado** ao fim da requisição: não é gravado no banco, em logs nem devolvido na resposta. O `cpfHash` também nunca é devolvido ao aplicativo; a resposta informa apenas o nível da conta.
6. Ao realizar reportes e denúncias, a API verifica apenas se a conta é de nível verificado.

### 5.5 Proteção do dado (LGPD)

- **Minimização:** o CPF nunca é armazenado; apenas o `cpfHash`.
- **Pseudonimização:** o `cpfHash` é gerado com HMAC e uma chave secreta guardada fora do banco de dados e do código-fonte (variável de ambiente do servidor). Sem a chave, o `cpfHash` não permite descobrir o CPF. Um hash simples (como SHA-256 sem chave) não seria suficiente, porque o número de CPFs possíveis é pequeno o bastante para ser testado por tentativa.
- **Finalidade:** o `cpfHash` é usado exclusivamente para garantir uma conta verificada por pessoa.
- **Base legal:** consentimento do usuário, coletado na tela de verificação.
- **Direito de exclusão:** ao excluir a conta, o `cpfHash` é apagado.
- **Segurança:** tráfego por HTTPS, limite de tentativas de verificação (para impedir que alguém descubra quais CPFs têm conta), mensagens de erro e logs sem o CPF, e backup da chave secreta separado do banco.
- O `cpfHash` continua sendo dado pessoal (pseudonimizado, não anônimo), pois quem possui a chave pode associá-lo ao CPF.

### 5.6 Limitações e evolução

- Sem consulta a uma base oficial, o sistema confirma que o CPF é válido e único, mas **não que pertence à pessoa** que o informou. A evolução natural é a integração com a API Consulta CPF do **Serpro** (serviço oficial e pago), feita sempre pela API do servidor, nunca pelo aplicativo.
- Não existe API oficial gratuita para essa consulta; serviços não oficiais não devem ser usados, porque implicariam compartilhar dados pessoais com terceiros sem base legal.
- A chave secreta não pode ser trocada sem que todos os usuários repitam a verificação, já que o CPF não é armazenado.
- Seria necessário um canal de contestação para o titular cujo CPF tenha sido usado indevidamente por outra pessoa.

---

## 6. Funcionalidade futura: central de notificações (decisão de 21/11/2026)

Texto pronto para adaptar em "Trabalhos Futuros" do relatório:

### 6.1 Motivação

Hoje o usuário não fica sabendo do que acontece com o próprio conteúdo depois de publicá-lo. O aviso de status de publicação (R28) já é atendido no protótipo: como a publicação é instantânea (R27), a própria resposta do envio confirma a publicação (ou informa o erro) e o aplicativo exibe a mensagem ao usuário. Além disso, o relatório prevê apenas o e-mail de punição (R70). Uma aba de notificações no aplicativo reúne esses avisos em um só lugar e dá transparência ao autor sobre a moderação.

### 6.2 O que a aba mostra

| Evento | Aviso ao usuário |
|---|---|
| Reporte publicado (R28) | "Seu reporte foi publicado." (no protótipo, o aviso já aparece na tela logo após o envio) |
| Reporte denunciado | "Um reporte seu foi denunciado." **Sem identificar quem denunciou** (o denunciante permanece anônimo). |
| Denúncia analisada pelo administrador | "A denúncia sobre seu reporte foi arquivada" ou "foi aceita". |
| Reporte removido pelo administrador (R61) | "Seu reporte foi removido", com a justificativa (R66). |
| Punição aplicada (R44, R70) | "Seu perfil foi punido", com acesso ao recurso. |
| Recurso analisado (R72) | "Seu recurso foi aceito" ou "rejeitado". |

### 6.3 Regras

- **Toda ação do usuário que precisa de retorno gera retorno:** a mensagem na tela no momento da ação e, quando a ação tiver um resultado posterior, uma notificação na aba.
- Cada notificação guarda: usuário destinatário, tipo, texto, data e se já foi lida.
- O aviso de denúncia **nunca** mostra quem denunciou. Para dificultar que o autor deduza o denunciante pelo horário, os avisos podem ser agrupados (ex.: "seu reporte recebeu denúncias").
- **Recurso:** a partir da notificação, o usuário pode recorrer, mas apenas depois de uma **decisão do administrador** (punição ou remoção do reporte), conforme R71/R72. Uma denúncia ainda pendente não tem decisão a contestar, então não gera recurso.
- O e-mail de punição (R70) continua existindo; a aba é um canal a mais, dentro do aplicativo.

### 6.4 Por que ficou fora do protótipo

Depende do painel administrativo, das punições e dos recursos (R44, R61, R69–R72), que já estão fora do escopo do MVP, e de uma tela nova no aplicativo.

### 6.5 Como seria no back-end (referência)

- Entidade `Notificacao` (`usuarioDestinatario`, `tipo` com enum `TipoNotificacao`, `texto`, `dataNotificacao`, `lida`).
- Os serviços que geram os eventos (denúncia, moderação, recurso) criam a notificação ao salvar.
- Rotas: `GET /api/usuarios/me/notificacoes` (mais recentes primeiro) e `PATCH /api/usuarios/me/notificacoes/{id}/lida`.

---

## 7. Nota do bairro, mapa e reputação (decisões de 21/11/2026)

**Para o relatório (§6, Classificação de Bairro / R65 e R15):**

- **Nota do bairro (R65):** escala de 0 a 5. Todo bairro começa com 5,0; cada reporte recente altera a nota pelo peso da sua subcategoria (insegurança diminui, segurança aumenta). Internamente a nota pode passar de 5 (os reportes de segurança formam uma reserva), mas o usuário vê no máximo 5.
- **Reportes recentes:** o mapa e a nota consideram apenas os reportes dos **últimos 30 dias** (janela móvel). Reportes mais antigos deixam de aparecer no mapa, mas continuam registrados e no histórico do autor.
- **Reputação do usuário (R15):** é **permanente**, no modelo do "karma" do Reddit. As denúncias recebidas continuam contando mesmo depois que o reporte sai do mapa ou é excluído, o que impede que um usuário apague reportes denunciados para limpar a própria reputação. Fórmula: `avaliação = 5 × (reportes + 1) / (reportes + 1 + denúncias recebidas)` — toda conta começa com 5, cada denúncia recebida reduz a nota e cada novo reporte a recupera aos poucos.

## 8. Funcionalidade futura: arquivamento de reportes (para "Trabalhos Futuros")

- **Motivação:** com o crescimento da base, manter todos os reportes na tabela principal deixa as consultas do mapa mais pesadas e mistura dados ativos com dados históricos.
- **Proposta:** uma rotina diária move os reportes com mais de N meses (a definir) e suas denúncias para tabelas de arquivo (`reporte_arquivado`, `denuncia_arquivada`). A tabela principal fica só com dados recentes; nada é perdido.
- **Reputação preservada:** o total de denúncias recebidas fica guardado no próprio usuário (contador), então o arquivamento não altera a reputação.
- **Evolução:** política de retenção alinhada à LGPD (exclusão definitiva após prazo), particionamento da tabela por mês e índices nas colunas usadas pelo mapa.
- **Por que ficou fora do protótipo:** só traz benefício com grande volume de dados, que não existe na fase de testes; a base (contador de denúncias e índices) já fica pronta no MVP.
