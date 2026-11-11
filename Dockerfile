FROM eclipse-temurin:21-jdk-alpine
COPY target/MaverickBank-0.0.1-SNAPSHOT.jar maverick-bank.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","maverick-bank.jar"]