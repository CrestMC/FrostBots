package me.blurmit.frostbots.kit;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.arena.Arena;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Kit {

    private String name;
    private String displayName;
    private ItemStack icon;
    private List<ItemStack> armor;
    private List<ItemStack> inventory;
    private boolean enabled;
    private boolean invincible;
    private boolean fallDamage;
    private List<PotionEffect> potionEffects;
    private List<Arena> arenaWhitelist;
    private KnockbackProfile knockbackProfile;
    private int lives;
    private int damageTicks;

    public void setArenaWhitelist(List<String> arenaWhitelist) {
        FrostBots plugin = JavaPlugin.getPlugin(FrostBots.class);

        this.arenaWhitelist = arenaWhitelist.stream()
                .map(arenaName -> plugin.getArenaManager().getArenaByName(arenaName))
                .collect(Collectors.toList());
    }

}
