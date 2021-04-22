FROM navikt/java:14
FROM mortenlj/kafka-debug:latest

COPY build/libs/*.jar ./
