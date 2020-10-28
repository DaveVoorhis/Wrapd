package org.reldb.toolbox.configuration;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationSettings {

    static class ConfigurationSetting {
        public final String element;
        public final String value;
        public final String comment;

        ConfigurationSetting(String element, String value, String comment) {
            this.element = element;
            this.value = value;
            this.comment = comment;
        }
    }

    private Map<String, ConfigurationSetting> settings = new HashMap<>();

    protected void add(String element, String value, String comment) {
        settings.put(element, new ConfigurationSetting(element, value, comment));
    }

    protected void add(String element, String value) {
        add(element, value, null);
    }

    public Map<String, ConfigurationSetting> getSettings() {
        return settings;
    }

    public abstract void registration();

}
