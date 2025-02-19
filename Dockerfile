FROM maven:3.9.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/Neotech-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
