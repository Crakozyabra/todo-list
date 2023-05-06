FROM tomcat:9.0.74-jdk17
COPY /target/todo-list.war /usr/local/tomcat/webapps/