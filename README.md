# Passe — Gestão da Liga de Futebol

Aplicação web para administrar uma liga fictícia: jogadores, clubes, mercado de transferências, escalações, partidas e classificação. O projeto tem um frontend React e uma API REST Java/Spring Boot, com PostgreSQL para persistência.

## Funcionalidades

- Cadastro, edição, consulta, filtros, ordenação e remoção lógica de jogadores.
- Cadastro e consulta de clubes, com caixa, valor do elenco, patrimônio e limite de 12 jogadores.
- Compra e venda de jogadores, contratos de fidelidade, taxas, multas, comissões e histórico financeiro.
- Exportação do histórico de transações em CSV.
- Montagem da escalação titular com formação tática, prévia em campo e lista de reservas.
- Simulação de partidas, visualização do placar e dos seis duelos, além da tabela de classificação.
- Interface em português, sem autenticação.

## Tecnologias

| Parte | Tecnologias |
| --- | --- |
| Frontend | React 18, Vite 5, React Router 6, Axios, Tailwind CSS 3 e Lucide |
| Backend | Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Jakarta Validation, Flyway e Springdoc OpenAPI |
| Banco | PostgreSQL 16 |
| Testes backend | JUnit 5, Spring Boot Test e MockMvc |

## Arquitetura

```text
Navegador
  └── React + React Router (frontend, porta 5173)
        └── Axios /api/v1 (backend, porta 8080)
              └── Controllers → DTOs e validação → Services → Repositories/JPA
                    └── PostgreSQL (porta 5433 no host)
```

### Frontend

- `src/main.jsx` inicializa React, `BrowserRouter`, contexto da liga e notificações globais.
- `src/App.jsx` declara as rotas e aplica `AppShell` (menu lateral, barra superior e conteúdo) às páginas internas.
- `src/pages/` contém as telas de início, jogadores, clubes, detalhe do clube, partidas, detalhe da partida e classificação.
- `src/components/` reúne o layout e componentes reutilizáveis de interface, como tabelas, campos, botões, modais e notificações.
- `src/services/` agrupa as chamadas HTTP por domínio. `http.js` configura Axios, a URL base e as mensagens de erro.
- `src/context/LeagueContext.jsx` disponibiliza totais da liga e funções para atualizar o resumo compartilhado.
- `src/hooks/` contém hooks para carregamento assíncrono, ações, formulários e debounce.
- As páginas usam os serviços para obter dados e enviar comandos; regras de negócio e persistência permanecem na API.

### Rotas da interface

| Rota | Tela |
| --- | --- |
| `/` | Início/apresentação da liga |
| `/players` | Jogadores e operações de mercado |
| `/clubs` | Lista e cadastro de clubes |
| `/clubs/:clubId` | Elenco, escalação e histórico do clube |
| `/matches` | Histórico e criação de partidas |
| `/matches/:matchId` | Placar, campo e duelos da partida |
| `/standings` | Classificação da liga |

### Backend

- `controller/` expõe os endpoints REST versionados em `/api/v1`.
- `dto/request/` define os dados de entrada e validações declarativas; `dto/response/` define o contrato enviado ao frontend.
- `service/` implementa as regras de aplicação e coordena as operações transacionais.
- `repository/` usa Spring Data JPA para persistência e consultas.
- `model/` contém as entidades persistidas.
- `domain/calculator/` concentra cálculos de domínio; `util/` contém mapeadores e utilitários compartilhados.
- `GlobalExceptionHandler` converte erros em respostas JSON sem expor stack traces.
- Flyway controla a evolução do schema em `src/main/resources/db/migration/`.
- Springdoc gera o contrato OpenAPI dos controllers; Swagger UI fica em `/swagger-ui.html` e o JSON em `/v3/api-docs`.

## Regras de negócio cobertas

As regras são aplicadas pela API, mesmo quando a interface também fornece validação para facilitar o uso. Entre elas estão os limites e atributos de jogadores, limite de elenco, saldo para compra, taxa federativa, fidelidade, multa e comissão de venda, elegibilidade e formação da escalação, condição dos atletas, cálculo dos duelos e critérios de classificação. A interface não substitui as validações do backend.

## Pré-requisitos

- Java 21.
- Maven 3.9 ou superior (ou Maven Wrapper, se funcionar no ambiente).
- Node.js 20 ou superior e npm.
- Docker com Docker Compose v2 para executar banco, API e frontend em containers.

