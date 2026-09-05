# Especificação Técnica dos Endpoints (MVP) - Aplicativo Vigia

Este documento contém a especificação técnica detalhada para o desenvolvimento da API REST do MVP do aplicativo **Vigia** (Spring Boot). Ele foi estruturado sob a metodologia **Spec-Driven Development (SDD)**, utilizando os requisitos, casos de uso e regras de negócio extraídos diretamente do relatório do projeto.

Cada endpoint abaixo corresponde a uma tela especificada em `vigia-specs-telas.md`, na mesma ordem.

> ⚠️ **STATUS: PROPOSTA.** Contrato derivado de `mockReports.json`, `mockBairros.json` e `vigia-specs-telas.md`. **Precisa ser validado com a pessoa do front-end antes da implementação.**

---

## Como usar este documento:
1. Escolha o endpoint que deseja desenvolver no momento.
2. Leia a seção correspondente (Requisitos, Contrato, Camadas, Regras).
3. Copie o **Prompt Pronto para Copiar** correspondente e envie para a IA.
4. Teste o `GET` no navegador e o `POST` no Postman ou Insomnia.
5. Faça o commit no Git ao validar o funcionamento antes de passar para o próximo.

---

## 1. Contexto Global da API

Para manter a consistência em todos os endpoints, siga as convenções abaixo.

```
Base URL (dev):  http://localhost:8080/api
Content-Type:    application/json
Datas:           ISO-8601 UTC (2026-08-28T10:30:00Z)
Identificadores: sem acentos nem cedilha nos nomes dos campos
```

| Código | Quando |
|---|---|
| `200 OK` | Consulta ou atualização bem-sucedida |
| `201 Created` | Recurso criado |
| `204 No Content` | Exclusão bem-sucedida |
| `400 Bad Request` | Validação falhou |
| `401 Unauthorized` | Credenciais inválidas |
| `404 Not Found` | Recurso inexistente |

**Formato padrão de erro** (produzido pelo `@RestControllerAdvice` do pacote `exception`):

```json
{
  "timestamp": "2026-09-05T14:22:10Z",
  "status": 400,
  "erro": "Requisicao invalida",
  "mensagem": "O campo email e obrigatorio",
  "caminho": "/api/usuarios"
}
```

---

## 2. ESPECIFICAÇÕES DETALHADAS POR ENDPOINT

### ENDPOINT 1: CADASTRO E AUTENTICAÇÃO (`controller/AuthController.java`, `controller/UsuarioController.java`)

*Atende a **TELA 1: LOGIN E CADASTRO**.*

#### A. Requisitos e Casos de Uso Vinculados
*   **Requisitos Funcionais**: **R01** (Cadastro de Cidadão), **R04** (Autenticação/Login).
*   **Casos de Uso**: Mantém Usuário.
*   **Regra de Negócio**: O acesso ao aplicativo e a visualização do mapa requerem autenticação prévia.

#### B. Contrato HTTP

**`POST /api/usuarios`** — cadastro

```json
{
  "nome": "Fernando Domingues",
  "email": "fernando@email.com",
  "senha": "123456",
  "dataNascimento": "2000-05-14",
  "telefone": "11999998888"
}
```

**`POST /api/auth/login`** — login

```json
{ "email": "fernando@email.com", "senha": "123456" }
```

**Saída dos dois (`201` / `200`)**

```json
{
  "idUsuario": 101,
  "nome": "Fernando Domingues",
  "email": "fernando@email.com"
}
```

> ⚠️ `avatar` e `avaliacao` **não** pertencem ao `Usuario` — ficam em `PerfilPublico` (a implementar, relação 1-1). Por isso não saem neste contrato.

#### C. Camadas Envolvidas
*   `model/UsuarioBase.java` → `@MappedSuperclass` (molde): id, nome, email, senha, dataNascimento, status (`TipoUsuario` — **sem tabela própria**).
*   `model/Usuario.java` → `@Entity extends UsuarioBase` com os próprios (telefone, dataCadastro).
*   `repository/UsuarioRepository.java` → `JpaRepository<Usuario, Long>` com `Optional<Usuario> findByEmail(String email)`.
*   `service/UsuarioService.java` → `cadastrar()`, `autenticar()`.
*   `dto/` → `UsuarioRequest` (record), `LoginRequest` (record), `UsuarioResponse` (record).

