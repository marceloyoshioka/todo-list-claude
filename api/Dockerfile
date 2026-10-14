# ---- Estagio 1: build (JDK + Maven wrapper) ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /build

# Copia so o necessario para resolver dependencias; essa camada fica em cache
# enquanto o pom.xml nao mudar.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q -DskipTests package

# ---- Estagio 2: runtime (apenas JRE) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

COPY --from=build /build/target/todo-list-*.jar app.jar

# Dentro do container o navegador chega pela rede do Docker (nao como localhost),
# entao o H2 console precisa aceitar conexoes "remotas". Fora do Docker o padrao
# continua false. Para desligar: docker run -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=false
ENV SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
