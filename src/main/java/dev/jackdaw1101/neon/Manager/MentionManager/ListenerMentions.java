package dev.jackdaw1101.neon.Manager.MentionManager;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ListenerMentions implements Listener {
    private final Neon plugin;
    private final Map<UUID, Long> mentionCooldowns = new HashMap<>();

    public ListenerMentions(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player sender = event.getPlayer();
        String message = event.getMessage();

        boolean permRequired = (boolean) plugin.getSettings().getValue("MENTION.PERMISSION-REQUIRED", true);

        if (permRequired && !sender.hasPermission(plugin.getPermissionManager().getPermission("MENTION"))) return;
        if (!(boolean) plugin.getSettings().getValue("MENTION.ENABLED", true)) return;

        String mentionSymbol = (String) plugin.getSettings().getValue("MENTION.SYMBOL", "@");
        String mentionColor = (String) plugin.getSettings().getValue("MENTION.COLOR", "§e");
        boolean everyoneEnabled = (boolean) plugin.getSettings().getValue("MENTION.EVERYONE.ENABLED", true);
        String everyoneWord = (String) plugin.getSettings().getValue("MENTION.EVERYONE.WORD", "everyone");
        long cooldownTime = (int) plugin.getSettings().getValue("MENTION.COOLDOWN");
        String afterMentionColor = (String) plugin.getSettings().getValue("MENTION.AFTER-COLOR", "&r");
        boolean cooldownTimeEnabled = (boolean) plugin.getSettings().getValue("MENTION.COOLDOWN-ENABLED");

        // Handle Cooldown
        if (cooldownTimeEnabled && !sender.hasPermission(plugin.getPermissionManager().getPermission("MENTION-COOLDOWN-BYPASS")) && mentionCooldowns.containsKey(sender.getUniqueId())) {
            long lastMentionTime = mentionCooldowns.get(sender.getUniqueId());
            if (System.currentTimeMillis() - lastMentionTime < cooldownTime) {
                sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MENTION-COOLDOWN-WARN")));
                return;
            }
        }

        boolean mentionedAnyone = false;
        String newMessage = message;

        // Check for "@everyone" or just "everyone" if mention symbol is empty
        if (everyoneEnabled && sender.hasPermission(plugin.getPermissionManager().getPermission("MENTION-EVERYONE"))) {
            if ((!mentionSymbol.isEmpty() && message.contains(mentionSymbol + everyoneWord)) || (mentionSymbol.isEmpty() && message.contains(everyoneWord))) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    notifyPlayer(target, sender);
                }
                String formattedMention = ColorHandler.color(mentionColor) + (mentionSymbol.isEmpty() ? "" : mentionSymbol) + everyoneWord + afterMentionColor;
                formattedMention = applyPlaceholders(sender, formattedMention);
                newMessage = newMessage.replace((mentionSymbol.isEmpty() ? "" : mentionSymbol) + everyoneWord, formattedMention);

                event.setMessage(newMessage);
                mentionedAnyone = true;
            }
        }

        // Match mentions for "@player" or just "player" if mention symbol is empty
        for (Player mentioned : Bukkit.getOnlinePlayers()) {
            if (mentioned.equals(sender)) continue;

            String mentionTag = mentionSymbol + mentioned.getName();
            boolean mentionedBySymbol = !mentionSymbol.isEmpty() && message.contains(mentionTag);
            boolean mentionedByNameOnly = mentionSymbol.isEmpty() && message.contains(mentioned.getName());

            if (mentionedBySymbol || mentionedByNameOnly) {
                notifyPlayer(mentioned, sender);
                mentionedAnyone = true;

                // Replace mention in message with color formatting and apply placeholders
                String formattedMention = ColorHandler.color(mentionColor) + mentioned.getName() + afterMentionColor;
                formattedMention = applyPlaceholders(sender, formattedMention);
                newMessage = newMessage.replace(mentionTag, formattedMention).replace(mentioned.getName(), formattedMention);
            }
        }

        if (mentionedAnyone) {
            event.setMessage(ColorHandler.color(newMessage));
            mentionCooldowns.put(sender.getUniqueId(), System.currentTimeMillis()); // Update cooldown
        }
    }

    private void notifyPlayer(Player target, Player sender) {
        // Send chat message notification
        String notifyMessage = plugin.getMessageManager().getMessage("MENTION-NOTIFY")
                .replace("%sender%", sender.getName());
        notifyMessage = applyPlaceholders(sender, notifyMessage);
        target.sendMessage(ColorHandler.color(notifyMessage));

        if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
            if ((boolean) plugin.getSettings().getValue("MENTION.ENABLE-SOUND", true)) {
                SoundUtil.playSound(target, (String) plugin.getSettings().getValue("MENTION.SOUND"), 1.0f, 1.0f);
            }
        } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
            XSounds.playSound(target, (String) plugin.getSettings().getValue("MENTION.SOUND"), 1.0f, 1.0f);
        }

        // Send title if enabled
        if ((boolean) plugin.getSettings().getValue("MENTION.TITLE.ENABLED", false)) {
            String title = (String) plugin.getSettings().getValue("MENTION.TITLE.HEADER", "&cMentioned!");
            String subtitle = (String) plugin.getSettings().getValue("MENTION.TITLE.FOOTER", "&7%sender% mentioned you!");

            title = applyPlaceholders(sender, title);
            subtitle = applyPlaceholders(sender, subtitle.replace("%sender%", sender.getName()));

            target.sendTitle(
                    ColorHandler.color(title),
                    ColorHandler.color(subtitle),
                    10, 40, 10
            );
        }
    }

    private String applyPlaceholders(Player player, String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }
}
