package org.teenkung123.damageindicator.Events;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.teenkung123.damageindicator.DamageIndicator;
import org.teenkung123.damageindicator.Utils.AttackType;

import java.util.Set;

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
        Set<DamageType> types = event.getAttack().getDamage().collectTypes();
        if (!types.contains(DamageType.PHYSICAL)) {
            plugin.getHoloDisplays().handleDamageEvent(event.getEntity(), event.getAttack().getDamage().getDamage(), AttackType.MAGIC, (event.getDamage().isWeaponCriticalStrike() || event.getDamage().isSkillCriticalStrike()));
        } else {
            plugin.getHoloDisplays().handleDamageEvent(event.getEntity(), event.getAttack().getDamage().getDamage(), AttackType.DAMAGE, event.getDamage().isWeaponCriticalStrike() || event.getDamage().isSkillCriticalStrike());
        }
    }

}
