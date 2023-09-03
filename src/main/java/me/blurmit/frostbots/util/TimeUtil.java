package me.blurmit.frostbots.util;

/**
 * This class provides utility methods for handling time-related operations.
 *
 * @author Blurmit
 * @since 1.0
 */
public class TimeUtil {

    /**
     * Retrieves the number of seconds elapsed from the given timestamp string.
     *
     * @param timestamp the timestamp string in the format "HH:MM:SS" or "MM:SS"
     * @return the number of seconds elapsed
     */
    public static int getSecondsElapsed(String timestamp) {
        String[] parts = timestamp.split(":");
        int seconds = 0;

        // Iterate over the parts in reverse order
        for (int i = parts.length - 1; i >= 0; i--) {
            int component = Integer.parseInt(parts[i]);
            seconds += component * Math.pow(60, parts.length - 1 - i);
        }

        return seconds;
    }

}
