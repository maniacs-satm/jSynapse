FROM centos:centos7

MAINTAINER dizzy "george.niculae79@gmail.com"

ADD . /jSynapse/src/
WORKDIR /jSynapse/src/

# get to the current
RUN yum update

# install tools for building jSynapse
RUN yum install -y java-1.7.0-openjdk maven

# let's build jSynapse
RUN mvn clean install

EXPOSE 5555:5555

# start jSynapse server
CMD java -jar target/jSynapse-1.0-SNAPSHOT.jar