#### D. Regras de Negócio e Validações
1.  **E-mail único**: rejeitar cadastro com `400` se `findByEmail` já retornar um registro.
2.  **Validação de entrada** via Bean Validation: `@NotBlank` em nome, email e senha; `@Email` no email; `@Past` na data de nascimento.
3.  **A senha nunca sai no JSON de resposta.** O `UsuarioResponse` simplesmente não a inclui.
4.  **Login simplificado**: comparação direta de senha, sem hash e sem token. **Não é seguro** — declarar como tal na monografia.
5.  `dataCadastro` recebe a data atual, definido no Service, não vindo do cliente.

---

#### 📥 PROMPT PRONTO PARA COPIAR (ENDPOINT 1):
> "Estou desenvolvendo o back-end em Spring Boot 4.1.1 (Java 21) do app de segurança comunitária 'Vigia'. Preciso implementar o cadastro e o login de usuários.
>
> Siga estas especificações:
> 1. Crie o molde `UsuarioBase` (`@MappedSuperclass`) em `model`, com: id (Long, `@GeneratedValue`), nome, email, senha, dataNascimento (LocalDate) e status (`TipoUsuario`). Crie `Usuario` (`@Entity extends UsuarioBase`) com telefone e dataCadastro (LocalDateTime).
> 2. Crie `UsuarioRepository` estendendo `JpaRepository<Usuario, Long>` com o método derivado `findByEmail`.
> 3. Crie os records `UsuarioRequest`, `LoginRequest` e `UsuarioResponse` no pacote `dto`. O `UsuarioResponse` **não** deve conter a senha.
> 4. Crie `UsuarioService` com `cadastrar()` (rejeitando e-mail duplicado) e `autenticar()` (comparação direta de senha, sem Spring Security).
> 5. Crie `UsuarioController` com `POST /api/usuarios` e `AuthController` com `POST /api/auth/login`.
> 6. Aplique Bean Validation nos DTOs de entrada.
>
> Explique cada anotação do Spring que aparecer pela primeira vez e me mostre uma camada de cada vez, começando pela Entity."

---

### ENDPOINT 2: LISTAGEM DE REPORTES (`controller/ReporteController.java`)

*Atende a **TELA 2: MAPA INTERATIVO**.*

#### A. Requisitos e Casos de Uso Vinculados
*   **Requisitos Funcionais**: **R16** (Visualizar Mapa com Ocorrências), **R17** (Filtragem por Categoria/Bairro), **R60** (Visualizar Detalhes do Reporte).
*   **Casos de Uso**: Realiza Buscas no Mapa.
*   **Regra de Negócio**: o mapa carrega os marcadores diferenciados visualmente por classificação e gravidade; o app precisa de todos os dados do pin numa única requisição.

#### B. Contrato HTTP

**`GET /api/reportes`** — parâmetros opcionais `?bairro={id}`, `?categoria=Seguranca|Inseguranca`

```json
[
  {
    "idReporte": 1,
    "idUsuario": 101,
    "autorNome": "Fernando Domingues",
    "titulo": "Rua com Iluminacao Inadequada",
    "descricao": "Varios postes apagados na rua de tras da USCS.",
    "latitude": -23.6182,
    "longitude": -46.5645,
    "endereco": "Rua Santo Antonio, Centro, Sao Caetano do Sul - SP",
    "tipoCategoria": "Inseguranca",
    "tipoSubcategoria": "Iluminacao Publica Ruim",
    "avaliacaoGravidade": 4,
    "bairroNome": "Centro",
    "dataReporte": "2026-08-28T10:30:00Z"
  }
]
```

**`GET /api/reportes/{id}`** — mesmo objeto, sem o array.