## Executar localmente

Os comandos abaixo partem da raiz do repositório.

### Aplicação completa com Docker Compose

Um único comando constrói e inicia PostgreSQL, API e frontend. O backend aguarda o banco ficar saudável; o frontend aguarda a API responder.

```bash
docker compose up --build -d
```

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificação OpenAPI: `http://localhost:8080/v3/api-docs`

Para acompanhar os serviços, use `docker compose logs -f`. Para pará-los, use `docker compose down`. Os dados ficam no volume `pgdata`; `docker compose down -v` também apaga esse volume.

O Compose inicia a interface com o servidor de desenvolvimento Vite. As imagens são construídas a partir de `backend/Dockerfile` e `frontend/Dockerfile`.

### 1. Iniciar o banco

Para executar a API e o frontend fora do Docker, inicie somente o banco:

```bash
docker compose up -d db
```

O banco fica disponível em `localhost:5433`, com database `senai_futebol`. O Compose define as credenciais locais `postgres`/`postgres`.

Ao iniciar a API, Flyway aplica automaticamente as migrações `V1` a `V7` em `backend/src/main/resources/db/migration/`:

| Migração | Conteúdo |
| --- | --- |
| `V1__criar_tabela_clubes.sql` | Tabela de clubes |
| `V2__criar_tabela_jogadores.sql` | Tabela de jogadores |
| `V3__criar_tabela_contratos.sql` | Contratos de fidelidade |
| `V4__criar_tabela_transacoes.sql` | Histórico financeiro |
| `V5__criar_tabela_escalacoes.sql` | Escalações |
| `V6__criar_tabela_partidas.sql` | Partidas |
| `V7__criar_tabela_duelos_partida.sql` | Duelos registrados por partida |

### 2. Iniciar a API

```bash
cd backend
mvn spring-boot:run
```

A API ficará em `http://localhost:8080/api/v1`. Também é possível iniciar pelo wrapper, quando disponível: `./mvnw spring-boot:run` (Linux/macOS) ou `./mvnw.cmd spring-boot:run` (Windows).

> Neste ambiente Windows, o `mvnw.cmd` apresentou uma falha interna do script do wrapper. O Maven instalado (`mvn`) foi usado para compilar e executar os testes.

### 3. Iniciar a interface

Em outro terminal, a partir da raiz:

```bash
cd frontend
npm ci
npm run dev
```

Abra `http://localhost:5173`. A variável `VITE_API_URL` define a origem da API e, por padrão, vale `http://localhost:8080`. Para personalizá-la, crie `frontend/.env` (pode copiar `frontend/.env.example`) e defina, por exemplo:

```dotenv
VITE_API_URL=http://localhost:8080
```

O Vite carrega variáveis `VITE_*` do diretório do frontend. Variáveis da API são configurações do processo Java; o arquivo `.env.example` na raiz serve como referência, mas não é carregado automaticamente pelo Spring Boot.

### Parar os serviços

Pare frontend e backend com `Ctrl+C` nos respectivos terminais e o banco com:

```bash
docker compose down
```

Para apagar também os dados persistidos do banco, use `docker compose down -v`. **Esse comando remove os dados do volume PostgreSQL.**

## Configuração

| Variável | Componente | Padrão | Descrição |
| --- | --- | --- | --- |
| `DB_HOST` | Backend | `localhost` | Host do PostgreSQL |
| `DB_PORT` | Backend | `5433` | Porta do PostgreSQL no host |
| `DB_NAME` | Backend | `senai_futebol` | Nome do banco |
| `DB_USER` | Backend | `postgres` | Usuário do banco |
| `DB_PASSWORD` | Backend | `postgres` | Senha do banco |
| `PORT` | Backend | `8080` | Porta HTTP da API |
| `CORS_ORIGIN` | Backend | `http://localhost:5173` | Origem permitida para chamadas do navegador |
| `VITE_API_URL` | Frontend | `http://localhost:8080` | Origem base da API; `/api/v1` é acrescentado pelo cliente HTTP |

O Swagger documenta os endpoints automaticamente a partir dos controllers e DTOs. A dependência `springdoc-openapi-starter-webmvc-ui` foi escolhida por integrar OpenAPI e Swagger UI ao Spring MVC/Spring Boot 3; não altera as regras nem os contratos de negócio da API.

