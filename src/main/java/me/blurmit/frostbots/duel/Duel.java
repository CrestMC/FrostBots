package me.blurmit.frostbots.duel;

import lombok.Getter;
import lombok.Setter;
import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.arena.Arena;
import me.blurmit.frostbots.config.Configuration;
import me.blurmit.frostbots.kit.Kit;
import me.blurmit.frostbots.util.ChatUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.PathStrategy;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.npc.ai.MCTargetStrategy;
import net.citizensnpcs.util.NMS;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.github.paperspigot.Title;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class Duel {

    @Getter
    private final Player opponent;
    @Getter
    private final Kit kit;
    @Getter
    private final Arena arena;
    @Getter
    private State state;

    private final FrostBots plugin;
    private final Configuration config;

    @Getter
    private NPC npc;
    private Navigator navigator;
    private BukkitTask duelTask;

    @Getter
    private Player winner;
    @Getter
    private Player loser;
    @Getter
    @Setter
    private boolean hitDelay;

    public Duel(Player opponent, Kit kit, Arena arena) {
        this.plugin = JavaPlugin.getPlugin(FrostBots.class);
        this.config = plugin.getConfiguration();

        this.opponent = opponent;
        this.kit = kit;
        this.arena = arena;
        this.state = State.REQUESTED;
    }

    public void start() {
        state = State.START;
        npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, config.getBotName());
        navigator = npc.getNavigator();

        npc.setProtected(true);
        npc.spawn(arena.getSpawnPositionB());

        opponent.getInventory().clear();
        opponent.teleport(arena.getSpawnPositionA());
        opponent.getInventory().setHeldItemSlot(0);
        opponent.getInventory().setContents(kit.getInventory().toArray(new ItemStack[0]));
        opponent.getInventory().setArmorContents(kit.getArmor().toArray(new ItemStack[0]));
        opponent.setAllowFlight(false);
        opponent.setFlying(false);

        setup();
        countdown();
    }

    private void setup() {
        (new BukkitRunnable() {
            @Override
            public void run() {
                if (state == State.IN_MATCH_END) {
                    cancel();
                    return;
                }

                if (!npc.isSpawned()) {
                    return;
                }

                Equipment equipment = npc.getTrait(Equipment.class);

                for (int i = 0; i < kit.getArmor().size(); i++) {
                    ItemStack item = kit.getArmor().get(i);
                    equipment.set(i + 1, item);
                }

                Player player = (Player) npc.getEntity();
                player.getInventory().setContents(kit.getInventory().toArray(new ItemStack[0]));
                player.getInventory().setArmorContents(kit.getArmor().toArray(new ItemStack[0]));

                navigator.getLocalParameters().speedModifier(1.33f);
                navigator.getDefaultParameters().range(300);
                navigator.getDefaultParameters().attackRange(5.9d);
                navigator.getDefaultParameters().stuckAction((npc, nav) -> false);

                npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, config.getBotSkin().equals("{PLAYER}") ? opponent.getName() : config.getBotSkin());
                npc.data().set(NPC.KEEP_CHUNK_LOADED_METADATA, true);
                npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, true);
                cancel();
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }

    private void countdown() {
        AtomicInteger seconds = new AtomicInteger(5);
        ConfigurationSection matchSection = config.get("messages.yml").getConfigurationSection("MESSAGES.MATCH");
        String countdown = ChatUtil.color(matchSection.getString("COUNTDOWN"));
        String countdownTitle = ChatUtil.color(matchSection.getString("COUNTDOWN-TITLE"));
        String countdownSubtitle = ChatUtil.color(matchSection.getString("COUNTDOWN-SUBTITLE"));
        String countdownStarted = ChatUtil.color(matchSection.getString("COUNTDOWN-STARTED"));
        String countdownAlert = ChatUtil.color(matchSection.getString("COUNTDOWN-ALERT"));
        String starting = ChatUtil.color(String.join("\n", matchSection.getStringList("STARTING")));

        opponent.sendMessage(starting
                .replace("<kit_name>", kit.getName())
                .replace("<arena_name>", arena.getName())
                .replace("<opponent_name>", ChatUtil.color(npc.getName()))
                .replace("<opponent_ping>", "0")
        );

        (new BukkitRunnable() {
            @Override
            public void run() {
                int newSeconds = seconds.getAndDecrement();

                if (newSeconds < 1) {
                    state = State.IN_MATCH;
                    duelTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, Duel.this::run, 0, 2L);

                    opponent.sendMessage(countdownStarted);
                    opponent.sendMessage(countdownAlert);
                    opponent.playSound(opponent.getLocation(), Sound.FIREWORK_BLAST, 50, 1);

                    EntityTarget target = navigator.getEntityTarget();
                    if (target == null || !target.getTarget().equals(opponent)) {
                        navigator.setTarget(opponent, false);
                    }

                    cancel();
                    return;
                }

                String secondsText = newSeconds + "";
                Title newCountdownStartingTitle = new Title(countdownTitle, countdownSubtitle.replace("<match_countdown>", secondsText), 0, 20, 0);

                opponent.sendTitle(newCountdownStartingTitle);
                opponent.sendMessage(countdown.replace("<match_countdown>", secondsText));
                opponent.playSound(opponent.getLocation(), Sound.NOTE_PLING, 50, 1);
            }
        }).runTaskTimer(plugin, 5L, 20L);
    }

    private void run() {
        try {
            if (!npc.isSpawned()) {
                return;
            }

            npc.getNavigator().setTarget(opponent.getLocation());
            Player player = (Player) npc.getEntity();

            if (npc.getStoredLocation().getY() <= arena.getDeadZone()) {
                eliminate(player);
            }

            if (opponent.getLocation().getY() <= arena.getDeadZone()) {
                eliminate(opponent);
            }

            if (!player.isSprinting()) {
                player.setSprinting(true);
            }

            if (canHit()) {
                NMS.attack(player, opponent);
            }

            if (kit.isInvincible()) {
                opponent.setHealth(opponent.getMaxHealth());
                player.setHealth(player.getMaxHealth());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error while ticking " + opponent.getName() + "'s duel", e);
        }
    }

    private boolean canHit() throws ReflectiveOperationException {
        BoundingBox handleBB = NMS.getBoundingBox(npc.getEntity());
        BoundingBox targetBB = NMS.getBoundingBox(opponent);
        return handleBB.maxY > targetBB.minY && handleBB.minY < targetBB.maxY && closeEnough(getDistance()) && hasLineOfSight();
    }

    private boolean hasLineOfSight() throws ReflectiveOperationException {
        PathStrategy strategy = npc.getNavigator().getPathStrategy();
        Method method = MCTargetStrategy.class.getDeclaredMethod("hasLineOfSight");
        method.setAccessible(true);
        return (boolean) method.invoke(strategy);
    }

    private boolean closeEnough(double distance) throws ReflectiveOperationException {
        PathStrategy strategy = npc.getNavigator().getPathStrategy();
        Method method = MCTargetStrategy.class.getDeclaredMethod("closeEnough", double.class);
        method.setAccessible(true);
        return (boolean) method.invoke(strategy, distance);
    }

    private double getDistance() {
        return opponent.getLocation().distanceSquared(npc.getEntity().getLocation());
    }

    public void eliminate(Player player) {
        FileConfiguration messages = config.get("messages.yml");
        String killedMessage = ChatUtil.color(messages.getString("MESSAGES.PLAYER.KILLED"));
        Player attacker;

        if (player.equals(opponent)) {
            attacker = (Player) npc.getEntity();
        } else {
            attacker = opponent;
        }

        opponent.sendMessage(killedMessage.replace("<victim>", player.getName()).replace("<killer>", attacker.getName()));
        stop(attacker, player);
    }

    private void stop(Player winner, Player loser) {
        state = State.IN_MATCH_END;
        duelTask.cancel();

        setWinner(winner);
        setLoser(loser);
        spectate(loser);

        ConfigurationSection matchSection = config.get("messages.yml").getConfigurationSection("MESSAGES.MATCH.POST-MATCH");
        List<String> postMatchMessage = matchSection.getStringList("NORMAL");
        ComponentBuilder builder = new ComponentBuilder("");

        for (String rawMessage : postMatchMessage) {
            String message = ChatUtil.color(rawMessage);

            if (message.contains("<winner>")) {
                message = message.replace("<winner>", ChatUtil.color(winner.getName()));
            }

            if (message.contains("<winnerHealth>")) {
                message = message.replace("<winnerHealth>", ChatUtil.color((int) winner.getHealth() + ""));
            }

            if (message.contains("<loser>")) {
                message = message.replace("<loser>", ChatUtil.color(loser.getName()));
            }

            opponent.sendMessage(message);
        }

        opponent.sendMessage(builder.create());
        opponent.setFireTicks(0);
        opponent.setHealth(opponent.getMaxHealth());
        opponent.setFoodLevel(20);

        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (npc.isSpawned()) {
                    npc.despawn();
                }

                CitizensAPI.getNPCRegistry().deregister(npc);
                npc.destroy();
                clear(opponent);

                opponent.teleport(config.getSpawnLocation());
                opponent.getInventory().setContents(plugin.getUserManager().getLobbyHotbar());
                opponent.setAllowFlight(false);
                opponent.setFlying(false);
            }, 100L);
        }

        plugin.getDuelManager().getDuels().remove(this);
    }

    public void setWinner(Player winner) {
        this.winner = winner;

        ConfigurationSection messages = config.get("messages.yml").getConfigurationSection("MESSAGES.MATCH");
        String titleText = ChatUtil.color(messages.getString("WINNER-TITLE"));
        String subTitle = ChatUtil.color(messages.getString("THIS-SUBTITLE").replace("<player>", winner.getName()));

        Title title = new Title(titleText, subTitle, 0, 100, 0);
        winner.sendTitle(title);
    }

    public void setLoser(Player loser) {
        this.loser = loser;

        ConfigurationSection messages = config.get("messages.yml").getConfigurationSection("MESSAGES.MATCH");
        String titleText = ChatUtil.color(messages.getString("LOSER-TITLE"));
        String subTitle = ChatUtil.color(messages.getString("THIS-SUBTITLE").replace("<player>", winner.getName()));

        Title title = new Title(titleText, subTitle, 0, 100, 0);
        loser.sendTitle(title);
    }

    public void spectate(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        clear(player);

        if (player.equals(npc.getEntity())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> npc.despawn());
        }
    }

    public void clear(Player player) {
        player.getInventory().clear();

        if (player.equals(opponent)) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
            return;
        }

        Equipment equipment = npc.getTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HELMET, null);
        equipment.set(Equipment.EquipmentSlot.CHESTPLATE, null);
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, null);
        equipment.set(Equipment.EquipmentSlot.BOOTS, null);
    }

    public enum State {
        REQUESTED,
        START,
        IN_MATCH,
        IN_MATCH_END;
    }

}
