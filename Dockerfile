# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia os arquivos de configuração primeiro (para melhor cache)
COPY pom.xml .
COPY src ./src

# Compila o projeto e gera o JAR
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia apenas o JAR final da etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
