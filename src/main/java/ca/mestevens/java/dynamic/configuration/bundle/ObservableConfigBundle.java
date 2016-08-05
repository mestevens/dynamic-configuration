package ca.mestevens.java.dynamic.configuration.bundle;

import ca.mestevens.java.dynamic.configuration.data.ConfigAccess;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ObservableConfigBundle {

    private Config lastConfig;

    @Getter
    private final Observable<Config> configObservable;

    @Inject
    public ObservableConfigBundle(final ConfigAccess configAccess,
                                  final Config config,
                                  @Named("dynamic.configuration.poll.time") final Long pollTime) {
        this.lastConfig = config;
        this.configObservable = Observable.<Config>create(subscriber -> {
            try {
                final Config s3Config = configAccess.getConfig();
                final Config mergedConfig = s3Config.withFallback(lastConfig);
                this.lastConfig = mergedConfig;
                subscriber.onNext(mergedConfig);
            } catch (final Exception ex) {
                log.info("Problem getting the config from S3: {}", ex.getMessage());
            }
        }).repeatWhen(observable -> Observable.interval(pollTime, TimeUnit.SECONDS));
    }

}
