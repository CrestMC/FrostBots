package me.blurmit.frostbots.duel;

import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;

import java.util.List;

public class DuelListener implements Listener {

    private final FrostBots plugin;
    private final DuelManager duelManager;

    private final FileConfiguration config;
    private final FileConfiguration messages;

    private final String notAllowInStateMessage;
    private final List<String> blockedCommands;

    public DuelListener(FrostBots plugin, DuelManager duelManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;

        this.config = plugin.getConfiguration().get("settings.yml");
        this.messages = plugin.getConfiguration().get("messages.yml");

        this.blockedCommands = config.getStringList("SETTINGS.MATCH.BLOCKED-COMMANDS");
        this.notAllowInStateMessage = ChatUtil.color(messages.getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getDuelByOpponent(player);

        if (duel == null) {
            return;
        }

        if (duel.getState() != Duel.State.START) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        player.teleport(from);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getDuelByOpponent(player);

        if (duel == null) {
            return;
        }

        if (duel.getState() != Duel.State.START && duel.getState() != Duel.State.IN_MATCH) {
            return;
        }

        event.setCancelled(false);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> event.getItemDrop().remove());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getDuelByOpponent(player);

        if (duel == null) {
            return;
        }

        if (duel.getState() != Duel.State.START && duel.getState() != Duel.State.IN_MATCH) {
            return;
        }

        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryInteract(InventoryInteractEvent event) {
        Player player = (Player) event.getWhoClicked();
        Duel duel = duelManager.getDuelByOpponent(player);

        if (duel == null) {
            return;
        }

        if (duel.getState() == Duel.State.REQUESTED || duel.getState() == Duel.State.IN_MATCH_END) {
            return;
        }

        if (!event.getInventory().equals(player.getInventory())) {
            return;
        }

        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER || event.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player attacked = (Player) event.getEntity();
        Duel duel = duelManager.getDuelByOpponent(attacked) != null ? duelManager.getDuelByOpponent(attacked) : duelManager.getDuelByOpponent(attacker) != null ? duelManager.getDuelByOpponent(attacker) : null;

        if (duel == null) {
            return;
        }

        if (duel.getState() != Duel.State.IN_MATCH) {
            return;
        }

        if (duel.isHitDelay()) {
            event.setCancelled(true);
            return;
        }

        if (duel.getKit().isInvincible()) {
            event.setDamage(0);
        }

        event.setCancelled(false);

        if (event.getFinalDamage() >= attacked.getHealth()) {
            duel.eliminate(attacked);
            event.setCancelled(true);
            return;
        }

        if (!attacked.equals(duel.getOpponent())) {
            duel.setHitDelay(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> duel.setHitDelay(false), 10L);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Duel duel = duelManager.getDuelByOpponent(player);

        if (duel == null) {
            return;
        }

        if (duel.getState() != Duel.State.IN_MATCH) {
            return;
        }

        duel.eliminate(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        Duel duel = duelManager.getDuelByOpponent(damaged);

        if (duel == null) {
            return;
        }

        if (duel.getState() != Duel.State.IN_MATCH) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getDuelByOpponent(player);

        if (duel == null) {
            return;
        }

        if (duel.getState() != Duel.State.IN_MATCH && duel.getState() != Duel.State.START) {
            return;
        }

        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDuelCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getDuelByOpponent(player);

        if (duel == null) {
            return;
        }

        String message = event.getMessage();

        if (message.length() == 1) {
            return;
        }

        String command = message.split("/")[1];

        if (command.contains(" ")) {
            command = command.split(" ")[0];
        }

        for (String rawBlocked : blockedCommands) {
            if (!rawBlocked.startsWith("/")) {
                continue;
            }

            if (rawBlocked.length() == 1) {
                continue;
            }

            String blocked = rawBlocked.split("/")[1];

            if (blocked.contains(" ")) {
                blocked = blocked.split(" ")[0];
            }

            if (blocked.equals(command)) {
                player.sendMessage(notAllowInStateMessage);
                event.setCancelled(true);
                break;
            }
        }
    }

}
