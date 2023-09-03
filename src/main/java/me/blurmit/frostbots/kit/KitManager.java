package me.blurmit.frostbots.kit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import me.blurmit.frostbots.FrostBots;

import java.util.*;

public class KitManager {

    private final FrostBots plugin;
    private final FileConfiguration config;

    private final Set<Kit> kits;

    public KitManager(FrostBots plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration().get("kits.yml");
        this.kits = new HashSet<>();

        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        Map<String, Object> kitsRaw = config.getConfigurationSection("kits").getValues(false);

        for (Map.Entry<String, Object> rawKitData : kitsRaw.entrySet()) {
            String name = rawKitData.getKey();
            ConfigurationSection data = (ConfigurationSection) rawKitData.getValue();

            Kit kit = new Kit();
            kit.setName(name);
            kit.setIcon(data.getItemStack("icon"));
            kit.setEnabled(data.getBoolean("enabled"));
            kit.setLives(data.getInt("lives"));
            kit.setArenaWhitelist(data.getStringList("arenaWhitelist"));
            kit.setDamageTicks(data.getInt("damageTicks"));
            kit.setDisplayName(data.getString("displayName"));
            kit.setPotionEffects(plugin.getConfiguration().getPotionEffects(config, "kits." + name + ".potionEffects"));
            kit.setKnockbackProfile(KnockbackProfile.getByName(data.getString("kbProfile")));
            kit.setArmor((List<ItemStack>) data.getList("armor"));
            kit.setInventory((List<ItemStack>) data.getList("contents"));
            kit.setFallDamage(!data.getBoolean("noFall"));
            kit.setInvincible(data.getBoolean("stickFight") || data.getBoolean("sumo") || data.getBoolean("spleef") || data.getBoolean("boxing"));

            kits.add(kit);
        }

        plugin.getLogger().info("Loaded " + kits.size() + " kits.");
    }

    public Kit getKitByName(String name) {
        return kits.stream()
                .filter(kit -> kit.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Set<Kit> getKits() {
        return kits;
    }

}
