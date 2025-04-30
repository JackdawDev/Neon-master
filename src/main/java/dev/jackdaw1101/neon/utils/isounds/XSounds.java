package dev.jackdaw1101.neon.utils.isounds;

import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;

public class XSounds {

    public static void playSound(Player player, String soundKey, float volume, float pitch) {
        XSound xSound = XSound.matchXSound(soundKey).orElse(null);
        if (xSound != null) {
            xSound.play(player, volume, pitch);
        }
    }
}

