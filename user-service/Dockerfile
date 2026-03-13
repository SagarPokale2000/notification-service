# ─────────────────────────────────────────────────────
# Dockerfile for user-service
# Multi-stage build — keeps final image small
# ─────────────────────────────────────────────────────

# ══════════════════════════════════════
# STAGE 1 — BUILD
# Uses full Maven + JDK image to compile
# and package the app into a .jar file
# ══════════════════════════════════════
FROM maven:3.9.6-eclipse-temurin-17 AS build
# ↑ This image has both Maven AND JDK 17
# 'AS build' = name this stage so Stage 2 can reference it

WORKDIR /app
# ↑ All commands run inside /app folder inside container

# Copy pom.xml FIRST (before source code)
# Docker caches layers — if pom.xml hasn't changed,
# it won't re-download dependencies on next build ✅
COPY pom.xml .

# Download all dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Now copy source code
COPY src ./src

# Build the jar, skip tests (tests run in CI pipeline)
RUN mvn clean package -DskipTests

# ══════════════════════════════════════
# STAGE 2 — RUN
# Uses tiny JRE-only image (no Maven, no JDK)
# Final image is ~200MB instead of ~500MB
# ══════════════════════════════════════
FROM eclipse-temurin:17-jre-alpine
# ↑ Alpine Linux = tiny (5MB base)
# jre = Java Runtime only (no compiler needed to RUN)

WORKDIR /app

# Copy ONLY the jar from Stage 1
# Everything else (Maven, source code) is left behind
COPY --from=build /app/target/*.jar app.jar
#    ↑ from Stage 1   ↑ the jar      ↑ rename to app.jar

# Tell Docker this container listens on port 8081
EXPOSE 8081

# Command to run when container starts
ENTRYPOINT ["java", "-jar", "app.jar"]



# 
## Why multi-stage build?

# Single stage (bad):
#   Image contains: JDK + Maven + source code + jar = ~600MB

# Multi-stage (good):
#   Stage 1: JDK + Maven + source → builds jar (thrown away after)
#   Stage 2: JRE + jar only       → final image = ~180MB ✅

# Smaller image = faster deployment, less storage, more secure