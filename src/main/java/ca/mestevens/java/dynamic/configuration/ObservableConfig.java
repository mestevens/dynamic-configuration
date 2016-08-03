package ca.mestevens.java.dynamic.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.functions.Action1;

import java.util.*;

@Slf4j
public class ObservableConfig {

    @Getter
    private Config config;

    private final Observable<Config> configObservable;

    private final Map<String, List<Action1>> subscribeValues;

    public ObservableConfig(final Config initialConfig,
                            final Observable<Config> configObservable) {
        this.config = initialConfig;
        this.configObservable = configObservable;
        this.subscribeValues = new HashMap<>();
        this.configObservable.subscribe(newConfig -> {
            subscribeValues.keySet().stream()
                    .forEach(key -> {
                        try {
                            final Object object = newConfig.getAnyRef(key);
                            boolean sameObject = true;
                            try {
                                final Object oldObject = this.config.getAnyRef(key);
                                sameObject = object.equals(oldObject);
                            } catch (final ConfigException.Missing ex) {
                                log.debug("Key {} was not found in old config.", key);
                                sameObject = false;
                            }
                            if (!sameObject) {
                                log.info("Key {} was updated in new config, updating subscribers.", key);
                                final List<Action1> actions = subscribeValues.get(key);
                                actions.stream()
                                        .forEach(action -> action.call(object));
                            }
                        } catch (final ConfigException.Missing ex) {
                            log.debug("Key {} was not found in new config.", key);
                        }
                    });
            this.config = newConfig;
        });
    }

    public <T> void subscribe(final String key,
                              final Action1<T> action) {
        if (subscribeValues.containsKey(key)) {
            subscribeValues.get(key).add(action);
        } else {
            final List<Action1> actionList = new ArrayList<>();
            actionList.add(action);
            subscribeValues.put(key, actionList);
        }
    }

    public void unsubscribe(final String key) {
        subscribeValues.remove(key);
    }

}
