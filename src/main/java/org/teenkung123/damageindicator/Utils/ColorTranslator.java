package org.teenkung123.damageindicator.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorTranslator {

    // Map single-character colour / format codes to MiniMessage tags
    private static final Map<Character, String> LEGACY_TO_MINI;
    static {
        LEGACY_TO_MINI = new HashMap<>();
        // ---- colours ----
        LEGACY_TO_MINI.put('0', "black");
        LEGACY_TO_MINI.put('1', "dark_blue");
        LEGACY_TO_MINI.put('2', "dark_green");
        LEGACY_TO_MINI.put('3', "dark_aqua");
        LEGACY_TO_MINI.put('4', "dark_red");
        LEGACY_TO_MINI.put('5', "dark_purple");
        LEGACY_TO_MINI.put('6', "gold");
        LEGACY_TO_MINI.put('7', "gray");
        LEGACY_TO_MINI.put('8', "dark_gray");
        LEGACY_TO_MINI.put('9', "blue");
        LEGACY_TO_MINI.put('a', "green");
        LEGACY_TO_MINI.put('b', "aqua");
        LEGACY_TO_MINI.put('c', "red");
        LEGACY_TO_MINI.put('d', "light_purple");
        LEGACY_TO_MINI.put('e', "yellow");
        LEGACY_TO_MINI.put('f', "white");
        // ---- formats ----
        LEGACY_TO_MINI.put('k', "obfuscated");
        LEGACY_TO_MINI.put('l', "bold");
        LEGACY_TO_MINI.put('m', "strikethrough");
        LEGACY_TO_MINI.put('n', "underlined");
        LEGACY_TO_MINI.put('o', "italic");
        LEGACY_TO_MINI.put('r', "reset");
    }

    /*
     * One big regex that matches, **in priority order**:
     *   1) &x&R&R&G&G&B&B or §x§R§R§G§G§B§B  – legacy “nibble” hex sequence
     *   2) &#RRGGBB or §#RRGGBB              – modern hex sequence
     *   3) &X or §X                          – single legacy code
     */
    private static final Pattern COMBINED_PATTERN = Pattern.compile(
            "(?i)([&§]x(?:[&§][0-9A-F]){6}|[&§]#[0-9A-F]{6}|[&§][0-9A-FK-OR])"
    );

    /**
     * Translates every legacy colour / format code in {@code input} into
     * its MiniMessage equivalent.
     *
     * Examples:
     *   §x§7§8§F§F§0§0Hello → <#78ff00>Hello
     *   &lBold &aGreen      → <bold>Bold <green>Green
     */
    public static String toMiniMessageFormat(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        Matcher matcher = COMBINED_PATTERN.matcher(input);
        StringBuffer out = new StringBuffer();

        while (matcher.find()) {
            String match = matcher.group(1);

            // -- Case 1: §x-style hex sequence -------------------------------------------------
            if (match.length() >= 2 && (match.charAt(1) | 0x20) == 'x') {   // char2 is 'x' (case-insensitive)
                // Strip all '&', '§' and the leading 'x', leaving the 6 nibbles
                String hex = match.replaceAll("(?i)[&§x]", "");
                matcher.appendReplacement(out, "<#" + hex + ">");
                continue;
            }

            // -- Case 2: &#RRGGBB / §#RRGGBB ----------------------------------------------------
            if (match.length() >= 2 && match.charAt(1) == '#') {
                String hex = match.substring(2);          // skip leading &/§ and '#'
                matcher.appendReplacement(out, "<#" + hex + ">");
                continue;
            }

            // -- Case 3: single legacy colour / format code -------------------------------------
            char code = Character.toLowerCase(match.charAt(1));
            String tag = LEGACY_TO_MINI.get(code);
            if (tag != null) {
                matcher.appendReplacement(out, "<" + tag + ">");
            } else {
                // unknown code – just drop it
                matcher.appendReplacement(out, "");
            }
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
