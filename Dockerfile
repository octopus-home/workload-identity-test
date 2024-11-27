FROM docker.io/library/openjdk:17
COPY ./target/widemo-1.0.jar widemo-1.0.jar
CMD ["java","-jar","widemo-1.0.jar"]