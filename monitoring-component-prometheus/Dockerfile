FROM oracle/graalvm-ce:20.2.0-java11 as builder

WORKDIR /app
COPY build/libs/kafka-consumer-lag-monitoring-prometheus*all.jar /app/jar/application.jar

RUN gu install native-image
RUN native-image -jar jar/application.jar --no-fallback --report-unsupported-elements-at-runtime --allow-incomplete-classpath

FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /work/
COPY --from=builder /app/application /work/application

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

CMD ["./application"]