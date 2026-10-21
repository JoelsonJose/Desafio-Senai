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

## Pré-requisitos

- Ubuntu 24.04 (ambiente de referência da avaliação).
- Java 21 e Maven 3.9 ou superior para executar a API localmente (validação feita com Maven 3.9.12).
- Node.js 20 LTS ou 22 LTS e npm (validação feita com Node.js 22.20.0 e npm 10.9.3) para executar o frontend localmente.
- Docker Engine 28.5.1 e Docker Compose 2.40.0 ou compatíveis para iniciar o banco ou a aplicação completa.
- Git para obter o repositório.

Para o fluxo recomendado com Compose, Docker é suficiente; Java, Maven e Node.js só são necessários para executar os componentes fora dos containers.

## Tecnologias e motivo de uso

| Parte | Biblioteca/ferramenta e versão | Motivo |
| --- | --- | --- |
| Frontend | React 18.3.1 | Construir a interface por componentes e manter estado das telas. |
| Frontend | Vite 5.4.21 | Servir e compilar o projeto React; oferece inicialização rápida no desenvolvimento. |
| Frontend | React Router 6.30.6 | Declarar navegação por URL entre as telas sem recarregar a página. |
| Frontend | Axios 1.20.0 | Centralizar chamadas HTTP, URL base e tratamento de falhas da API. |
| Frontend | Tailwind CSS 3.4.19 | Compor estilos utilitários da interface a partir do mockup. |
| Frontend | Lucide React 0.344.0 | Usar ícones vetoriais consistentes com os controles da interface. |
| Backend | Java 21; Spring Boot 3.3.5 | Implementar a API REST e organizar sua configuração e execução. |
| Backend | Spring Web (gerenciado pelo Spring Boot) | Expor controllers e endpoints HTTP. |
| Backend | Spring Data JPA/Hibernate (gerenciado pelo Spring Boot) | Persistir entidades e executar consultas no PostgreSQL. |
| Backend | Jakarta Validation (gerenciado pelo Spring Boot) | Validar dados de entrada declarativamente nos DTOs. |
| Backend | Flyway (gerenciado pelo Spring Boot) | Versionar e aplicar as migrações do schema na inicialização. |
| Backend | Springdoc OpenAPI 2.6.0 | Gerar a especificação OpenAPI e a Swagger UI dos endpoints. |
| Backend | PostgreSQL JDBC (gerenciado pelo Spring Boot) | Conectar a aplicação Java ao PostgreSQL. |
| Testes | Spring Boot Test, JUnit 5 e JUnit Params (gerenciados pelo Spring Boot/JUnit) | Executar testes unitários, de integração e parametrizados. |
| Infraestrutura | PostgreSQL 16, Docker Compose v2 | Disponibilizar banco e serviços locais de forma reproduzível. |

As versões transitivas do backend são gerenciadas pelo Spring Boot 3.3.5. O `frontend/package-lock.json` fixa as versões efetivamente instaladas pelo npm.

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

Estrutura principal: `src/components/` (layout e componentes comuns), `src/context/` (resumo compartilhado da liga), `src/hooks/` (comportamentos reutilizáveis), `src/pages/` (telas), `src/services/` (cliente HTTP por domínio) e `src/utils/` (formatação e funções auxiliares).

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

Estrutura principal: `src/main/java/.../controller`, `dto`, `service`, `repository`, `model`, `domain` e `util`; configurações e migrações ficam em `src/main/resources/`.

## Regras de negócio cobertas

As regras são aplicadas pela API, mesmo quando a interface também fornece validação para facilitar o uso. Entre elas estão os limites e atributos de jogadores, limite de elenco, saldo para compra, taxa federativa, fidelidade, multa e comissão de venda, elegibilidade e formação da escalação, condição dos atletas, cálculo dos duelos e critérios de classificação. A interface não substitui as validações do backend.

## Executar localmente

