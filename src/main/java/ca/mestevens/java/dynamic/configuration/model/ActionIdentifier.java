package ca.mestevens.java.dynamic.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import rx.functions.Action1;

import java.util.UUID;

@Value
@AllArgsConstructor
public class ActionIdentifier {

    private final String identifier;

    private final Action1 action;

    public ActionIdentifier(final Action1 action) {
        this.identifier = UUID.randomUUID().toString();
        this.action = action;
    }

}
