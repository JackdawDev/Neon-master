package dev.jackdaw1101.neon.API.modules.grammar;

import java.util.Map;

public interface GrammarAPI {
    void addAutoCorrectWord(String incorrect, String correct);
    void removeAutoCorrectWord(String incorrect);
    void setAutoCorrectEnabled(boolean enabled);
    void setPunctuationCheckEnabled(boolean enabled);
    void setCapitalizationEnabled(boolean enabled);
    void setMinMessageLength(int length);

    Map<String, String> getAutoCorrectWords();
    boolean isAutoCorrectEnabled();
    boolean isPunctuationCheckEnabled();
    boolean isCapitalizationEnabled();
    int getMinMessageLength();

    String processMessage(String message);
}
