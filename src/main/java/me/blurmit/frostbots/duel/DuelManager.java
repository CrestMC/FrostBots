package me.blurmit.frostbots.duel;

import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.arena.Arena;
import me.blurmit.frostbots.arena.ArenaType;
import me.blurmit.frostbots.config.Configuration;
import me.blurmit.frostbots.kit.Kit;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class DuelManager {

    private final FrostBots plugin;
    private final DuelListener listener;
    private final Set<Duel> duels;

    public DuelManager(FrostBots plugin) {
        this.plugin = plugin;
        this.listener = new DuelListener(plugin, this);
        this.duels = new HashSet<>();
    }

    public Duel getDuelByOpponent(Player opponent) {
        return duels.stream()
                .filter(duel -> duel.getOpponent().equals(opponent))
                .findFirst()
                .orElse(null);
    }

    public Duel getDuelByNPC(NPC npc) {
        return duels.stream()
                .filter(duel -> duel.getNpc().equals(npc))
                .findFirst()
                .orElse(null);
    }

    public void request(Player opponent, Kit kit, ArenaType arenaType) {
        Configuration config = plugin.getConfiguration();

        if (!config.getAllowedTypes().contains(arenaType.getName())) {
            return;
        }

        BaseComponent[] hoverComponent = new ComponentBuilder("Click to accept").color(ChatColor.GRAY).create();
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duelbot");

        String rawMessage = String.join("\n", config.getBotDuelReceived());
        BaseComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', rawMessage));

        message.setColor(ChatColor.GRAY);
        message.setClickEvent(clickEvent);
        message.setHoverEvent(hoverEvent);

        Arena arena = plugin.getArenaManager().getRandomArena(kit);
        Duel duel = new Duel(opponent, kit, arena);
        duels.add(duel);

        opponent.sendMessage(message);

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (duel.getState() == Duel.State.REQUESTED) {
                duels.remove(duel);
            }
        }, 600L);
    }

    public void knockback(Player player, Kit kit) {
        try {
            String knockbackProfile = kit.getKnockbackProfile().getName();

            Class<?> knockbackServiceClass = Class.forName("dev.imanity.knockback.api.KnockbackService");
            Object knockbackService = knockbackServiceClass.getMethod("get").invoke(null);

            Class<?> knockbackClass = Class.forName("dev.imanity.knockback.api.Knockback");
            Object knockback;

            Method getKnockbackByNameMethod = knockbackServiceClass.getMethod("getKnockbackByName", String.class);
            Method getGlobalKnockbackMethod = knockbackServiceClass.getMethod("getGlobalKnockback");
            Method setKnockbackMethod = knockbackServiceClass.getMethod("setKnockback", Player.class, knockbackClass);

            Object testKnockback = getKnockbackByNameMethod.invoke(knockbackService, knockbackProfile);
            knockback = (testKnockback != null ? testKnockback : getGlobalKnockbackMethod.invoke(knockbackService));

            setKnockbackMethod.invoke(knockbackService, player, knockback);
        } catch (Exception e) {
            plugin.getLogger().warning("Unable to apply knockback to " + player.getName());
        }
    }

    public Set<Duel> getDuels() {
        return duels;
    }

}
