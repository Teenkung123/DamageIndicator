package org.teenkung123.damageindicator.Events;

import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.teenkung123.damageindicator.DamageIndicator;
import org.teenkung123.damageindicator.Utils.AttackType;

import java.util.Set;
import java.util.UUID;

public class MythicMobsDamage implements Listener {

    private final DamageIndicator plugin;

    public MythicMobsDamage(DamageIndicator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(PlayerAttackEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.getConfigLoader().getIsWhitelisted() && !plugin.getConfigLoader().getWorlds().contains(event.getEntity().getWorld().getName())) {
            return;
        }
        if (!plugin.getConfigLoader().getIsWhitelisted() && plugin.getConfigLoader().getWorlds().contains(event.getEntity().getWorld().getName())) {
            return;
        }
        Set<DamageType> types = event.getAttack().getDamage().collectTypes();
        if (!types.contains(DamageType.PHYSICAL)) {
            plugin.getHoloDisplays().handleDamageEvent(event.getEntity(), event.getAttack().getDamage().getDamage(), AttackType.MAGIC, (event.getDamage().isWeaponCriticalStrike() || event.getDamage().isSkillCriticalStrike()));
        } else {
            plugin.getHoloDisplays().handleDamageEvent(event.getEntity(), event.getAttack().getDamage().getDamage(), AttackType.DAMAGE, event.getDamage().isWeaponCriticalStrike() || event.getDamage().isSkillCriticalStrike());
        }
    }

    @EventHandler
    public void onDespawn(MythicMobDespawnEvent event) {
        UUID entityId = event.getEntity().getUniqueId();
        String healthId = "damageindicator_health_" + entityId;
        if (plugin.getHologramManager().getHologram(healthId).isPresent()) {
            plugin.getHologramManager().remove(healthId);
        }
        if (plugin.getHoloDisplays().getTaskMap().containsKey(entityId)) {
            plugin.getHoloDisplays().getTaskMap().get(entityId).cancel();
            plugin.getHoloDisplays().getTaskMap().remove(entityId);
        }
    }

}
