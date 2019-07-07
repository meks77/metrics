package at.meks.metrics.bdd.tests.common;

public class TestedApplication {

    private String containerImageName;
    private String webContextRoot;
    private Integer mappedPort;

    public void setContainerImageName(String imageName) {
        containerImageName = imageName;
    }

    String getContainerImageName() {
        return containerImageName;
    }

    public void setApplicationWebContextRoot(String webContextRoot) {
        this.webContextRoot = webContextRoot;
    }

    String getApplicationWebContextRoot() {
        return this.webContextRoot;
    }

    void setApplicationServerPort(Integer mappedPort) {
        this.mappedPort = mappedPort;
    }

    Integer getApplicationServerPort() {
        return mappedPort;
    }
}
