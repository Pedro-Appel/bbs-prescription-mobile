FROM openjdk:17

WORKDIR /home/app

RUN mkdir doctor
RUN mkdir patient

ARG PORT=8080

ENV PORT=$PORT
ENV JAVA_OPTS=""

EXPOSE $PORT

COPY target/mobile-app.jar app.jar

CMD [ "mkdir doctor", "mkdir patient" ]

ENTRYPOINT [ "java",$JAVA_OPTS, "-jar", "app.jar" ]