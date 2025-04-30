package dev.jackdaw1101.neon.api.features.antiswear.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
public class SwearPunishEvent extends AntiSwearEvent {
    private final int strikes;
    @Setter
    private String punishCommand;

    public SwearPunishEvent(Player player, String message, int strikes, String punishCommand) {
        super(player, message);
        this.strikes = strikes;
        this.punishCommand = punishCommand;
    }

}
