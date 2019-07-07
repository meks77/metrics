package at.meks.metrics.demo.jee7;

import at.meks.metrics.bdd.tests.common.ContainerManager;
import at.meks.metrics.bdd.tests.common.TestedApplication;
import cucumber.api.java.en.Given;

public class Jee7TestSteps {

    private final ContainerManager containerManager;

    public Jee7TestSteps(TestedApplication testedApplication, ContainerManager containerManager) {
        testedApplication.setContainerImageName("test/demo-jee7:latest");
        testedApplication.setApplicationWebContextRoot("jee7");
        this.containerManager = containerManager;
    }

    @Given("the jee7 demo application")
    public void setJee7DemoContext() {
        containerManager.startContainerAndGetContainerPort();
    }
}
