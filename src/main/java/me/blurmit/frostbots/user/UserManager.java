package me.blurmit.frostbots.user;

import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.arena.ArenaType;
import me.blurmit.frostbots.config.Configuration;
import me.blurmit.frostbots.kit.Kit;
import me.blurmit.frostbots.util.ScoreboardParser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;

public class UserManager {

    private final FrostBots plugin;
    private final Configuration config;
    private final UserListener listener;

    private final Set<User> users;
    private ItemStack[] hotbar;

    public UserManager(FrostBots plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.users = new HashSet<>();
        this.listener = new UserListener(plugin, this);

        load();
    }

    private void load() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Avoid the dreaded CME
            Set<User> users = new HashSet<>(this.users);

            users.stream()
                    .map(User::getPlayer)
                    .forEach(player -> {
                        Scoreboard scoreboard = player.getScoreboard();
                        int queueTime = ScoreboardParser.getQueueTime(scoreboard);

                        if (queueTime == -1) {
                            return;
                        }

                        if (queueTime != config.getBotRequestThreshold()) {
                            return;
                        }

                        Kit kit = ScoreboardParser.getQueueKit(scoreboard);
                        ArenaType type = ScoreboardParser.getQueueType(scoreboard);
                        plugin.getDuelManager().request(player, kit, type);
                    });
        }, 20L, 20L);
    }

    public User createUser(Player player) {
        if (users.size() == 0) {
            hotbar = player.getInventory().getContents();
        }

        User user = new User(player);
        users.add(user);

        return user;
    }

    public void deleteUser(User user) {
        users.remove(user);
    }

    public User getUser(Player player) {
        return users.stream()
                .filter(user -> user.getUUID().equals(player.getUniqueId()))
                .findFirst()
                .orElseGet(() -> createUser(player));
    }

    public ItemStack[] getLobbyHotbar() {
        return hotbar;
    }

    public Set<User> getUsers() {
        return users;
    }

}
