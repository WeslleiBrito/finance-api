


# Finance API

A **Finance API** é uma solução pensada para quem deseja ter maior controle sobre suas finanças pessoais, integrando **entradas e saídas de dinheiro** com **gestão de investimentos** em um único sistema.

---

## 💡 Motivação

Hoje, a maioria das pessoas utiliza aplicativos financeiros simples, que geralmente oferecem **apenas duas funções isoladas**:
- Controle de entradas e saídas (fluxo de caixa pessoal);
- Ou controle de investimentos (acompanhamento de carteira).

O problema é que **poucos sistemas conseguem integrar os dois mundos** em uma única plataforma.  
Essa falta de integração gera algumas dificuldades comuns:
- Pessoas precisam usar **dois ou mais aplicativos** para ter uma visão completa da sua vida financeira;
- O **fluxo de caixa** não conversa com os **investimentos**, dificultando projeções de médio e longo prazo;
- A análise fica fragmentada, tornando difícil tomar **decisões financeiras mais inteligentes**.

Segundo pesquisas recentes:
- **67% dos brasileiros não conseguem controlar seus gastos mensais de forma eficiente** (fonte: SPC Brasil);  
- Apenas **21% da população investe regularmente** (fonte: B3, 2024);  
- Um dos principais motivos citados é a **falta de ferramentas acessíveis e integradas** que ajudem no planejamento financeiro.

---

## 🎯 A Importância de uma Ferramenta Integrada

A **Finance API** surge justamente para preencher essa lacuna no mercado, oferecendo:
- **Controle de entradas e saídas** detalhado, com categorização e histórico;  
- **Gestão de investimentos** no mesmo ambiente, permitindo acompanhar ganhos, aportes e projeções;  
- **Relatórios e dashboards** para oferecer uma visão unificada da saúde financeira;  
- **Automação** do fluxo financeiro, ajudando a reduzir erros humanos e melhorar o planejamento.

Essa integração garante que o usuário consiga:
- Entender para onde o dinheiro está indo;  
- Avaliar como seus investimentos se comportam em relação à sua renda;  
- Planejar com clareza tanto o **curto prazo** (fluxo de caixa) quanto o **longo prazo** (crescimento patrimonial).

---

## 🚀 Objetivo do Projeto

O projeto busca se tornar um **gerenciador financeiro completo**, ajudando pessoas a:
- Ganhar mais **clareza sobre sua vida financeira**;  
- Evitar endividamento desnecessário;  
- Aumentar sua **capacidade de investir com segurança**;  
- E alcançar uma **melhor saúde financeira** no longo prazo.

---



## 🔧 Pré-requisitos

Antes de rodar o projeto, é necessário instalar o **Docker** no seu sistema operacional.  

### Instalação do Docker

#### Windows
1. Baixe e instale o **Docker Desktop**: [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)  
2. Durante a instalação, habilite a opção **"Use WSL 2 instead of Hyper-V"** (caso disponível).  
3. Após instalar, reinicie o computador.  
4. Verifique se está funcionando:
   ```powershell
   docker --version


#### Linux (Ubuntu/Debian)

1. Atualize os pacotes:

   ```bash
   sudo apt update && sudo apt upgrade -y
   ```
2. Instale dependências:

   ```bash
   sudo apt install apt-transport-https ca-certificates curl software-properties-common -y
   ```
3. Adicione a chave oficial do Docker:

   ```bash
   curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker.gpg
   ```
4. Adicione o repositório:

   ```bash
   echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
   ```
5. Instale o Docker:

   ```bash
   sudo apt update
   sudo apt install docker-ce -y
   ```
6. Verifique a instalação:

   ```bash
   docker --version
   ```

#### MacOS

1. Baixe e instale o **Docker Desktop for Mac**: [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Após a instalação, abra o Docker Desktop e aguarde inicializar.
3. Verifique se está funcionando:

   ```bash
   docker --version
   ```

---

## ⚙️ Configuração do Projeto

O projeto é executado em containers Docker usando **docker-compose**. Antes de rodar, configure corretamente o arquivo `docker-compose.yml`.

### Estrutura do `docker-compose.yml`

```yaml
services:
  db:
    image: postgres:15
    container_name: finance-db
    environment:
      POSTGRES_DB: financeapi
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - finance_network

  app:
    build: .
    container_name: finance-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/financeapi
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: admin
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
      JWT_SECRET: [CHAVE SECRETA COM PELO MENOS 32 CARACTERES, RECOMENDADO 50]
      JWT_EXPIRATION: [UM VALOR INTEIRO EM MILISSEGUNDOS, ex. 86400000 = 1 dia]
    ports:
      - "8080:8080"
    depends_on:
      - db
    networks:
      - finance_network

volumes:
  postgres_data:

networks:
  finance_network:
```

### Variáveis Importantes

* `POSTGRES_DB`: Nome do banco de dados usado pela aplicação.
* `POSTGRES_USER`: Usuário do banco de dados.
* `POSTGRES_PASSWORD`: Senha do banco de dados.
* `SPRING_DATASOURCE_URL`: URL de conexão com o banco (já configurada para usar o container `db`).
* `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`: credenciais do banco.
* `JWT_SECRET`: chave secreta para geração de tokens JWT (mínimo 32 caracteres, recomendado 50).
* `JWT_EXPIRATION`: tempo de expiração do token JWT em milissegundos (exemplo: `86400000` = 1 dia).

---

## ▶️ Subindo os Containers

Após configurar o arquivo `docker-compose.yml`, execute os seguintes comandos:

```bash
# Criar e subir os containers
docker-compose up -d --build

# Verificar se os containers estão rodando
docker ps
```

A aplicação estará disponível em:
👉 [http://localhost:8080](http://localhost:8080)

---

## 📖 Documentação da API

Toda a documentação dos endpoints está disponível no Postman:
👉 [Documentação da Finance API no Postman](https://documenter.getpostman.com/view/26586405/2sB3HooeZm)

---

## 🚀 Status do Projeto

* [x] Cadastro e autenticação de usuários (signup, login e atualização de senha)
* [x] Criação e gerenciamento de contas (Account)
* [x] Cadastro de fornecedores/pagadores
* [x] Cadastro de documentos e parcelas
* [ ] Controle de investimentos
* [ ] Relatórios e dashboards financeiros

> O projeto está em desenvolvimento ativo e novas funcionalidades serão adicionadas em breve.

