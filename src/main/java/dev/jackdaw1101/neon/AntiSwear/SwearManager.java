package dev.jackdaw1101.neon.AntiSwear;

import dev.jackdaw1101.neon.Neon;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SwearManager {


    private final Neon plugin;
    private final Map<String, Integer> playerStrikes;

    public SwearManager(Neon plugin) {
        this.plugin = plugin;
        this.playerStrikes = new HashMap<>();
    }

    /**
     * Adds a swear strike to the player.
     *
     * @param player the player who swore.
     * @return the number of strikes the player now has.
     */
    public int addSwear(Player player) {
        String playerName = player.getName();
        int currentStrikes = playerStrikes.getOrDefault(playerName, 0) + 1;
        playerStrikes.put(playerName, currentStrikes);
        return currentStrikes;
    }

    /**
     * Retrieves the number of strikes for a player.
     *
     * @param player the player to get strikes for.
     * @return the number of strikes the player has.
     */
    public int getStrikes(Player player) {
        return playerStrikes.getOrDefault(player.getName(), 0);
    }

    /**
     * Resets the strikes for a specific player.
     *
     * @param player the player to reset strikes for.
     */
    public void resetStrikes(Player player) {
        playerStrikes.remove(player.getName());
    }
}
