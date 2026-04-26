package dev.jackdaw1101.neon.API.utilities;

import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

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

    static {
        if (USE_BUKKIT_CHAT_COLOR) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Using legacy color formatting as you are running a 1.15 or below Server");
        } else {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Using hex color formatting as you are running a 1.16+ Server");
        }
    }

    private static boolean isLegacyVersion() {
        try {
            String version = Bukkit.getBukkitVersion().split("-")[0];
            String[] split = version.split("\\.");
            int major = Integer.parseInt(split[0]);
            int minor = Integer.parseInt(split[1]);
            return major < 1 || (major == 1 && minor < 16);
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * Colorizes text using either legacy or modern formatting
     * @param text The text to colorize
     * @return Colorized text
     */
    public static String color(String text) {
        if (text == null || text.isEmpty()) return "";

        if (USE_BUKKIT_CHAT_COLOR) {
            return ChatColor.translateAlternateColorCodes('&', text);
        } else {
            return processModernColors(text);
        }
    }

    /**
     * Colorizes text and returns a legacy string (for APIs that need it)
     * @param text The text to colorize
     * @return Colorized text in legacy format
     */
    public static String colorLegacy(String text) {
        return color(text);
    }

    /**
     * Main processing method for modern Minecraft versions (1.16+)
     */
    private static String processModernColors(String text) {

        text = applyPlaceholders(text);

        text = processNamedColors(text);
        text = processHexColorTags(text);
        text = processInlineHexColors(text);
        text = processRainbowTags(text);
        text = processGradientTags(text);

        text = ChatColor.translateAlternateColorCodes('&', text);

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

    /**
     * Process named color tags like <red>text</red>
     */
    private static String processNamedColors(String input) {
        Pattern namedColorPattern = Pattern.compile("<(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white)>(.*?)</\\1>", Pattern.DOTALL);
        Matcher matcher = namedColorPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String colorName = matcher.group(1);
            String text = matcher.group(2);
            String hexColor = NAMED_COLORS.get(colorName);

            if (hexColor != null) {

                text = processModernColors(text);
                String replacement = hexToMinecraftFormat(hexColor) + text;
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Process HTML-style hex color tags like <#FF0000>text</#FF0000>
     */
    private static String processHexColorTags(String input) {
        Pattern hexPattern = Pattern.compile("<#([0-9a-fA-F]{6})>(.*?)</#([0-9a-fA-F]{6})>", Pattern.DOTALL);
        Matcher matcher = hexPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String text = matcher.group(2);
            String endHex = matcher.group(3);

            text = processModernColors(text);

            if (!startHex.equalsIgnoreCase(endHex)) {
                String gradientText = applyGradient(startHex, endHex, text);
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(gradientText));
            } else {
                String solidColorText = hexToMinecraftFormat(startHex) + text;
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(solidColorText));
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Process inline hex colors like &#FF0000 or &#FF0000text (the format you mentioned)
     * This handles patterns like &#A40000O&#AD0000w correctly
     */
    private static String processInlineHexColors(String input) {

        Pattern inlineHexPattern = Pattern.compile("&#([0-9a-fA-F]{6})([^&]*)");
        Matcher matcher = inlineHexPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String followingText = matcher.group(2);

            String replacement = hexToMinecraftFormat(hexCode);
            if (!followingText.isEmpty()) {

                followingText = processModernColors(followingText);
                replacement += followingText;
            }

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Process gradient tags like <gradient:#FF0000:#00FF00>text</gradient>
     */
    private static String processGradientTags(String input) {
        Pattern gradientPattern = Pattern.compile("<gradient:#([0-9a-fA-F]{6}):#([0-9a-fA-F]{6})>(.*?)</gradient>", Pattern.DOTALL);
        Matcher matcher = gradientPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String text = matcher.group(3);

            text = processModernColors(text);
            String gradientText = applyGradient(startHex, endHex, text);

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(gradientText));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Process rainbow tags like <rainbow>text</rainbow>
     */
    private static String processRainbowTags(String input) {
        Pattern rainbowPattern = Pattern.compile("<rainbow>(.*?)</rainbow>", Pattern.DOTALL);
        Matcher matcher = rainbowPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String text = matcher.group(1);

            text = processModernColors(text);
            String rainbowText = applyRainbow(text);

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(rainbowText));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * Applies a gradient effect to the text, transitioning from startColor to endColor.
     */
    private static String applyGradient(String startHex, String endHex, String text) {
        if (text == null || text.isEmpty()) return "";

        int length = text.length();
        if (length == 1) return hexToMinecraftFormat(startHex) + text;

        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);
        StringBuilder gradientText = new StringBuilder();

        for (int i = 0; i < length; i++) {
            double ratio = (double) i / (length - 1);
            int r = (int) Math.round(startRGB[0] + ratio * (endRGB[0] - startRGB[0]));
            int g = (int) Math.round(startRGB[1] + ratio * (endRGB[1] - startRGB[1]));
            int b = (int) Math.round(startRGB[2] + ratio * (endRGB[2] - startRGB[2]));

            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));

            String hexColor = String.format("%02X%02X%02X", r, g, b);
            gradientText.append(hexToMinecraftFormat(hexColor)).append(text.charAt(i));
        }

        return gradientText.toString();
    }

    /**
     * Applies a rainbow effect to the text.
     */
    private static String applyRainbow(String text) {
        if (text == null || text.isEmpty()) return "";

        int length = text.length();
        String[] rainbowColors = {
                "FF0000", // Red
                "FF7F00", // Orange
                "FFFF00", // Yellow
                "00FF00", // Green
                "0000FF", // Blue
                "4B0082", // Indigo
                "9400D3"  // Violet
        };

        StringBuilder rainbowText = new StringBuilder();

        for (int i = 0; i < length; i++) {
            String hexColor = rainbowColors[i % rainbowColors.length];
            rainbowText.append(hexToMinecraftFormat(hexColor)).append(text.charAt(i));
        }

        return rainbowText.toString();
    }

    /**
     * Converts a hex color string to Minecraft's internal format (net.md_5.bungee.api.ChatColor style)
     * This format is required for hex colors to work properly in 1.16+
     */
    private static String hexToMinecraftFormat(String hex) {

        hex = hex.toUpperCase();
        if (hex.length() != 6) {
            return ""; // Invalid hex
        }

        return "§x" +
                "§" + hex.charAt(0) +
                "§" + hex.charAt(1) +
                "§" + hex.charAt(2) +
                "§" + hex.charAt(3) +
                "§" + hex.charAt(4) +
                "§" + hex.charAt(5);
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

    private static String applyPlaceholders(String text) {
        if (text == null) return "";
        return text
                .replace("{prefix}", PREFIX != null ? PREFIX : "")
                .replace("{main_theme}", MAINTHEME != null ? MAINTHEME : "")
                .replace("{second_theme}", SECONDTHEME != null ? SECONDTHEME : "")
                .replace("{third_theme}", THIRDTHEME != null ? THIRDTHEME : "");
    }

    /**
     * Strips all color codes from a string
     * @param text The text to strip colors from
     * @return Text without any color codes
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }

    /**
     * Translates alternate color codes using the specified character
     * @param altChar The alternate color code character
     * @param text The text to translate
     * @return Translated text
     */
    public static String translateAlternateColorCodes(char altChar, String text) {
        return ChatColor.translateAlternateColorCodes(altChar, text);
    }
}