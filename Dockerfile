# Build with gradle image
FROM gradle:7.5.1 as builder

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
FROM adoptopenjdk/openjdk16:debianslim-jre

# Set working dir
WORKDIR /

# Copy jar into runtime image
COPY --from=builder build/libs/mchllngr-slack-bot-*.jar bot.jar

# Run it
CMD [ "java", "-jar",  "./bot.jar" ]
