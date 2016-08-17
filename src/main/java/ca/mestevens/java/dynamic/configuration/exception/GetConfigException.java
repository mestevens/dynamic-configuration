package ca.mestevens.java.dynamic.configuration.exception;

public class GetConfigException extends Exception {

    public GetConfigException(final Throwable exception) {
        super(exception);
    }

    public GetConfigException(final String message) {
        super(message);
    }

}
