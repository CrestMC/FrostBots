package me.blurmit.frostbots.user;

import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.duel.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserListener implements Listener {

    private final FrostBots plugin;
    private final UserManager userManager;

    public UserListener(FrostBots plugin, UserManager userManager) {
        this.plugin = plugin;
        this.userManager = userManager;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        userManager.createUser(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        User user = userManager.getUser(player);
        userManager.deleteUser(user);

        Duel duel = plugin.getDuelManager().getDuelByOpponent(player);
        if (duel != null) {
            duel.eliminate(player);
        }
    }

}
