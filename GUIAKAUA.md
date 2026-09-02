# GUIA KAUA — Como trabalhar no Back-End do Vigia

Este guia é para você, que vai trabalhar no back-end (API REST) do aplicativo **Vigia**.
Ele cobre desde pegar o projeto e rodar, até as regras que não podem ser quebradas.
Leia o `CLAUDE.md` na raiz também — ele é lido por toda assistente de IA em toda sessão.

---

## 1. O que é o Vigia

Plataforma colaborativa de segurança urbana de São Caetano do Sul (MVP de TCC).
Este repositório é a **API REST** consumida pelo app mobile. Só devolve JSON.

Coordenadas de referência: `lat -23.6182, lon -46.5645`.

### Divisão de trabalho

| Frente | Repositório |
|---|---|
| Back-end (API) — eu e você | este (`Ericoos/Vigia-Backend`) |
| Front-end (React Native + Expo) | separado (`FabricioBucardi/Vigia`) |
| Banco de dados MySQL | ainda não iniciado |

Nunca é compartilhado código entre repositórios — só o **contrato da API** (`docs/vigia-specs-endpoints.md`).

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
cd /home/erick/Documentos/ProjetoVigia/vigia
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
│   ├── controller/             # Recebe HTTP, devolve JSON
│   ├── service/                # Regras de negócio
│   ├── repository/             # Interfaces do Spring Data
│   ├── model/                  # As @Entity
│   ├── dto/                    # Entrada e saída da API (records)
│   ├── exception/              # Tratamento de erros
│   └── integration/            # OpenStreetMap, notícias, redes sociais
├── src/main/resources/
│   └── application.properties  # Configuração do banco
└── CLAUDE.md                   # Regras persistentes para IA
```

**`VigiaApplication.java` não pode sair da raiz do pacote.** `@SpringBootApplication` varre o próprio pacote e os subpacotes à procura de beans — se for movido, o Spring para de encontrar tudo.

---

## 6. Regras que não podem ser quebradas

1. **Nunca use `nativeQuery`.** O banco final é MySQL e o código precisa continuar portável. Use métodos derivados do Spring Data ou JPQL.
2. **Chave estrangeira vira referência de objeto**, nunca `int`. Ex.: `idBairro` vira um campo `Bairro` com `@ManyToOne`.
3. **Métodos de regra de negócio vão para o `@Service`**, nunca para as `@Entity`. Ex.: `criarReporte()`, `suspenderUsuario()`, `calcularClassificacao()`.
4. **Sem acentos nem cedilha em identificadores Java.** `endereço` vira `endereco`.
5. **Herança** usa `@Inheritance(strategy = InheritanceType.JOINED)`.
6. **A senha nunca aparece em nenhuma resposta da API.**
7. **A falha da integração (OpenStreetMap/Nominatim) nunca pode impedir um reporte de ser salvo.**
8. **Sem SQL escrito à mão.** O schema é gerado pelo Hibernate a partir das `@Entity` (`ddl-auto=update`).
9. **Autenticação é simplificada** — sem Spring Security, sem hash, sem token. Declarada como insegura na monografia.

---

## 7. Fluxo de trabalho

- O `README`/`docs/` descrevem o plano fase a fase: **Fase 0** (infra — concluída), **Fase 1** (primeira entidade de ponta a ponta), **Fase 2** (DTOs, erros, Swagger, CORS), **Fase 3** (domínio completo + integrações), **Fase 4** (entrega).
- A cada passo: **escreve → roda → confirma → entendendo → avança**. Um commit por camada que funcionar.
- Todo compromisso (commit/push) precisa de **aprovação explícita** — nada de commitar sozinho.
- A cada interação que alterar o projeto, **atualize `docs/ESTADO_DESENVOLVIMENTO.md`** para registrar onde paramos.

### Mensagens de commit
Em português, concisas, refletindo o estilo do histórico.

---

## 8. Onde estamos agora (ponto de controle)

- Fase 0 concluída: projeto gerado, pacotes criados, `CLAUDE.md` e `docs/` existem, repositório no GitHub com primeiro push, banco configurado (`application.properties`) e app rodando em 8080 com h2-console acessível.
- **Próximo passo: Fase 1** — escrever a primeira `@Entity`, confirmar a tabela no h2-console, e subir Repository → Service → Controller, com commit a cada camada.
- Pendências para o grupo resolverem: validar contrato com o front, definir fórmula de `calcularClassificacao()`, credenciais do MySQL, CORS.

---

## 9. Documentos de referência (em `docs/`)

- `planejamento-desenvolvimento-vigia-backend.md` — stack, estrutura, escopo, ordem de implementação
- `vigia-specs-endpoints.md` — especificação de cada endpoint (contrato JSON + prompts prontos)
- `ESTADO_DESENVOLVIMENTO.md` — onde paramos na última sessão

Antes de implementar qualquer entidade ou endpoint, **consulte a spec correspondente**.