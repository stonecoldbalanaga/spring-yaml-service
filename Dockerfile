# -----------------------------------------------
# Stage 1: Build
# -----------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

# Copy Maven wrapper and POM first for better layer caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Download dependencies (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR (skip tests during image build)
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# -----------------------------------------------
# Stage 2: Run
# -----------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from build stage
COPY --from=build /workspace/target/*.jar app.jar

# Optional: mount your own appvalues.yaml at /app/appvalues.yaml
# or place it in src/main/resources before building
VOLUME /app/config

# Use the non-root user
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
