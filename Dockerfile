FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY app.java calculator.java ./

RUN javac app.java calculator.java

EXPOSE 8080

CMD ["java", "app"]