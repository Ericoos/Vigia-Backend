# Guia de Desenvolvimento — Back-End do Vigia

Este guia é para quem for trabalhar no back-end (API REST) do aplicativo **Vigia**. Ele cobre desde pegar o projeto e rodar, até as regras que não podem ser quebradas. Leia o `AGENTS.md` na raiz também — ele é lido por toda assistente de IA em toda sessão.

---

## 1. O que é o Vigia

Plataforma colaborativa de segurança urbana de São Caetano do Sul (MVP de TCC).
Este repositório é a **API REST** consumida pelo app mobile. Só devolve JSON.

**Foco atual**: módulo de **Reporte** — o app cria um reporte, a API recebe, valida e armazena, e devolve a listagem para os pins do mapa.

Coordenadas de referência: `lat -23.6182, lon -46.5645`.

### Divisão de trabalho

| Frente | Repositório |
|---|---|
| Back-end (API) | este (`Ericoos/Vigia-Backend`) |
| Front-end (React Native + Expo) | separado (`FabricioBucardi/Vigia`) |
| Banco de dados MySQL | ainda não iniciado |

Nunca é compartilhado código entre repositórios — só o **contrato da API** (`vigia-specs-endpoints.md`).

---

## 2. Stack

- **Java 21**
- **Spring Boot 4.1.1** (Spring Web, Spring Data JPA, Validation)
- **Maven** com wrapper (`./mvnw`)
- **Lombok** (gera getters/setters via `@Getter @Setter`)
- **MySQL 8.4** em container Docker (desde 24/11/2026; o H2 saiu do projeto)

---

## 3. Preparando o ambiente

1. **Java 21** instalado (`java -version` diz `21`).
2. **Git** instalado.
3. **IntelliJ IDEA** (qualquer edição). No Community, instale os plugins: `Spring Boot Assistant`, `JPA Support`, `Lombok Builder Helper` — e confirme que o plugin `Lombok` do JetBrains está ativo (é embutido em versões recentes).
4. **Clone** o repositório:
   ```bash
   git clone https://github.com/Ericoos/Vigia-Backend.git
   cd Vigia-Backend
   ```
5. Abra a pasta no IntelliJ como **projeto** (ele detecta o `pom.xml`). Confirme que o painel **Maven** carregou e que `src/main/java` aparece com ícone azul (Source Root).

6. **Crie o arquivo da chave do JWT** (ele não vai para o Git):
   - copie `src/main/resources/application-local.properties.example` para `src/main/resources/application-local.properties`;
   - troque o valor de `JWT_SECRET` por uma chave aleatória com **32 caracteres ou mais**. Para gerar uma no PowerShell:
     ```powershell
     [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Max 256 }))
     ```
   - sem esse arquivo (ou a variável de ambiente `JWT_SECRET`), a API **não sobe** e avisa que falta `JWT_SECRET`. Em produção, a chave vem só da variável de ambiente.
   - no mesmo arquivo, preencha `DB_SENHA` (senha do MySQL local) e `DB_PORTA` (3306, ou 3307 se já houver um MySQL instalado no Windows).

7. **Suba o MySQL** (Docker Desktop aberto), trocando a senha e a porta pelas do passo 6:
   ```powershell
   docker run -d --name vigia-mysql -e MYSQL_ROOT_PASSWORD=<senha> -e MYSQL_DATABASE=vigia -p 127.0.0.1:3307:3306 mysql:8.4
   ```
   Nas próximas vezes basta `docker start vigia-mysql`.

> Dica: se `New → Java Class` não aparecer no menu, o Maven não foi importado. Reabra a pasta escolhendo "Open as Project".

---

## 4. Rodando o projeto

```bash
cd Vigia-Backend
./mvnw spring-boot:run
```

Esperado:
- `Tomcat started on port 8080`
- O programa **fica rodando** (não termina) — é normal. Para parar: `Ctrl+C`.

Consultando o banco: `docker exec -it vigia-mysql mysql -uroot -p vigia` (ou qualquer cliente MySQL em `localhost:<DB_PORTA>`).

> `localhost:8080` sem caminho mostra uma página de erro Whitelabel — é normal, ainda não há rota na raiz. Acesse sempre caminhos como `/api/...`.

---

## 5. Estrutura de pastas

