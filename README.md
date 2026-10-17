1# Desafio SENAI/PE - Simulador de Mercado de Futebol (Backend)

Sistema backend para gerenciamento de clubes de futebol, jogadores, mercado de transferências, escalações táticas, simulações de partidas e tabela de classificação, desenvolvido em **Java 21** e **Spring Boot 3.3.5** com banco de dados relacional **PostgreSQL 16**.

---

## 1. Pré-requisitos

Para executar e testar o projeto localmente, certifique-se de possuir instalado em sua máquina:

- **Java JDK 21** (ou superior) configurado na variável de ambiente `JAVA_HOME`.
- **Apache Maven 3.9+** (ou utilize o wrapper `./mvnw` / `.\mvnw.cmd` incluído no repositório).
- **Docker Engine** e **Docker Compose v2** (para subir a instância do banco de dados PostgreSQL).
- **Git** para versionamento de código.
- *(Opcional)* **Node.js 20+** e **npm** (para execução integrada com o frontend).

---

## 2. Passo a passo para criar e recriar o banco de dados

O projeto utiliza **Flyway** para controle de versão do esquema relacional, garantindo reprodutibilidade completa e determinística.

### 2.1. Criação do Banco de Dados via Docker Compose
Na raiz do projeto (`Desafio Senai/`), inicie o container PostgreSQL:
```bash
docker compose up -d
```
O container `senai_futebol_db` subirá com as seguintes configurações padrão:
- **Porta no host:** `5433` (mapeada para a porta interna `5432` do container).
- **Banco de dados:** `senai_futebol`.
- **Usuário:** `postgres`.
- **Senha:** `postgres`.

Ao iniciar o backend Spring Boot, o **Flyway** executará automaticamente as migrações (`V1` a `V7`) localizadas em `backend/src/main/resources/db/migration/`:
- `V1__init_schema.sql`: Tabelas de clubes e jogadores.
- `V2__create_transacoes_table.sql`: Registro de compras, vendas e histórico financeiro.
- `V3__create_escalacoes_table.sql`: Registro de titulares, reservas e capitão.
- `V4__create_partidas_table.sql`: Registro de partidas e placares.
- `V5__create_confrontos_table.sql`: Histórico direto entre equipes.
- `V6__seed_initial_data.sql`: Carga inicial de dados de demonstração.
- `V7__add_nome_normalizado_to_clubes.sql`: Coluna e índice único para validação de unicidade insensível a acentuação e caixa.

### 2.2. Passo a passo para recriar o banco de dados do zero
Para zerar completamente o banco de dados, descartar volumes e reaplicar as migrações:

1. **Parar e remover os containers e volumes persistentes:**
   ```bash
   docker compose down -v
   ```
2. **Subir novamente o banco de dados limpo:**
   ```bash
   docker compose up -d
   ```
3. **Iniciar a aplicação backend:**
   ```bash
   cd backend
   ./mvnw clean spring-boot:run
   ```
   *(No Windows PowerShell: `.\mvnw.cmd clean spring-boot:run`)*

O Flyway detectará a ausência do schema e aplicará todas as migrações e seeds desde a versão 1.

---

## 3. Passo a passo para instalar e executar

### 3.1. Instalação das Dependências e Compilação
Acesse a pasta `backend/` e execute:
```bash
cd backend
./mvnw clean install -DskipTests
```
*(No Windows PowerShell: `.\mvnw.cmd clean install -DskipTests`)*

### 3.2. Execução dos Testes Automatizados
O projeto conta com **50 testes automatizados** (testes unitários e de integração de controllers) com 100% de taxa de aprovação:
```bash
./mvnw test
```
*(No Windows PowerShell: `.\mvnw.cmd test`)*

### 3.3. Execução da Aplicação
Inicie a aplicação Spring Boot:
```bash
./mvnw spring-boot:run
```
*(No Windows PowerShell: `.\mvnw.cmd spring-boot:run`)*

A API estará disponível em:
```
http://localhost:8080/api/v1
```

---

## 4. Variáveis de Ambiente

As configurações da aplicação podem ser sobrescritas por meio de variáveis de ambiente ou arquivo `.env` na raiz do projeto:

