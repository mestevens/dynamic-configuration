package ca.mestevens.java.dynamic.configuration.data;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.InputStreamReader;

public class S3ConfigAccess implements ConfigAccess {

    private final AmazonS3 s3Client;
    private final String bucket;
    private final String key;

    public S3ConfigAccess(final AmazonS3 s3Client,
                          final Config config) {
        this.s3Client = s3Client;
        this.bucket = config.getString("s3.dynamic.config.bucket");
        this.key = config.getString("s3.dynamic.config.key");
    }

    @Override
    public Config getConfig() {
        final S3Object s3 = s3Client.getObject(bucket, key);
        return ConfigFactory.parseReader(new InputStreamReader(s3.getObjectContent()));
    }

}
