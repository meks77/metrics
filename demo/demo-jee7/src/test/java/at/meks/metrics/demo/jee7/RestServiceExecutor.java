package at.meks.metrics.demo.jee7;

import io.restassured.RestAssured;
import org.jetbrains.annotations.NotNull;

class RestServiceExecutor {

    private static final String EMPLOYEE_BASE_URL = "jee7/api/v1.0/employee/15";
    private static final String OFFICE_BASE_URL = "jee7/api/v1.0/employee/office/15";
    private final int serverPort;

    RestServiceExecutor(int serverPort) {
        this.serverPort = serverPort;
    }

    void requestEmployee() {
        requestEmployee(0);
    }

    private void requestUrl(String s) {
        RestAssured.when().get(getUrl(s)).then().statusCode(200);
    }

    String getUrl(String relativeUrl) {
        return "http://localhost:" + serverPort + "/" + relativeUrl;
    }

    void requestOffice() {
        requestUrl(getUrlWithDurationParam(0, OFFICE_BASE_URL));
    }

    void requestEmployee(double duration) {
        requestUrl(getUrlWithDurationParam(duration, EMPLOYEE_BASE_URL));
    }

    @NotNull
    private String getUrlWithDurationParam(double duration, String employeeBaseUrl) {
        return employeeBaseUrl + "?sleepInMs=" + (int) (duration * 1000);
    }

    void requestOffice(double duration) {
        requestUrl(getUrlWithDurationParam(duration, OFFICE_BASE_URL));
    }
}
