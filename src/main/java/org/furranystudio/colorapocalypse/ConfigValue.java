/**
 * File: ConfigValue.java
 * Author: Maxime66410
 * Created: 2026-08-27
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse;

// One setting: a key, its current value, and its default. Loader-agnostic replacement for
// ForgeConfigSpec.IntValue/BooleanValue, backed by Config's hand-rolled JSON file instead.
public final class ConfigValue<T> {

    private final String key;
    private final T defaultValue;
    private T value;

    ConfigValue(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        Config.save();
    }

    String key() {
        return key;
    }

    T defaultValue() {
        return defaultValue;
    }

    void setRaw(T value) {
        this.value = value;
    }
}
