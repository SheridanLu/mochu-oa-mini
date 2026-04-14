FROM amazoncorretto:17

WORKDIR /app

COPY mochu-oa-1.0.0.jar app.jar

EXPOSE 9090

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]