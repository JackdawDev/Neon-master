package dev.jackdaw1101.neon.API.modules.moderation;


import org.bukkit.entity.Player;
import java.util.List;
import java.util.Set;

public interface AntiSwearAPI {
    boolean isSwearWord(String word);
    boolean containsSwear(String message);
    void addToBlacklist(String word);
    void removeFromBlacklist(String word);
    void addToWhitelist(String word);
    void removeFromWhitelist(String word);
    void addTemporaryBlacklistWord(String word);
    void removeTemporaryBlacklistWord(String word);
    void addTemporaryWhitelistWord(String word);
    void removeTemporaryWhitelistWord(String word);
    void clearTemporaryBlacklist();
    void clearTemporaryWhitelist();
    List<String> getBlacklist();
    List<String> getWhitelist();
    Set<String> getTemporaryBlacklist();
    Set<String> getTemporaryWhitelist();
    int getSwearStrikes(Player player);
    void resetSwearStrikes(Player player);
    void setSwearStrikes(Player player, int strikes);
    String censorMessage(String message);
    String sanitizeMessage(String message);
    void reloadConfiguration();
}
