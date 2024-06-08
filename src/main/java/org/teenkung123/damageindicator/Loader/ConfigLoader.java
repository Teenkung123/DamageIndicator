package org.teenkung123.damageindicator.Loader;

import org.bukkit.configuration.file.FileConfiguration;
import org.teenkung123.damageindicator.DamageIndicator;
import org.teenkung123.damageindicator.Utils.HealthBarSettings;

import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    private final DamageIndicator plugin;
    private final FileConfiguration config;
    private final HealthBarSettings defaultSettings;

    public ConfigLoader(DamageIndicator plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        defaultSettings = new HealthBarSettings(
                config.getDouble("Holograms.HealthBar.HeightOffset"),
                config.getInt("Holograms.HealthBar.HologramDuration"),
                config.getStringList("Holograms.HealthBar.Lines").toArray(new String[0]),
                config.getString("Holograms.HealthBar.Bar.Prefix"),
                config.getString("Holograms.HealthBar.Bar.Suffix"),
                config.getString("Holograms.HealthBar.Bar.Filler"),
                config.getString("Holograms.HealthBar.Bar.HealthFiller"),
                config.getString("Holograms.HealthBar.Bar.SeparateFiller"),
                config.getInt("Holograms.HealthBar.Bar.Width"),
                config.getString("Holograms.HealthBar.Bar.FillerColor"),
                config.getString("Holograms.HealthBar.Bar.HealthColor"),
                config.getBoolean("Holograms.HealthBar.Enabled", true)
        );
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
    }

    // HealthBar configurations
    public int getHealthBarUpdateRate() {
        return config.getInt("Holograms.HealthBar.UpdateRate", 2);
    }

    public HealthBarSettings getDefaultSettings() {
        return defaultSettings;
    }

    // DamageIndicator configurations
    public int getDamageIndicatorUpdateRate() {
        return config.getInt("Holograms.DamageIndicator.UpdateRate", 1);
    }

    public double getDamageIndicatorHeightOffset() {
        return config.getDouble("Holograms.DamageIndicator.HeightOffset", 0.0);
    }

    public double getDamageIndicatorInitialYSpeed() {
        return config.getDouble("Holograms.DamageIndicator.InitialYSpeed", 1.0);
    }

    public double getDamageIndicatorGravity() {
        return config.getDouble("Holograms.DamageIndicator.Gravity", 0.05);
    }

    public double getDamageIndicatorAnimationSpeed() {
        return config.getDouble("Holograms.DamageIndicator.AnimationSpeed", 0.1);
    }

    public String getDamageIndicatorDamagePrefix() {
        return config.getString("Holograms.DamageIndicator.Damage.Prefix", "-");
    }

    public String getDamageIndicatorDamageSuffix() {
        return config.getString("Holograms.DamageIndicator.Damage.Suffix", "");
    }

    public String getDamageIndicatorDamageColor() {
        return config.getString("Holograms.DamageIndicator.Damage.Color", "&c");
    }

    public String getDamageIndicatorDamageCriticalPrefix() {
        return config.getString("Holograms.DamageIndicator.Damage.CriticalPrefix", "-");
    }

    public String getDamageIndicatorDamageCriticalSuffix() {
        return config.getString("Holograms.DamageIndicator.Damage.CriticalSuffix", "");
    }

    public String getDamageIndicatorDamageCriticalColor() {
        return config.getString("Holograms.DamageIndicator.Damage.CriticalColor", "&c");
    }

    public String getDamageIndicatorHealingPrefix() {
        return config.getString("Holograms.DamageIndicator.Healing.Prefix", "+");
    }

    public String getDamageIndicatorHealingSuffix() {
        return config.getString("Holograms.DamageIndicator.Healing.Suffix", "");
    }

    public String getDamageIndicatorHealingColor() {
        return config.getString("Holograms.DamageIndicator.Healing.Color", "&a");
    }

    public String getDamageIndicatorMagicPrefix() {
        return config.getString("Holograms.DamageIndicator.Others.Prefix", "+");
    }

    public String getDamageIndicatorMagicSuffix() {
        return config.getString("Holograms.DamageIndicator.Others.Suffix", "");
    }

    public String getDamageIndicatorMagicColor() {
        return config.getString("Holograms.DamageIndicator.Others.Color", "&a");
    }

    public String getDamageIndicatorMagicCriticalPrefix() {
        return config.getString("Holograms.DamageIndicator.Others.CriticalPrefix", "-");
    }

    public String getDamageIndicatorMagicCriticalSuffix() {
        return config.getString("Holograms.DamageIndicator.Others.CriticalSuffix", "");
    }

    public String getDamageIndicatorMagicCriticalColor() {
        return config.getString("Holograms.DamageIndicator.Others.CriticalColor", "&c");
    }

    public boolean getDamageIndicatorEnabled() {
        return config.getBoolean("Holograms.DamageIndicator.Enabled", true);
    }

    @SuppressWarnings("DataFlowIssue")
    public Map<String, String> getTextReplace() {
        Map<String, String> replacements = new HashMap<>();
        for (String key : config.getConfigurationSection("Replacement").getKeys(false)) {
            replacements.put(key, config.getString("Replacement." + key));
        }
        return replacements;
    }

    @SuppressWarnings("DataFlowIssue")
    public Map<String, HealthBarSettings> getCustomHealthBars() {
        Map<String, HealthBarSettings> bars = new HashMap<>();
        for (String key : config.getConfigurationSection("MythicMobs").getKeys(false)) {
            HealthBarSettings settings = new HealthBarSettings(
                    config.getDouble("MythicMobs." + key + ".HeightOffset"),
                    config.getInt("MythicMobs." + key + ".HologramDuration"),
                    config.getStringList("MythicMobs." + key + ".Lines").toArray(new String[0]),
                    config.getString("MythicMobs." + key + ".Bar.Prefix"),
                    config.getString("MythicMobs." + key + ".Bar.Suffix"),
                    config.getString("MythicMobs." + key + ".Bar.Filler"),
                    config.getString("MythicMobs." + key + ".Bar.HealthFiller"),
                    config.getString("MythicMobs." + key + ".Bar.SeparateFiller"),
                    config.getInt("MythicMobs." + key + ".Bar.Width"),
                    config.getString("MythicMobs." + key + ".Bar.FillerColor"),
                    config.getString("MythicMobs." + key + ".Bar.HealthColor"),
                    config.getBoolean("MythicMobs." + key + ".Enabled", true)
            );
            bars.put(key, settings);
        }
        return bars;
    }

}
