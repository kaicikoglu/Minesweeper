# Start from Ubuntu
FROM ubuntu:latest

# Install curl and OpenJDK 17
RUN apt-get update && apt-get install -y curl openjdk-17-jdk

# Set the working directory
WORKDIR /app

# Download and extract sbt
RUN curl -L -o sbt-1.9.8.tgz https://github.com/sbt/sbt/releases/download/v1.9.8/sbt-1.9.8.tgz && \
    tar -xvzf sbt-1.9.8.tgz && \
    rm sbt-1.9.8.tgz

# Update PATH environment variable
ENV PATH="/app/sbt/bin:${PATH}"

# Copy the project, excluding specified directories and files
COPY . ./
RUN find . -name target -type d -exec rm -rf {} + && \
    rm -rf .idea .bsp field.json field.xml

RUN sbt compile

# Run the ModelServer
CMD sbt "model/runMain ModelServer"