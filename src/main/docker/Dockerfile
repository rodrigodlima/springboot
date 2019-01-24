FROM rhel7

ENV ENVIRONMENT prod

RUN yum install -y java-1.8.0-openjdk-headless && yum clean all && rm -rf /var/cache/yum

ADD *.jar /app.jar

RUN sh -c 'touch /app.jar'

EXPOSE 8080

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar", "--spring.profiles.active=${ENVIRONMENT}"]