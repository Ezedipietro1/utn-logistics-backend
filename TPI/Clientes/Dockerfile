FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . /app
RUN mvn -B -DskipTests -Dproject.build.sourceEncoding=UTF-8 package


FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/data/clientes.db
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.sqlite.JDBC
EXPOSE 8081
VOLUME ["/data"]
ENTRYPOINT ["java","-jar","/app/app.jar"]