package org.furranystudio.colorapocalypse.settings;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SettingsRegistry {

    public interface Setting {
        String get();

        /**
         * @return {@code null} on success, or an error message describing why the value was rejected
         */
        String trySet(String rawValue);
    }

    private static final Map<String, Setting> SETTINGS = new LinkedHashMap<>();

    private SettingsRegistry() {
    }

    public static void register(String name, Setting setting) {
        SETTINGS.put(name, setting);
    }

    public static Setting get(String name) {
        return SETTINGS.get(name);
    }

    public static Iterable<String> names() {
        return SETTINGS.keySet();
    }
}
