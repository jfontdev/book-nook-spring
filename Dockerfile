FROM maven:3.8.5-openjdk-17

WORKDIR .

COPY . .

CMD mvn spring-boot:run