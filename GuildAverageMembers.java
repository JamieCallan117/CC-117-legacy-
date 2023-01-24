import org.jetbrains.annotations.NotNull;

public class GuildAverageMembers implements Comparable<GuildAverageMembers>{
    private final String name;
    private double average;

    /**
     * Creates a GuildAverageMembers object.
     * @param name Name of the guild.
     * @param average Current average members.
     */
    public GuildAverageMembers(String name, double average) {
        this.name = name;
        this.average = average;
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
        return String.format("%-20s          %-11s\n", name, average);
    }

    /**
     * Allows the object to be compared to another based on the average.
     * @param other the object to be compared.
     * @return Whether it should be sorted above or below the compared object.
     */
    @Override
    public int compareTo(@NotNull GuildAverageMembers other) {
        return Double.compare(average, other.getAverage());
    }
}
