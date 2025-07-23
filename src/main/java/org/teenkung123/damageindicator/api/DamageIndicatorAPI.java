package org.teenkung123.damageindicator.api;

import com.maximde.hologramlib.hologram.HologramManager;
import org.bukkit.entity.LivingEntity;
import org.teenkung123.damageindicator.DamageIndicator;
import org.teenkung123.damageindicator.Utils.HoloDisplays;
import org.teenkung123.damageindicator.manager.PlaceholderManager;

import java.util.function.Function;

/**
 * Public API access point for the DamageIndicator plugin.
 */
public class DamageIndicatorAPI {

    private static DamageIndicatorAPI instance;
    private final DamageIndicator plugin;

    public DamageIndicatorAPI(DamageIndicator plugin) {
        this.plugin = plugin;
        instance = this;
    }

    /**
     * Returns the API instance.
     */
    public static DamageIndicatorAPI getInstance() {
        return instance;
    }

    /**
     * Registers a placeholder for health bar holograms.
     *
     * @param key      placeholder key without angle brackets
     * @param function function to compute the replacement text
     */
    public void registerPlaceholder(String key, Function<LivingEntity, String> function) {
        plugin.getPlaceholderManager().registerPlaceholder(key, function);
    }

    /**
     * Unregisters a placeholder.
     *
     * @param key placeholder key without angle brackets
     */
    public void unregisterPlaceholder(String key) {
        plugin.getPlaceholderManager().unregisterPlaceholder(key);
    }

    /**
     * Displays a custom damage indicator at the given entity.
     *
     * @param entity target entity
     * @param text   text to display
     */
    public void displayDamageIndicator(LivingEntity entity, String text) {
        plugin.getHoloDisplays().displayCustomIndicator(entity, text);
    }

    /**
     * Access to the plugin's hologram manager.
     */
    public HologramManager getHologramManager() {
        return plugin.getHologramManager();
    }

    /**
     * Access to the HoloDisplays manager.
     */
    public HoloDisplays getHoloDisplays() {
        return plugin.getHoloDisplays();
    }

    /**
     * Access to the placeholder manager.
     */
    public PlaceholderManager getPlaceholderManager() {
        return plugin.getPlaceholderManager();
    }
}
