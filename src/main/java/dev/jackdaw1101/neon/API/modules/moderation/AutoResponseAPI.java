package dev.jackdaw1101.neon.API.modules.moderation;

import java.util.List;
import java.util.Map;

public interface AutoResponseAPI {
    void addResponse(String triggerWord, List<String> responses);
    void removeResponse(String triggerWord);
    void updateResponse(String triggerWord, List<String> responses);

    void setGlobalHoverText(List<String> hoverText);
    void setGlobalSound(String sound);
    void setGlobalSoundEnabled(boolean enabled);
    void setGlobalHoverEnabled(boolean enabled);

    Map<String, List<String>> getAllResponses();
    List<String> getResponsesForWord(String triggerWord);
    List<String> getGlobalHoverText();
    String getGlobalSound();
    boolean isSoundEnabled();
    boolean isHoverEnabled();

    void reloadResponses();
    void saveResponses();
}
