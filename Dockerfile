FROM openjdk:17
COPY target/vocabulary-0.1.jar /deployments/vocabulary-0.1.jar
CMD ["java", "-jar", "/deployments/vocabulary-0.1.jar"]