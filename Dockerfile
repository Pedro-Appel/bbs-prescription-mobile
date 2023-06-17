FROM maven:3.9.2-eclipse-temurin-17 AS MAVEN_BUILD
RUN mkdir /home/project && mkdir -p /root/.m2/repository/br/com/bbs/crypto
WORKDIR /home/project
COPY crypto-component.zip ../
RUN apt-get update \
&& apt-get install unzip \
&& unzip ../crypto-component.zip -d /root/.m2/repository/br/com/bbs/crypto
COPY . .
RUN mvn clean package

FROM eclipse-temurin:17-jre-alpine
RUN apk add dumb-init
RUN mkdir /app
RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
WORKDIR /app
RUN mkdir doctor
RUN mkdir patient
ARG PORT=8080
ENV PORT=$PORT
EXPOSE $PORT
COPY --from=MAVEN_BUILD /home/project/target/mobile-app.jar app.jar
RUN chown -R javauser:javauser /app
USER javauser
CMD ["dumb-init", "java", "-jar", "app.jar"]