package org.teenkung123.damageindicator.Events;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.teenkung123.damageindicator.DamageIndicator;

import java.util.UUID;

public class DeathEvent implements Listener {

    private final DamageIndicator plugin;

    public DeathEvent(DamageIndicator plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the deletion of health holograms and cancellation of scheduled tasks when an entity dies.
     *
     * @param event The EntityDeathEvent triggered when an entity dies.
     */
    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        UUID entityId = event.getEntity().getUniqueId();
        String healthId = "damageindicator_health_" + entityId;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeHolo(healthId, entityId);
        }, 20);

    }

    @EventHandler
    public void onDeath2(MythicMobDeathEvent event) {
        UUID entityId = event.getEntity().getUniqueId();
        String healthId = "damageindicator_health_" + entityId;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeHolo(healthId, entityId);
        }, 20);
    }

    private void removeHolo(String healthId, UUID entityId) {
        if (plugin.getHologramManager().getHologram(healthId).isPresent()) {
            plugin.getHologramManager().remove(healthId);
        }
        if (plugin.getHoloDisplays().getTaskMap().containsKey(entityId)) {
            plugin.getHoloDisplays().getTaskMap().get(entityId).cancel();
            plugin.getHoloDisplays().getTaskMap().remove(entityId);
        }
    }

}
