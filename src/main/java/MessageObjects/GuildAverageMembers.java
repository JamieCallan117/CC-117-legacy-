package MessageObjects;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class GuildAverageMembers implements Comparable<GuildAverageMembers> {
    private final String name;
    private final double average;
    private final int currentlyOnline;

    /**
     * Creates a GuildAverageMembers object.
     * @param name Name of the guild.
     * @param average Current average members.
     */
    public GuildAverageMembers(String name, double average, int currentlyOnline) {
        this.name = name;
        this.average = average;
        this.currentlyOnline = currentlyOnline;
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

        return String.format("%-20s         %-11s                   %-11s\n", name, df.format(average), currentlyOnline);
    }

    /**
     * Allows the object to be compared to another based on the average.
     * @param other the object to be compared.
     * @return Whether it should be sorted above or below the compared object.
     */
    @Override
    public int compareTo(GuildAverageMembers other) {
        return Double.compare(average, other.getAverage());
    }
}
