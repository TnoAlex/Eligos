FROM eclipse-temurin:17-jdk-jammy

ENV TZ=GMT
ENV LANG C.UTF-8

COPY . /opt/eligos

RUN chmod +x /opt/eligos/gradlew
RUN ["/bin/sh","/opt/eligos/gradlew", "-p", "/opt/eligos","build"]
RUN chmod +x /opt/eligos/eligos-cli/build/scriptsShadow/eligos-cli
RUN ln -s /opt/eligos/eligos-cli/build/scriptsShadow/eligos-cli /usr/bin/eligos-cli
RUN mkdir -p /opt/eligos/eligos-cli/build/lib
RUN mv /opt/eligos/eligos-cli/build/libs/* /opt/eligos/eligos-cli/build/lib

RUN mkdir -p /dist/project /dist/result
WORKDIR /dist
VOLUME /dist/project /dist/result

ENTRYPOINT ["eligos-cli"]
