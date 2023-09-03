package me.blurmit.frostbots.bot;

import lombok.Getter;
import lombok.Setter;
import me.blurmit.frostbots.arena.Arena;
import me.blurmit.frostbots.arena.ArenaType;
import me.blurmit.frostbots.config.Configuration;
import me.blurmit.frostbots.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.blurmit.frostbots.FrostBots;

public class Bot {

    @Getter
    private Player target;

    @Getter
    @Setter
    private Arena arena;

    @Getter
    @Setter
    private ArenaType arenaType;

    @Getter
    @Setter
    private Kit kit;

    public void spawn() {
        Configuration config = JavaPlugin.getPlugin(FrostBots.class).getConfiguration();
        String name = config.getBotName();
        String skin = config.getBotSkin();
    }

}
