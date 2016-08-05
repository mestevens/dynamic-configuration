package ca.mestevens.java.dynamic.configuration;

import ca.mestevens.java.dynamic.configuration.bundle.ObservableConfigBundle;
import ca.mestevens.java.dynamic.configuration.model.ActionIdentifier;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import rx.functions.Action1;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ObservableConfig {

    @Getter
    private Config config;

    private final Map<String, List<ActionIdentifier>> subscribeValues;

    @Inject
    public ObservableConfig(final Config initialConfig,
                            final ObservableConfigBundle observableConfigBundle) {
        this.config = initialConfig;
        this.subscribeValues = new HashMap<>();
        observableConfigBundle.getConfigObservable().subscribe(newConfig -> {
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
                                final List<ActionIdentifier> actions = subscribeValues.get(key);
                                actions.stream()
                                        .forEach(actionIdentifier -> actionIdentifier.getAction().call(object));
                            }
                        } catch (final ConfigException.Missing ex) {
                            log.debug("Key {} was not found in new config.", key);
                        }
                    });
            this.config = newConfig;
        });
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
        return actionIdentifier.getIdentifier();
    }

    public void unsubscribe(final String key,
                            final String identifier) {
        if (subscribeValues.containsKey(key)) {
            subscribeValues.put(key, subscribeValues.get(key)
                    .stream()
                    .filter(actionIdentifier -> !actionIdentifier.getIdentifier().equals(identifier))
                    .collect(Collectors.toList()));
        }
    }

}
