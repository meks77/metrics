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

    void requestEmployee(boolean requestWithError) {
        requestEmployee(0, requestWithError);
    }

    void requestEmployee(double duration, boolean requestWithError) {
        requestUrl(getUrlWithDurationParam(duration, EMPLOYEE_BASE_URL, requestWithError), requestWithError);
    }

    private void requestUrl(String s, boolean expectError) {
        if(expectError) {
            requestUrlAndVerifyHttpStatusCode(s, 500);
        } else {
            requestUrlAndVerifyHttpStatusCode(s, 200);
        }
    }

    private void requestUrlAndVerifyHttpStatusCode(String s, int expectedStatusCode) {
        RestAssured.when().get(getUrl(s)).then().statusCode(expectedStatusCode);
    }

    String getUrl(String relativeUrl) {
        return "http://localhost:" + serverPort + "/" + relativeUrl;
    }

    @NotNull
    private String getUrlWithDurationParam(double duration, String employeeBaseUrl, boolean requestWithError) {
        return employeeBaseUrl + "?sleepInMs=" + (int) (duration * 1000) + "&fail=" + requestWithError;
    }

    void requestOffice(boolean requestWithError) {
        requestUrl(getUrlWithDurationParam(0, OFFICE_BASE_URL, requestWithError), requestWithError);
    }

    void requestOffice(double duration, boolean requestWithError) {
        requestUrl(getUrlWithDurationParam(duration, OFFICE_BASE_URL, requestWithError), requestWithError);
    }
}
