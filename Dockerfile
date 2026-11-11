FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sh mvnw dependency:go-offline -q
COPY src src
RUN sh mvnw clean package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 app \
 && mkdir -p /data/almacen \
 && chown -R app:app /data
USER app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]