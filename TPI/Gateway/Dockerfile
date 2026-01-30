# --- ETAPA 1: Build (Compilación) ---
# Usamos la imagen de Maven con JDK 21 (basado en tu error anterior)
FROM maven:3.9-eclipse-temurin-21 AS builder

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos primero el pom.xml para cachear las dependencias
COPY pom.xml .
# Descargamos las dependencias
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente
COPY src ./src

# Compilamos el proyecto y creamos el .jar (saltamos tests)
RUN mvn package -DskipTests

# --- ETAPA 2: Run (Ejecución) ---
# Usamos una imagen de JRE 21 minimalista
FROM eclipse-temurin:21-jre-jammy

# Establecemos el directorio de trabajo
WORKDIR /app

# Definimos un argumento para encontrar el .jar (sin saber la versión)
ARG JAR_FILE=target/*.jar

# Copiamos el .jar compilado desde la etapa 'builder'
COPY --from=builder /app/${JAR_FILE} app.jar

# Exponemos el puerto en el que corre el Gateway
EXPOSE 8083

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]