package dev.jackdaw1101.neon.IGUI;

import com.cryptomorin.xseries.XMaterial;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class NeonGUI implements Listener {
    private final String title;
    private final int size;
    private final String permission;
    private final Map<Integer, String> commands = new HashMap<>();
    private final Inventory inventory;
    private final Neon plugin;

    public NeonGUI(Neon plugin, String title, int size, String permission) {
        this.plugin = plugin;
        this.title = title;
        this.size = size;
        this.permission = permission;
        this.inventory = Bukkit.createInventory(null, size, title);

        // Fill empty slots with black stained glass pane
        ItemStack filler = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
        if (filler != null) {
            ItemMeta meta = filler.getItemMeta();
            if (meta != null) meta.setDisplayName(" ");
            filler.setItemMeta(meta);

            for (int i = 0; i < size; i++) inventory.setItem(i, filler);
        }
    }

    public String getTitle() {
        return title;
    }

    public void addItem(int slot, ItemStack item, String displayName, List<String> lore, String command) {
        if (slot < 0 || slot >= size) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName.replace("&", "§"));
            List<String> formattedLore = new ArrayList<>();
            for (String line : lore) {
                formattedLore.add(line.replace("&", "§"));
            }
            meta.setLore(formattedLore);
            item.setItemMeta(meta);
        }

        inventory.setItem(slot, item);
        commands.put(slot, command);
    }

    public void openGUI(Player player) {
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage("You don't have permission to open this GUI.");
            return;
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals(title)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (commands.containsKey(slot)) {
                String command = commands.get(slot).replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
}
