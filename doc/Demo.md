# Docker Setup for automatic Tests

Testing is done by build the demo artifact and deploying it to docker image with the appropiate java runtime container(jee7, jee8, microprofile server).

That means, when testing in an jee7 environment the demo-jee7 build
 * builds the application war
 * puts the application war to the docker image
 * builds a docker image using wildfly 10
 * starts the tests

For each test the docker container is started and after the test it is stopped. That takes a lot of time, but on the other hand it was less complex to implement the tests, because the metrics are reset.

It is expected that the docker host is available localy but with port 2375. That is done to enable Windows Home clients, to run Docker in a VM and forward the Port to the docker VM.

For the Test a lot of Ports need to be forwarded to the Docker VM. To this easily just execute the following scripts in a Powershell on Windows:

```
for($i=32700;$i -le 32999;$i++) {
  .\VBoxManage.exe controlvm "Ubuntu Server" natpf1 "Tcp$i,tcp,,$i,,$i"
}
```

If you want to remove those ports again a script can be executed:
```
for($i=32700;$i -le 32999;$i++) {
  .\VBoxManage.exe controlvm "Ubuntu Server" natpf1 delete "Tcp$i"
}
```

In both scripts "Ubuntu Server" must be replaced by the VM Name of the Docker VM in your Virtual-Box environment.

# Demo Applications
You can for sure simply deploy the demo applications to a java containter. The applications are prepared for wildfly and thorntail containers.

## EE7
Wildfly 10

* Method1: http://localhost:8081/jee7/api/v1.0/employee/15
* Method2: http://localhost:8081/jee7/api/v1.0/employee/office/15
* Metrics: http://localhost:8081/jee7/metrics

## EE8
Wildfly 16

* Method1: http://localhost:8081/jee8/api/v1.0/employee/15
* Method2: http://localhost:8081/jee8/api/v1.0/employee/office/15
* Metrics: http://localhost:8081/jee8/metrics

## Microprofile 13
Thorntail Microprofile 2.2 with Metrics 1.3

* Method1: http://localhost:8080/microprofile/v1.0/employee/15
* Method2: http://localhost:8080/microprofile/v1.0/employee/office/15
* Metrics: http://localhost:8080/microprofile/v1.0/metrics/application
