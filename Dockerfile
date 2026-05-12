FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
COPY config/ config/

RUN ./mvnw -q -DskipTests package \
    && cp "$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' | head -n 1)" target/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring \
    && adduser -S spring -G spring \
    && mkdir -p /app/logs \
    && chown -R spring:spring /app

COPY --from=build /workspace/target/app.jar /app/app.jar

ENV APP_LOG_PATH=/app/logs

EXPOSE 8080

USER spring

ENTRYPOINT ["sh", "-c", "if [ -n \"$DATABASE_URL\" ] && [ -z \"$DB_URL\" ]; then raw=\"$DATABASE_URL\"; case \"$raw\" in jdbc:*) DB_URL=\"$raw\" ;; postgresql://*) rest=${raw#postgresql://}; creds=${rest%%@*}; hostdb=${rest#*@}; dbuser=${creds%%:*}; dbpass=${creds#*:}; DB_URL=\"jdbc:postgresql://$hostdb?user=$dbuser&password=$dbpass\" ;; *) DB_URL=\"$raw\" ;; esac; export DB_URL; fi; exec java -jar /app/app.jar"]
