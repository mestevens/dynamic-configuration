package ca.mestevens.java.dynamic.configuration.guice;

import ca.mestevens.java.dynamic.configuration.ObservableConfig;
import ca.mestevens.java.dynamic.configuration.data.ConfigAccess;
import ca.mestevens.java.dynamic.configuration.data.S3ConfigAccess;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;

public class ObservableConfigS3Module extends AbstractModule {

    private final Duration pollTime;
    private final String bucket;
    private final String key;
    private final boolean bindConfig;
    private final boolean bindAmazonS3;

    public ObservableConfigS3Module(final Duration pollTime,
                                    final String bucket,
                                    final String key) {
        this(pollTime, bucket, key, true, true);
    }

    public ObservableConfigS3Module(final Duration pollTime,
                                    final String bucket,
                                    final String key,
                                    final boolean bindConfig) {
        this(pollTime, bucket, key, bindConfig, true);
    }

    public ObservableConfigS3Module(final Duration pollTime,
                                    final String bucket,
                                    final String key,
                                    final boolean bindConfig,
                                    final boolean bindAmazonS3) {
        this.pollTime = pollTime;
        this.bucket = bucket;
        this.key = key;
        this.bindConfig = bindConfig;
        this.bindAmazonS3 = bindAmazonS3;
    }

    @Override
    protected void configure() {
        bind(ObservableConfig.class).asEagerSingleton();
        bind(ConfigAccess.class).to(S3ConfigAccess.class);
        bind(Long.class).annotatedWith(Names.named("dynamic.configuration.poll.time")).toInstance(pollTime.getSeconds());
        bind(String.class).annotatedWith(Names.named("dynamic.configuration.s3.bucket")).toInstance(bucket);
        bind(String.class).annotatedWith(Names.named("dynamic.configuration.s3.key")).toInstance(key);
        if (bindConfig) {
            bind(Config.class).toInstance(ConfigFactory.load());
        }
        if (bindAmazonS3) {
            final AWSCredentialsProviderChain awsCredentialsProviderChain = new AWSCredentialsProviderChain(
                    new ProfileCredentialsProvider(),
                    new EnvironmentVariableCredentialsProvider(),
                    new SystemPropertiesCredentialsProvider(),
                    new InstanceProfileCredentialsProvider());
            final AmazonS3 amazonS3 = new AmazonS3Client(awsCredentialsProviderChain);
            bind(AmazonS3.class).toInstance(amazonS3);
        }
    }

}
