# dynamic-configuration

## Description

A dropwizard based dynamic configuration add-on. Allows you to have typesafe config values that you can subscribe to for updates when they are updated.

## Dependency Information

```
<dependency>
    <groupId>ca.mestevens.java</groupId>
    <artifactId>dynamic-configuration</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage

### Configuration

In order to use this, you'll need to use the [Dropwizard](dropwizard.github.io/dropwizard) framework.

If you're using Guice, this becomes extremely simple to add to your project. In your Dropwizard Application class in the `run` method simply add the following module to your Guice Injector.

```
new AbstractModule(environment, config)
```

Where `environment` is your dropwizard environment, and `config` is the typesafe config you want to use as a base configuration class.

If you're not using Guice, you should be able to create an `ObservableConfig` object (which will have to be shared around your project in order to use the dynamic config), by calling the `configure` method on an `ObservableConfigBundle`. For example the following code will create an `ObservableConfig` backed by a file in S3.

```
final AmazonS3 amazonS3 = new AmazonS3Client();
final ConfigAccess configAccess = new S3ConfigAccess(amazonS3, config);
final ObservableConfigBundle observableConfigBundle = new ObservableConfigBundle(configAccess);
final ObservableConfig observableConfig = observableConfigBundle.configure(environment, config);
```

Again, where `environment` is your dropwizard environment, and `config` is the typesafe config you want to use as a base configuration class.

### Typesafe Configuration Values

Additionally, in your typesafe config file that you're using as the base, you'll need the following values:

```
s3 {
  dynamic {
    config {
      bucket = s3BucketHere
      key = confFileHere
      pollTime = 1m
    }
  }
}
```

`bucket` is the S3 bucket to use, `key` is the file/key name of the file to use, and `pollTime` is the duration at which to poll the config.

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