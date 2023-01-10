import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot {
    public static void main(String[] args) {
        Token token = new Token();

        JDA bot = JDABuilder.create(token.getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.watching(" over Corkus Island"))
                .addEventListeners(new EventListener())
                .build();

        bot.upsertCommand("updateranks", "Updates the rank of every member of the server.").queue();
        bot.upsertCommand("verify", "Updates the rank of every member of the server.").addOption(OptionType.STRING, "player_name", "Your Minecraft username to verify as.").queue();
    }
}
