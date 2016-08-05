package ca.mestevens.java.dynamic.configuration.data;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.InputStreamReader;

public class S3ConfigAccess implements ConfigAccess {

    private final AmazonS3 s3Client;
    private final String bucket;
    private final String key;

    @Inject
    public S3ConfigAccess(final AmazonS3 s3Client,
                          @Named("dynamic.configuration.s3.bucket") final String bucket,
                          @Named("dynamic.configuration.s3.key") final String key) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public Config getConfig() {
        final S3Object s3 = s3Client.getObject(bucket, key);
        return ConfigFactory.parseReader(new InputStreamReader(s3.getObjectContent()));
    }

}
