package me.blurmit.frostbots.user;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.blurmit.frostbots.FrostBots;

import java.util.UUID;

public class User {

    private final FrostBots plugin;

    private final Player player;

    public User(Player player) {
        this.plugin = JavaPlugin.getPlugin(FrostBots.class);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUUID() {
        return player.getUniqueId();
    }

    public String getName() {
        return player.getName();
    }

}