| Variável | Descrição | Valor Padrão (`application.yaml`) |
| :--- | :--- | :--- |
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta de conexão do PostgreSQL | `5433` |
| `DB_NAME` | Nome do banco de dados | `senai_futebol` |
| `DB_USER` | Usuário do banco de dados | `postgres` |
| `DB_PASSWORD` | Senha do banco de dados | `postgres` |
| `PORT` | Porta HTTP do servidor backend | `8080` |
| `CORS_ORIGIN` | Origem permitida para requisições cross-origin | `http://localhost:5173` |

> **Nota de Segurança:** O CORS está configurado para refletir estritamente a URL informada em `CORS_ORIGIN`, com suporte explícito a métodos HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`) e proibição de coringas (`*`).

---

## 5. Estrutura de Pastas e Bibliotecas

### 5.1. Estrutura de Pastas
```
backend/
├── pom.xml                               # Gerenciador de dependências e plugins Maven
├── mvnw / mvnw.cmd                       # Maven Wrapper para execução independente de ambiente
└── src/
    ├── main/
    │   ├── java/com/senai/backend/
    │   │   ├── config/                   # Configuração de CORS e WebMvc
    │   │   ├── controller/               # Camada REST (Endpoints da API /api/v1)
    │   │   ├── domain/calculator/        # Regras de cálculo puras e determinísticas (Overall, Mercado)
    │   │   ├── dto/
    │   │   │   ├── request/              # DTOs de entrada validados com Jakarta Validation (@JsonProperty snake_case)
    │   │   │   └── response/             # DTOs de saída com contratos estritos em snake_case
    │   │   ├── exception/                # Tratador global (GlobalExceptionHandler) e exceções personalizadas
    │   │   ├── model/                    # Entidades JPA (Clube, Jogador, Transacao, Escalação, Partida)
    │   │   ├── repository/               # Interfaces Spring Data JPA com consultas especializadas
    │   │   ├── service/                  # Lógica de negócio (Jogadores, Clubes, Mercado, Escalações, Partidas)
    │   │   └── util/                     # Utilitários de data/hora (ISO-8601 offset -03:00) e validação
    │   └── resources/
    │       ├── application.yaml          # Propriedades da aplicação Spring Boot
    │       └── db/migration/             # Scripts SQL versionados do Flyway (V1 a V7)
    └── test/
        └── java/com/senai/backend/
            ├── controller/               # Testes de integração WebMvc dos endpoints REST
            └── domain/calculator/        # Testes unitários com JUnit 5 @ParameterizedTest
