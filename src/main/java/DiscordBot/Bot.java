package DiscordBot;

import EventListener.EventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    public static void main(String[] args) {
        //Gets the token object to retrieve bot token.
        Token token = new Token();

        //Creates the bot.
        JDA bot = JDABuilder.create(token.getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .setActivity(Activity.watching(" over Corkus Island"))
                .addEventListeners(new EventListener())
                .build();

        //Sets up the slash commands for the bot.
        bot.upsertCommand("updateranks", "Updates the rank of every member of the server.").queue();
        bot.upsertCommand("verify", "Updates your rank based on the given username.").addOption(OptionType.STRING, "player_name", "Your Minecraft username to verify as.", true).queue();
        bot.upsertCommand("setguild", "Sets the Guild this server corresponds to.").addOption(OptionType.STRING, "guild_name", "The main Guild for this server.", true).queue();
        bot.upsertCommand("addally", "Adds an Ally Guild.").addOption(OptionType.STRING, "guild_name", "The name of the Ally guild you want to add.", true).queue();
        bot.upsertCommand("removeally", "Removes and Ally Guild.").addOption(OptionType.STRING, "guild_name", "The name of the guild you want to remove as an Ally.", true).queue();
        bot.upsertCommand("trackguild", "Track a guild to see its average online players.").addOption(OptionType.STRING, "guild_name", "The name of the guild you want to track.", true).queue();
        bot.upsertCommand("untrackguild", "No longer track a guild's online players.").addOption(OptionType.STRING, "guild_name", "The name of the guild you no longer want to track.", true).queue();
        bot.upsertCommand("trackedguilds", "View the average number of online players for each tracked guild.").queue();

        OptionData timezoneOptions = new OptionData(OptionType.STRING, "timezone", "The timezone to display active/dead hours in. Default UTC.")
                .addChoice("BST", "BST")
                .addChoice("EDT", "EDT")
                .addChoice("EST", "EST")
                .addChoice("GMT", "GMT")
                .addChoice("PDT", "PDT")
                .addChoice("PST", "PST");

        bot.upsertCommand("activehours", "View the active/dead hours for each tracked guilds.").addOption(OptionType.STRING, "guild_name", "The name of the guild you want to see active/dead hours for.", true).addOptions(timezoneOptions).queue();
        bot.upsertCommand("lastlogins", "View the last time each member of a guild logged in.").addOption(OptionType.STRING, "guild_name", "The name of the guild you want to see last logins for.", true).queue();
        bot.upsertCommand("updateprefixes", "Updates the list of all guild prefixes.").queue();
    }
}
