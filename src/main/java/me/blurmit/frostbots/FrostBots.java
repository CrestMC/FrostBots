package me.blurmit.frostbots;

import lombok.Getter;
import me.blurmit.frostbots.config.Configuration;
import me.blurmit.frostbots.kit.KitManager;
import org.bukkit.plugin.java.JavaPlugin;
import me.blurmit.frostbots.arena.ArenaManager;
import me.blurmit.frostbots.bot.BotManager;
import me.blurmit.frostbots.command.DuelBotCommand;
import me.blurmit.frostbots.duel.Duel;
import me.blurmit.frostbots.duel.DuelManager;
import me.blurmit.frostbots.user.UserManager;

public final class FrostBots extends JavaPlugin {

    @Getter
    private Configuration configuration;

    @Getter
    private ArenaManager arenaManager;
    @Getter
    private BotManager botManager;
    @Getter
    private KitManager kitManager;
    @Getter
    private UserManager userManager;
    @Getter
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        getLogger().info("Loading configuration...");
        configuration = new Configuration(this);

        getLogger().info("Loading arenas...");
        arenaManager = new ArenaManager(this);

        getLogger().info("Loading kits...");
        kitManager = new KitManager(this);

        getLogger().info("Loading user manager...");
        userManager = new UserManager(this);

        getLogger().info("Loading duel manager...");
        duelManager = new DuelManager(this);

        getLogger().info("Initializing bots...");
        botManager = new BotManager(this);

        getLogger().info("Registering commands...");
        new DuelBotCommand(this);

        getLogger().info("Frost Bots has been successfully loaded and enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving & destroying configuration instance...");
        saveConfig();
        configuration = null;

        getLogger().info("Stopping all active duels...");
        if (duelManager != null) {
            for (Duel duel : duelManager.getDuels()) {
                duel.eliminate(duel.getOpponent());
            }

            duelManager.getDuels().clear();
            duelManager = null;
        }

        getLogger().info("Destroying arena manager instance...");
        arenaManager = null;

        getLogger().info("Destroying kits manager instance...");
        kitManager = null;

        getLogger().info("Destroying bots manager instance...");
        botManager = null;

        getLogger().info("Destroying user manager instance...");
        userManager = null;

        getLogger().info("Frost Bots has been successfully disabled.");
    }

}
