package dev.jackdaw1101.neon.anticaps;

import dev.jackdaw1101.neon.api.features.anticaps.AntiCapsEvent;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.utils.CC;
import dev.jackdaw1101.neon.api.utils.ColorHandler;
import dev.jackdaw1101.neon.utils.isounds.SoundUtil;
import dev.jackdaw1101.neon.utils.isounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AntiCapsSystem implements Listener {
    private final Neon plugin;

    public AntiCapsSystem(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if (plugin.getSettings().getBoolean("ANTI-CAPS.ENABLED")) {
            handleCapsCheck(event.getPlayer(), event.getMessage(), event, false);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        if (plugin.getSettings().getBoolean("ANTI-CAPS.CHECK-COMMANDS")) {
            handleCapsCheck(event.getPlayer(), event.getMessage(), event, true);
        }
    }

    private void handleCapsCheck(Player player, String message, Cancellable cancellable, boolean isCommand) {
        if (player.hasPermission(plugin.getPermissionManager().getString("ANTI-CAPS-BYPASS"))) {
            return;
        }

        CapsCheckResult result = checkCaps(message,
            plugin.getSettings().getInt("ANTI-CAPS.MIN-MESSAGE-LENGTH"),
            plugin.getSettings().getInt("ANTI-CAPS.REQUIRED-PERCENTAGE")
        );

        if (result.isCapsViolation()) {

            String warningMessage = plugin.getMessageManager().getString("ANTI-CAPS-WARNING");


            if (warningMessage == null || warningMessage.isEmpty()) {
                warningMessage = "&cPlease avoid using excessive capital letters! (Your message was {percentage}% caps, max is {max_percentage}%)";
                plugin.getLogger().warning("ANTI-CAPS-WARNING message not found in config, using default");
            }


            warningMessage = warningMessage
                .replace("{player}", player.getName())
                .replace("{percentage}", String.format("%.1f", result.getCapsPercentage()))
                .replace("{max_percentage}", String.valueOf(result.getRequiredPercentage()));


            warningMessage = ColorHandler.color(warningMessage);
            boolean isdebug = plugin.getSettings().getBoolean("DEBUG-MODE");
            if (isdebug) {
                plugin.getLogger().info("Final formatted warning: " + warningMessage);
            }

            AntiCapsEvent capsEvent = new AntiCapsEvent(
                player,
                message,
                result.getCapsPercentage(),
                result.getUpperChars(),
                result.getLowerChars(),
                result.getMinLength(),
                result.getRequiredPercentage(),
                isCommand,
                true,
                warningMessage,
                plugin.getSettings().getString("ANTI-CAPS.SOUND"),
                plugin.getSettings().getBoolean("ANTI-CAPS.SOUND-ENABLED")
            );

            plugin.getServer().getPluginManager().callEvent(capsEvent);

            if (capsEvent.isCancelled()) {
                return;
            }

            if (capsEvent.shouldCancel()) {
                cancellable.setCancelled(true);
                if (isdebug) {
                    Bukkit.getConsoleSender().sendMessage(CC.GRAY + capsEvent.getPlayer().getName() + " Triggered Anti Caps");
                }
                player.sendMessage(capsEvent.getWarningMessage());

                if (capsEvent.shouldPlaySound() && capsEvent.getSound() != null) {
                    playSound(player, capsEvent.getSound());
                }
            }
        }
    }

    private CapsCheckResult checkCaps(String message, int minLength, int requiredPercentage) {
        int upperChar = 0;
        int lowerChar = 0;

        if (message.length() >= minLength) {
            for (char c : message.toCharArray()) {
                if (Character.isLetter(c)) {
                    if (Character.isUpperCase(c)) {
                        upperChar++;
                    } else {
                        lowerChar++;
                    }
                }
            }

            if (upperChar + lowerChar > 0) {
                double capsPercentage = (double) upperChar / (upperChar + lowerChar) * 100;
                return new CapsCheckResult(
                    capsPercentage >= requiredPercentage,
                    capsPercentage,
                    upperChar,
                    lowerChar,
                    minLength,
                    requiredPercentage
                );
            }
        }
        return new CapsCheckResult(false, 0, 0, 0, minLength, requiredPercentage);
    }

    private void playSound(Player player, String sound) {
        if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
            SoundUtil.playSound(player, sound, 1.0f, 1.0f);
        } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
            XSounds.playSound(player, sound, 1.0f, 1.0f);
        }
    }

    private static class CapsCheckResult {
        private final boolean capsViolation;
        private final double capsPercentage;
        private final int upperChars;
        private final int lowerChars;
        private final int minLength;
        private final int requiredPercentage;

        public CapsCheckResult(boolean capsViolation, double capsPercentage,
                               int upperChars, int lowerChars,
                               int minLength, int requiredPercentage) {
            this.capsViolation = capsViolation;
            this.capsPercentage = capsPercentage;
            this.upperChars = upperChars;
            this.lowerChars = lowerChars;
            this.minLength = minLength;
            this.requiredPercentage = requiredPercentage;
        }

        public boolean isCapsViolation() {
            return capsViolation;
        }

        public double getCapsPercentage() {
            return capsPercentage;
        }

        public int getUpperChars() {
            return upperChars;
        }

        public int getLowerChars() {
            return lowerChars;
        }

        public int getMinLength() {
            return minLength;
        }

        public int getRequiredPercentage() {
            return requiredPercentage;
        }
    }
}
