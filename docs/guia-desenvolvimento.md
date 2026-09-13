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
- **H2** em modo arquivo (`MODE=MySQL`) — ponte temporária
- **MySQL** — banco final (de outra pessoa, ainda não existe)

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

Validando o console H2 (web): navegador em `localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:file:./data/vigia`
- User Name: `sa`
- Password: (vazio)

> `localhost:8080` sem caminho mostra uma página de erro Whitelabel — é normal, ainda não há rota na raiz. Acesse sempre caminhos como `/h2-console` ou `/api/...`.

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