FROM jboss/wildfly:10.1.0.Final
LABEL descripion = "Wildfly with java ee7 and the demo for metrics"
ADD build/libs/demo-jee7-*.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080