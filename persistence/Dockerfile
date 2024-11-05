FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

# Set the working directory
WORKDIR /app

# Copy the project, excluding specified directories and files
ADD . /app
RUN find . -name target -type d -exec rm -rf {} + && \
    rm -rf .idea .bsp field.json field.xml

RUN sbt compile

# Run the PersistenceServer
CMD sbt "persistence/runMain PersistenceServer"