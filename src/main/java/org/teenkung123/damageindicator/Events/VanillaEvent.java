package org.teenkung123.damageindicator.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.teenkung123.damageindicator.Utils.AttackType;
import org.teenkung123.damageindicator.DamageIndicator;

public class VanillaEvent implements Listener {

    private final DamageIndicator plugin;

    public VanillaEvent(DamageIndicator plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles damage events caused by entities.
     *
     * @param event The EntityDamageByEntityEvent triggered when an entity is damaged by another entity.
     */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (plugin.getHoloDisplays().shouldHandleEvent(event.getEntity()) && !event.isCancelled()) {
            plugin.getHoloDisplays().handleDamageEvent(event.getEntity(), event.getFinalDamage(), AttackType.DAMAGE, event.isCritical()); // Red for damage
        }
    }

    /**
     * Handles damage events not caused by entities (e.g., environmental damage).
     *
     * @param event The EntityDamageEvent triggered when an entity is damaged by any cause.
     */
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (plugin.getHoloDisplays().shouldHandleEvent(event.getEntity()) && !(event instanceof EntityDamageByEntityEvent) && event.isCancelled()) {
            plugin.getHoloDisplays().handleDamageEvent(event.getEntity(), event.getFinalDamage(), AttackType.DAMAGE, false); // Red for damage
        }
    }

}