```
vigia-api/
├── docs/                        # Documentação
├── src/main/java/com/uscs/vigia/
│   ├── VigiaApplication.java   # Ponto de entrada — raiz do pacote
│   ├── controller/             # Recebe HTTP, recebe DTOs e devolve JSON
│   ├── service/                # Regras de negócio e conversão DTO ↔ entidade
│   ├── repository/             # Interfaces do Spring Data
│   ├── model/                  # As @Entity
│   ├── dto/                    # Entrada e saída da API (records)
│   ├── exception/              # Tratamento de erros (@RestControllerAdvice)
│   └── config/                 # Configurações sob demanda (ex.: seed)
├── src/main/resources/
│   └── application.properties  # Configuração do banco
└── AGENTS.md                   # Regras persistentes para IA
```

**`VigiaApplication.java` não pode sair da raiz do pacote.** `@SpringBootApplication` varre o próprio pacote e os subpacotes à procura de beans — se for movido, o Spring para de encontrar tudo.

---

## 6. Regras que não podem ser quebradas

1. **Nunca use `nativeQuery`.** O banco final é MySQL e o código precisa continuar portável. Use métodos derivados do Spring Data ou JPQL.
2. **Chave estrangeira vira referência de objeto**, nunca `int`. Ex.: `idSubcategoria` vira um campo `Subcategoria` com `@ManyToOne`.
3. **Métodos de regra de negócio vão para o `@Service`**, nunca para as `@Entity`. Ex.: `criarReporte()`.
4. **Sem acentos nem cedilha em identificadores Java.** `endereço` vira `endereco`.
5. **A entidade não aparece no contrato da API.** Entrada e saída usam **DTOs** (records no pacote `dto`); a conversão DTO ↔ entidade acontece no `@Service`.
6. **Sem SQL escrito à mão.** O schema é gerado pelo Hibernate a partir das `@Entity` (`ddl-auto=update`).

---

## 7. Fluxo de trabalho

- O plano fase a fase está em `docs/planejamento-desenvolvimento-vigia-backend.md`: **Fase 0** (infra — ✅), **Fase 1** (fatia vertical do Reporte — ✅), **Fase 2** (regras de criar/listar — ✅), **Fase 3** (DTOs + validação — ✅), **Fase 4** (integração com o front-end — ⏳ pendente).
- A cada passo: **escreve → roda → confirma → entende → avança**. Um commit por camada que funcionar.
- Todo compromisso (commit/push) precisa de **aprovação explícita** — nada de commitar sozinho.
- A cada interação que alterar o projeto, **atualize `docs/ESTADO_DESENVOLVIMENTO.md`** para registrar onde paramos.

### Mensagens de commit
Em português, concisas, refletindo o estilo do histórico.

---

## 8. Onde estamos agora (ponto de controle)

- **Backend do módulo Reporte concluído (naive v1)**: Fases 0 a 3 ✅.
  - Regras de negócio no `ReporteService` (`criar`, `listar` só PUBLICADO, `listarPorId`).
  - DTOs (`ReporteRequest`/`ReporteResponse`), Bean Validation e erros padronizados (`@RestControllerAdvice`).
  - Seed de `Categoria`/`Subcategoria` no boot (`config/DataSeeder`).
  - Fuso `America/Sao_Paulo` configurado (`application.properties` + criação do reporte).
- **Próximo passo: Fase 4** — integração com o front (contrato via IP da rede + teste ponta a ponta). É a **Tarefa 10** do checklist `tarefas-vigia-naive-v1.md`.
- `avaliacaoGravidade` foi **removida** — não está mais no escopo.

---

## 9. Documentos de referência (na pasta `docs/`)

- `planejamento-desenvolvimento-vigia-backend.md` — escopo, fases e modelo de dados do reporte
- `vigia-specs-endpoints.md` — especificação dos endpoints de reporte (contrato JSON + padrão de DTOs + prompts prontos)
- `mudancas-docs-relatorio.md` — lista de mudanças na documentação do relatório
- `guia-desenvolvimento.md` — este guia de integração para quem entra no time
- `ESTADO_DESENVOLVIMENTO.md` — onde paramos na última sessão
- `diagramas/` — os `.drawio` do TCC

Antes de implementar qualquer entidade ou endpoint, **consulte a spec correspondente**.