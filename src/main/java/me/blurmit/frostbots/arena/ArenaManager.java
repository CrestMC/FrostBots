package me.blurmit.frostbots.arena;

import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.kit.Kit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaManager {

    private final FrostBots plugin;
    private final FileConfiguration config;

    private final Set<Arena> arenas;

    public ArenaManager(FrostBots plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration().get("arenas.yml");
        this.arenas = new HashSet<>();

        load();
    }

    private void load() {
        Map<String, Object> arenasRaw = config.getConfigurationSection("arenas").getValues(false);

        for (Map.Entry<String, Object> rawArenaData : arenasRaw.entrySet()) {
            String name = rawArenaData.getKey();
            MemorySection data = (MemorySection) rawArenaData.getValue();

            Arena arena = new Arena();
            arena.setName(name);
            arena.setEnabled(data.getBoolean("enabled"));
            arena.setSpawnPositionA(plugin.getConfiguration().getLocation(config, "arenas." + name + ".a"));
            arena.setSpawnPositionB(plugin.getConfiguration().getLocation(config, "arenas." + name + ".b"));
            arena.setMin(plugin.getConfiguration().getLocation(config, "arenas." + name + ".min"));
            arena.setMax(plugin.getConfiguration().getLocation(config, "arenas." + name + ".max"));
            arena.setMaxBuildHeight(data.getInt("build-max"));
            arena.setDeadZone(data.getInt("dead-zone"));

            arenas.add(arena);
        }

        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }

    public Arena getArenaByName(String name) {
        return arenas.stream()
                .filter(arena -> arena.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Arena getRandomArena(Kit kit) {
        List<Arena> kitArenas = getArenasForKit(kit);
        int kitsAmount = kitArenas.size();

        if (kitsAmount < 1) {
            return null;
        }

        int choice = ThreadLocalRandom.current().nextInt(kitsAmount);

        return kitArenas.get(choice);
    }

    public List<Arena> getArenasForKit(Kit kit) {
        return arenas.stream()
                .filter(arena -> kit.getArenaWhitelist().contains(arena))
                .collect(Collectors.toList());
    }

    public Set<Arena> getArenas() {
        return arenas;
    }

}
