package ca.mestevens.java.dynamic.configuration.managed;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConfigManaged implements Managed {

    private final AmazonS3 s3Client;
    private final String bucket;
    private final String key;
    private final Subscriber<? super Config> subscriber;
    private Config config;
    private final long seconds;

    private Subscription subscription;

    public ConfigManaged(final AmazonS3 s3Client,
                         final Subscriber<? super Config> subscriber,
                         final Config config) {
        this.s3Client = s3Client;
        this.config = config;
        this.subscriber = subscriber;
        this.bucket = config.getString("s3.dynamic.config.bucket");
        this.key = config.getString("s3.dynamic.config.key");
        this.seconds = config.getDuration("s3.dynamic.config.pollTime").getSeconds();
    }

    public ConfigManaged(final Subscriber<? super Config> subscriber,
                         final Config config) {
        this(new AmazonS3Client(new ProfileCredentialsProvider()), subscriber, config);
    }

    @Override
    public void start() throws Exception {
        this.subscription = Observable.interval(seconds, TimeUnit.SECONDS).subscribe(l -> {
            try {
                final URL s3Url = s3Client.getUrl(bucket, key);
                final Config s3Config = ConfigFactory.parseURL(s3Url);
                final Config mergedConfig = s3Config.withFallback(config);
                this.config = mergedConfig;
                subscriber.onNext(mergedConfig);
            } catch (final Exception ex) {
                log.info("Problem getting the config from S3: {}", ex.getMessage());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        this.subscription.unsubscribe();
    }

}
