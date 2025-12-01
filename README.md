# DESENVOLVIMENTO DE UM SISTEMA INTELIGENTE DE RECRUTAMENTO PERSONALIZADO

> Trabalho de Conclusão de Curso

## Informações Institucionais

- **Instituição:** Centro Universitário da Grande Dourados
- **Curso:** Bacharel em Engenharia de Software
- **Autora:** Bianca Maria Michels de Barcelos
- **Orientador(a):** Prof. Msc. Antonio Pires de Almeida Junior

---

## 📋 Sumário

- [Visão Geral](#-visão-geral)
- [Diferencial: Inteligência Artificial](#-diferencial-inteligência-artificial)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Funcionalidades](#-funcionalidades)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Configuração](#-instalação-e-configuração)
- [Testes](#-testes)
- [Licença](#-licença)

---

## 🎯 Visão Geral

Sistema web completo para gerenciamento de processos de recrutamento e seleção, conectando organizações recrutadoras a candidatos em busca de oportunidades profissionais. O sistema oferece funcionalidades avançadas para gestão de vagas, perfis de candidatos, processos seletivos e análise inteligente de compatibilidade.

### Objetivo

Facilitar e otimizar o processo de recrutamento através de uma plataforma integrada que utiliza inteligência artificial para análise de compatibilidade entre candidatos e vagas, reduzindo o tempo de triagem e melhorando a qualidade das contratações.

---

## 🤖 Diferencial: Inteligência Artificial

O sistema incorpora **Spring AI** integrado com **OpenAI GPT-4** para oferecer funcionalidades inteligentes:

### Análise de Compatibilidade Candidato-Vaga
- Análise semântica de currículos e descrições de vagas
- Cálculo automático de score de compatibilidade
- Parsing inteligente de PDFs de currículos
- Identificação de competências e experiências relevantes

### Otimização de Performance
- Sistema de cache com TTL configurável
- Fallback para análise básica em caso de indisponibilidade
- Processamento assíncrono para não bloquear operações

---

## 🏗️ Arquitetura

O projeto foi desenvolvido seguindo princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**, implementando uma **Arquitetura Hexagonal** (Ports & Adapters).

### Bounded Contexts

O domínio é organizado em 6 contextos delimitados:

1. **Autenticação e Usuário** - Gestão de identidade e acesso
2. **Organização** - Gestão de empresas e recrutadores
3. **Perfil do Candidato** - Informações profissionais completas
4. **Vagas** - Gestão de oportunidades e benefícios
5. **Processo Seletivo** - Workflow de candidaturas e etapas
6. **Avaliação** - Sistema de feedback e ratings

### Camadas (Backend)

```
src/main/java/com/barcelos/recrutamento/
├── api/          # Camada de Apresentação
│   ├── controller/   # REST Controllers
│   ├── dto/          # Data Transfer Objects
│   └── exception/    # Exception Handlers
├── core/         # Camada de Domínio
│   ├── model/        # Entidades e Value Objects
│   ├── service/      # Regras de Negócio
│   └── port/         # Interfaces (Ports)
├── data/         # Camada de Infraestrutura
│   ├── adapter/      # Implementações dos Ports
│   ├── entity/       # Entidades JPA
│   ├── mapper/       # Conversores Domain ↔ Entity
│   └── repository/   # Repositórios
└── config/       # Configurações
```

### Modelo de Domínio

- **26 entidades de domínio**
- **9 value objects** (Email, CPF, CNPJ, Endereço, etc.)
- **12 enums** (Status, TipoContrato, ModalidadeTrabalho, etc.)

---

## 🛠️ Tecnologias

### Backend

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 25 | Linguagem base |
| Spring Boot | 3.5.5 | Framework principal |
| Spring Security | 3.5.5 | Segurança e autenticação |
| Spring AI | 1.0.3 | Integração com IA |
| PostgreSQL | 14+ | Banco de dados |
| MapStruct | 1.6.3 | Mapeamento de objetos |
| JWT | 0.12.3 | Autenticação stateless |
| JUnit 5 | - | Testes unitários |
| Mockito | - | Mocks para testes |
| ArchUnit | 1.4.1 | Testes arquiteturais |

### Frontend

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| React | 19.1 | Framework UI |
| TypeScript | 5.9 | Tipagem estática |
| Vite | 7.2 | Build tool |
| TailwindCSS | 3.4 | Estilização |
| Radix UI | - | Componentes acessíveis |
| Axios | 1.13 | Cliente HTTP |
| React Router | 7.9 | Roteamento |

---

## ✨ Funcionalidades

### Para Candidatos
- Cadastro e gerenciamento de perfil profissional completo
- Busca avançada de vagas com filtros
- Sistema de candidaturas com acompanhamento de status
- Salvamento de vagas favoritas
- Avaliação de organizações
- Geração automática de currículo em PDF
- Recebimento de feedback do processo seletivo

### Para Recrutadores
- Gestão completa de vagas (criar, editar, cancelar)
- Cadastro de benefícios organizacionais
- Definição de etapas customizadas para processo seletivo
- Análise de compatibilidade de candidatos com IA
- Gestão de candidaturas por etapa
- Envio de feedback para candidatos
- Convite direto de candidatos para vagas
- Busca avançada de candidatos

### Para Administradores
- Gestão de usuários do sistema


---

## 📋 Pré-requisitos

### Obrigatórios

- **Java 21** ou superior
- **Node.js 18** ou superior
- **PostgreSQL 14** ou superior
- **Maven 3.8** ou superior
- **OpenAI API Key** (obrigatória para funcionalidades de IA)
- **Conta de email Gmail** ou similar (para envio de notificações)

---

## 🚀 Instalação e Configuração

### 1. Clone o Repositório

```bash
git clone [url-do-repositorio]
cd TCC
```

### 2. Configuração do Banco de Dados

```bash
# Criar banco de dados PostgreSQL
createdb recrutamento

# Executar script de criação de tabelas e tipos
psql -d recrutamento -f banco.sql
```

### 3. Configuração do Backend

Crie um arquivo `.env` ou configure as variáveis de ambiente:

```
OPENAI_API_KEY=sua-chave-openai
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-app
JWT_SECRET=sua-chave-secreta-jwt
```

Edite `src/main/resources/application.yml` se necessário.

```bash
# Build e execução
mvn clean install
mvn spring-boot:run
```

O backend estará disponível em `http://localhost:8080`

### 4. Configuração do Frontend

```bash
cd frontend/recrutamento

# Instalar dependências
npm install

# Configurar URL da API (se necessário)
# Editar arquivo de configuração com endpoint do backend

# Executar em modo desenvolvimento
npm run dev
```

O frontend estará disponível em `http://localhost:5173`

---

## 🧪 Testes

### Backend

O projeto conta com **testes automatizados** focados na cobertura essencial do domínio:

- **Testes de Model:** Validação de entidades e value objects
- **Testes de Service:** Regras de negócio e fluxos principais
- **Testes Arquiteturais:** Verificação de conformidade com padrões (ArchUnit)

```bash
# Executar todos os testes
mvn test

# Executar apenas testes de uma classe específica
mvn test -Dtest=CandidaturaServiceTest
```

### Frontend

```bash
cd frontend/recrutamento

# Executar testes
npm test

```

---

## 🔒 Segurança

- **Autenticação JWT** com access tokens e refresh tokens
- **Controle de acesso baseado em roles** (CANDIDATO, RECRUTADOR, ADMIN)
- **Validação de ownership** de recursos
- **Spring Security** para proteção de endpoints
- **CORS** configurado para ambiente de desenvolvimento

---

## 📡 API REST

- **22 Controllers** organizados por contexto
- **125+ endpoints** RESTful
- Autenticação via Bearer Token (JWT)

---

## 💾 Banco de Dados

- **PostgreSQL** com tipos ENUM customizados
- Script de criação completo em `banco.sql`
- Relacionamentos bem definidos com chaves estrangeiras
- Índices para otimização de consultas

---

## 📊 Qualidade de Código

- Princípios **SOLID** aplicados
- **Clean Code** e boas práticas
- **Testes automatizados** com alta cobertura do domínio
- **Validações robustas** de regras de negócio
- **Separação de responsabilidades** através de arquitetura em camadas

---