#### C. Camadas Envolvidas
*   `model/Reporte.java`, `model/Categoria.java`, `model/Subcategoria.java`, `model/Bairro.java`.
*   `repository/ReporteRepository.java` → `findByBairroId`, `findBySubcategoriaCategoriaId`.
*   `service/ReporteService.java` → `listar()` com os filtros opcionais.
*   `dto/ReporteResponse.java` → o record que achata os relacionamentos.

#### D. Regras de Negócio e Validações
1.  **O JSON é desnormalizado de propósito.** `autorNome`, `tipoCategoria`, `tipoSubcategoria` e `bairroNome` saem como texto, não como ids. No banco são relacionamentos `@ManyToOne`; achatar é papel do DTO de resposta. Isso evita que o app faça várias requisições para desenhar um pin.
2.  **Reportes com status `EXCLUIDO` ou `RASCUNHO` não aparecem** na listagem pública.
3.  Filtros são opcionais e combináveis; sem parâmetros, devolve todos os publicados.

---

#### 📥 PROMPT PRONTO PARA COPIAR (ENDPOINT 2):
> "Preciso do endpoint de listagem de reportes da API do Vigia, que alimenta os pins do mapa no app.
>
> Siga estas especificações:
> 1. Crie as `@Entity` `Reporte`, `Categoria`, `Subcategoria` e `Bairro` no pacote `model`, com os relacionamentos mapeados por `@ManyToOne` e referência de objeto (nunca id solto do tipo int).
> 2. Crie `ReporteRepository` com métodos derivados para filtrar por bairro e por categoria.
> 3. Crie o record `ReporteResponse` no pacote `dto` achatando os relacionamentos: `autorNome`, `tipoCategoria`, `tipoSubcategoria` e `bairroNome` devem sair como String.
> 4. Crie `ReporteService.listar()` recebendo os filtros opcionais de bairro e categoria, excluindo reportes com status EXCLUIDO ou RASCUNHO.
> 5. Crie `ReporteController` com `GET /api/reportes` (aceitando `@RequestParam` opcionais) e `GET /api/reportes/{id}`.
>
> Não use `nativeQuery` — o banco final é MySQL e o código precisa continuar portável. Explique como a conversão da Entity para o DTO funciona."

---

### ENDPOINT 3: CRIAÇÃO DE REPORTE (`controller/ReporteController.java`)

*Atende a **TELA 3: CRIAR REPORTE**.*

#### A. Requisitos e Casos de Uso Vinculados
*   **Requisitos Funcionais**: **R19** (Classificação do Reporte), **R20** (Seleção de Subcategoria), **R22** (Preenchimento de Informações do Reporte).
*   **Casos de Uso**: Criar Reportes (API).
*   **Regras de Negócio**: todo reporte deve ter classificação ("Segurança" ou "Insegurança"), subcategoria correspondente, gravidade de 1 a 5 e descrição obrigatória.

#### B. Contrato HTTP

**`POST /api/reportes`**

```json
{
  "idUsuario": 101,
  "titulo": "Tentativa de Assalto",
  "descricao": "Individuo suspeito abordando pedestres.",
  "latitude": -23.6234,
  "longitude": -46.5598,
  "idSubcategoria": 3,
  "avaliacaoGravidade": 5
}
```

**Saída `201`**: o reporte criado, no formato do Endpoint 2.

**`GET /api/categorias`** — alimenta os seletores da tela

```json
[
  {
    "id": 1,
    "tipoCategoria": "Inseguranca",
    "subcategorias": [
      { "id": 3, "tipoSubcategoria": "Iluminacao Publica Ruim" },
      { "id": 4, "tipoSubcategoria": "Assalto/Roubo" }
    ]
  }
]
```

#### C. Camadas Envolvidas
*   `service/ReporteService.criar()`, `service/GeocodificacaoService` (ver Endpoint 6).
*   `controller/CategoriaController.java` → `GET /api/categorias`.
*   `dto/ReporteRequest.java` (record).

