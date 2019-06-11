package at.meks.metrics.demo.application;


import at.meks.metrics.api.MonitorDurationHistogram;
import at.meks.metrics.api.MonitorException;
import at.meks.metrics.api.MonitorExecutionCount;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    public String getEmployee(@PathParam("id") String id) {
        return doRandomCall("employee " + id);
    }

    @GET
    @Path("office/{employeeId}")
    @Produces("text/plain")
    @MonitorException
    @MonitorDurationHistogram
    @MonitorExecutionCount
    public String getOfficeOfEmployee(@PathParam("employeeId") String employeeId) {
        return doRandomCall("office of employee " + employeeId);
    }

    private String doRandomCall(String returnValue) {
        if (rnd.nextDouble() > 0.99) {
            throw new IllegalStateException("Employee service not available");
        } else {
            try {
                Thread.sleep(rnd.nextInt(300));
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "sleep was interrupted", e);
                Thread.currentThread().interrupt();
            }
            return returnValue;
        }
    }

}
