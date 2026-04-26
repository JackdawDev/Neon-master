package dev.jackdaw1101.neon.implementions;

import dev.jackdaw1101.neon.API.modules.moderation.IAntiSwear;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class IAntiSwearImpl implements IAntiSwear {
    private final Neon plugin;
    private final SwearManager swearManager;

    private final Pattern wordPattern = Pattern.compile("[^a-zA-Z]");
    private final Pattern punctuationAndDigitsPattern = Pattern.compile("[\\p{Punct}\\d]");
    private final Pattern spacePattern = Pattern.compile("\\s+");

    private final Set<String> temporaryBlacklist = ConcurrentHashMap.newKeySet();
    private final Set<String> temporaryWhitelist = ConcurrentHashMap.newKeySet();

    private final Map<String, Boolean> swearCache = new ConcurrentHashMap<>();
    private final Map<String, String> censorCache = new ConcurrentHashMap<>();
    private static final int CACHE_MAX_SIZE = 1000;

    private static final Map<Character, String> LEET_REPLACEMENTS = new HashMap<>();
    private static final Map<Character, String> LEET_REPLACEMENTS_ALT = new HashMap<>();

    static {
        LEET_REPLACEMENTS.put('3', "e");
        LEET_REPLACEMENTS.put('1', "i");
        LEET_REPLACEMENTS.put('!', "i");
        LEET_REPLACEMENTS.put('@', "a");
        LEET_REPLACEMENTS.put('7', "t");
        LEET_REPLACEMENTS.put('0', "o");
        LEET_REPLACEMENTS.put('5', "s");
        LEET_REPLACEMENTS.put('$', "s");
        LEET_REPLACEMENTS.put('8', "b");

        LEET_REPLACEMENTS_ALT.put('3', "s");
        LEET_REPLACEMENTS_ALT.put('1', "i");
        LEET_REPLACEMENTS_ALT.put('!', "i");
        LEET_REPLACEMENTS_ALT.put('@', "a");
        LEET_REPLACEMENTS_ALT.put('7', "t");
        LEET_REPLACEMENTS_ALT.put('0', "o");
        LEET_REPLACEMENTS_ALT.put('5', "s");
        LEET_REPLACEMENTS_ALT.put('$', "s");
        LEET_REPLACEMENTS_ALT.put('8', "b");
    }

    public IAntiSwearImpl(Neon plugin, SwearManager swearManager) {
        this.plugin = plugin;
        this.swearManager = swearManager;
    }

    @Override
    public boolean isSwearWord(String word) {
        if (word == null || word.isEmpty()) return false;

        String sanitized = sanitizeWord(word);
        if (sanitized.isEmpty()) return false;

        return getCombinedBlacklist().contains(sanitized.toLowerCase());
    }

    @Override
    public boolean containsSwear(String message) {
        if (message == null || message.isEmpty()) return false;

        String cacheKey = message + "_" + getBlacklistHash();
        Boolean cached = swearCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        boolean ignoreSpaces = plugin.getSettings().getBoolean("ANTI-SWEAR.IGNORE-SPACES");
        String messageToCheck = ignoreSpaces ? removeSpaces(message) : message;

        String sanitized = sanitizeMessage(messageToCheck);
        if (sanitized.isEmpty()) return false;

        List<String> blacklist = getCombinedBlacklist();
        Set<String> whitelistSet = new HashSet<>(getCombinedWhitelist());

        boolean result = false;
        for (String swear : blacklist) {
            if (swear == null || swear.isEmpty()) continue;

            if (sanitized.contains(swear)) {
                // Check if the swear word is part of a whitelisted word
                boolean isWhitelisted = false;
                for (String whitelistWord : whitelistSet) {
                    if (whitelistWord != null && !whitelistWord.isEmpty() &&
                            sanitized.contains(whitelistWord) &&
                            whitelistWord.contains(swear)) {
                        isWhitelisted = true;
                        break;
                    }
                }

                if (!isWhitelisted) {
                    result = true;
                    break;
                }
            }
        }

        manageCacheSize(swearCache);
        swearCache.put(cacheKey, result);

        return result;
    }

    private String removeSpaces(String message) {
        return spacePattern.matcher(message).replaceAll("");
    }

    @Override
    public void addToBlacklist(String word) {
        if (word == null || word.isEmpty()) return;

        List<String> blacklist = getBlacklist();
        String lowerWord = sanitizeWord(word).toLowerCase();

        if (!lowerWord.isEmpty() && !blacklist.contains(lowerWord)) {
            blacklist.add(lowerWord);
            plugin.getSettings().set("ANTI-SWEAR.BLACKLIST", blacklist);
            plugin.getSettings().save();
            clearCaches();
        }
    }

    @Override
    public void removeFromBlacklist(String word) {
        if (word == null || word.isEmpty()) return;

        List<String> blacklist = getBlacklist();
        String lowerWord = sanitizeWord(word).toLowerCase();

        if (blacklist.remove(lowerWord)) {
            plugin.getSettings().set("ANTI-SWEAR.BLACKLIST", blacklist);
            plugin.getSettings().save();
            clearCaches();
        }
    }

    @Override
    public void addToWhitelist(String word) {
        if (word == null || word.isEmpty()) return;

        List<String> whitelist = getWhitelist();
        String lowerWord = sanitizeWord(word).toLowerCase();

        if (!lowerWord.isEmpty() && !whitelist.contains(lowerWord)) {
            whitelist.add(lowerWord);
            plugin.getSettings().set("ANTI-SWEAR.WHITELIST", whitelist);
            plugin.getSettings().save();
            clearCaches();
        }
    }

    @Override
    public void removeFromWhitelist(String word) {
        if (word == null || word.isEmpty()) return;

        List<String> whitelist = getWhitelist();
        String lowerWord = sanitizeWord(word).toLowerCase();

        if (whitelist.remove(lowerWord)) {
            plugin.getSettings().set("ANTI-SWEAR.WHITELIST", whitelist);
            plugin.getSettings().save();
            clearCaches();
        }
    }

    @Override
    public void addTemporaryBlacklistWord(String word) {
        if (word != null && !word.isEmpty()) {
            temporaryBlacklist.add(sanitizeWord(word).toLowerCase());
            clearCaches();
        }
    }

    @Override
    public void removeTemporaryBlacklistWord(String word) {
        if (word != null && !word.isEmpty()) {
            temporaryBlacklist.remove(sanitizeWord(word).toLowerCase());
            clearCaches();
        }
    }

    @Override
    public void addTemporaryWhitelistWord(String word) {
        if (word != null && !word.isEmpty()) {
            temporaryWhitelist.add(sanitizeWord(word).toLowerCase());
            clearCaches();
        }
    }

    @Override
    public void removeTemporaryWhitelistWord(String word) {
        if (word != null && !word.isEmpty()) {
            temporaryWhitelist.remove(sanitizeWord(word).toLowerCase());
            clearCaches();
        }
    }

    @Override
    public void clearTemporaryBlacklist() {
        temporaryBlacklist.clear();
        clearCaches();
    }

    @Override
    public void clearTemporaryWhitelist() {
        temporaryWhitelist.clear();
        clearCaches();
    }

    @Override
    public List<String> getBlacklist() {
        List<String> blacklist = plugin.getSettings().getStringList("ANTI-SWEAR.BLACKLIST");
        return blacklist != null ? new ArrayList<>(blacklist) : new ArrayList<>();
    }

    @Override
    public List<String> getWhitelist() {
        List<String> whitelist = plugin.getSettings().getStringList("ANTI-SWEAR.WHITELIST");
        return whitelist != null ? new ArrayList<>(whitelist) : new ArrayList<>();
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
        if (player == null) return 0;
        return swearManager.getStrikes(player);
    }

    @Override
    public void resetSwearStrikes(Player player) {
        if (player != null) {
            swearManager.resetStrikes(player);
        }
    }

    @Override
    public void setSwearStrikes(Player player, int strikes) {
        if (player == null || strikes < 0) return;

        swearManager.resetStrikes(player);
        for (int i = 0; i < strikes; i++) {
            swearManager.addSwear(player);
        }
    }

    @Override
    public String censorMessage(String message) {
        if (message == null || message.isEmpty()) return message;

        String cacheKey = message + "_" + getBlacklistHash();
        String cached = censorCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        boolean ignoreSpaces = plugin.getSettings().getBoolean("ANTI-SWEAR.IGNORE-SPACES");
        String messageToCheck = ignoreSpaces ? removeSpaces(message) : message;

        String sanitized = sanitizeMessage(messageToCheck);
        List<String> blacklist = getCombinedBlacklist();
        Set<String> whitelistSet = new HashSet<>(getCombinedWhitelist());
        String censorSymbol = getCensorSymbol();

        String result = message;
        for (String swear : blacklist) {
            if (swear == null || swear.isEmpty()) continue;

            if (sanitized.contains(swear)) {
                boolean isWhitelisted = false;
                for (String whitelistWord : whitelistSet) {
                    if (whitelistWord != null && !whitelistWord.isEmpty() &&
                            sanitized.contains(whitelistWord) &&
                            whitelistWord.contains(swear)) {
                        isWhitelisted = true;
                        break;
                    }
                }

                if (!isWhitelisted) {
                    result = censorWord(result, swear, censorSymbol, ignoreSpaces);
                }
            }
        }

        manageCacheSize(censorCache);
        censorCache.put(cacheKey, result);

        return result;
    }

    @Override
    public String sanitizeMessage(String message) {
        if (message == null || message.isEmpty()) return "";

        String result = message.toLowerCase();

        boolean threeReturnE = plugin.getSettings().getBoolean("ANTI-SWEAR.SENSITIVE-CHECK-THREE-RETURN-E");
        Map<Character, String> replacements = threeReturnE ? LEET_REPLACEMENTS : LEET_REPLACEMENTS_ALT;

        StringBuilder sb = new StringBuilder();
        for (char c : result.toCharArray()) {
            String replacement = replacements.get(c);
            if (replacement != null) {
                sb.append(replacement);
            } else {
                sb.append(c);
            }
        }
        result = sb.toString();

        result = punctuationAndDigitsPattern.matcher(result).replaceAll("");

        return result.trim();
    }

    @Override
    public void reloadConfiguration() {
        plugin.getSettings().reload();
        clearTemporaryBlacklist();
        clearTemporaryWhitelist();
        clearCaches();
    }

    public String sanitizeWord(String word) {
        if (word == null || word.isEmpty()) return "";
        return wordPattern.matcher(word.toLowerCase()).replaceAll("");
    }

    private String getCensorSymbol() {
        String symbol = plugin.getSettings().getString("ANTI-SWEAR.CENSOR.SYMBOL");
        return symbol != null && !symbol.isEmpty() ? symbol : "*";
    }

    private List<String> getCombinedBlacklist() {
        List<String> combined = new ArrayList<>(getBlacklist());
        combined.addAll(temporaryBlacklist);
        combined.removeIf(s -> s == null || s.isEmpty());
        return combined;
    }

    private List<String> getCombinedWhitelist() {
        List<String> combined = new ArrayList<>(getWhitelist());
        combined.addAll(temporaryWhitelist);
        combined.removeIf(s -> s == null || s.isEmpty());
        return combined;
    }

    private String censorWord(String message, String swear, String censorSymbol, boolean ignoreSpaces) {
        if (message == null || swear == null || swear.isEmpty()) return message;

        try {
            String cleanedSwear = wordPattern.matcher(swear).replaceAll("").toLowerCase();
            if (cleanedSwear.isEmpty()) return message;

            String replacement = String.join("", Collections.nCopies(swear.length(), censorSymbol));

            StringBuilder regexBuilder = new StringBuilder("(?i)");
            for (char c : cleanedSwear.toCharArray()) {
                if (ignoreSpaces) {
                    regexBuilder.append("[^a-zA-Z]*\\s*");
                } else {
                    regexBuilder.append("[^a-zA-Z]*");
                }
                regexBuilder.append(Pattern.quote(String.valueOf(c)));
            }
            if (ignoreSpaces) {
                regexBuilder.append("[^a-zA-Z]*\\s*");
            } else {
                regexBuilder.append("[^a-zA-Z]*");
            }

            return message.replaceAll(regexBuilder.toString(), replacement);
        } catch (Exception e) {
            return message;
        }
    }

    private String getBlacklistHash() {
        return String.valueOf(getBlacklist().hashCode() +
                getWhitelist().hashCode() +
                temporaryBlacklist.hashCode() +
                temporaryWhitelist.hashCode());
    }

    private void clearCaches() {
        swearCache.clear();
        censorCache.clear();
    }

    private void manageCacheSize(Map<?, ?> cache) {
        if (cache.size() > CACHE_MAX_SIZE) {
            cache.clear();
        }
    }
}