package dev.jackdaw1101.neon.Manager.MentionManager;


import dev.jackdaw1101.neon.API.Features.MentionEvent;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ListenerMentions implements Listener {

    private final Neon plugin;
    private final Map<UUID, Long> mentionCooldowns = new HashMap<>();

    public ListenerMentions(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player sender = event.getPlayer();
        String message = event.getMessage();

        boolean permRequired = (boolean) plugin.getSettings().getBoolean("MENTION.PERMISSION-REQUIRED");

        if (permRequired && !sender.hasPermission(plugin.getPermissionManager().getString("MENTION"))) return;
        if (!(boolean) plugin.getSettings().getBoolean("MENTION.ENABLED")) return;

        String mentionSymbol = (String) plugin.getSettings().getString("MENTION.SYMBOL");
        String mentionColor = (String) plugin.getSettings().getString("MENTION.COLOR");
        boolean everyoneEnabled = (boolean) plugin.getSettings().getBoolean("MENTION.EVERYONE.ENABLED");
        String everyoneWord = (String) plugin.getSettings().getString("MENTION.EVERYONE.WORD");
        long cooldownTime = (int) plugin.getSettings().getInt("MENTION.COOLDOWN");
        String afterMentionColor = (String) plugin.getSettings().getString("MENTION.AFTER-COLOR");
        boolean cooldownTimeEnabled = (boolean) plugin.getSettings().getBoolean("MENTION.COOLDOWN-ENABLED");


        if (cooldownTimeEnabled && !sender.hasPermission(plugin.getPermissionManager().getString("MENTION-COOLDOWN-BYPASS")) && mentionCooldowns.containsKey(sender.getUniqueId())) {
            long lastMentionTime = mentionCooldowns.get(sender.getUniqueId());
            if (System.currentTimeMillis() - lastMentionTime < cooldownTime) {
                sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MENTION-COOLDOWN-WARN")));
                return;
            }
        }

        boolean mentionedAnyone = false;
        String newMessage = message;


        if (everyoneEnabled && sender.hasPermission(plugin.getPermissionManager().getString("MENTION-EVERYONE"))) {
            if ((!mentionSymbol.isEmpty() && message.contains(mentionSymbol + everyoneWord)) || (mentionSymbol.isEmpty() && message.contains(everyoneWord))) {
                for (Player target : Bukkit.getOnlinePlayers()) {

                    notifyPlayer(target, sender);
                }
                String formattedMention = ColorHandler.color(mentionColor) + (mentionSymbol.isEmpty() ? "" : mentionSymbol) + everyoneWord + afterMentionColor;
                formattedMention = applyPlaceholders(sender, formattedMention);
                newMessage = newMessage.replace((mentionSymbol.isEmpty() ? "" : mentionSymbol) + everyoneWord, formattedMention);


                MentionEvent mentionEvent = new MentionEvent(sender, null, newMessage, false, mentionSymbol, true, false);
                Bukkit.getPluginManager().callEvent(mentionEvent);

                event.setMessage(newMessage);
                mentionedAnyone = true;
            }
        }


        for (Player mentioned : Bukkit.getOnlinePlayers()) {
            if (mentioned.equals(sender)) continue;

            String mentionTag = mentionSymbol + mentioned.getName();
            boolean mentionedBySymbol = !mentionSymbol.isEmpty() && message.contains(mentionTag);
            boolean mentionedByNameOnly = mentionSymbol.isEmpty() && message.contains(mentioned.getName());

            if (mentionedBySymbol || mentionedByNameOnly) {
                notifyPlayer(mentioned, sender);
                mentionedAnyone = true;


                String formattedMention = ColorHandler.color(mentionColor) + mentioned.getName() + afterMentionColor;
                formattedMention = applyPlaceholders(sender, formattedMention);
                newMessage = newMessage.replace(mentionTag, formattedMention).replace(mentioned.getName(), formattedMention);


                MentionEvent mentionEvent = new MentionEvent(sender, mentioned, newMessage, mentionedBySymbol, mentionSymbol, false, false);
                Bukkit.getPluginManager().callEvent(mentionEvent);
            }
        }

        if (mentionedAnyone) {
            event.setMessage(ColorHandler.color(newMessage));
            mentionCooldowns.put(sender.getUniqueId(), System.currentTimeMillis());
        }
    }

    private void notifyPlayer(Player target, Player sender) {

        String notifyMessage = plugin.getMessageManager().getString("MENTION-NOTIFY")
            .replace("%sender%", sender.getName());
        notifyMessage = applyPlaceholders(sender, notifyMessage);
        target.sendMessage(ColorHandler.color(notifyMessage));

        if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
            if ((boolean) plugin.getSettings().getBoolean("MENTION.ENABLE-SOUND")) {
                SoundUtil.playSound(target, (String) plugin.getSettings().getString("MENTION.SOUND"), 1.0f, 1.0f);
            }
        } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
            XSounds.playSound(target, (String) plugin.getSettings().getString("MENTION.SOUND"), 1.0f, 1.0f);
        }


        if ((boolean) plugin.getSettings().getBoolean("MENTION.TITLE.ENABLED")) {
            String title = (String) plugin.getSettings().getString("MENTION.TITLE.HEADER");
            String subtitle = (String) plugin.getSettings().getString("MENTION.TITLE.FOOTER");

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
