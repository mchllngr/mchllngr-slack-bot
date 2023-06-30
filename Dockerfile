# Build with gradle image
FROM gradle:8.1.1 as builder

# Set build info, will be overridden
ARG SLACK_BOT_VERSION=unknown
ARG SLACK_BOT_COMMIT_HASH=unknown

# Set working dir
WORKDIR /

# Copy sourcecode into build image
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY versions.properties .
COPY src/ src/

# Build jar
RUN gradle clean build --no-daemon

# Switch to the java runtime image
FROM eclipse-temurin:20-jre-alpine

# Set working dir
WORKDIR /

# Copy jar into runtime image
COPY --from=builder build/libs/mchllngr-slack-bot-*.jar bot.jar

# Run it
CMD [ "java", "-jar",  "./bot.jar" ]
