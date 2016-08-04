package ca.mestevens.java.dynamic.configuration.guice;

import ca.mestevens.java.dynamic.configuration.ObservableConfig;
import ca.mestevens.java.dynamic.configuration.bundle.ObservableConfigBundle;
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
import com.typesafe.config.Config;
import io.dropwizard.setup.Environment;

public class ObservableConfigS3Module extends AbstractModule {

    private final Environment environment;
    private final Config config;

    public ObservableConfigS3Module(final Environment environment,
                                    final Config config) {
        this.environment = environment;
        this.config = config;
    }

    @Override
    protected void configure() {
        final AWSCredentialsProviderChain awsCredentialsProviderChain = new AWSCredentialsProviderChain(
                new ProfileCredentialsProvider(),
                new EnvironmentVariableCredentialsProvider(),
                new SystemPropertiesCredentialsProvider(),
                new InstanceProfileCredentialsProvider());
        final AmazonS3 amazonS3 = new AmazonS3Client(awsCredentialsProviderChain);
        final ConfigAccess configAccess = new S3ConfigAccess(amazonS3, config);
        final ObservableConfigBundle observableConfigBundle = new ObservableConfigBundle(configAccess);
        bind(ObservableConfig.class).toInstance(observableConfigBundle.configure(environment, config));
    }

}
