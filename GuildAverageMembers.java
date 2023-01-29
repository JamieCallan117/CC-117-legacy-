import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class GuildAverageMembers implements Comparable<main.java.GuildAverageMembers>{
    private final String name;
    private final double average;
    private int activeHour;
    private int deadHour;
    private final String timezone;

    /**
     * Creates a GuildAverageMembers object.
     * @param name Name of the guild.
     * @param average Current average members.
     */
    public GuildAverageMembers(String name, double average, int activeHour, int deadHour, String timezone) {
        this.name = name;
        this.average = average;
        this.activeHour = activeHour;
        this.deadHour = deadHour;
        this.timezone = timezone;
    }

    /**
     * Returns the average members.
     * @return The average members.
     */
    public double getAverage() {
        return average;
    }

    /**
     * Gets the guild name and average members as a formatted string.
     * @return The formatted string.
     */
    public String getAverageString() {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        String activeHourStr = activeHour + ":00";
        String deadHourStr = activeHour + ":00";

        //Use given timezone to determine active/dead hours in local time.
        switch (timezone) {
            case "BST" -> {
                if (activeHour == 23) {
                    activeHourStr = "0:00";
                } else {
                    activeHourStr = (activeHour + 1) + ":00";
                }

                if (deadHour == 23) {
                    deadHourStr = "0:00";
                } else {
                    deadHourStr = (deadHour + 1) + ":00";
                }
            }
            case "EST" -> {
                activeHour = activeHour - 5;

                if (activeHour < 0) {
                    activeHour = activeHour + 24;
                }

                deadHour = deadHour - 5;

                if (deadHour < 0) {
                    deadHour = deadHour + 24;
                }

                activeHourStr = activeHour + ":00";
                deadHourStr = deadHour + ":00";
            }
            case "PST" -> {
                activeHour = activeHour - 8;

                if (activeHour < 0) {
                    activeHour = activeHour + 24;
                }

                deadHour = deadHour - 8;

                if (deadHour < 0) {
                    deadHour = deadHour + 24;
                }

                activeHourStr = activeHour + ":00";
                deadHourStr = deadHour + ":00";
            }
            case "GMT", "UTC" -> activeHourStr = activeHour + ":00";
        }
        return String.format("%-20s         %-11s                   %-11s         %-11s\n", name, df.format(average), activeHourStr, deadHourStr);
    }

    /**
     * Allows the object to be compared to another based on the average.
     * @param other the object to be compared.
     * @return Whether it should be sorted above or below the compared object.
     */
    @Override
    public int compareTo(@NotNull main.java.GuildAverageMembers other) {
        return Double.compare(average, other.getAverage());
    }
}
