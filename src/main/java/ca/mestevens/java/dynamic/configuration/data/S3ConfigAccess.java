package ca.mestevens.java.dynamic.configuration.data;

import ca.mestevens.java.dynamic.configuration.exception.GetConfigException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStreamReader;

@Slf4j
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
    public Config getConfig() throws GetConfigException {
        try {
            final S3Object s3 = s3Client.getObject(bucket, key);
            log.info("Successfully got the config from S3.");
            try {
                return ConfigFactory.parseReader(new InputStreamReader(s3.getObjectContent()));
            } catch (final Throwable throwable) {
                throw new GetConfigException("Problem parsing S3 object content into config object.");
            }
        } catch (final AmazonClientException exception) {
            throw new GetConfigException(exception);
        }
    }

}
