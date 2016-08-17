package ca.mestevens.java.dynamic.configuration;

import ca.mestevens.java.dynamic.configuration.data.ConfigAccess;
import ca.mestevens.java.dynamic.configuration.model.ActionIdentifier;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.functions.Action1;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ObservableConfig {

    @Getter
    private Config config;
    private final ConfigAccess configAccess;
    private final Long pollTime;
    private final Map<String, List<ActionIdentifier>> subscribeValues;

    @Inject
    public ObservableConfig(final Config initialConfig,
                            final ConfigAccess configAccess,
                            @Named("dynamic.configuration.poll.time")final Long pollTime) {
        this.config = initialConfig;
        this.configAccess = configAccess;
        this.pollTime = pollTime;
        this.subscribeValues = new HashMap<>();
        createObservable()
                .repeatWhen(this::shouldRepeat)
                .subscribe(this::subscription);
    }

    private Observable<Config> createObservable() {
        return Observable.create(subscriber -> {
            try {
                final Config s3Config = configAccess.getConfig();
                final Config mergedConfig = s3Config.withFallback(config);
                subscriber.onNext(mergedConfig);
            } catch (final Throwable throwable) {
                log.error("Problem getting the config from S3: {}", throwable.getMessage());
            }
        });
    }

    private Observable<Long> shouldRepeat(final Observable<? extends Void> observable) {
        return Observable.interval(pollTime, TimeUnit.SECONDS);
    }

    private void subscription(final Config newConfig) {
        subscribeValues.keySet().stream()
                .forEach(key -> {
                    try {
                        final Object object = newConfig.getAnyRef(key);
                        if (isObjectUpdated(object, key)) {
                            log.info("Key {} was updated in new config, updating subscribers.", key);
                            final List<ActionIdentifier> actions = subscribeValues.get(key);
                            actions.stream()
                                    .forEach(actionIdentifier -> {
                                        try {
                                            actionIdentifier.getAction().call(object);
                                        } catch (final Throwable throwable) {
                                            log.error("Identifier {} threw exception.", actionIdentifier.getIdentifier(), throwable);
                                        }
                                    });
                        }
                    } catch (final ConfigException.Missing ex) {
                        log.debug("Key {} was not found in new config.", key);
                    }
                });
        this.config = newConfig;
    }

    private boolean isObjectUpdated(final Object object,
                                    final String key) {
        try {
            final Object oldObject = this.config.getAnyRef(key);
            return object.equals(oldObject);
        } catch (final ConfigException.Missing ex) {
            log.debug("Key {} was not found in old config.", key);
            return false;
        }
    }

    public <T> String subscribe(final String key,
                              final Action1<T> action) {
        final ActionIdentifier actionIdentifier = new ActionIdentifier(action);
        if (subscribeValues.containsKey(key)) {
            subscribeValues.get(key).add(actionIdentifier);
        } else {
            final List<ActionIdentifier> actionList = new ArrayList<>();
            actionList.add(actionIdentifier);
            subscribeValues.put(key, actionList);
        }
        final String identifier = actionIdentifier.getIdentifier();
        log.info("Identifier {} subscribed to key {}.", identifier, key);
        return identifier;
    }

    public void unsubscribe(final String key,
                            final String identifier) {
        if (subscribeValues.containsKey(key)) {
            subscribeValues.put(key, subscribeValues.get(key)
                    .stream()
                    .filter(actionIdentifier -> !actionIdentifier.getIdentifier().equals(identifier))
                    .collect(Collectors.toList()));
            if (subscribeValues.get(key).size() == 0) {
                subscribeValues.remove(key);
            }
            log.info("Identifier {} unsubscribed to key {}.", identifier, key);
        }
    }

}
