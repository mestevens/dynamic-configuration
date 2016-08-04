package ca.mestevens.java.dynamic.configuration.bundle;

import ca.mestevens.java.dynamic.configuration.ObservableConfig;
import ca.mestevens.java.dynamic.configuration.data.ConfigAccess;
import ca.mestevens.java.dynamic.configuration.managed.ConfigManaged;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.dropwizard.setup.Environment;
import rx.Observable;

public class ObservableConfigBundle {

    private final ConfigAccess configAccess;

    @Inject
    public ObservableConfigBundle(final ConfigAccess configAccess) {
        this.configAccess = configAccess;
    }

    public ObservableConfig configure(final Environment environment,
                                      final Config config) {
        final Observable<Config> managedSubscriberObservable =
                Observable.create(subscriber -> {
                    final ConfigManaged configManaged = new ConfigManaged(configAccess, subscriber, config);
                    environment.lifecycle().manage(configManaged);
                });

        return new ObservableConfig(config, managedSubscriberObservable);
    }
}
