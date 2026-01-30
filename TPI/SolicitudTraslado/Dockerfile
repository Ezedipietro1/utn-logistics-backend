# --- ETAPA 1: Build (Compilación) ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src

# --- ¡CAMBIO CLAVE AQUÍ! ---
# Forzamos a Maven a LIMPIAR cualquier 'target' viejo antes de empaquetar.
RUN mvn clean package -DskipTests

# --- ETAPA 2: Run (Ejecución) ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar

# Exponemos el puerto de SolicitudTraslado
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]