#### D. Regras de Negócio e Validações
1.  **`titulo` e `descricao` obrigatórios** — `@NotBlank`.
2.  **`avaliacaoGravidade` entre 1 e 5** — `@Min(1) @Max(5)`.
3.  **A categoria não vem do cliente.** O request manda `idSubcategoria`; a categoria é derivada da subcategoria no Service. Isso impede a combinação inválida de categoria "Segurança" com subcategoria "Assalto".
4.  **`endereco` e `idBairro` são derivados no servidor** a partir de latitude e longitude, via geocodificação reversa do OpenStreetMap (Endpoint 6). O cliente não envia nenhum dos dois.
5.  `dataReporte` recebe a data atual e `status` inicia em `PUBLICADO`, ambos definidos no Service.

---

#### 📥 PROMPT PRONTO PARA COPIAR (ENDPOINT 3):
> "Preciso do endpoint de criação de reportes da API do Vigia.
>
> Siga estas especificações:
> 1. Crie o record `ReporteRequest` no pacote `dto` com: idUsuario, titulo, descricao, latitude, longitude, idSubcategoria e avaliacaoGravidade.
> 2. Aplique Bean Validation: `@NotBlank` em titulo e descricao, `@Min(1)` e `@Max(5)` em avaliacaoGravidade, `@NotNull` nas coordenadas.
> 3. Em `ReporteService.criar()`, derive a categoria a partir da subcategoria informada — o cliente não envia a categoria, para impedir combinações inválidas.
> 4. Preencha `dataReporte` com a data atual e `status` com PUBLICADO dentro do Service, nunca a partir do request.
> 5. Adicione `POST /api/reportes` ao `ReporteController`, devolvendo `201 Created` com o reporte no formato de resposta da listagem.
> 6. Crie também `CategoriaController` com `GET /api/categorias`, devolvendo cada categoria com suas subcategorias aninhadas.
>
> Explique por que a validação fica no DTO e não na Entity."

---

### ENDPOINT 4: DETALHE DO BAIRRO (`controller/BairroController.java`)

*Atende a **TELA 4: DETALHE DO BAIRRO**.*

#### A. Requisitos e Casos de Uso Vinculados
*   **Requisitos Funcionais**: **R64** (Visualizar Visão Geral do Bairro), **R17** (Filtragem por Bairro).
*   **Regra de Negócio**: o sistema deve calcular automaticamente e exibir uma nota consolidada de segurança de cada bairro de São Caetano do Sul, com base nas avaliações dos usuários locais.

#### B. Contrato HTTP

**`GET /api/bairros`**

```json
[
  { "id": 1, "nome": "Centro", "classificacaoSeguranca": 4.2 }
]
```

**`GET /api/bairros/{id}`**

```json
{
  "id": 1,
  "nome": "Centro",
  "classificacaoSeguranca": 4.2,
  "totalSeguranca": 8,
  "totalInseguranca": 4,
  "reportes": []
}
```

#### C. Camadas Envolvidas
*   `model/Bairro.java` → id, nome, classificacaoSeguranca.
*   `repository/BairroRepository.java`.
*   `service/BairroService.java` → `calcularClassificacao()`, o método que vem do diagrama de classes.

#### D. Regras de Negócio e Validações
1.  **`calcularClassificacao()` é regra de negócio e mora no Service**, nunca na `@Entity` `Bairro`. É o exemplo mais direto da camada `REGRAS_NEGOCIO` do diagrama de pacotes.
2.  A nota é derivada das `avaliacaoGravidade` dos reportes do bairro — reportes de "Segurança" elevam a nota, os de "Insegurança" reduzem. **A fórmula exata ainda precisa ser definida com o grupo.**
3.  `totalSeguranca` e `totalInseguranca` são contadores derivados, não colunas do banco.
4.  `GET /api/bairros/{id}` inexistente devolve `404`.

---

