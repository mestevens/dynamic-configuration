package ca.mestevens.java.dynamic.configuration.dropwizard.rest;

import ca.mestevens.java.dynamic.configuration.ObservableConfig;
import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/another")
public class AnotherResource {

    private String endpointText;

    @Inject
    public AnotherResource(final ObservableConfig observableConfig) {
        this.endpointText = observableConfig.getConfig().getString("strValue");
        observableConfig.<String>subscribe("strValue", str -> endpointText = str);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        return Response.ok(String.format("Formatted! %s", endpointText)).build();
    }

}
