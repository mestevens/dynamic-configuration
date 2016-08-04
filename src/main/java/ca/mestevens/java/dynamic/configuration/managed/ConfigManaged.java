package ca.mestevens.java.dynamic.configuration.managed;

import ca.mestevens.java.dynamic.configuration.data.ConfigAccess;
import com.typesafe.config.Config;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ConfigManaged implements Managed {

    private final ConfigAccess configAccess;
    private final Subscriber<? super Config> subscriber;
    private Config config;
    private final long seconds;

    private Subscription subscription;

    public ConfigManaged(final ConfigAccess configAccess,
                         final Subscriber<? super Config> subscriber,
                         final Config config) {
        this.configAccess = configAccess;
        this.config = config;
        this.subscriber = subscriber;
        this.seconds = config.getDuration("s3.dynamic.config.pollTime").getSeconds();
    }

    @Override
    public void start() throws Exception {
        this.subscription = Observable.interval(seconds, TimeUnit.SECONDS).subscribe(l -> {
            try {
                final Config s3Config = configAccess.getConfig();
                final Config mergedConfig = s3Config.withFallback(config);
                this.config = mergedConfig;
                subscriber.onNext(mergedConfig);
            } catch (final Exception ex) {
                log.info("{}", ex);
                log.info("Problem getting the config from S3: {}", ex.getMessage());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        this.subscription.unsubscribe();
    }

}
