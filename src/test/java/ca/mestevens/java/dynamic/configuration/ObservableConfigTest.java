package ca.mestevens.java.dynamic.configuration;

import ca.mestevens.java.UnitTest;
import ca.mestevens.java.dynamic.configuration.data.ConfigAccess;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

import javax.ws.rs.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Category(UnitTest.class)
public class ObservableConfigTest {

    private Config config;
    private ConfigAccess configAccess;
    private ObservableConfig observableConfig;

    @Before
    @SneakyThrows
    public void setUp() {
        this.config = ConfigFactory.load();
        this.configAccess = Mockito.mock(ConfigAccess.class);
        Mockito.when(configAccess.getConfig())
                .thenReturn(config);
        this.observableConfig = new ObservableConfig(config, configAccess, 1L);
    }

    @After
    public void tearDown() {
        this.observableConfig = null;
        this.configAccess = null;
        this.config = null;
    }

    @Test
    @SneakyThrows
    public void subscriptionCreated() {

        final String keyValue = "strValue";
        final String newValue = "Another Value";
        final StringWrapper stringWrapper = new StringWrapper();

        final Observable<Boolean> booleanObservable = createBooleanObservableForConfigValue(keyValue, stringWrapper, value -> Assert.assertEquals(newValue, value));

        final Config modifiedConfig = createNewConfig(config, keyValue, newValue);
        Mockito.when(configAccess.getConfig())
                .thenReturn(modifiedConfig);

        Assert.assertEquals(booleanObservable.toBlocking().first(), true);
    }

    @Test
    @SneakyThrows
    public void subscriptionRemoved() {

        final String keyValue = "strValue";
        final String newValue = "Another Value";
        final StringWrapper stringWrapper = new StringWrapper();
        final IntWrapper intWrapper = new IntWrapper(0);

        final Observable<Boolean> booleanObservable = createBooleanObservableForConfigValue(keyValue, stringWrapper, value -> {
            intWrapper.addOne();
            Assert.assertEquals(newValue, value);
        });

        final Config modifiedConfig = createNewConfig(config, keyValue, newValue);
        Mockito.when(configAccess.getConfig())
                .thenReturn(modifiedConfig);

        Assert.assertEquals(booleanObservable.toBlocking().first(), true);

        observableConfig.unsubscribe(keyValue, stringWrapper.getString());

        Thread.sleep(2000);

        Assert.assertEquals(1, intWrapper.getInteger().intValue());
    }

    @Test
    @SneakyThrows
    public void multipleSubscriptionsCreated() {

        final String keyValue = "strValue";
        final String newValue = "Another Value";
        final StringWrapper stringWrapper = new StringWrapper();

        final Observable<Boolean> booleanObservable = createBooleanObservableForConfigValue(keyValue, stringWrapper, value -> Assert.assertEquals(newValue, value));
        final Observable<Boolean> secondBooleanObservable = createBooleanObservableForConfigValue(keyValue, stringWrapper, value -> Assert.assertEquals(newValue, value));

        final Config modifiedConfig = createNewConfig(config, keyValue, newValue);
        Mockito.when(configAccess.getConfig())
                .thenReturn(modifiedConfig);

        Assert.assertEquals(booleanObservable.toBlocking().first(), true);
        Assert.assertEquals(secondBooleanObservable.toBlocking().first(), true);
    }

    private Config createNewConfig(final Config config,
                                   final String key,
                                   final Object object) {
        return config.withValue(key, ConfigValueFactory.fromAnyRef(object));
    }

    private Observable<Boolean> createBooleanObservableForConfigValue(final String keyValue,
                                                                      final StringWrapper stringWrapper,
                                                                      final Action1 action1) {
        return Observable.create(subscriber -> {
            final String identifier = observableConfig.subscribe(keyValue, value -> {
                try {
                    action1.call(value);
                    subscriber.onNext(true);
                } catch (final Throwable throwable) {
                    subscriber.onNext(false);
                }
            });
            stringWrapper.setString(identifier);
        });
    }

    @Data
    private static class StringWrapper {
        private String string;
    }

    @Data
    @AllArgsConstructor
    private static class IntWrapper {
        private Integer integer;

        public void addOne() {
            integer++;
        }
    }

}
