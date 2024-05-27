package org.teenkung123.damageindicator.Utils;

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
        String healthColor
) {
}
