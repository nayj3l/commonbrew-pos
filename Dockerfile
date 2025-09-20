# Step 1: Build the application
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# copy source (includes src/main/resources/ca.pem)
COPY src src

# create truststore inside the build container (non-interactive)
RUN keytool -import -trustcacerts -alias mysqlCA \
    -file src/main/resources/ca.pem \
    -keystore /app/truststore.jks -storepass changeit -noprompt

RUN ./mvnw dependency:go-offline
RUN ./mvnw clean package -DskipTests

# Step 2: Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy truststore & jar from build stage
COPY --from=build /app/truststore.jks /app/truststore.jks
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

CMD ["java","-Djavax.net.ssl.trustStore=/app/truststore.jks","-Djavax.net.ssl.trustStorePassword=changeit","-jar","app.jar"]
