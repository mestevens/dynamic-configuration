package ca.mestevens.java.dynamic.configuration.data;

import ca.mestevens.java.dynamic.configuration.exception.GetConfigException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.typesafe.config.Config;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class S3ConfigAccessTest {

    private AmazonS3 amazonS3;
    private final String BUCKET = "test-bucket";
    private final String KEY = "test-key";
    private S3ConfigAccess s3ConfigAccess;

    @Before
    public void setUp() {
        amazonS3 = Mockito.mock(AmazonS3.class);
        s3ConfigAccess = new S3ConfigAccess(amazonS3, BUCKET, KEY);
    }

    @After
    public void tearDown() {
        s3ConfigAccess = null;
        amazonS3 = null;
    }

    @Test
    @SneakyThrows
    public void s3GetObjectSuccess() {
        final S3Object s3Object = Mockito.mock(S3Object.class);
        final InputStream inputStream = new ByteArrayInputStream("tempValue = asdf".getBytes());

        Mockito.when(amazonS3.getObject(BUCKET, KEY))
                .thenReturn(s3Object);
        Mockito.when(s3Object.getObjectContent())
                .thenReturn(new S3ObjectInputStream(inputStream, null));

        final Config config = s3ConfigAccess.getConfig();
        Assert.assertEquals("asdf", config.getString("tempValue"));
    }

    @Test(expected = GetConfigException.class)
    @SneakyThrows
    public void s3GetObjectThrowsAmazonClientException() {
        Mockito.when(amazonS3.getObject(BUCKET, KEY))
                .thenThrow(new AmazonClientException(""));

        s3ConfigAccess.getConfig();
    }

    @Test(expected = GetConfigException.class)
    @SneakyThrows
    public void s3GetObjectThrowsAmazonServiceException() {
        Mockito.when(amazonS3.getObject(BUCKET, KEY))
                .thenThrow(new AmazonServiceException(""));

        s3ConfigAccess.getConfig();
    }

    @Test(expected = GetConfigException.class)
    @SneakyThrows
    public void s3GetObjectContentReturnsNull() {
        final S3Object s3Object = Mockito.mock(S3Object.class);

        Mockito.when(amazonS3.getObject(BUCKET, KEY))
                .thenReturn(s3Object);
        Mockito.when(s3Object.getObjectContent())
                .thenReturn(null);

        s3ConfigAccess.getConfig();
    }

}
