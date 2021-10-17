FROM ubuntu:focal

ARG JDK_URL

ADD setup.sh /setup.sh
RUN ./setup.sh $JDK_URL

ENV JAVA_HOME /opt/openjdk
ENV PATH $JAVA_HOME/bin:$PATH