package org.teenkung123.damageindicator.Utils;

import java.util.List;

public record HealthBarSettings(
        double heightOffset,
        int hologramDuration,
        String[] lines,
        String prefix,
        String suffix,
        String filler,
        String healthFiller,
        String separator,
        int width,
        String fillerColor,
        String healthColor,
        boolean enabled,
        boolean alwaysShow,
        int alwaysShowDistance,
        List<String> alwaysShowDisabledWorlds,
        String percentColor,
        boolean overrideColor
) {
}