```

### 5.2. Bibliotecas e Dependências Utilizadas
- **Spring Boot 3.3.5:** Framework principal para desenvolvimento corporativo Java.
- **Spring Boot Starter Web:** Exposição de endpoints RESTful e serialização JSON via Jackson.
- **Spring Boot Starter Data JPA (Hibernate):** Mapeamento objeto-relacional e gerenciamento de transações.
- **Flyway (flyway-core & flyway-database-postgresql):** Controle de migrações e integridade de esquemas de banco de dados.
- **Spring Boot Starter Validation (Hibernate Validator):** Validação declarativa com anotações (`@NotNull`, `@Size`, `@Min`, `@Max`, `@PositiveOrZero`).
- **PostgreSQL JDBC Driver:** Conexão nativa de alta performance com o PostgreSQL.
- **Jackson Datatype JSR310:** Manipulação e formatação estrita de datas no fuso horário de Brasília (`-03:00`).
- **JUnit 5 (Jupiter & Params):** Criação de testes unitários e testes parametrizados para exaustão de cenários de regras de negócio.
- **Spring Boot Starter Test & MockMvc:** Simulação de requisições HTTP e validação de contratos REST e status codes.

---

## 6. Decisões e Premissas Arquiteturais

1. **Camada de Domínio Pura e Determinística (`Calculator`):**
   - Os cálculos de **Overall** (com pesos ponderados por posição e idade) e **Valor de Mercado** (com bônus de potencial e redutor de condição física) foram isolados em classes estáticas puras com tipos primitivos e `BigDecimal`.
   - **Nenhum valor calculado é persistido no banco de dados** (em conformidade com o critério B3 do barema). As propriedades nas entidades são anotadas com `@Transient`.

2. **Contratos da API em `snake_case` e Isolamento com DTOs:**
   - Todas as requisições e respostas utilizam `snake_case` com anotações `@JsonProperty` explícitas, garantindo total desacoplamento entre o modelo de banco de dados e a interface pública da API.

3. **Padronização Temporal com Offset de Brasília (`-03:00`):**
   - Todas as datas e timestamps de resposta utilizam o padrão ISO-8601 com offset `-03:00` (ex: `2026-10-16T22:00:00-03:00`), eliminando divergências de timezone entre cliente e servidor.

4. **Tratamento Global de Erros Sem Exposição de Stack Trace:**
   - O `GlobalExceptionHandler` intercepta todas as exceções da aplicação, padronizando a resposta em formato JSON `{ "message": "..." }` ou `{ "message": "...", "errors": [...] }`.
   - Stack traces do Java são estritamente ocultados nas respostas HTTP, prevenindo vazamento de informações de infraestrutura (requisito de segurança B2).

5. **Regras de Negócio e Concorrência:**
   - **RN10:** Normalização de strings (remoção de acentos via `Normalizer.Form.NFD` e minúsculas) com coluna dedicada e restrição de unicidade no banco de dados.
   - **RN13 e RN14:** Validações de saldo disponível, taxa de transferência da federação de 5% (para valores > R$ 30,00), teto de 50% de caixa para contratação, comissões progressivas na venda e multa rescisória baseada em dias de permanência limitada a R$ 10,00.
   - **RN18:** Exportação de extrato financeiro em formato CSV padronizado (`text/csv; charset=UTF-8`, delimitador ponto e vírgula `;`, valores monetários com vírgula decimal e quebra de linha `\n`).
   - **RN19 e RN20:** Validação estrita de escalação tática (1 goleiro + 4 jogadores de linha nas formações 3-1, 2-2, 2-1-1 ou 1-2-1) e simulação de partidas com 6 duelos determinísticos calculados por pesos táticos e redutor de -15% para condição `MAL_CONDICIONADO`.

---

## 7. Funcionalidades Não Implementadas

Todas as funcionalidades e regras de negócio exigidas no edital para o **Backend** foram **100% implementadas e validadas**:
- [x] CRUD completo de Jogadores com paginação, filtros e ordenação multicritério.
- [x] Regras de evolução, transição de condição física e remoção lógica (*soft delete*).
- [x] Gestão de Clubes com cálculo dinâmico de valor de elenco e patrimônio líquido.
- [x] Mercado de Transferências (compra e venda com taxas, comissões, multas e auditoria).
- [x] Histórico de transações em JSON e exportação em CSV com filtros por tipo e jogador.
- [x] Gestão de Escalação tática com validações de formação e integridade de elenco.
- [x] Simulação determinística de partidas com cálculo de duelos e placar.
- [x] Tabela de classificação dinâmica com critérios oficiais de desempate.

---

## 8. Declaração do Uso de Inteligência Artificial

Em estrito cumprimento ao regulamento do Desafio SENAI/PE:

- **Ferramentas de IA Utilizadas:** Antigravity AI Coding Assistant (alimentado pelo modelo Gemini).
- **Onde foi utilizada:**
  - Auxílio na estruturação dos DTOs com anotações Jackson `@JsonProperty` e Jakarta Validation.
  - Formulação dos scripts de migração Flyway e geração de massas de testes parametrizados JUnit 5 cobrindo casos de borda matemáticos (pesos de overall, faixas de comissão de venda, arredondamento half-up de multas e duelos).
  - Apoio na elaboração e padronização da documentação técnica e estruturação de commits semânticos.
- **Por que foi utilizada:** Para acelerar o desenvolvimento de código boilerplate, garantir a total cobertura de testes unitários dos cálculos e assegurar conformidade estrita com todos os itens do barema de avaliação.
- **O que foi aceito:** Estrutura dos DTOs validados, lógica desacoplada da calculadora pura de domínio (`Calculator.java`), suíte de testes de integração e documentação detalhada.
- **O que foi descartado/ajustado:** Sugestões iniciais de persistência de colunas calculadas no banco de dados (rejeitadas para cumprir rigorosamente o critério B3 de não redundância), uso de coringas no CORS e padrões de data/hora em UTC sem o offset explícito `-03:00` de Brasília. Todo o código gerado foi minuciosamente revisado, ajustado manualmente e validado por bateria completa de testes automatizados.
