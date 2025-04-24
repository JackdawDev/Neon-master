package dev.jackdaw1101.neon.API.Features.AntiSwear;

import dev.jackdaw1101.neon.AntiSwear.SwearManager;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Pattern;

public class AntiSwearAPIImpl implements AntiSwearAPI {
    private final Neon plugin;
    private final SwearManager swearManager;
    private final Pattern wordPattern = Pattern.compile("[^a-zA-Z]");
    private final Set<String> temporaryBlacklist = new HashSet<>();
    private final Set<String> temporaryWhitelist = new HashSet<>();

    public AntiSwearAPIImpl(Neon plugin, SwearManager swearManager) {
        this.plugin = plugin;
        this.swearManager = swearManager;
    }

    @Override
    public boolean isSwearWord(String word) {
        String sanitized = sanitizeWord(word);
        return getCombinedBlacklist().contains(sanitized.toLowerCase());
    }

    @Override
    public boolean containsSwear(String message) {
        String sanitized = sanitizeMessage(message);
        List<String> blacklist = getCombinedBlacklist();
        List<String> whitelist = getCombinedWhitelist();

        for (String swear : blacklist) {
            if (whitelist.stream().noneMatch(sanitized::contains) && sanitized.contains(swear)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addToBlacklist(String word) {
        List<String> blacklist = getBlacklist();
        String lowerWord = word.toLowerCase();
        if (!blacklist.contains(lowerWord)) {
            blacklist.add(lowerWord);
            plugin.getSettings().set("ANTI-SWEAR.BLACKLIST", blacklist);
            plugin.getSettings().save();
        }
    }

    @Override
    public void removeFromBlacklist(String word) {
        List<String> blacklist = getBlacklist();
        if (blacklist.remove(word.toLowerCase())) {
            plugin.getSettings().set("ANTI-SWEAR.BLACKLIST", blacklist);
            plugin.getSettings().save();
        }
    }

    @Override
    public void addToWhitelist(String word) {
        List<String> whitelist = getWhitelist();
        String lowerWord = word.toLowerCase();
        if (!whitelist.contains(lowerWord)) {
            whitelist.add(lowerWord);
            plugin.getSettings().set("ANTI-SWEAR.WHITELIST", whitelist);
            plugin.getSettings().save();
        }
    }

    @Override
    public void removeFromWhitelist(String word) {
        List<String> whitelist = getWhitelist();
        if (whitelist.remove(word.toLowerCase())) {
            plugin.getSettings().set("ANTI-SWEAR.WHITELIST", whitelist);
            plugin.getSettings().save();
        }
    }

    @Override
    public void addTemporaryBlacklistWord(String word) {
        temporaryBlacklist.add(word.toLowerCase());
    }

    @Override
    public void removeTemporaryBlacklistWord(String word) {
        temporaryBlacklist.remove(word.toLowerCase());
    }

    @Override
    public void addTemporaryWhitelistWord(String word) {
        temporaryWhitelist.add(word.toLowerCase());
    }

    @Override
    public void removeTemporaryWhitelistWord(String word) {
        temporaryWhitelist.remove(word.toLowerCase());
    }

    @Override
    public void clearTemporaryBlacklist() {
        temporaryBlacklist.clear();
    }

    @Override
    public void clearTemporaryWhitelist() {
        temporaryWhitelist.clear();
    }

    @Override
    public List<String> getBlacklist() {
        return plugin.getSettings().getStringList("ANTI-SWEAR.BLACKLIST");
    }

    @Override
    public List<String> getWhitelist() {
        return plugin.getSettings().getStringList("ANTI-SWEAR.WHITELIST");
    }

    @Override
    public Set<String> getTemporaryBlacklist() {
        return Collections.unmodifiableSet(temporaryBlacklist);
    }

    @Override
    public Set<String> getTemporaryWhitelist() {
        return Collections.unmodifiableSet(temporaryWhitelist);
    }

    @Override
    public int getSwearStrikes(Player player) {
        return swearManager.getStrikes(player);
    }

    @Override
    public void resetSwearStrikes(Player player) {
        swearManager.resetStrikes(player);
    }

    @Override
    public void setSwearStrikes(Player player, int strikes) {
        swearManager.resetStrikes(player);
        for (int i = 0; i < strikes; i++) {
            swearManager.addSwear(player);
        }
    }

    @Override
    public String censorMessage(String message) {
        String sanitized = sanitizeMessage(message);
        List<String> blacklist = getCombinedBlacklist();
        List<String> whitelist = getCombinedWhitelist();
        String censorSymbol = plugin.getSettings().getString("ANTI-SWEAR.CENSOR.SYMBOL");

        for (String swear : blacklist) {
            if (whitelist.stream().noneMatch(sanitized::contains) && sanitized.contains(swear)) {
                message = censorWord(message, swear, censorSymbol);
            }
        }
        return message;
    }

    @Override
    public String sanitizeMessage(String message) {
        boolean threeReturnE = plugin.getSettings().getBoolean("ANTI-SWEAR.SENSITIVE-CHECK-THREE-RETURN-E");
        message = message.toLowerCase();

        if (threeReturnE) {
            message = message.replace("3", "e")
                .replace("1", "i")
                .replace("!", "i")
                .replace("@", "a")
                .replace("7", "t")
                .replace("0", "o")
                .replace("5", "s")
                .replace("$", "s")
                .replace("8", "b");
        } else {
            message = message.replace("3", "s")
                .replace("1", "i")
                .replace("!", "i")
                .replace("@", "a")
                .replace("7", "t")
                .replace("0", "o")
                .replace("5", "s")
                .replace("$", "s")
                .replace("8", "b");
        }

        return message.replaceAll("\\p{Punct}|\\d", "").trim();
    }

    @Override
    public void reloadConfiguration() {
        plugin.getSettings().reload();
        temporaryBlacklist.clear();
        temporaryWhitelist.clear();
    }

    private List<String> getCombinedBlacklist() {
        List<String> combined = new ArrayList<>(getBlacklist());
        combined.addAll(temporaryBlacklist);
        return combined;
    }

    private List<String> getCombinedWhitelist() {
        List<String> combined = new ArrayList<>(getWhitelist());
        combined.addAll(temporaryWhitelist);
        return combined;
    }

    private String sanitizeWord(String word) {
        return wordPattern.matcher(word.toLowerCase()).replaceAll("");
    }

    private String censorWord(String message, String swear, String censorSymbol) {
        String cleanedSwear = wordPattern.matcher(swear).replaceAll("").toLowerCase();
        StringBuilder replacement = new StringBuilder();
        for (int i = 0; i < swear.length(); i++) {
            replacement.append(censorSymbol);
        }

        StringBuilder regexBuilder = new StringBuilder("(?i)");
        for (char c : cleanedSwear.toCharArray()) {
            regexBuilder.append("[^a-zA-Z]*");
            regexBuilder.append(Pattern.quote(String.valueOf(c)));
        }

        try {
            return message.replaceAll(regexBuilder.toString(), replacement.toString());
        } catch (Exception e) {
            return message;
        }
    }
}
