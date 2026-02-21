# Build: docker build -t blk-hacking-ind-name-lastname .
# OS: Linux (Eclipse Temurin Alpine for minimal footprint and security updates)
FROM eclipse-temurin:17-jre-alpine

# Labels and maintainer
LABEL maintainer="BlackRock Challenge"
# Application must run on port 5477 inside the container
EXPOSE 5477

WORKDIR /app

# Copy built artifact (build with: mvn -DskipTests clean package)
COPY target/*.jar app.jar

# Run on port 5477
ENV SERVER_PORT=5477
ENTRYPOINT ["java", "-jar", "app.jar"]
