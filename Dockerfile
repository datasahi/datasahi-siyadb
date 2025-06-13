# Build stage
FROM gradle:8-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bundleDistribution -x test

# Runtime stage
FROM openjdk:21-slim
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/config /app/work/logs

# Copy application artifacts
COPY --from=build /app/build/libs/datasahi-siyadb-0.1.2-all.jar /app/datasahi-siyadb-0.1.2-all.jar
COPY src/main/assembly/docker-start-datasahi-siyadb.sh /app/start.sh
COPY src/main/assembly/stop-datasahi-siyadb.sh /app/stop.sh

# Set default environment variables from the .RUN echo "Using version: $APP_VERSION"env file
ENV DATASAHI_PORT=8082
ENV DATASAHI_WORK_DIR=/app/work
ENV DATASAHI_CONFIG_PATHS=/app/config/siyadb.json

# Expose the port from the env file
EXPOSE 8082

# Set executable permissions
RUN chmod +x /app/start.sh /app/stop.sh

# Entrypoint remains the start script
CMD ["/app/start.sh"]
