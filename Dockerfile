FROM docker.io/library/openjdk:17
COPY ./target/workload-identity-1.0.jar workload-identity-1.0.jar
CMD ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005","-jar","workload-identity-1.0.jar"]