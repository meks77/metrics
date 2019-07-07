package at.meks.metrics.bdd.tests.common;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ContainerManager {

    private final TestedApplication testedApplication;
    private GenericContainer jeeContainer;

    public ContainerManager(TestedApplication testedApplication) {
        this.testedApplication = testedApplication;
    }

    public void startContainerAndGetContainerPort() {
        jeeContainer = new GenericContainer(testedApplication.getContainerImageName())
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/" + testedApplication.getApplicationWebContextRoot())
                        .forStatusCode(403)
                        .withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES)));
        jeeContainer.start();
        testedApplication.setApplicationServerPort(jeeContainer.getFirstMappedPort());
    }

    void stopContainer() {
        if (jeeContainer.isRunning()) {
            jeeContainer.stop();
        }
    }

}
