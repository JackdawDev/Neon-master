package dev.jackdaw1101.neon.API.utilities;

import dev.jackdaw1101.neon.Neon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorHandler {

    private static final boolean USE_BUKKIT_CHAT_COLOR = isLegacyVersion();
    public static final String PREFIX = Neon.getInstance().getMessageManager().getString("PREFIX");
    public static final String MAINTHEME = Neon.getInstance().getMessageManager().getString("MAIN-THEME");
    public static final String SECONDTHEME = Neon.getInstance().getMessageManager().getString("SECOND-THEME");
    public static final String THIRDTHEME = Neon.getInstance().getMessageManager().getString("THIRD-THEME");

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    static {
        if (USE_BUKKIT_CHAT_COLOR) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Using legacy color formatting as you are running a 1.15 or below Server");
        } else {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Using hex color formatting as you are running a 1.16+ Server");
        }
    }

    private static boolean isLegacyVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0];
        String[] split = version.split("\\.");
        int major = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]);
        return major < 1 || (major == 1 && minor < 16);
    }

    /**
     * Colorizes text using either legacy or modern formatting
     * @param text The text to colorize
     * @return Colorized text
     */
    public static String color(String text) {
        if (text == null || text.isEmpty()) return "";

        if (USE_BUKKIT_CHAT_COLOR) {
            return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
        } else {
            return applyHexColors(text);
        }
    }

    /**
     * Colorizes text using MiniMessage formatting
     * @param text The text to colorize
     * @return Colorized Component
     */
    public static Component colorComponent(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return miniMessage.deserialize(applyPlaceholders(text));
    }

    /**
     * Converts a MiniMessage Component to legacy formatted string
     * @param component The component to convert
     * @return Legacy formatted string
     */
    public static String componentToLegacy(Component component) {
        return legacySerializer.serialize(component);
    }

    private static String applyHexColors(String text) {
        try {
            Component component = miniMessage.deserialize(text);
            text = legacySerializer.serialize(component);
            text = org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
            text = replaceAmpersandHexColors(text);
            text = replaceHtmlHexColors(text);
            text = replaceMiniMessageGradient(text);
            text = replaceRainbowColors(text);
            text = replaceNamedColors(text);
            text = applyPlaceholders(text);
        } catch (Exception e) {
            text = org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
            text = replaceAmpersandHexColors(text);
            text = replaceHtmlHexColors(text);
            text = replaceMiniMessageGradient(text);
            text = replaceRainbowColors(text);
            text = replaceNamedColors(text);
            text = applyPlaceholders(text);
        }
        return text;
    }

    private static final Map<String, String> NAMED_COLORS = new HashMap<>();

    static {
        NAMED_COLORS.put("black", "000000");
        NAMED_COLORS.put("dark_blue", "0000AA");
        NAMED_COLORS.put("dark_green", "00AA00");
        NAMED_COLORS.put("dark_aqua", "00AAAA");
        NAMED_COLORS.put("dark_red", "AA0000");
        NAMED_COLORS.put("dark_purple", "AA00AA");
        NAMED_COLORS.put("gold", "FFAA00");
        NAMED_COLORS.put("gray", "AAAAAA");
        NAMED_COLORS.put("dark_gray", "555555");
        NAMED_COLORS.put("blue", "5555FF");
        NAMED_COLORS.put("green", "55FF55");
        NAMED_COLORS.put("aqua", "55FFFF");
        NAMED_COLORS.put("red", "FF5555");
        NAMED_COLORS.put("light_purple", "FF55FF");
        NAMED_COLORS.put("yellow", "FFFF55");
        NAMED_COLORS.put("white", "FFFFFF");
    }

    private static String replaceNamedColors(String input) {
        Pattern namedColorPattern = Pattern.compile("<(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white)>(.*?)</\\1>");
        Matcher matcher = namedColorPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String colorName = matcher.group(1);
            String text = matcher.group(2);

            String hexColor = NAMED_COLORS.get(colorName);
            if (hexColor != null) {
                String formattedColor = hexToChatColor(hexColor);
                matcher.appendReplacement(buffer, formattedColor + text);
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Converts hex color codes of format &#0000FF to Minecraft color codes.
     */
    private static String replaceAmpersandHexColors(String input) {
        Pattern hexPattern = Pattern.compile("&#([0-9a-fA-F]{6})");
        Matcher matcher = hexPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String formattedColor = hexToChatColor(hexCode);
            matcher.appendReplacement(buffer, formattedColor);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Converts hex color codes of format <#0000FF>Message</#FFFFFF> to Minecraft color codes.
     */
    private static String replaceHtmlHexColors(String input) {
        Pattern hexPattern = Pattern.compile("<#([0-9a-fA-F]{6})>(.*?)</#([0-9a-fA-F]{6})>");
        Matcher matcher = hexPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String text = matcher.group(2);
            String endHex = matcher.group(3);

            if (!startHex.equalsIgnoreCase(endHex)) {
                String gradientText = applyGradient(startHex, endHex, text);
                matcher.appendReplacement(buffer, gradientText);
            } else {
                String solidColorText = hexToChatColor(startHex) + text;
                matcher.appendReplacement(buffer, solidColorText);
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Applies a gradient effect to the text, transitioning from startColor to endColor.
     */
    private static String applyGradient(String startHex, String endHex, String text) {
        int length = text.length();
        if (length == 0) return "";

        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);
        StringBuilder gradientText = new StringBuilder();

        for (int i = 0; i < length; i++) {
            double ratio = (double) i / (length - 1);
            int r = (int) (startRGB[0] + ratio * (endRGB[0] - startRGB[0]));
            int g = (int) (startRGB[1] + ratio * (endRGB[1] - startRGB[1]));
            int b = (int) (startRGB[2] + ratio * (endRGB[2] - startRGB[2]));

            String hexColor = String.format("%02X%02X%02X", r, g, b);
            gradientText.append(hexToChatColor(hexColor)).append(text.charAt(i));
        }

        return gradientText.toString();
    }

    private static String replaceRainbowColors(String input) {
        Pattern rainbowPattern = Pattern.compile("<rainbow>(.*?)</rainbow>");
        Matcher matcher = rainbowPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String text = matcher.group(1);
            String rainbowText = applyRainbow(text);
            matcher.appendReplacement(buffer, rainbowText);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Applies a rainbow effect to the text.
     */
    private static String applyRainbow(String text) {
        int length = text.length();
        if (length == 0) return "";

        String[] rainbowColors = {
            "FF0000",
            "FF7F00",
            "FFFF00",
            "00FF00",
            "0000FF",
            "4B0082",
            "9400D3"
        };

        StringBuilder rainbowText = new StringBuilder();

        for (int i = 0; i < length; i++) {
            String hexColor = rainbowColors[i % rainbowColors.length];
            rainbowText.append(hexToChatColor(hexColor)).append(text.charAt(i));
        }

        return rainbowText.toString();
    }

    private static String replaceMiniMessageGradient(String input) {
        Pattern miniMessagePattern = Pattern.compile("<gradient:#([0-9a-fA-F]{6}):#([0-9a-fA-F]{6})>(.*?)</gradient>");
        Matcher matcher = miniMessagePattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String text = matcher.group(3);

            String gradientText = applyGradient(startHex, endHex, text);
            matcher.appendReplacement(buffer, gradientText);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Converts a hex color string to an RGB array.
     */
    private static int[] hexToRGB(String hex) {
        return new int[]{
            Integer.parseInt(hex.substring(0, 2), 16),
            Integer.parseInt(hex.substring(2, 4), 16),
            Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static String hexToChatColor(String hex) {
        char colorChar = '§';
        return colorChar + "x"
            + colorChar + hex.charAt(0) + colorChar + hex.charAt(1)
            + colorChar + hex.charAt(2) + colorChar + hex.charAt(3)
            + colorChar + hex.charAt(4) + colorChar + hex.charAt(5);
    }

    private static String applyPlaceholders(String text) {
        return text
            .replace("{prefix}", PREFIX)
            .replace("{main_theme}", MAINTHEME)
            .replace("{second_theme}", SECONDTHEME)
            .replace("{third_theme}", THIRDTHEME);
    }
}
