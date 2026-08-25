package com.chatutils.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_BRACKET_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private ColorUtil() {}

    /**
     * Renk kodlarını (&, § ve HEX &#RRGGBB) dönüştürür.
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // HEX desteği: &#FFFFFF
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        text = buffer.toString();

        // Alternatif HEX desteği: <#FFFFFF>
        Matcher bracketMatcher = HEX_BRACKET_PATTERN.matcher(text);
        buffer = new StringBuffer();
        while (bracketMatcher.find()) {
            String hex = bracketMatcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            bracketMatcher.appendReplacement(buffer, replacement.toString());
        }
        bracketMatcher.appendTail(buffer);
        text = buffer.toString();

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colorize(List<String> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(ColorUtil::colorize).collect(Collectors.toList());
    }
}
