FROM eclipse-temurin:17

LABEL maintainer="suhaskumarb748@gmail.com"

WORKDIR /app

COPY target/fear_greed_tracker-1.0.0.jar /app/feargreedtracker-docker.jar

ENTRYPOINT ["java","-jar","feargreedtracker-docker.jar"]