package MessageObjects;

import me.bed0.jWynn.api.common.GuildRank;

public class PlayerLastLogin implements Comparable<PlayerLastLogin> {
    String username;
    GuildRank rank;
    boolean isOnline;
    long inactiveDays;
    boolean displayColours;
    final int RECRUIT_INACTIVE_MAX = 25;
    final int CAPTAIN_INACTIVE_MAX = 40;
    final int STRATEGIST_INACTIVE_MAX = 80;
    final int CHIEF_INACTIVE_MAX = 365;
    final int OWNER_INACTIVE_MAX = 1825;

    public PlayerLastLogin(String username, GuildRank rank, long inactiveDays, boolean displayColours) {
        this.username = username;
        this.rank = rank;
        this.inactiveDays = inactiveDays;
        isOnline = false;
        this.displayColours = displayColours;
    }

    public PlayerLastLogin(String username, GuildRank rank, boolean isOnline, boolean displayColours) {
        this.username = username;
        this.rank = rank;
        this.isOnline = isOnline;
        inactiveDays = -1;
        this.displayColours = displayColours;
    }

    public String toString() {
        if (displayColours) {
            String colour = getColour();

            if (isOnline) {
                return String.format(colour + " %-16s (%-11s is currently online!\n", username, (rank + ")"));
            } else if (inactiveDays == 1) {
                return String.format(colour + " %-16s (%-11s has been inactive for " + inactiveDays + " day!\n", username, (rank + ")"));
            } else if (inactiveDays == 0) {
                return String.format(colour + " %-16s (%-11s was last online today!\n", username, (rank + ")"));
            } else {
                return String.format(colour + " %-16s (%-11s has been inactive for " + inactiveDays + " days!\n", username, (rank + ")"));
            }
        } else {
            if (isOnline) {
                return String.format("%-16s (%-11s is currently online!\n", username, (rank + ")"));
            } else if (inactiveDays == 1) {
                return String.format("%-16s (%-11s has been inactive for " + inactiveDays + " day!\n", username, (rank + ")"));
            } else if (inactiveDays == 0) {
                return String.format("%-16s (%-11s was last online today!\n", username, (rank + ")"));
            } else {
                return String.format("%-16s (%-11s has been inactive for " + inactiveDays + " days!\n", username, (rank + ")"));
            }
        }
    }

    public String getColour() {
        switch (rank) {
            case OWNER -> {
                if (inactiveDays > OWNER_INACTIVE_MAX) {
                    return "-";
                } else {
                    return "+";
                }
            }
            case CHIEF -> {
                if (inactiveDays > CHIEF_INACTIVE_MAX) {
                    return "-";
                } else {
                    return "+";
                }
            }
            case STRATEGIST -> {
                if (inactiveDays > STRATEGIST_INACTIVE_MAX) {
                    return "-";
                } else {
                    return "+";
                }
            }
            case CAPTAIN -> {
                if (inactiveDays > CAPTAIN_INACTIVE_MAX) {
                    return "-";
                } else {
                    return "+";
                }
            }

            default -> {
                if (inactiveDays > RECRUIT_INACTIVE_MAX) {
                    return "-";
                } else {
                    return "+";
                }
            }
        }
    }

    @Override
    public int compareTo(PlayerLastLogin other) {
        return Long.compare(other.inactiveDays, this.inactiveDays);
    }
}
