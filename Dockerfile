FROM maven:3.9.9 AS builder

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN mvn package

FROM openjdk:21-jdk

COPY --from=builder /usr/src/app/target/bids-bridge-*.jar /usr/app/app.jar

ENTRYPOINT ["java","-jar","/usr/app/app.jar"]