package org.teenkung123.damageindicator.Utils;

import com.maximde.hologramlib.hologram.TextHologram;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.teenkung123.damageindicator.DamageIndicator;
import org.teenkung123.damageindicator.Loader.ConfigLoader;
import org.teenkung123.damageindicator.manager.PlaceholderManager;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class HoloDisplays {

    private final DamageIndicator plugin;
    private final PlaceholderManager placeholderManager;
    private final boolean useMythicMobs;
    private final boolean usePlaceholderAPI;
    private final Object mythicBukkit;

    // We store the last health-bar text for each entity to avoid unnecessary .update() calls:
    private final Map<UUID, String> lastHealthTextMap = new HashMap<>();

    private final Team damageIndicatorTeam;
    private final HealthBarSettings defaultSettings;
    private final Map<String, HealthBarSettings> customHealthBars;
    private final boolean replacementEnabled;
    private final Map<Pattern, String> patternReplaceMap = new HashMap<>();
    private static final DecimalFormat TWO_DECIMAL_FORMAT = new DecimalFormat("0.00");

    // Health bar config
    private final int healthBarUpdateRate;

    // Damage indicator config
    private final int damageIndicatorUpdateRate;
    private final double damageIndicatorHeightOffset;
    private final double damageIndicatorInitialYSpeed;
    private final double damageIndicatorGravity;
    private final double damageIndicatorAnimationSpeed;
    private final String damageIndicatorDamagePrefix;
    private final String damageIndicatorDamageSuffix;
    private final String damageIndicatorDamageColor;
    private final String damageIndicatorDamageCriticalPrefix;
    private final String damageIndicatorDamageCriticalSuffix;
    private final String damageIndicatorDamageCriticalColor;
    private final String damageIndicatorHealPrefix;
    private final String damageIndicatorHealSuffix;
    private final String damageIndicatorHealColor;
    private final String damageIndicatorMagicPrefix;
    private final String damageIndicatorMagicSuffix;
    private final String damageIndicatorMagicColor;
    private final String damageIndicatorMagicCriticalPrefix;
    private final String damageIndicatorMagicCriticalSuffix;
    private final String damageIndicatorMagicCriticalColor;
    private final boolean damageIndicatorEnabled;

    private final Map<UUID, BukkitRunnable> taskMap = new HashMap<>();
    private final Map<UUID, Location> lastTeleportLoc = new HashMap<>();


    // For piecewise gradient color
    private static final int GREEN  = 0x00FF00;
    private static final int YELLOW = 0xFFFF00;
    private static final int ORANGE = 0xFFA500;
    private static final int RED    = 0xFF0000;

    public HoloDisplays(DamageIndicator plugin, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.placeholderManager = placeholderManager;
        this.useMythicMobs = plugin.getUseMythicMobs();
        this.usePlaceholderAPI = plugin.getUsePlaceholderAPI();
        this.mythicBukkit = plugin.getMythicBukkit();

        ConfigLoader configLoader = plugin.getConfigLoader();
        this.defaultSettings = configLoader.getDefaultSettings();
        this.healthBarUpdateRate = configLoader.getHealthBarUpdateRate();
        this.damageIndicatorUpdateRate = configLoader.getDamageIndicatorUpdateRate();
        this.damageIndicatorHeightOffset = configLoader.getDamageIndicatorHeightOffset();
        this.damageIndicatorInitialYSpeed = configLoader.getDamageIndicatorInitialYSpeed();
        this.damageIndicatorGravity = configLoader.getDamageIndicatorGravity();
        this.damageIndicatorAnimationSpeed = configLoader.getDamageIndicatorAnimationSpeed();
        this.damageIndicatorDamagePrefix = configLoader.getDamageIndicatorDamagePrefix();
        this.damageIndicatorDamageSuffix = configLoader.getDamageIndicatorDamageSuffix();
        this.damageIndicatorDamageColor = configLoader.getDamageIndicatorDamageColor();
        this.damageIndicatorDamageCriticalPrefix = configLoader.getDamageIndicatorDamageCriticalPrefix();
        this.damageIndicatorDamageCriticalSuffix = configLoader.getDamageIndicatorDamageCriticalSuffix();
        this.damageIndicatorDamageCriticalColor = configLoader.getDamageIndicatorDamageCriticalColor();
        this.damageIndicatorHealPrefix = configLoader.getDamageIndicatorHealingPrefix();
        this.damageIndicatorHealSuffix = configLoader.getDamageIndicatorHealingSuffix();
        this.damageIndicatorHealColor = configLoader.getDamageIndicatorHealingColor();
        this.damageIndicatorMagicPrefix = configLoader.getDamageIndicatorMagicPrefix();
        this.damageIndicatorMagicSuffix = configLoader.getDamageIndicatorMagicSuffix();
        this.damageIndicatorMagicColor = configLoader.getDamageIndicatorMagicColor();
        this.damageIndicatorMagicCriticalPrefix = configLoader.getDamageIndicatorMagicCriticalPrefix();
        this.damageIndicatorMagicCriticalSuffix = configLoader.getDamageIndicatorMagicCriticalSuffix();
        this.damageIndicatorMagicCriticalColor = configLoader.getDamageIndicatorMagicCriticalColor();
        this.damageIndicatorEnabled = configLoader.getDamageIndicatorEnabled();
        this.customHealthBars = configLoader.getCustomHealthBars();
        this.replacementEnabled = configLoader.getReplacementEnabled();
        Map<String, String> textReplace = configLoader.getTextReplace();

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
        Team team = mainScoreboard.getTeam("DamageIndicatorBars");
        if (team == null) {
            team = mainScoreboard.registerNewTeam("DamageIndicatorBars");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        this.damageIndicatorTeam = team;

        for (Map.Entry<String, String> entry : textReplace.entrySet()) {
            patternReplaceMap.put(Pattern.compile(entry.getKey()), entry.getValue());
        }
    }

    public Map<UUID, BukkitRunnable> getTaskMap() { return taskMap; }

    // Decide if we should handle this entity at all.
    public boolean shouldHandleEvent(Entity entity) {
        return !entity.isInvulnerable() && !entity.hasMetadata("NPC");
    }

    public void handleDamageEvent(Entity entity, double amount, AttackType type, boolean isCritical) {
        if (!(entity instanceof LivingEntity living)
                || living.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null) {
            return;
        }
        // Refresh the health bar when damage is taken
        displayHealthBar(living, false);

        // Show damage indicator if enabled
        if (damageIndicatorEnabled && amount != 0.0) {
            displayDamageIndicator(living, amount, type, isCritical);
        }
    }

    /**
     * Displays or refreshes the health bar hologram for an entity.
     * If the hologram doesn't exist, we create a new one with setUpdateTaskPeriod
     * so HologramLib can handle movement updates automatically.
     */
    public void displayHealthBar(LivingEntity entity, boolean noUpdate) {
        String mythicMobsType = getMythicMobType(entity);
        HealthBarSettings currentSettings = customHealthBars.getOrDefault(mythicMobsType, defaultSettings);
        if (!currentSettings.enabled()) return;

        // Skip certain entities
        if (plugin.getUseMyPet() &&
                entity.getClass().getName().equals("de.Keyle.MyPet.api.entity.MyPetBukkitEntity")) return;
        if (entity.getType() == EntityType.ARMOR_STAND || entity instanceof Player
                || !entity.getPassengers().isEmpty() || entity.hasMetadata("NPC")) {
            return;
        }

        UUID entityId = entity.getUniqueId();
        final String healthId = "damageindicator_health_" + entityId;

        // Create location above the entity
        Location baseLoc = entity.getLocation().clone()
                .add(0, entity.getHeight() + currentSettings.heightOffset(), 0);

        TextHologram healthHolo;

        if (!plugin.getHologramManager().hologramExists(healthId)) {
            // Create new hologram only once
            healthHolo = new TextHologram(healthId)
                    .setUpdateTaskPeriod(5L);
            plugin.getHologramManager().spawn(healthHolo, baseLoc);
        } else {
            healthHolo = (TextHologram) plugin.getHologramManager().getHologram(healthId).get();
            healthHolo.teleport(baseLoc);
        }

        // Update text right away
        updateHealthHologram(healthHolo, entity, mythicMobsType);

        // Cancel any old scheduled removal or movement task
        if (taskMap.containsKey(entityId)) {
            taskMap.get(entityId).cancel();
        }

        // Create a repeating task to track movement
        BukkitRunnable updateHealthTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid()) {
                    // If entity is dead or invalid, remove the hologram
                    plugin.getHologramManager().remove(healthId);
                    cancel();
                    return;
                }

                // Calculate desired location
                Location newLoc = entity.getLocation().clone()
                        .add(0, entity.getHeight() + currentSettings.heightOffset(), 0);

                // Compare with last known location
                Location oldLoc = lastTeleportLoc.get(entity.getUniqueId());
                if (oldLoc == null || oldLoc.distanceSquared(newLoc) > 0.01) {
                    // Only teleport if distance >= 0.1 blocks
                    healthHolo.teleport(newLoc);
                    lastTeleportLoc.put(entity.getUniqueId(), newLoc);
                }

                // Optionally re-check text (if we want “live” health changes, e.g. regen):
                if (!noUpdate) {
                    updateHealthHologram(healthHolo, entity, mythicMobsType);
                }
            }
        };
        updateHealthTask.runTaskTimer(plugin, healthBarUpdateRate, healthBarUpdateRate);

        // Store the task so it can be canceled if needed
        taskMap.put(entityId, updateHealthTask);
    }

    /**
     * Creates a floating damage/healing indicator.
     */
    public void displayDamageIndicator(LivingEntity entity, double amount, AttackType type, boolean isCritical) {
        String mythicMobsType = getMythicMobType(entity);
        HealthBarSettings currentSettings = customHealthBars.getOrDefault(mythicMobsType, defaultSettings);

        String hologramId = "damageindicator_" + (type == AttackType.HEALING ? "heal_" : "damage_")
                + ThreadLocalRandom.current().nextLong();

        Location baseLoc = entity.getLocation().clone()
                .add(0, entity.getHeight() + damageIndicatorHeightOffset, 0);

        TextHologram amountHolo = new TextHologram(hologramId)
                // Quick updates for the short-lived indicator
                .setUpdateTaskPeriod(1L)
                .setViewRange(plugin.getConfigLoader().getHealthBarAlwaysVisibleDistance());
        plugin.getHologramManager().spawn(amountHolo, baseLoc);

        // Format text
        String formattedAmount = TWO_DECIMAL_FORMAT.format(amount);
        String line;
        switch (type) {
            case DAMAGE -> {
                if (!isCritical) {
                    line = damageIndicatorDamagePrefix + damageIndicatorDamageColor
                            + replace(formattedAmount) + damageIndicatorDamageSuffix;
                } else {
                    line = damageIndicatorDamageCriticalPrefix + damageIndicatorDamageCriticalColor
                            + replace(formattedAmount) + damageIndicatorDamageCriticalSuffix;
                }
            }
            case HEALING -> {
                line = damageIndicatorHealPrefix + damageIndicatorHealColor
                        + replace(formattedAmount) + damageIndicatorHealSuffix;
            }
            case MAGIC -> {
                if (!isCritical) {
                    line = damageIndicatorMagicPrefix + damageIndicatorMagicColor
                            + replace(formattedAmount) + damageIndicatorMagicSuffix;
                } else {
                    line = damageIndicatorMagicCriticalPrefix + damageIndicatorMagicCriticalColor
                            + replace(formattedAmount) + damageIndicatorMagicCriticalSuffix;
                }
            }
            default -> {
                line = damageIndicatorDamagePrefix + damageIndicatorDamageColor
                        + replace(formattedAmount) + damageIndicatorDamageSuffix;
            }
        }
        amountHolo.setMiniMessageText(ColorTranslator.toMiniMessageFormat(line));

        animateHologram(amountHolo, entity, currentSettings);

        BukkitRunnable removeAmountHoloTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getHologramManager().remove(hologramId);
            }
        };
        removeAmountHoloTask.runTaskLater(plugin, currentSettings.hologramDuration());
    }

    /**
     * Displays a custom text indicator above an entity.
     *
     * @param entity target entity
     * @param text   text to display
     */
    public void displayCustomIndicator(LivingEntity entity, String text) {
        String mythicMobsType = getMythicMobType(entity);
        HealthBarSettings currentSettings = customHealthBars.getOrDefault(mythicMobsType, defaultSettings);

        String hologramId = "damageindicator_custom_" + ThreadLocalRandom.current().nextLong();

        Location baseLoc = entity.getLocation().clone()
                .add(0, entity.getHeight() + damageIndicatorHeightOffset, 0);

        TextHologram holo = new TextHologram(hologramId)
                .setUpdateTaskPeriod(1L)
                .setViewRange(plugin.getConfigLoader().getHealthBarAlwaysVisibleDistance());
        plugin.getHologramManager().spawn(holo, baseLoc);

        String line = placeholderManager.applyPlaceholders(text, entity);
        holo.setMiniMessageText(ColorTranslator.toMiniMessageFormat(line));

        animateHologram(holo, entity, currentSettings);

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getHologramManager().remove(hologramId);
            }
        }.runTaskLater(plugin, currentSettings.hologramDuration());
    }

    /**
     * Animate the indicator drifting up; no .update() calls needed
     * since setUpdateTaskPeriod(1L) handles it in the background.
     */
    private void animateHologram(TextHologram holo, Entity entity, HealthBarSettings currentSettings) {
        double angle = ThreadLocalRandom.current().nextDouble(0, 2*Math.PI);
        final double xSpeed = damageIndicatorAnimationSpeed * Math.cos(angle);
        final double zSpeed = damageIndicatorAnimationSpeed * Math.sin(angle);
        final double[] ySpeed = { damageIndicatorInitialYSpeed };

        final Location loc = holo.getLocation().clone();
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 40 || loc.getY() <= entity.getLocation().getY()) {
                    plugin.getHologramManager().remove(holo);
                    cancel();
                    return;
                }
                loc.add(xSpeed, ySpeed[0], zSpeed);
                ySpeed[0] -= damageIndicatorGravity;
                holo.teleport(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, damageIndicatorUpdateRate, damageIndicatorUpdateRate);
    }

    /**
     * Core method to update the entity's health bar text.
     * We call compareAndSetText(...) to avoid spamming .update().
     */
    private void updateHealthHologram(TextHologram holo, LivingEntity entity, @Nullable String mobType) {
        double currentHealth = entity.getHealth();
        double maxHealth = Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

        HealthBarSettings settings = customHealthBars.getOrDefault(mobType, defaultSettings);
        String entityName = entity.getName();
        String entityNameUncolored = entityName.replaceAll("(?i)[§&][0-9A-FK-OR]", "");

        // Build all lines
        StringBuilder sb = new StringBuilder();
        for (String s : settings.lines()) {
            String line = s
                    .replace("<name>", replace(entityName))
                    .replace("<name_uncolored>", replace(entityNameUncolored))
                    .replace("<health>", TWO_DECIMAL_FORMAT.format(currentHealth))
                    .replace("<max_health>", TWO_DECIMAL_FORMAT.format(maxHealth))
                    .replace("<bar>", createHealthBar(currentHealth, maxHealth, mobType, false))
                    .replace("<hp_percent>", TWO_DECIMAL_FORMAT.format((currentHealth / maxHealth) * 100))
                    .replace("<hp_percent_bar>", createHealthBar(currentHealth, maxHealth, mobType, true));

            if (usePlaceholderAPI) {
                line = PlaceholderAPI.setPlaceholders(null, line);
            }
            line = placeholderManager.applyPlaceholders(line, entity);
            sb.append(line).append("\n");
        }
        // Trim trailing newline
        String finalText = sb.substring(0, sb.length() - 1);

        // Apply text only if changed, to avoid spamming .update()
        compareAndSetText(holo, entity.getUniqueId(), finalText);
    }

    /**
     * Compares newText to last known text for this entity.
     * If different, sets text and calls .update() once.
     */
    private void compareAndSetText(TextHologram holo, UUID entityId, String newText) {
        String oldText = lastHealthTextMap.getOrDefault(entityId, "");
        if (!oldText.equals(newText)) {
            holo.setMiniMessageText(ColorTranslator.toMiniMessageFormat(newText));
            holo.update();  // Force immediate text refresh
            lastHealthTextMap.put(entityId, newText);
        }
    }

    private String createHealthBar(double currentHealth, double maxHealth, @Nullable String mobType, boolean withPercent) {
        HealthBarSettings settings = customHealthBars.getOrDefault(mobType, defaultSettings);
        final int width = settings.width();
        if (width <= 0) return "";

        double ratio = Math.max(0, Math.min(1, currentHealth / maxHealth));
        int filledBars = (int) Math.round(ratio * width);
        int emptyBars = width - filledBars;

        boolean doOverride = settings.overrideColor();
        final String barColor = doOverride ? getPiecewiseGradientColor(ratio) : settings.fillerColor();

        String prefix = settings.prefix();
        String suffix = settings.suffix();
        String fillerColor = settings.fillerColor();
        String healthFiller = settings.healthFiller();
        String filler = settings.filler();

        StringBuilder builder = new StringBuilder(prefix.length() + suffix.length() + (width * 2) + 30);
        builder.append(prefix);

        if (withPercent) {
            double pct = ratio * 100.0;
            final String percentColor = doOverride ? barColor : settings.percentColor();
            String percentText = percentColor + " " + TWO_DECIMAL_FORMAT.format(pct) + "% ";
            int insertIndex = (filledBars + emptyBars) / 2;
            for (int i = 0; i < (filledBars + emptyBars); i++) {
                if (i == insertIndex) {
                    builder.append(percentText);
                }
                if (i < filledBars) {
                    builder.append(barColor).append(healthFiller);
                } else {
                    builder.append(fillerColor).append(filler);
                }
            }
        } else {
            for (int i = 0; i < filledBars; i++) {
                builder.append(barColor).append(healthFiller);
            }
            for (int i = 0; i < emptyBars; i++) {
                builder.append(fillerColor).append(filler);
            }
        }
        builder.append(suffix);
        return builder.toString();
    }

    /**
     * Returns the MythicMobs type for the entity if relevant.
     */
    private String getMythicMobType(Entity entity) {
        if (useMythicMobs && mythicBukkit != null) {
            try {
                if (plugin.getMythicBukkit().getMobManager().isMythicMob(entity)) {
                    return plugin.getMythicBukkit().getMobManager()
                            .getMythicMobInstance(entity)
                            .getMobType();
                }
            } catch (NullPointerException ignored) {
            }
        }
        return null;
    }

    private String replace(String string) {
        if (!replacementEnabled) {
            return string;
        }
        for (Map.Entry<Pattern, String> entry : patternReplaceMap.entrySet()) {
            string = entry.getKey().matcher(string).replaceAll(entry.getValue());
        }
        return string;
    }

    // Gradient color logic
    private String getPiecewiseGradientColor(double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        if (ratio >= 0.75) {
            double sub = (ratio - 0.75) / 0.25;
            int color = interpolateColor(YELLOW, GREEN, sub);
            return toColorCode(color);
        } else if (ratio >= 0.50) {
            double sub = (ratio - 0.50) / 0.25;
            int color = interpolateColor(ORANGE, YELLOW, sub);
            return toColorCode(color);
        } else if (ratio >= 0.25) {
            double sub = (ratio - 0.25) / 0.25;
            int color = interpolateColor(RED, ORANGE, sub);
            return toColorCode(color);
        } else {
            return toColorCode(RED);
        }
    }

    private int interpolateColor(int startColor, int endColor, double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r1 = (startColor >> 16) & 0xFF, g1 = (startColor >> 8) & 0xFF, b1 = startColor & 0xFF;
        int r2 = (endColor >> 16) & 0xFF, g2 = (endColor >> 8) & 0xFF, b2 = endColor & 0xFF;
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    private String toColorCode(int color) {
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        return String.format("&#%02X%02X%02X", r, g, b);
    }
}
