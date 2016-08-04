package ca.mestevens.java.dynamic.configuration.dropwizard;

import ca.mestevens.java.dynamic.configuration.dropwizard.rest.AnotherResource;
import ca.mestevens.java.dynamic.configuration.dropwizard.rest.TestResource;
import ca.mestevens.java.dynamic.configuration.guice.ObservableConfigS3Module;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.configuration.bundle.TypesafeConfigurationBundle;

public class TestConfigApplication extends Application<TypesafeConfiguration> {

    public static void main(final String[] args) throws Exception {
        new TestConfigApplication().run(args);
    }

    @Override
    public String getName() {
        return "TestConfig";
    }

    @Override
    public void initialize(final Bootstrap<TypesafeConfiguration> bootstrap) {
        bootstrap.addBundle(new TypesafeConfigurationBundle());
    }

    @Override
    public void run(final TypesafeConfiguration configuration,
                    final Environment environment) {

        final Injector injector = Guice.createInjector(new ObservableConfigS3Module(environment, configuration.getConfig()));

        environment.jersey().register(injector.getInstance(TestResource.class));
        environment.jersey().register(injector.getInstance(AnotherResource.class));

    }

}
