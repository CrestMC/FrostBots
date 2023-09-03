package me.blurmit.frostbots.util;

import me.blurmit.frostbots.FrostBots;
import me.blurmit.frostbots.arena.ArenaType;
import me.blurmit.frostbots.config.Configuration;
import me.blurmit.frostbots.kit.Kit;
import me.blurmit.frostbots.kit.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class provides utility methods for parsing scoreboard entries.
 *
 * @author Blurmit
 * @since 1.0
 */
public class ScoreboardParser {

    /**
     * Retrieves the queue time from the scoreboard.
     *
     * @param scoreboard the scoreboard to retrieve the queue time from.
     * @return the queue time as a string if found, or null if not found.
     */
    public static int getQueueTime(Scoreboard scoreboard) {
        Pattern pattern = Pattern.compile("^\\d{2}(?::\\d{2})+$");

        for (String entry : getParsedEntries(scoreboard)) {
            Matcher matcher = pattern.matcher(ChatColor.stripColor(entry));

            if (matcher.matches()) {
                return TimeUtil.getSecondsElapsed(matcher.group());
            }
        }

        return -1;
    }

    /**
     * Checks the type of the queue in the scoreboard.
     *
     * @param scoreboard the scoreboard to check.
     * @return the arena type of the queue, or ArenaType.UNKNOWN if not found.
     */
    public static ArenaType getQueueType(Scoreboard scoreboard) {
        String entry = getScoreboardLineWithText(scoreboard, "<queued_type>");

        if (entry == null) {
            return ArenaType.UNKNOWN;
        }

        entry = ChatColor.stripColor(entry);

        if (ArenaType.getFromName(entry) != ArenaType.UNKNOWN) {
            return ArenaType.getFromName(entry);
        }

        String[] parsedEntries = entry.split(" ");

        for (String parsedEntry : parsedEntries) {
            ArenaType testType = ArenaType.getFromName(parsedEntry);

            if (testType != ArenaType.UNKNOWN) {
                return testType;
            }
        }

        return ArenaType.UNKNOWN;
    }

    /**
     * Retrieves the kit of the queue in the scoreboard.
     *
     * @param scoreboard the scoreboard to check.
     * @return the kit of the queue, or null if not found.
     */
    public static Kit getQueueKit(Scoreboard scoreboard) {
        FrostBots plugin = JavaPlugin.getPlugin(FrostBots.class);
        KitManager kitManager = plugin.getKitManager();
        String entry = getScoreboardLineWithText(scoreboard, "<queued_kit>");

        if (entry == null) {
            return null;
        }

        entry = ChatColor.stripColor(entry);
        Kit kit = kitManager.getKitByName(entry);

        if (kit != null) {
            return kit;
        }

        String[] parsedEntries = entry.split(" ");

        for (String parsedEntry : parsedEntries) {
            Kit testKit = kitManager.getKitByName(parsedEntry);

            if (testKit != null) {
                return testKit;
            }
        }

        return null;
    }

    /**
     * Parses the specified text entry in the scoreboard to retrieve the associated value.
     *
     * @param scoreboard the scoreboard to parse.
     * @param text the text entry to parse.
     * @return the parsed entry as a string, or null if not found.
     */
    public static String getScoreboardLineWithText(Scoreboard scoreboard, String text) {
        int typeEntry = getScoreboardEntry(text);

        if (typeEntry == -1) {
            Bukkit.getLogger().warning(text + " was not found in any scoreboard configuration. Is it a disallowed type?");
            return null;
        }

        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        String entry = scoreboard.getEntries().stream()
                .map(objective::getScore)
                .filter(score -> score.getScore() == typeEntry)
                .map(Score::getEntry)
                .findFirst()
                .orElse(null);

        if (entry == null) {
            return null;
        }

        return formatEntry(scoreboard, entry);
    }

    /**
     * Retrieves the parsed entries from the scoreboard.
     *
     * @param scoreboard the scoreboard to retrieve the parsed entries from.
     * @return a list of parsed entries as strings.
     */
    public static List<String> getParsedEntries(Scoreboard scoreboard) {
        return scoreboard.getEntries().stream()
                .map(entry -> formatEntry(scoreboard, entry))
                .collect(Collectors.toList());
    }

    /**
     * Parses the specified entry in the scoreboard, applying color codes and team formatting.
     *
     * @param scoreboard the scoreboard to parse.
     * @param entry the entry to parse.
     * @return the parsed entry as a string.
     */
    public static String formatEntry(Scoreboard scoreboard, String entry) {
        entry = ChatColor.translateAlternateColorCodes('&', entry).trim();
        Team team = scoreboard.getTeam(entry);
        String prefix = team.getPrefix() != null ? team.getPrefix() : "";
        String suffix = team.getSuffix() != null ? team.getSuffix() : "";

        return (prefix + entry + suffix).trim();
    }

    /**
     * Retrieves the scoreboard entry associated with the specified text.
     *
     * @param text the text to search for in the scoreboard configuration.
     * @return the index of the scoreboard entry, or -1 if not found.
     */
    public static int getScoreboardEntry(String text) {
        FrostBots plugin = JavaPlugin.getPlugin(FrostBots.class);
        Configuration config = plugin.getConfiguration();
        FileConfiguration scoreboardConfig = config.get("scoreboard.yml");

        for (String allowedType : config.getAllowedTypes()) {
            ArenaType type = ArenaType.getFromName(allowedType);
            List<String> serializedEntries = scoreboardConfig.getStringList("SCOREBOARD.IN-" + type.getName().toUpperCase() + "-QUEUE");

            for (int i = 0; i < serializedEntries.size(); i++) {
                String entry = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', serializedEntries.get(i))).trim();

                if (entry.contains(text)) {
                    return serializedEntries.size() - (i - 1);
                }
            }

        }

        return -1;
    }

}