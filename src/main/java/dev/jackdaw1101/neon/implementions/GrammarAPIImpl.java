package dev.jackdaw1101.neon.implementions;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.modules.grammar.GrammarAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GrammarAPIImpl implements GrammarAPI {
    private final Neon plugin;
    private final Map<String, String> customAutoCorrectWords = new HashMap<>();

    public GrammarAPIImpl(Neon plugin) {
        this.plugin = plugin;

        customAutoCorrectWords.put("i", "I");
        customAutoCorrectWords.put("im", "I'm");
        customAutoCorrectWords.put("i'm", "I'm");
        customAutoCorrectWords.put("ill", "I'll");
        customAutoCorrectWords.put("i'll", "I'll");
        customAutoCorrectWords.put("cant", "can't");
        customAutoCorrectWords.put("youre", "you're");
        customAutoCorrectWords.put("dont", "don't");
        customAutoCorrectWords.put("theyre", "they're");
        customAutoCorrectWords.put("couldnt", "couldn't");
        customAutoCorrectWords.put("whos", "who's");
        customAutoCorrectWords.put("alot", "a lot");
    }

    @Override
    public void addAutoCorrectWord(String incorrect, String correct) {
        customAutoCorrectWords.put(incorrect.toLowerCase(), correct);
    }

    @Override
    public void removeAutoCorrectWord(String incorrect) {
        customAutoCorrectWords.remove(incorrect.toLowerCase());
    }

    @Override
    public void setAutoCorrectEnabled(boolean enabled) {
        plugin.getSettings().set("GRAMMAR-API.AUTO-CORRECT.ENABLED", enabled);
    }

    @Override
    public void setPunctuationCheckEnabled(boolean enabled) {
        plugin.getSettings().set("GRAMMAR-API.PUNCTUATION-CHECK", enabled);
    }

    @Override
    public void setCapitalizationEnabled(boolean enabled) {
        plugin.getSettings().set("GRAMMAR-API.CAPITALIZATION", enabled);
    }

    @Override
    public void setMinMessageLength(int length) {
        plugin.getSettings().set("GRAMMAR-API.MIN-MESSAGE-LENGTH", length);
    }

    @Override
    public Map<String, String> getAutoCorrectWords() {
        return new HashMap<>(customAutoCorrectWords);
    }

    @Override
    public boolean isAutoCorrectEnabled() {
        return plugin.getSettings().getBoolean("GRAMMAR-API.AUTO-CORRECT.ENABLED");
    }

    @Override
    public boolean isPunctuationCheckEnabled() {
        return plugin.getSettings().getBoolean("GRAMMAR-API.PUNCTUATION-CHECK");
    }

    @Override
    public boolean isCapitalizationEnabled() {
        return plugin.getSettings().getBoolean("GRAMMAR-API.CAPITALIZATION");
    }

    @Override
    public int getMinMessageLength() {
        return plugin.getSettings().getInt("GRAMMAR-API.MIN-MESSAGE-LENGTH");
    }

    @Override
    public String processMessage(String message) {
        String processed = message;

        if (isCapitalizationEnabled()) {
            try {
                processed = processed.substring(0, 1).toUpperCase() + processed.substring(1);
            } catch (Exception ignored) {}
        }

        if (isPunctuationCheckEnabled()) {
            char lastChar = processed.charAt(processed.length() - 1);
            if (!Arrays.asList('!', '.', ',', '?').contains(lastChar)) {
                processed += ".";
            }
        }

        if (isAutoCorrectEnabled()) {
            String[] words = processed.split(" ");
            StringBuilder sb = new StringBuilder();

            for (String word : words) {
                String corrected = customAutoCorrectWords.getOrDefault(word.toLowerCase(), word);
                sb.append(corrected).append(" ");
            }

            processed = sb.toString().trim();
        }

        return processed;
    }
}