Os valores mostrados são convenientes para desenvolvimento local. Use credenciais próprias e restrinja a origem CORS em outros ambientes.

## API REST

Base: `http://localhost:8080/api/v1`. Os nomes de campos JSON seguem `snake_case`.

| Método | Endpoint | Ação |
| --- | --- | --- |
| `GET`, `POST` | `/players` | Listar/pesquisar e cadastrar jogadores |
| `GET`, `PUT`, `DELETE` | `/players/{id}` | Consultar, editar e remover logicamente um jogador |
| `GET` | `/players/club/{clubId}` | Listar jogadores de um clube |
| `PATCH` | `/players/{id}/condition` | Atualizar condição |
| `POST` | `/players/{id}/buy` | Comprar/vincular jogador |
| `POST` | `/players/{id}/sell` | Vender jogador |
| `GET`, `POST` | `/clubs` | Listar e cadastrar clubes |
| `GET`, `DELETE` | `/clubs/{id}` | Consultar e excluir clube conforme regras |
| `GET`, `PUT` | `/clubs/{id}/lineup` | Consultar e salvar escalação |
| `GET` | `/clubs/{id}/transactions` | Consultar histórico financeiro |
| `GET` | `/clubs/{id}/transactions/export` | Exportar histórico CSV |
| `GET`, `POST` | `/matches` | Listar e jogar partidas |
| `GET` | `/matches/{id}` | Consultar detalhes e duelos |
| `GET` | `/standings` | Consultar classificação |

## Testes e estado conhecido

Execute os testes a partir de `backend/`:

```bash
mvn test
```

A suíte contém testes unitários e de integração. Os testes de controller usam `@SpringBootTest` e a configuração de datasource da aplicação (`localhost:5433/senai_futebol` por padrão). Portanto, execute-os em um banco dedicado de testes, configurando `DB_NAME`, `DB_PORT` e credenciais para uma instância descartável. Não execute testes contra dados que queira preservar: fixtures de testes limpam tabelas e esperam um banco sem dados de execução manual.

Na verificação local mais recente, a compilação de produção e testes passou e os 24 testes de `CalculatorTest` passaram. A suíte completa executou, mas falhou em testes de integração porque a base configurada continha registros já existentes e relações que impediram a preparação/limpeza das fixtures. Assim, ainda não há confirmação de aprovação da suíte completa em um banco isolado. Os logs desta execução estão em `backend/target/surefire-reports/`.

## Oportunidades de evolução

Estes itens são sugestões, não funcionalidades declaradas como concluídas:

1. **Isolar os testes:** configurar datasource exclusivo para testes e fixtures reprodutíveis, sem dependência do banco usado pela aplicação local.
2. **Compose completo:** adicionar serviços para API e frontend, além do PostgreSQL, com variáveis de ambiente, health checks e dependências de inicialização.
3. **Swagger/OpenAPI:** publicar documentação interativa dos endpoints, parâmetros, exemplos, respostas e erros.
4. **Testes de interface:** automatizar fluxos críticos em navegador (cadastro, compra/venda, escalação e partida), além dos testes de API.
5. **Acessibilidade e responsividade:** validar navegação por teclado, foco dos modais, leitores de tela e telas menores em todas as rotas.
6. **Observabilidade de integração:** melhorar mensagens e diagnóstico quando a API ou banco estiver indisponível.

## Uso de inteligência artificial

Ferramentas de IA foram usadas como apoio ao desenvolvimento, revisão e documentação. O backend teve apoio do Antigravity/Gemini; o frontend teve apoio de Claude; Codex foi usado nesta etapa para revisar pontos de qualidade do backend, centralizar o cálculo de fidelidade, declarar a validação de paginação e consolidar esta documentação. A pessoa desenvolvedora revisou e integrou as alterações e realizou testes manuais da aplicação.

A IA foi usada para acelerar tarefas de implementação e análise, não como substituta da validação humana. O backend não foi alterado nas regras de negócio nesta etapa: o cálculo existente foi extraído para compartilhamento entre serviço e mapper, e `page`/`limit` passaram a ser validados por anotações no DTO. A suíte completa ainda precisa ser executada com datasource isolado, conforme a seção de testes.
