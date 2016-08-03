package ca.mestevens.java.dynamic.configuration.dropwizard.rest;

import ca.mestevens.java.dynamic.configuration.ObservableConfig;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    private String endpointText;

    @Inject
    public TestResource(final ObservableConfig observableConfig) {
        final Config config = observableConfig.getConfig();
        endpointText = config.getString("strValue");

        observableConfig.<String>subscribe("strValue", str -> endpointText = str);
    }

    @GET
    public Response getTest() {
        return Response.ok(endpointText).build();
    }

}
