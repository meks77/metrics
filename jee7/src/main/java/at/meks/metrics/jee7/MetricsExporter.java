package at.meks.metrics.jee7;


import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/metrics")
public class MetricsExporter {

    private Logger logger = Logger.getLogger(getClass().getName());

    @GET
    @Path("/application")
    @Produces("text/plain")
    public String getMetricsInTextFormat() {
        try(StringWriter writer = new StringWriter()) {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            return writer.toString();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOExcption happened while providing metrics a string", e);
            throw new MetricsExportException(e);
        }
    }

    private class MetricsExportException extends RuntimeException {
        private MetricsExportException(IOException e) {
            super(e);
        }
    }
}
