package org.teenkung123.damageindicator.manager;

import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Handles custom placeholders for health bar holograms.
 */
public class PlaceholderManager {

    private final Map<String, Function<LivingEntity, String>> placeholders = new ConcurrentHashMap<>();

    /**
     * Registers a placeholder.
     *
     * @param key placeholder key without angle brackets
     * @param function function to compute the replacement text
     */
    public void registerPlaceholder(String key, Function<LivingEntity, String> function) {
        placeholders.put(format(key), function);
    }

    /**
     * Unregisters a placeholder.
     *
     * @param key placeholder key without angle brackets
     */
    public void unregisterPlaceholder(String key) {
        placeholders.remove(format(key));
    }

    private String format(String key) {
        String result = key;
        if (!result.startsWith("<")) {
            result = "<" + result;
        }
        if (!result.endsWith(">")) {
            result = result + ">";
        }
        return result;
    }

    /**
     * Applies all registered placeholders to the given text.
     *
     * @param text text containing placeholders
     * @param entity entity context
     * @return processed text
     */
    public String applyPlaceholders(String text, LivingEntity entity) {
        String result = text;
        for (Map.Entry<String, Function<LivingEntity, String>> e : placeholders.entrySet()) {
            result = result.replace(e.getKey(), e.getValue().apply(entity));
        }
        return result;
    }
}
