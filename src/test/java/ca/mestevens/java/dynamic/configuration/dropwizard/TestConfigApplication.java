package ca.mestevens.java.dynamic.configuration.dropwizard;

import ca.mestevens.java.dynamic.configuration.dropwizard.rest.AnotherResource;
import ca.mestevens.java.dynamic.configuration.dropwizard.rest.TestResource;
import ca.mestevens.java.dynamic.configuration.guice.ObservableConfigS3Module;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.configuration.bundle.TypesafeConfigurationBundle;

public class TestConfigApplication extends Application<TypesafeConfiguration> {

    public static void main(final String[] args) throws Exception {
        new TestConfigApplication().run("server");
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

        final Config config = configuration.getConfig();

        final Injector injector = Guice.createInjector(
                new ObservableConfigS3Module(config.getDuration("s3.dynamic.config.pollTime"),
                        config.getString("s3.dynamic.config.bucket"),
                        config.getString("s3.dynamic.config.key")));

        environment.jersey().register(injector.getInstance(TestResource.class));
        environment.jersey().register(injector.getInstance(AnotherResource.class));

    }

}
