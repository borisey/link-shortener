FROM maven:3.9.9-eclipse-temurin-23

WORKDIR /link-shortener
COPY . .
RUN mvn clean install -DskipTests

CMD mvn spring-boot:run