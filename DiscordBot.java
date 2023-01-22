import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot {
    public static void main(String[] args) {
        //Gets the token object to retrieve bot token.
        Token token = new Token();

        //Creates the bot.
        JDA bot = JDABuilder.create(token.getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.watching(" over Corkus Island"))
                .addEventListeners(new EventListener())
                .build();

        //Sets up the slash commands for the bot.
        bot.upsertCommand("updateranks", "Updates the rank of every member of the server.").queue();
        bot.upsertCommand("verify", "Updates your rank based on the given username.").addOption(OptionType.STRING, "player_name", "Your Minecraft username to verify as.").queue();
        bot.upsertCommand("setguild", "Sets the Guild this server corresponds to.").addOption(OptionType.STRING, "guild_name", "The main Guild for this server.").queue();
        bot.upsertCommand("addally", "Adds an Ally Guild.").addOption(OptionType.STRING, "guild_name", "The name of the Ally guild you want to add.").queue();
        bot.upsertCommand("removeally", "Removes and Ally Guild.").addOption(OptionType.STRING, "guild_name", "The name of the guild you want to remove as an Ally.").queue();
    }
}
