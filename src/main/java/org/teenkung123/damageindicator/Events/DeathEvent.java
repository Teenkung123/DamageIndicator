package org.teenkung123.damageindicator.Events;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
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
        String healthId = "DamageIndicator_Health_" + entityId;
        Hologram healthHolo = DHAPI.getHologram(healthId);
        if (healthHolo != null) {
            healthHolo.delete();
        }
        if (plugin.getHoloDisplays().getTaskMap().containsKey(entityId)) {
            plugin.getHoloDisplays().getTaskMap().get(entityId).cancel();
            plugin.getHoloDisplays().getTaskMap().remove(entityId);
        }
    }

}
