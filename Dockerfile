FROM openjdk:17
WORKDIR /src
COPY target/dwarfs-game-bot-0.0.1-SNAPSHOT.jar /src/dwarfs-game-bot-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "dwarfs-game-bot-0.0.1-SNAPSHOT.jar"]