# =========================================================
# Etapa 1: Build - compila el proyecto con Maven
# =========================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar el cache de capas de Docker:
# si no cambian las dependencias, esta capa no se vuelve a descargar.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# =========================================================
# Etapa 2: Extract - separa el JAR en capas (layertools)
# Esto permite que Docker cachee dependencias por separado
# del código de la app, lo cual acelera builds futuros.
# =========================================================
FROM eclipse-temurin:21-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/target/events-service-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# =========================================================
# Etapa 3: Runtime - imagen final, liviana y sin herramientas de build
# =========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Buenas prácticas: no correr como root dentro del contenedor
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos las capas extraídas en orden de menor a mayor frecuencia de cambio
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/ ./

# Puerto definido en application.yml (server.port)
EXPOSE 8081

# Healthcheck básico usando el endpoint de actuator ya expuesto en tu config
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]