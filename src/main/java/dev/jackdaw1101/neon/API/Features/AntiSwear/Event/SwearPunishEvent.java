package dev.jackdaw1101.neon.API.Features.AntiSwear.Event;

import org.bukkit.entity.Player;

public class SwearPunishEvent extends AntiSwearEvent {
    private final int strikes;
    private String punishCommand;

    public SwearPunishEvent(Player player, String message, int strikes, String punishCommand) {
        super(player, message);
        this.strikes = strikes;
        this.punishCommand = punishCommand;
    }

    public int getStrikes() {
        return strikes;
    }

    public String getPunishCommand() {
        return punishCommand;
    }

    public void setPunishCommand(String punishCommand) {
        this.punishCommand = punishCommand;
    }
}
