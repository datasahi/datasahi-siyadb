# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bundleDistribution -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S datasahi && adduser -S datasahi -G datasahi

# Create necessary directories
WORKDIR /app
RUN mkdir -p /app/config /app/work/logs && \
    chown -R datasahi:datasahi /app

# Copy application artifacts
COPY --from=build /app/build/libs/datasahi-siyadb-0.1.3-all.jar /app/datasahi-siyadb-0.1.3-all.jar
COPY src/main/assembly/docker-start-datasahi-siyadb.sh /app/start.sh
COPY src/main/assembly/stop-datasahi-siyadb.sh /app/stop.sh

# Set ownership and permissions
RUN chown -R datasahi:datasahi /app && \
    chmod +x /app/start.sh /app/stop.sh && \
    chmod 440 /app/datasahi-siyadb-0.1.3-all.jar

# Set default environment variables from the .RUN echo "Using version: $APP_VERSION"env file
ENV DATASAHI_PORT=8082
ENV DATASAHI_WORK_DIR=/app/work
ENV DATASAHI_CONFIG_PATHS=/app/config/siyadb.json

# Security hardening
RUN apk add --no-cache dumb-init && \
    rm -rf /var/cache/apk/*

# Switch to non-root user
USER datasahi

# Expose the port from the env file
EXPOSE 8082

# Use dumb-init as entrypoint
ENTRYPOINT ["/usr/bin/dumb-init", "--"]

# Entrypoint remains the start script
CMD ["/app/start.sh"]
