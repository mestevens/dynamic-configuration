package ca.mestevens.java.dynamic.configuration.integration;

import ca.mestevens.java.IntegrationTest;
import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.dynamic.configuration.dropwizard.TestConfigApplication;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

@Category(IntegrationTest.class)
public class DynamicConfigurationIntegrationTest {

    @ClassRule
    public static DropwizardAppRule<TypesafeConfiguration> dropwizardAppRule =
            new DropwizardAppRule<>(TestConfigApplication.class);

    private static Client client;

    @BeforeClass
    public static void setUp() {
        client = new JerseyClientBuilder(dropwizardAppRule.getEnvironment()).build("Test Client");
    }

    @Test
    @SneakyThrows
    public void dynamicConfigWorksAfterUpdate() {

        final Response response = client
                .target(String.format("http://localhost:8080/test"))
                .request()
                .get();

        Assert.assertEquals("InitialValue", response.readEntity(String.class));

        Thread.sleep(4000);

        final Response secondResponse = client
                .target(String.format("http://localhost:8080/test"))
                .request()
                .get();

        Assert.assertEquals("asdf", secondResponse.readEntity(String.class));

    }

}
