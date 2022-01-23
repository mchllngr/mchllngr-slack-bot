# Build with gradle image
FROM gradle:7.1.1 as builder

# Set working dir
WORKDIR /

# Copy sourcecode into build image
COPY build.gradle.kts ./
COPY settings.gradle.kts ./
COPY versions.properties ./
COPY src/ ./src/

# Build jar
RUN gradle clean build --no-daemon

# Switch to the java runtime image
FROM adoptopenjdk/openjdk16:debianslim-jre

# Set working dir
WORKDIR /

# Copy jar into runtime image
COPY --from=builder ./home/gradle/build/libs/mchllngr-slack-bot-1.0.jar ./bot.jar

# Run it
CMD [ "java", "-jar",  "./bot.jar" ]
