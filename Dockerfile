FROM tomcat:11-jdk21
RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

RUN useradd --system --uid 10001 --shell /usr/sbin/nologin appuser \
    && chown -R 10001:0 /usr/local/tomcat \
    && chmod -R g+rwX /usr/local/tomcat
USER 10001