#### 📥 PROMPT PRONTO PARA COPIAR (ENDPOINT 4):
> "Preciso dos endpoints de bairro da API do Vigia, que alimentam a tela de visão geral do bairro.
>
> Siga estas especificações:
> 1. Crie a `@Entity` `Bairro` com id, nome e classificacaoSeguranca (double).
> 2. Crie `BairroRepository` e `BairroService`.
> 3. Implemente `calcularClassificacao()` **no Service**, não na Entity — é regra de negócio. A nota deriva das avaliações de gravidade dos reportes do bairro.
> 4. Crie o record `BairroDetalheResponse` com nome, classificacaoSeguranca, totalSeguranca, totalInseguranca e a lista de reportes daquele bairro.
> 5. Crie `BairroController` com `GET /api/bairros` e `GET /api/bairros/{id}`, devolvendo 404 para bairro inexistente.
>
> Antes de escrever o cálculo, me pergunte qual fórmula usar — ela ainda não foi definida pelo grupo."

---

### ENDPOINT 5: PERFIL E HISTÓRICO (`controller/UsuarioController.java`)

*Atende a **TELA 5: PERFIL DO CIDADÃO**.*

#### A. Requisitos e Casos de Uso Vinculados
*   **Requisitos Funcionais**: **R11** (Visualizar Informações do Cidadão), **R13** (Exibir Histórico de Reportes Próprios).
*   **Casos de Uso**: Mantém Perfil.
*   **Regra de Negócio**: o cidadão deve poder visualizar seus dados, sua classificação de reputação na comunidade e seu histórico pessoal de interações.

#### B. Contrato HTTP

**`GET /api/usuarios/{id}`**

```json
{
  "idUsuario": 101,
  "nome": "Fernando Domingues",
  "email": "fernando@email.com",
  "avatar": null,
  "avaliacao": 4.5,
  "dataCadastro": "2026-08-01T09:00:00Z",
  "totalReportes": 7
}
```

**`GET /api/usuarios/{id}/reportes`** — lista no formato do Endpoint 2, incluindo rascunhos e reportes sob moderação (é o próprio autor consultando).

#### C. Camadas Envolvidas
*   `model/PerfilPublico.java`, `model/Historico.java` (conforme o diagrama de classes).
*   `service/UsuarioService.buscarPerfil()`, `service/ReporteService.listarPorUsuario()`.

#### D. Regras de Negócio e Validações
1.  **O histórico do próprio usuário inclui todos os status** (publicado, rascunho, sob análise), diferente da listagem pública do Endpoint 2.
2.  `avaliacao` é a reputação do usuário e vem da entidade `PerfilPublico` (relação 1-1 com `Usuario`), **não** do `Usuario`. Enquanto `PerfilPublico` não existir, `avaliacao` e `avatar` ficam fora dos contratos. O selo textual ("Cidadão Protetor", "Colaborador Bronze") é decisão de apresentação do app — a API devolve só o número.
3.  A senha nunca aparece na resposta.
4.  Usuário inexistente devolve `404`.

---

#### 📥 PROMPT PRONTO PARA COPIAR (ENDPOINT 5):
> "Preciso dos endpoints de perfil da API do Vigia, que alimentam a tela de perfil do cidadão.
>
> Siga estas especificações:
> 1. Crie `GET /api/usuarios/{id}` devolvendo nome, email, avatar, avaliacao, dataCadastro e totalReportes (contador derivado, não coluna do banco).
> 2. Crie `GET /api/usuarios/{id}/reportes` devolvendo o histórico do usuário no mesmo formato da listagem pública, mas **incluindo** os status rascunho e sob análise.
> 3. Garanta que a senha nunca apareça em nenhuma resposta.
> 4. Devolva 404 para usuário inexistente, usando o `@RestControllerAdvice` do pacote `exception`.
>
> Explique a diferença entre esta listagem e a do endpoint público de reportes."

---

### ENDPOINT 6: GEOCODIFICAÇÃO REVERSA — OPENSTREETMAP (`integration/NominatimClient.java`)

*Base do sistema. Sem mapa não há aplicativo.*

