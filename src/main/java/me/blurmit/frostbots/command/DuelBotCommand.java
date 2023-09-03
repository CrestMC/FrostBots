package me.blurmit.frostbots.command;

import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.config.Configuration;
import me.blurmit.frostbots.duel.Duel;
import me.blurmit.frostbots.util.ChatUtil;
import me.blurmit.frostbots.util.ScoreboardParser;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DuelBotCommand implements CommandExecutor {

    private final FrostBots plugin;
    private final Configuration config;

    public DuelBotCommand(FrostBots plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();

        plugin.getCommand("duelbot").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frostbots.commands.duelbot")) {
            sender.sendMessage(ChatUtil.color("&cYou don't have permission to use that command."));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can use this command."));
            return false;
        }

        Player player = (Player) sender;
        Duel duel = plugin.getDuelManager().getDuelByOpponent(player);

        if (duel == null || duel.getState() != Duel.State.REQUESTED) {
            player.sendMessage(ChatUtil.color("&cYou have no pending bot duel requests."));
            return false;
        }

        if (ScoreboardParser.getQueueType(player.getScoreboard()) == null) {
            player.sendMessage(ChatUtil.color("&cYou are not in a duel queue."));
            return false;
        }

        player.getInventory().setHeldItemSlot(8);
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.getItemInHand(), null, BlockFace.SELF);
        plugin.getServer().getPluginManager().callEvent(event);

        duel.start();
        return true;
    }

}
