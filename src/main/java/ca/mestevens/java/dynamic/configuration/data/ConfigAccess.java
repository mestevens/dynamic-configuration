package ca.mestevens.java.dynamic.configuration.data;

import ca.mestevens.java.dynamic.configuration.exception.GetConfigException;
import com.typesafe.config.Config;

public interface ConfigAccess {

    Config getConfig() throws GetConfigException;

}
