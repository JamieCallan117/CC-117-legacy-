package MessageObjects;

public class PossibleGuilds {
    private String prefix;
    private String name;

    public PossibleGuilds(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    public String getFormattedString() {
        return "[" + prefix + "] " + name;
    }
}