Os comandos abaixo partem da raiz do repositório no Ubuntu 24.04. O endpoint base da API (sem `/api/v1`) é `http://localhost:8080`; a interface fica em `http://localhost:5173`. A trava do frontend, `frontend/package-lock.json`, está versionada e é instalada por `npm ci`.

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
cp .env.example .env
npm ci
npm run dev
```

Abra `http://localhost:5173`. A variável `VITE_API_URL` define a origem da API e, por padrão, vale `http://localhost:8080`. Para personalizá-la, crie `frontend/.env` (pode copiar `frontend/.env.example`) e defina, por exemplo:

```dotenv
VITE_API_URL=http://localhost:8080
```

O Vite carrega `VITE_*` do diretório do frontend. Para desenvolvimento local, copie `frontend/.env.example` para `frontend/.env`. O arquivo `.env.example` da raiz documenta as variáveis da API; ele é referência para configurar o processo Java, não é carregado automaticamente pelo Spring Boot.

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
| `VITE_API_URL` | Frontend (`frontend/.env`) | `http://localhost:8080` | Origem base da API; `/api/v1` é acrescentado pelo cliente HTTP |

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

O backend mantém os testes automatizados unitários dos cálculos de domínio (Overall, valor base e valor de mercado), parametrizados com 24 cenários. Eles não precisam de conexão com PostgreSQL. Execute-os a partir de `backend/`:

```bash
mvn -Dtest=CalculatorTest test
```

Na última execução local, os 24 cenários de cálculo passaram, sem falhas ou erros. Esses testes verificam diretamente os cálculos previstos nas regras RN02 a RN06. A configuração da suíte de testes não depende do banco de dados da aplicação.

## Decisões e premissas

- A interface e a documentação estão em português, seguindo os textos do mockup e do enunciado.
- O frontend não implementa autenticação: o fluxo definido para esta aplicação não exige login.
- O cliente HTTP usa `VITE_API_URL` como origem e acrescenta `/api/v1`; em execução local, a origem padrão é `http://localhost:8080`.
- A API é a fonte dos valores calculados e das regras de negócio. O frontend valida os campos para orientar o preenchimento, mas exibe o resultado/erro devolvido pela API e não recalcula regras de domínio.
- Datas e horas apresentadas ou enviadas pela interface seguem o horário de Brasília (UTC−3), como requerido no desafio.
- `docker compose up --build -d` é o caminho mais simples para iniciar a aplicação completa; a execução individual dos serviços também está descrita acima.

## Funcionalidades não implementadas

Não há funcionalidade obrigatória conhecida pendente na aplicação. Permanecem melhorias diferenciais, que não fazem parte do fluxo funcional obrigatório: testes automatizados de componente ou ponta a ponta no frontend; comandos de lint e verificação de formatação do frontend; e logs estruturados por requisição. A responsividade foi conferida manualmente pelo desenvolvedor em viewport de 375 px.

## Uso de inteligência artificial

- **Antigravity/Gemini — `backend/`:** apoio à implementação da API e das regras do desafio. O desenvolvedor revisou as alterações e conferiu os fluxos e validações; o código foi aceito após essa revisão.
- **Claude — `frontend/src/`:** apoio à arquitetura React, às telas baseadas no mockup do Figma e ao consumo da API. O desenvolvedor comparou a interface com o mockup, fez ajustes e testou os fluxos; as telas integradas foram aceitas após revisão.
- **Codex — backend, infraestrutura e este README:** apoio à revisão de paginação/DTO, extração do cálculo de fidelidade já existente para reuso, configuração OpenAPI/Swagger, Compose completo e documentação. Essas alterações foram revisadas e integradas; a extração preservou a regra de cálculo e não foi solicitada mudança de regra de negócio.

As sugestões de IA foram tratadas como propostas e conferidas contra o enunciado, a API e o mockup antes da integração. Alterações que não correspondessem a esses requisitos não foram mantidas. Não há uma proposta descartada específica registrada na versão final.