#### A. Requisitos e Casos de Uso Vinculados
*   **Casos de Uso**: API Integração (Open Source Map).
*   **Diagrama de Pacotes**: `INTEGRAÇÕES EXTERNAS` → pacote `integration`.
*   **Regra de Negócio**: todo reporte é georreferenciado e precisa de um endereço legível e de um bairro associado. O usuário fornece apenas as coordenadas; o endereço é resolvido pelo servidor.

#### B. Contrato HTTP

Uso interno, chamado por `ReporteService.criar()`. Opcionalmente exposto para o app:

**`GET /api/geo/reverso?lat=-23.6182&lon=-46.5645`**

```json
{
  "endereco": "Rua Santo Antonio, Centro, Sao Caetano do Sul - SP",
  "bairroNome": "Centro",
  "idBairro": 1
}
```

**Serviço externo consumido**: Nominatim (OpenStreetMap) —
`https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}`

#### C. Camadas Envolvidas
*   `integration/NominatimClient.java` → o cliente HTTP (`RestClient` do Spring).
*   `integration/dto/NominatimResponse.java` → o record que mapeia a resposta do Nominatim.
*   `service/GeocodificacaoService.java` → traduz a resposta do Nominatim para o bairro do sistema.

#### D. Regras de Negócio e Validações
1.  **O Nominatim exige um `User-Agent` identificando a aplicação.** Requisições sem ele são bloqueadas.
2.  **Limite de uso**: a instância pública permite cerca de 1 requisição por segundo. Aceitável para o protótipo; em produção exigiria cache ou instância própria.
3.  **A integração pode falhar** (rede fora, serviço indisponível, coordenada sem endereço). O reporte **não pode deixar de ser salvo por causa disso** — em caso de falha, gravar `endereco` como nulo e registrar o erro. A criação do reporte não depende de serviço externo.
4.  **Mapeamento do bairro**: o Nominatim devolve o bairro como texto livre. É preciso casar esse texto com a tabela `Bairro` do sistema. Bairro não encontrado deve cair num registro genérico, nunca quebrar a requisição.
5.  Coordenadas fora de São Caetano do Sul: definir com o grupo se são rejeitadas ou aceitas.

---

#### 📥 PROMPT PRONTO PARA COPIAR (ENDPOINT 6):
> "Preciso integrar a API do Vigia com o OpenStreetMap para geocodificação reversa — converter latitude e longitude no endereço e no bairro do reporte.
>
> Siga estas especificações:
> 1. Crie `NominatimClient` no pacote `integration`, usando o `RestClient` do Spring, consumindo `https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}`.
> 2. Envie um cabeçalho `User-Agent` identificando a aplicação — o Nominatim bloqueia requisições sem ele.
> 3. Crie o record `NominatimResponse` mapeando apenas os campos que usamos do JSON de retorno.
> 4. Crie `GeocodificacaoService` que chama o cliente e casa o bairro retornado com a tabela `Bairro` do sistema.
> 5. **Trate a falha**: se o Nominatim estiver fora do ar ou não retornar endereço, o reporte ainda assim deve ser salvo, com `endereco` nulo e o erro registrado em log. A criação do reporte não pode depender de serviço externo.
>
> Explique como o `RestClient` funciona e por que a falha da integração não deve derrubar a requisição principal."

---

## 3. Pendências deste Contrato

1. **Validar tudo com o front-end.** Nada aqui foi acordado ainda.
2. **`avaliacaoGravidade`**: existe em `mockReports.json`, na Tela 3 (R19/R22) e neste contrato, mas **não existe** na classe `Reporte` do diagrama de classes. Corrigir o diagrama.
3. **Fórmula de `calcularClassificacao()`** do bairro não definida.
4. **CORS**: liberar a origem do app no backend, senão toda requisição falha.
5. **`localhost` não funciona no celular.** O app precisará do IP da máquina na rede local (`192.168.x.x`).
6. **Coordenadas fora de São Caetano do Sul**: rejeitar ou aceitar?
7. **Códigos de requisito** (R01, R04, R11, R13, R16, R17, R19, R20, R22, R60, R64) foram herdados de `vigia-specs-telas.md`. Conferir com o relatório se há requisitos exclusivos de back-end ainda não mapeados aqui.
