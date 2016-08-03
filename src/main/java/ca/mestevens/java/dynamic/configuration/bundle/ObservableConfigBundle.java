package ca.mestevens.java.dynamic.configuration.bundle;

import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.configuration.bundle.TypesafeConfigurationBundle;
import ca.mestevens.java.dynamic.configuration.ObservableConfig;
import ca.mestevens.java.dynamic.configuration.managed.ConfigManaged;
import com.amazonaws.services.s3.AmazonS3;
import com.typesafe.config.Config;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import rx.Observable;

public class ObservableConfigBundle<T extends TypesafeConfiguration> implements ConfiguredBundle<T> {

    @Getter
    private ObservableConfig observableConfig;

    private final AmazonS3 amazonS3;

    public ObservableConfigBundle() {
        this.amazonS3 = null;
    }

    public ObservableConfigBundle(final AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public void initialize(final Bootstrap bootstrap) {
        bootstrap.addBundle(new TypesafeConfigurationBundle());
    }

    @Override
    public void run(final T configuration,
                    final Environment environment) throws Exception {
        final Observable<Config> managedSubscriberObservable =
                Observable.create(subscriber -> {
                    final ConfigManaged configManaged;
                    if (amazonS3 != null) {
                        configManaged = new ConfigManaged(amazonS3, subscriber, configuration.getConfig());
                    } else {
                        configManaged = new ConfigManaged(subscriber, configuration.getConfig());
                    }
                    environment.lifecycle().manage(configManaged);
                });

        this.observableConfig = new ObservableConfig(configuration.getConfig(), managedSubscriberObservable);
    }
}
