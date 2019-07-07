package at.meks.metrics.demo.jee8;

import at.meks.metrics.bdd.tests.common.ContainerManager;
import at.meks.metrics.bdd.tests.common.TestedApplication;
import cucumber.api.java.en.Given;

public class Jee8TestSteps {

    private final ContainerManager containerManager;

    public Jee8TestSteps(TestedApplication testedApplication, ContainerManager containerManager) {
        testedApplication.setContainerImageName("test/demo-jee8:latest");
        testedApplication.setApplicationWebContextRoot("jee8");
        this.containerManager = containerManager;
    }

    @Given("the jee8 demo application")
    public void setJee7DemoContext() {
        containerManager.startContainerAndGetContainerPort();
    }
}
