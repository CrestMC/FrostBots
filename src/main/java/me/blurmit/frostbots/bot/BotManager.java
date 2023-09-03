package me.blurmit.frostbots.bot;

import me.blurmit.frostbots.FrostBots;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BotManager {

    private final FrostBots plugin;

    private final Set<Bot> bots;

    public BotManager(FrostBots plugin) {
        this.plugin = plugin;
        this.bots = new HashSet<>();

        load();
    }

    private void load() {

    }

    public Bot getBotByOpponent(Player player) {
        return bots.stream()
                .filter(bot -> bot.getTarget().getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    public Bot getBotByOpponent(UUID uuid) {
        return bots.stream()
                .filter(bot -> bot.getTarget().getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public Set<Bot> getBots() {
        return bots;
    }

}
