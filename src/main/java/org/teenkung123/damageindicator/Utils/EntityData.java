package org.teenkung123.damageindicator.Utils;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Simple data container for storing minimal info about an entity.
 * No direct calls to Bukkit methods here—just storing plain data.
 */
public record EntityData(
        LivingEntity entity,
        double currentHealth,
        double maxHealth,
        Location location
) {}
