# Mobiauto Backend 202502

Bem-vindo ao **Mobiauto Backend 202502**, uma solução robusta e escalável desenvolvida como parte do teste técnico para a Mobiauto! Este projeto demonstra habilidades em arquitetura de sistemas, desenvolvimento backend em Java e boas práticas de engenharia de software.

---

## 1. Introdução

### Propósito

O **Mobiauto Backend** é uma API RESTful projetada para gerenciar revendas de veículos, usuários e veículos em um sistema de concessionárias. Ele permite a criação, consulta, atualização e exclusão de entidades, com controle de acesso baseado em cargos e revendas, garantindo segurança e escalabilidade.

### Principais Funcionalidades

- **Gestão de Usuários**: Cadastro, edição e exclusão de usuários com diferentes cargos (Administrador, Proprietário, Gerente, Assistente).
- **Gestão de Veículos**: CRUD de veículos, vinculados a revendas específicas.
- **Controle de Acesso**: Restrições baseadas em cargos e revendas, utilizando Spring Security.
- **Persistência de Dados**: Integração com banco de dados relacional via Spring Data JPA.

---

## 2. Visão Geral

### Pré-requisitos

- **Java 17**: Versão mínima para execução.
- **Maven 3.8+**: Gerenciamento de dependências e build.
- **Docker**: Para execução via containers com docker-compose.
- **PostgreSQL 15**: Banco de dados relacional (incluído no `docker-compose`).

### 3. Bibliotecas e Serviços Externos

- **Spring Boot 3**: Framework principal para a API.
- **Spring Security**: Autenticação e autorização.
- **Spring Data JPA**: Persistência de dados.
- **Lombok**: Redução de boilerplate no código.
- **JUnit 5 e Mockito**: Testes unitários.

### 4. Componentes da Arquitetura

- **Controllers**: Camada de entrada para requisições HTTP, mapeando endpoints REST.
- **Services**: Lógica de negócio e regras de autorização.
- **Repositories**: Acesso aos dados via JPA.
- **DTOs e Mappers**: Transferência de dados e conversão entre entidades e respostas.
- **Model**: Entidades JPA representando usuários, revendas e veículos.

### 5. Docker Compose
O projeto inclui um arquivo docker-compose.yml para facilitar a execução em containers. Ele configura a aplicação e o banco de dados PostgreSQL.

```
version: '3.8'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
      POSTGRES_DB: mobiauto
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```
### 6. Dependências

* spring-boot-starter-web
* spring-boot-starter-data-jpa
* spring-boot-starter-security
* postgresql
* lombok
* spring-boot-starter-test
* mapstruct
* jjwt-api
* caelum-stella-core

### 7. Variáveis de Ambiente

```
spring:
    datasource:
        url: jdbc:postgresql://localhost:5432/mobiauto
        username: admin
        password: admin123
```

### 8. Suporte e Contato

* E-mail: lucaschiavicatti@hotmail.com
* [GITHUB](https://github.com/lucaschiavicatti/mobiauto-backend-202502)



