package at.meks.metrics.demo.application;


import at.meks.metrics.api.MonitorDurationHistogram;
import at.meks.metrics.api.MonitorException;
import at.meks.metrics.api.MonitorExecutionCount;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
@Path("/v1.0/employee")
@MonitorExecutionCount
public class EmployeeService {

    private Random rnd = new Random();

    private Logger logger = Logger.getLogger(EmployeeService.class.getName());

    @GET
    @Path("{id}")
    @Produces("text/plain")
    @MonitorException
    @MonitorDurationHistogram
    @MonitorExecutionCount
    public String getEmployee(@PathParam("id") String id,
            @DefaultValue("-1") @QueryParam("sleepInMs") int sleepInMs,
            @QueryParam("fail") Boolean fail) {
        failOrSleep(fail, sleepInMs);
        return "employee " + id;
    }

    private void failOrSleep(Boolean fail, int sleepInMs) {
        throwExceptionIfShouldFail(fail);
        sleepFor(sleepInMs);
    }

    private void throwExceptionIfShouldFail(Boolean fail) {
        if ((fail == null && rnd.nextDouble() > 0.99) || Boolean.FALSE.equals(fail)) {
            throw new IllegalStateException("Employee service not available");
        }
    }

    private void sleepFor(int sleepInMs) {
        if (sleepInMs == -1) {
            sleepRandomly();
        } else {
            sleep(sleepInMs);
        }
    }

    private void sleepRandomly() {
        int millis = rnd.nextInt(300);
        sleep(millis);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "sleep was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    @GET
    @Path("office/{employeeId}")
    @Produces("text/plain")
    @MonitorException
    @MonitorDurationHistogram
    @MonitorExecutionCount
    public String getOfficeOfEmployee(@PathParam("employeeId") String employeeId,
            @DefaultValue("-1") @QueryParam("sleepInMs") int sleepInMs,
            @QueryParam("fail") Boolean fail) {
        failOrSleep(fail, sleepInMs);
        return "office of employee " + employeeId;
    }

}
