package dev.jackdaw1101.neon.modules.automated;

import dev.jackdaw1101.neon.API.modules.events.NeonPlayerChatEvent;
import dev.jackdaw1101.neon.API.modules.moderation.IAutoResponse;
import dev.jackdaw1101.neon.implementions.IAutoResponseImpl;
import dev.jackdaw1101.neon.API.modules.events.AutoResponseEvent;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class AutoResponse implements Listener {
    private final Neon plugin;
    private final IAutoResponseImpl api;

    public AutoResponse(Neon plugin) {
        this.plugin = plugin;
        this.api = new IAutoResponseImpl(plugin);
        plugin.getServer().getServicesManager().register(IAutoResponse.class, api, plugin, org.bukkit.plugin.ServicePriority.Normal);
    }

    @EventHandler
    public void onChat(NeonPlayerChatEvent event) {
        if (event.isCancelled() || !plugin.getSettings().getBoolean("AUTO-RESPONSE-ENABLED")) {
            return;
        }

        Player player = event.getSender();
        String message = event.getMessage().toLowerCase();

        for (String triggerWord : api.getAllResponses().keySet()) {
            if (message.contains(triggerWord)) {
                List<String> responses = api.getResponsesForWord(triggerWord);
                if (responses.isEmpty()) continue;

                AutoResponseEvent responseEvent = new AutoResponseEvent(
                        player,
                        triggerWord,
                        responses,
                        api.getGlobalHoverText(),
                        api.getGlobalSound(),
                        api.isSoundEnabled(),
                        api.isHoverEnabled()
                );

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(responseEvent);
                });

                if (responseEvent.isCancelled()) return;

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        sendResponse(player, responseEvent);
                    } catch (Exception e) {
                        DebugUtil.debugError("Failed to call response event: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, 1L * plugin.getSettings().getInt("ASYNC.AUTO-RESPONSE-FIX_TICKS"));
            }
        }
    }

    private void sendResponse(Player player, AutoResponseEvent event) {
        String format = plugin.getMessageManager().getString("FORMAT");
        format = ColorHandler.color(format);

        TextComponent hoverComponent = null;
        if (event.shouldUseHover() && !event.getHoverText().isEmpty()) {
            StringBuilder hoverText = new StringBuilder();
            for (String line : event.getHoverText()) {
                hoverText.append(PlaceholderAPI.setPlaceholders(player, ColorHandler.color(line))).append("\n");
            }
            hoverComponent = new TextComponent(hoverText.toString().trim());
        }

        for (String responseLine : event.getResponses()) {
            String formattedResponse = PlaceholderAPI.setPlaceholders(player, ColorHandler.color(responseLine));
            String finalMessage = format.replace("%answer%", formattedResponse);

            TextComponent textComponent = new TextComponent(finalMessage);

            if (hoverComponent != null) {
                textComponent.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new TextComponent[]{hoverComponent}
                ));
            }

            Bukkit.getScheduler().runTask(plugin, () ->
                player.spigot().sendMessage(textComponent)
            );

            if (event.shouldPlaySound() && event.getSound() != null) {
                if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                    ISound.playSound(player, event.getSound(), 1.0f, 1.0f);
                } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                    XSounds.playSound(player, event.getSound(), 1.0f, 1.0f);
                }
            }
        }
    }
}
