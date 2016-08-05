package ca.mestevens.java.dynamic.configuration.guice;

import ca.mestevens.java.dynamic.configuration.ObservableConfig;
import ca.mestevens.java.dynamic.configuration.data.ConfigAccess;
import ca.mestevens.java.dynamic.configuration.data.S3ConfigAccess;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.time.Duration;

public class ObservableConfigS3Module extends AbstractModule {

    private final Duration pollTime;
    private final String bucket;
    private final String key;

    public ObservableConfigS3Module(final Duration pollTime,
                                    final String bucket,
                                    final String key) {
        this.pollTime = pollTime;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    protected void configure() {
        bind(ObservableConfig.class).asEagerSingleton();
        bind(ConfigAccess.class).to(S3ConfigAccess.class);
        bind(Long.class).annotatedWith(Names.named("dynamic.configuration.poll.time")).toInstance(pollTime.getSeconds());
        bind(String.class).annotatedWith(Names.named("dynamic.configuration.s3.bucket")).toInstance(bucket);
        bind(String.class).annotatedWith(Names.named("dynamic.configuration.s3.key")).toInstance(key);
    }

}
