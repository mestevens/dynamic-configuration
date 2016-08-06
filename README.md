# dynamic-configuration

## Description

A dynamic configuration for typesafe configs.

## Dependency Information

```
<dependency>
    <groupId>ca.mestevens.java</groupId>
    <artifactId>dynamic-configuration</artifactId>
    <version>0.5</version>
</dependency>
```

## Usage

### Configuration

If you're using Guice, this becomes extremely simple to add to your project. Simply add the following module to your Guice Injector.

```
final Duration pollTime = Duration.ofSeconds(10);
final String bucket = "s3BucketHere";
final String key = "s3KeyOfBucketObjectHere.conf";
new ObservableConfigS3Module(pollTime, bucket, key);
```

The above example will poll the S3 bucket "s3BucketHere" every 10 seconds for the key "s3KeyOfBucketObjectHere.conf". Using the module as shown above will automatically bind the default loaded typesafe config object as well as a default AmazonS3 object configured to check a bunch of places for credentials.

If you want to bind your own Config object and/or AmazonS3 object you can tell the module to NOT bind those objects for you as shown below.

```
final Duration pollTime = Duration.ofSeconds(10);
final String bucket = "s3BucketHere";
final String key = "s3KeyOfBucketObjectHere.conf";
final boolean bindConfig = false;
final boolean bindAmazonS3 = false;
new ObservableConfigS3Module(pollTime, bucket, key, bindConfig, bindAmazonS3);
```

If you're not using Guice, you should be able to create an `ObservableConfig` object (which will have to be shared around your project in order to use the dynamic config).

```
final Config config = ConfigurationFactory.load();
final AmazonS3 amazonS3 = new AmazonS3Client();
final ConfigAccess configAccess = new S3ConfigAccess(amazonS3, "s3BucketHere", "s3KeyOfBucketObjectHere.conf");
final ObservableConfig observableConfig = new ObservableConfig(config, configAccess, 10);
```

### Consuming

After the dynamic configuration has been configured, you can get updated config values by doing the following:

```
public class TestClass {

    private String text;

    public TestClass(final ObservableConfig observableConfig) {
        this.text = observableConfig.getConfig().getString("key");
        observableConfig.<String>subscribe("key", str -> text = str);
    }

}
```

Now whenever the key "key" is updated in s3, the action specified in the subscribe method will be called.