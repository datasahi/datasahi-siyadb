# Build stage
FROM amazoncorretto:21 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bundleDistribution -x test

# Runtime stage
FROM amazoncorretto:21
WORKDIR /app

# Update system packages and specifically update libxml2 to the required version
RUN yum update -y && \
    yum install -y shadow-utils && \
    yum update -y libxml2-2.9.1-6.amzn2.5.18 && \
    yum clean all && \
    rm -rf /var/cache/yum

# Create necessary directories
RUN mkdir -p /app/config /app/work/logs

# Copy application artifacts
COPY --from=build /app/build/libs/datasahi-siyadb-0.1.3-all.jar /app/datasahi-siyadb-0.1.3-all.jar
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
CMD ["/bin/bash", "/app/start.sh"]
