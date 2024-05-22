# Start from Ubuntu
FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

# Set the working directory
WORKDIR /app

# Copy the project, excluding specified directories and files
COPY . /app

RUN sbt clean update compile

EXPOSE 8080

# Run the ModelServer
CMD ["sbt", "model/runMain ModelServer"]