package org.teenkung123.damageindicator.Utils;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.utils.PAPI;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.teenkung123.damageindicator.Loader.ConfigLoader;
import org.teenkung123.damageindicator.DamageIndicator;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class HoloDisplays {

    private final DamageIndicator plugin;
    private final ConfigLoader configLoader;
    private final Map<UUID, BukkitRunnable> taskMap = new HashMap<>();
    private final HealthBarSettings defaultSettings;
    private Team damageIndicatorTeam;

    // Configuration for health bar properties
    private final int healthBarUpdateRate;

    // Configuration for damage indicator properties
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

    private final Map<String, HealthBarSettings> customHealthBars;
    private final Map<String, String> textReplace;

    public HoloDisplays(DamageIndicator plugin) {
        this.plugin = plugin;
        this.configLoader = plugin.getConfigLoader();
        this.defaultSettings = configLoader.getDefaultSettings();

        // Load health bar configurations
        this.healthBarUpdateRate = configLoader.getHealthBarUpdateRate();

        // Load damage indicator configurations
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

        this.textReplace = configLoader.getTextReplace();
        this.customHealthBars = configLoader.getCustomHealthBars();

        // Initialize scoreboard team
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getMainScoreboard();
        damageIndicatorTeam = board.getTeam("DamageIndicatorBars");
        if (damageIndicatorTeam == null) {
            damageIndicatorTeam = board.registerNewTeam("DamageIndicatorBars");
            damageIndicatorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
    }

    /**
     * Determines whether the event should be handled for the given entity.
     *
     * @param entity The entity to check.
     * @return True if the event should be handled, false otherwise.
     */
    public boolean shouldHandleEvent(Entity entity) {
        if (entity instanceof Player) {
            return false;
        }
        if (entity.isInvulnerable()) {
            return false;
        }
        return !entity.hasMetadata("NPC");
    }

    /**
     * Handles the logic for creating and updating holograms when an entity is damaged or healed.
     *
     * @param entity    The entity that was damaged or healed.
     * @param amount    The amount of damage or healing.
     * @param type      The type of damage display.
     */
    @SuppressWarnings("ExtractMethodRecommender")
    public void handleDamageEvent(Entity entity, double amount, AttackType type, boolean isCritical) {
        if (((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH) == null) {
            return;
        }

        String mythicMobsType = null;
        if (plugin.getUseMythicMobs()) {
            boolean isMythicMob = plugin.getMythicBukkit().getMobManager().isMythicMob(entity);
            if (isMythicMob) {
                mythicMobsType = plugin.getMythicBukkit().getMobManager().getMythicMobInstance(entity).getMobType();
            }
        }

        // Handle health bar hologram
        if (!(entity instanceof Player)) {
            UUID entityId = entity.getUniqueId();
            String healthId = "DamageIndicator_Health_" + entityId;
            Hologram healthHolo = DHAPI.getHologram(healthId);
            if (healthHolo == null) {
                healthHolo = DHAPI.createHologram(healthId, entity.getLocation().clone().add(0, entity.getHeight() + customHealthBars.getOrDefault(mythicMobsType, defaultSettings).heightOffset(), 0));
                healthHolo.showAll();
                initializeHealthHologram(healthHolo, mythicMobsType);
            }
            updateHealthHologram(healthHolo, (LivingEntity) entity, mythicMobsType);

            // Assign entity to the DamageIndicatorBars team to hide name tag
            damageIndicatorTeam.addEntry(entity.getUniqueId().toString());

            Hologram finalHealthHolo = healthHolo;
            String finalMythicMobsType = mythicMobsType;
            BukkitRunnable updateHealthTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (entity.isValid()) {
                        finalHealthHolo.setLocation(entity.getLocation().clone().add(0, entity.getHeight() + customHealthBars.getOrDefault(finalMythicMobsType, defaultSettings).heightOffset(), 0));
                        updateHealthHologram(finalHealthHolo, (LivingEntity) entity, finalMythicMobsType);
                        finalHealthHolo.realignLines();
                    } else {
                        finalHealthHolo.delete();
                        this.cancel();
                    }
                }
            };
            updateHealthTask.runTaskTimer(plugin, healthBarUpdateRate, healthBarUpdateRate);

            // Schedule removal of health hologram if no new damage or heal occurs
            if (taskMap.containsKey(entityId)) {
                taskMap.get(entityId).cancel();
            }
            BukkitRunnable removeHealthTask = new BukkitRunnable() {
                @Override
                public void run() {
                    damageIndicatorTeam.removeEntry(entity.getUniqueId().toString());
                    finalHealthHolo.delete();
                    updateHealthTask.cancel();
                }
            };
            removeHealthTask.runTaskLater(plugin, customHealthBars.getOrDefault(mythicMobsType, defaultSettings).hologramDuration()); // healthBarHologramDuration ticks
            taskMap.put(entityId, removeHealthTask);
        }

        if (amount == 0) {
            return;
        }

        // Handle damage or heal hologram
        String hologramId = "DamageIndicator_" + (type.equals(AttackType.HEALING) ? "Heal_" : "Damage_") + UUID.randomUUID();
        Hologram amountHolo = DHAPI.createHologram(hologramId, entity.getLocation().clone().add(0, entity.getHeight() + damageIndicatorHeightOffset, 0));
        if (type.equals(AttackType.DAMAGE)) {
            DHAPI.addHologramLine(amountHolo,
                    ((!isCritical) ? damageIndicatorDamagePrefix : damageIndicatorDamageCriticalColor) +
                            ((!isCritical) ? damageIndicatorDamageColor : damageIndicatorDamageCriticalColor) +
                            replace(String.format("%.2f", amount)) +
                            ((!isCritical) ? damageIndicatorDamageSuffix : damageIndicatorDamageCriticalSuffix)
            );
        } else if (type.equals(AttackType.HEALING)) {
            DHAPI.addHologramLine(amountHolo, damageIndicatorHealPrefix + damageIndicatorHealColor + replace(String.format("%.2f", amount)) + damageIndicatorHealSuffix);
        } else {
            DHAPI.addHologramLine(amountHolo,
                    ((!isCritical) ? damageIndicatorMagicPrefix : damageIndicatorMagicCriticalColor) +
                            ((!isCritical) ? damageIndicatorMagicColor : damageIndicatorMagicCriticalColor) +
                            replace(String.format("%.2f", amount)) +
                            ((!isCritical) ? damageIndicatorMagicSuffix : damageIndicatorMagicCriticalSuffix)
            );
        }
        amountHolo.showAll();

        animateHologram(amountHolo, entity, mythicMobsType);

        BukkitRunnable removeAmountHoloTask = new BukkitRunnable() {
            @Override
            public void run() {
                amountHolo.delete();
            }
        };
        removeAmountHoloTask.runTaskLater(plugin, customHealthBars.getOrDefault(mythicMobsType, defaultSettings).hologramDuration());

    }

    /**
     * Initializes the health hologram with empty lines.
     *
     * @param holo The hologram to initialize.
     */
    private void initializeHealthHologram(Hologram holo, @Nullable String mobType) {
        for (String ignored : customHealthBars.getOrDefault(mobType, defaultSettings).lines()) {
            DHAPI.addHologramLine(holo, "");
        }
    }

    /**
     * Animates the damage or heal hologram to simulate a floating and falling effect.
     *
     * @param holo   The hologram to animate.
     * @param entity The entity that the hologram is related to.
     */
    private void animateHologram(Hologram holo, Entity entity, @Nullable String mobType) {
        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double xSpeed = damageIndicatorAnimationSpeed * Math.cos(angle);
        double zSpeed = damageIndicatorAnimationSpeed * Math.sin(angle);
        final double[] ySpeed = {damageIndicatorInitialYSpeed};

        new BukkitRunnable() {
            int ticks = 0;
            final Location loc = holo.getLocation().clone();

            @Override
            public void run() {
                if (ticks >= 40 || loc.getY() <= entity.getLocation().getY()) { // Stop after 2 seconds (40 ticks)
                    holo.delete();
                    this.cancel();
                    return;
                }
                loc.add(xSpeed, ySpeed[0], zSpeed);
                ySpeed[0] -= damageIndicatorGravity; // Simulate gravity
                holo.setLocation(loc);
                holo.realignLines();
                ticks++;
            }
        }.runTaskTimer(plugin, damageIndicatorUpdateRate, damageIndicatorUpdateRate);
    }

    /**
     * Updates the health hologram to display the current health status of the entity.
     *
     * @param holo   The health hologram to update.
     * @param entity The living entity whose health status is displayed.
     */
    @SuppressWarnings("DataFlowIssue")
    private void updateHealthHologram(Hologram holo, LivingEntity entity, @Nullable String mobType) {
        double currentHealth = entity.getHealth();
        double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        for (int i = 0; i < customHealthBars.getOrDefault(mobType, defaultSettings).lines().length; i++) {
            String line = customHealthBars.getOrDefault(mobType, defaultSettings).lines()[i]
                    .replace("<name>", replace(entity.getName()))
                    .replace("<health>", String.format("%.2f", currentHealth))
                    .replace("<max_health>", String.format("%.2f", maxHealth))
                    .replace("<bar>", createHealthBar(currentHealth, maxHealth, mobType));
            if (plugin.getUsePlaceholderAPI()) {
                DHAPI.setHologramLine(holo, i, PAPI.setPlaceholders(null, line));
            } else {
                DHAPI.setHologramLine(holo, i, line);
            }
        }
    }

    /**
     * Creates a visual representation of the health bar.
     *
     * @param currentHealth The current health of the entity.
     * @param maxHealth     The maximum health of the entity.
     * @return A string representing the health bar.
     */
    private String createHealthBar(double currentHealth, double maxHealth, @Nullable String mobType) {
        if (customHealthBars.getOrDefault(mobType, defaultSettings).width() > 0) {

            int filledBars = (int) (currentHealth / maxHealth * customHealthBars.getOrDefault(mobType, defaultSettings).width());
            int emptyBars = customHealthBars.getOrDefault(mobType, defaultSettings).width() - filledBars;

            HealthBarSettings settings = customHealthBars.getOrDefault(mobType, defaultSettings);

            return settings.prefix() +
                    settings.healthColor() + settings.healthFiller().repeat(Math.max(0, filledBars)) +
                    settings.separator() +
                    settings.fillerColor() + settings.filler().repeat(Math.max(0, emptyBars)) +
                    settings.suffix();
        } else {
            return "";
        }
    }

    private String replace(String string) {
        for (String key :textReplace.keySet()) {
            string = string.replaceAll(key, textReplace.getOrDefault(key, key));
        }
        return string;
    }

    public Map<UUID, BukkitRunnable> getTaskMap() {
        return taskMap;
    }

}
