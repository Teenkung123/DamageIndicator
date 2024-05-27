package org.teenkung123.damageindicator.Events;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.teenkung123.damageindicator.DamageIndicator;
import org.teenkung123.damageindicator.Utils.AttackType;

public class HealEvent implements Listener {

    private final DamageIndicator plugin;

    public HealEvent(DamageIndicator plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles health regeneration events.
     *
     * @param event The EntityRegainHealthEvent triggered when an entity regains health.
     */
    @SuppressWarnings("DataFlowIssue")
    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {
        if (plugin.getHoloDisplays().shouldHandleEvent(event.getEntity())) {
            if (((LivingEntity) event.getEntity()).getHealth() < ((LivingEntity) event.getEntity()).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                plugin.getHoloDisplays().handleDamageEvent(event.getEntity(), event.getAmount(), AttackType.HEALING, false); // Green for healing
            }
        }
    }

}
