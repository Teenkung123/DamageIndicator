package org.teenkung123.damageindicator.Utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;
import org.teenkung123.damageindicator.DamageIndicator;
import org.teenkung123.damageindicator.Loader.ConfigLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HealthBarBatchUpdater {

    private final DamageIndicator plugin;
    private final ConfigLoader configLoader;
    private final HoloDisplays holoDisplays;

    // Keep track of the last time we updated each entity (to throttle updates).
    private final Map<UUID, Long> lastUpdateTime = new ConcurrentHashMap<>();
    private static final long MIN_INTERVAL_MS = 0L;
    private int roundRobinCounter = 0;


    private BukkitTask task;

    public HealthBarBatchUpdater(DamageIndicator plugin,
                                 ConfigLoader configLoader,
                                 HoloDisplays holoDisplays) {
        this.plugin = plugin;
        this.configLoader = configLoader;
        this.holoDisplays = holoDisplays;
    }

    public void startBatchUpdating() {
        plugin.getLogger().info("Starting two-phase health bar updater...");

        long mainIntervalTicks = 5L; // how often we run the batch

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // 1) Get all players and pick a subset
            Collection<? extends Player> allPlayers = Bukkit.getOnlinePlayers();
            List<Player> partialPlayers = new ArrayList<>();

            int index = 0;
            for (Player p : allPlayers) {
                // only pick those whose (index % 4) == roundRobinCounter
                if (index % 4 == roundRobinCounter) {
                    partialPlayers.add(p);
                }
                index++;
            }
            // increment and wrap
            roundRobinCounter = (roundRobinCounter + 1) % 4;

            // 2) gather in-range for only that subset
            Set<UUID> inRangeEntities = gatherInRangeEntities(partialPlayers);

            // 3) async & final sync
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // heavy computations if needed
                Bukkit.getScheduler().runTask(plugin, () -> {
                    spawnOrRefreshBars(inRangeEntities);
                    // removeOutOfRangeBars(inRangeEntities); // or not, up to you
                });
            });
        }, 0L, mainIntervalTicks);
    }


    /**
     * Gathers the set of living entities that are in range (and not fully excluded).
     * This is basically your gatherDataSync, except we store all entity UUIDs in a Set.
     */
    private Set<UUID> gatherInRangeEntities(List<Player> players) {
        Set<UUID> result = new HashSet<>();

        if (players.isEmpty()) {
            return result;
        }

        // Cache config
        double range = configLoader.getHealthBarAlwaysVisibleDistance();
        int softEntityThreshold = configLoader.getSoftEntityThreshold();
        int hardEntityThreshold = configLoader.getHardEntityThreshold();
        List<String> disabledWorlds = configLoader.getHealthBarAlwaysVisibleDisabledWorlds();

        for (Player player : players) {
            if (disabledWorlds.contains(player.getWorld().getName())) {
                continue;
            }

            // Collect all living entities around this player in range
            Set<LivingEntity> livingSet = new HashSet<>();
            for (Entity e : player.getNearbyEntities(range, range, range)) {
                if (!(e instanceof LivingEntity living) || living instanceof Player) {
                    continue;
                }
                if (e.isInvulnerable()) {
                    continue;
                }
                // Example skipping silverfish, vex
                if (e.getType() == EntityType.SILVERFISH || e.getType() == EntityType.VEX) {
                    continue;
                }
                livingSet.add(living);
                if (livingSet.size() > hardEntityThreshold) {
                    break;
                }
            }

            // If we exceeded the “hard” threshold, skip them entirely
            if (livingSet.size() > hardEntityThreshold) {
                continue;
            }

            // If we have more than “soft threshold” entities, only include those not at full health
            if (livingSet.size() > softEntityThreshold) {
                for (LivingEntity living : livingSet) {
                    double maxHealth = getMaxHealth(living);
                    if (living.getHealth() < maxHealth) {
                        result.add(living.getUniqueId());
                    }
                }
            } else {
                // Otherwise, include them all
                for (LivingEntity living : livingSet) {
                    result.add(living.getUniqueId());
                }
            }
        }
        return result;
    }

    /**
     * Spawns or updates a health bar for each entity in “processedSet”.
     */
    private void spawnOrRefreshBars(Set<UUID> processedSet) {
        // “now” used for update throttling
        long now = System.currentTimeMillis();

        for (UUID entityId : processedSet) {
            Entity e = Bukkit.getEntity(entityId);
            if (!(e instanceof LivingEntity living)) {
                continue;
            }

            // Throttle: only update once per 2 seconds
            long last = lastUpdateTime.getOrDefault(entityId, 0L);
            if ((now - last) < MIN_INTERVAL_MS) {
                continue;
            }
            lastUpdateTime.put(entityId, now);

            // This spawns or updates the bar (with no timed removal).
            holoDisplays.displayHealthBar(living, true);
        }
    }

    /**
     * Removes holograms for any entity that is NOT in the processedSet.
     * <p>
     * That way if you walk out of range, the bar disappears.
     */
    private void removeOutOfRangeBars(Set<UUID> processedSet) {
        // Get all hologram IDs from the manager
        // We only want to remove “damageindicator_health_...” ones,
        // since those are your HealthBar ones.
        for (String holoId : plugin.getHologramManager().getHologramIds()) {
            if (!holoId.startsWith("damageindicator_health_")) {
                continue; // skip other holograms, like damage popups
            }
            // Grab the entity UUID from the ID
            // e.g. “damageindicator_health_<entity-uuid>”
            try {
                String suffix = holoId.substring("damageindicator_health_".length());
                UUID entityId = UUID.fromString(suffix);

                // If that entity is not in processedSet => remove hologram
                if (!processedSet.contains(entityId)) {
                    plugin.getHologramManager().remove(holoId);
                    lastUpdateTime.remove(entityId); // reset any tracking
                }
            } catch (IllegalArgumentException ex) {
                // Not a valid UUID or something else => skip
            }
        }
    }

    private double getMaxHealth(LivingEntity living) {
        return living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
                ? living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
                : 20.0;
    }

    public void stopBatchUpdating() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    // Optional debug method from your snippet
    public void debugGatherDataSync(Player admin) {
        // Example usage, not updated with the new approach, but you can adapt.
        admin.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Use the new gatherInRangeEntities approach for debugging as well."));
    }
}
