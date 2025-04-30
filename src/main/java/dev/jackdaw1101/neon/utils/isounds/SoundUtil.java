package dev.jackdaw1101.neon.Utils.ISounds;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoundUtil {

    private static final String VERSION;
    private static final Map<String, Sound> SOUND_MAP = new HashMap<>();

    static {

        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);


        try {
            if (getMajorVersion() >= 13) {
                SOUND_MAP.put("LEVEL_UP", Sound.valueOf("ENTITY_PLAYER_LEVELUP"));
                SOUND_MAP.put("CLICK", Sound.valueOf("UI_BUTTON_CLICK"));
                SOUND_MAP.put("EXPLODE", Sound.valueOf("ENTITY_GENERIC_EXPLODE"));
                SOUND_MAP.put("HURT", Sound.valueOf("ENTITY_PLAYER_HURT"));
                SOUND_MAP.put("DEATH", Sound.valueOf("ENTITY_PLAYER_DEATH"));
                SOUND_MAP.put("EAT", Sound.valueOf("ENTITY_PLAYER_BURP"));
                SOUND_MAP.put("ANVIL", Sound.valueOf("BLOCK_ANVIL_LAND"));
                SOUND_MAP.put("BOW", Sound.valueOf("ENTITY_ARROW_SHOOT"));
                SOUND_MAP.put("DOOR", Sound.valueOf("BLOCK_WOODEN_DOOR_OPEN"));
                SOUND_MAP.put("CHEST", Sound.valueOf("BLOCK_CHEST_OPEN"));
                SOUND_MAP.put("FIZZ", Sound.valueOf("BLOCK_FIRE_EXTINGUISH"));
                SOUND_MAP.put("NOTE", Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"));
                SOUND_MAP.put("THUNDER", Sound.valueOf("ENTITY_LIGHTNING_BOLT_THUNDER"));
                SOUND_MAP.put("VILLAGER_TRADE", Sound.valueOf("ENTITY_VILLAGER_YES"));
                SOUND_MAP.put("VILLAGER_NO", Sound.valueOf("ENTITY_VILLAGER_NO"));
                SOUND_MAP.put("DOOR_CLOSE", Sound.valueOf("BLOCK_WOODEN_DOOR_CLOSE"));
                SOUND_MAP.put("ZOMBIE_ATTACK", Sound.valueOf("ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR"));
                SOUND_MAP.put("SPIDER_WALK", Sound.valueOf("ENTITY_SPIDER_STEP"));
                SOUND_MAP.put("ENDER_DRAGON_DEATH", Sound.valueOf("ENTITY_ENDER_DRAGON_DEATH"));
                SOUND_MAP.put("WOLF_HOWL", Sound.valueOf("ENTITY_WOLF_HOWL"));
            } else {
                SOUND_MAP.put("LEVEL_UP", Sound.valueOf("LEVEL_UP"));
                SOUND_MAP.put("CLICK", Sound.valueOf("CLICK"));
                SOUND_MAP.put("EXPLODE", Sound.valueOf("EXPLODE"));
                SOUND_MAP.put("HURT", Sound.valueOf("HURT_FLESH"));
                SOUND_MAP.put("DEATH", Sound.valueOf("DEATH"));
                SOUND_MAP.put("EAT", Sound.valueOf("BURP"));
                SOUND_MAP.put("ANVIL", Sound.valueOf("ANVIL_LAND"));
                SOUND_MAP.put("BOW", Sound.valueOf("SHOOT_ARROW"));
                SOUND_MAP.put("DOOR", Sound.valueOf("DOOR_OPEN"));
                SOUND_MAP.put("CHEST", Sound.valueOf("CHEST_OPEN"));
                SOUND_MAP.put("FIZZ", Sound.valueOf("FIZZ"));
                SOUND_MAP.put("NOTE", Sound.valueOf("NOTE_PLING"));
                SOUND_MAP.put("THUNDER", Sound.valueOf("AMBIENCE_THUNDER"));
                SOUND_MAP.put("VILLAGER_TRADE", Sound.valueOf("VILLAGER_YES"));
                SOUND_MAP.put("VILLAGER_NO", Sound.valueOf("VILLAGER_NO"));
                SOUND_MAP.put("DOOR_CLOSE", Sound.valueOf("DOOR_CLOSE"));
                SOUND_MAP.put("ZOMBIE_ATTACK", Sound.valueOf("ZOMBIE_WOOD"));
                SOUND_MAP.put("SPIDER_WALK", Sound.valueOf("SPIDER_WALK"));
                SOUND_MAP.put("ENDER_DRAGON_DEATH", Sound.valueOf("ENDERDRAGON_DEATH"));
                SOUND_MAP.put("WOLF_HOWL", Sound.valueOf("WOLF_HOWL"));
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static void playSound(Player player, String soundKey, float volume, float pitch) {
        Sound sound = SOUND_MAP.get(soundKey);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static String getVersion() {
        return VERSION;
    }

    private static int getMajorVersion() {
        String[] split = VERSION.split("_");
        return Integer.parseInt(split[1]);
    }
}
