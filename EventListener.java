import me.bed0.jWynn.WynncraftAPI;
import me.bed0.jWynn.api.v1.guild.WynncraftGuild;
import me.bed0.jWynn.api.v1.guild.WynncraftGuildMember;
import me.bed0.jWynn.api.v1.network.WynncraftOnlinePlayers;
import me.bed0.jWynn.api.v1.network.WynncraftServerOnlinePlayers;
import me.bed0.jWynn.api.v2.player.WynncraftPlayer;
import me.bed0.jWynn.exceptions.APIRateLimitExceededException;
import me.bed0.jWynn.exceptions.APIResponseException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventListener extends ListenerAdapter {
    WynncraftAPI wynnAPI = new WynncraftAPI();
    Role ownerRole;
    Role chiefRole;
    Role strategistRole;
    Role captainRole;
    Role recruiterRole;
    Role recruitRole;
    Role championRole;
    Role heroRole;
    Role vipPlusRole;
    Role vipRole ;
    Role vetRole;
    Role unverifiedRole;
    Role memberOfRole;
    Role allyRole;
    Role allyOwnerRole;
    Role[] rankRoles;
    Role[] guildRoles;
    Role[] allyRoles;
    File guildFile;
    File allyFile;
    File trackedFile;
    File tempFile;
    WynncraftGuild mainGuild;
    String guildName = "";
    List<WynncraftGuild> allyGuilds = new ArrayList<>();
    List<Role> rolesToAdd = new ArrayList<>();
    List<Role> rolesToRemove = new ArrayList<>();
    WynncraftPlayer player;
    String trackedHeader = "";
    List<String> trackedPages = new ArrayList<>();
    int currentTrackedPage;

    /**
     * When bot starts in a server, set up file paths and start the thread to run updateOnlineAverage every hour and
     * updateRanks every 12 hours.
     * @param event The event when a bot is ready in a guild.
     */
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        super.onGuildReady(event);

        guildFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "guild.txt");
        allyFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "allies.txt");
        trackedFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "tracked.txt");
        tempFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "temp.txt");

        ownerRole = event.getGuild().getRoleById("919792790567788564");
        chiefRole = event.getGuild().getRoleById("919794161220222977");
        strategistRole = event.getGuild().getRoleById("919794296545234964");
        captainRole = event.getGuild().getRoleById("919794331286646835");
        recruiterRole = event.getGuild().getRoleById("919794383631577099");
        recruitRole = event.getGuild().getRoleById("919794568021565510");
        championRole = event.getGuild().getRoleById("1011887156182126593");
        heroRole = event.getGuild().getRoleById("1011886987319447582");
        vipPlusRole = event.getGuild().getRoleById("1011886859091202048");
        vipRole = event.getGuild().getRoleById("1011886757366743100");
        vetRole = event.getGuild().getRoleById("1011886630291918928");
        unverifiedRole = event.getGuild().getRoleById("1061791662541647913");
        memberOfRole = event.getGuild().getRoleById("919790925528596551");
        allyRole = event.getGuild().getRoleById("1055652963143663651");
        allyOwnerRole = event.getGuild().getRoleById("1055652974245974026");

        rankRoles = new Role[]{vipRole, vipPlusRole, heroRole, championRole};
        guildRoles = new Role[]{recruitRole, recruiterRole, captainRole, strategistRole, chiefRole, ownerRole};
        allyRoles = new Role[]{allyRole, allyOwnerRole};

        try {
            Files.createDirectories(Path.of("/home/opc/CC-117/" + event.getGuild().getId()));
        } catch (IOException ex) {
            System.out.println("Unable to create directory for guild: " + event.getGuild().getId());
        }

        Thread thread = new Thread(() -> {
            System.out.println("Running at " + Instant.now().atZone(ZoneOffset.UTC).getHour() + ":" + Instant.now().atZone(ZoneOffset.UTC).getMinute());
            updateOnlineAverage(event.getGuild());
            String response = updateRanks(event.getGuild());

            if (!response.equals("Updated roles for 0 members!")) {
                TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");

                if (channel != null) {
                    channel.sendMessage(response).queue();
                } else {
                    System.out.println("Unable to find channel with ID: 1061698530651144212");
                }
            }
        });

        ScheduledExecutorService updateExecutor = Executors.newScheduledThreadPool(1);
        updateExecutor.scheduleAtFixedRate(thread, 0, 1, TimeUnit.HOURS);
    }

    /**
     * Adds the unverified role to a new member when they join the server.
     * Also mentions them in the verify channel to get them to go there and verify themselves.
     * @param event The event of the member joining to get information from.
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Role unverifiedRole = event.getGuild().getRoleById("1061791662541647913");

        if (unverifiedRole != null) {
            //Give unverified role to the member who joined.
            event.getGuild().addRoleToMember(event.getMember(), unverifiedRole).queue();
        } else {
            TextChannel botChannel = event.getGuild().getTextChannelById("1061698530651144212");

            if (botChannel != null) {
                botChannel.sendMessage("Could not find unverified role with ID: 1061791662541647913").queue();
            } else {
                System.out.println("Could not locate bot channel with ID: 1061698530651144212");
            }
        }

        TextChannel verifyChannel = event.getGuild().getTextChannelById("1061730979913404506");

        if (verifyChannel != null) {
            //Send message to the verify channel mentioning the new member, then delete the message after 10 seconds.
            verifyChannel.sendMessage(event.getMember().getAsMention()).queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        } else {
            System.out.println("Could not locate verify channel with ID: 1061730979913404506");
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (event.getGuild() == null) {
            event.reply("Error running command, no guild found").queue();
        }

        if (event.getMember() == null) {
            event.reply("Error running command, no member found").queue();
        }

        //Determine which command was used.
        switch (event.getName()) {
            case "updateranks" -> {
                if (canUseCommand(event.getMember())) {
                    event.deferReply().queue();
                    event.getHook().sendMessage(updateRanks(event.getGuild())).queue();
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }
            case "verify" -> {
                event.deferReply().setEphemeral(true).queue();
                OptionMapping playerNameOption = event.getOption("player_name");

                //Get the name of the player to be verified as.
                if (playerNameOption != null) {
                    String playerName = playerNameOption.getAsString();
                    String response = verify(playerName, event.getGuild(), event.getMember());
                    event.getHook().sendMessage(response).setEphemeral(true).queue();
                    TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");

                    if (channel != null) {
                        channel.sendMessage(response).queue();
                    } else {
                        System.out.println("Unable to find channel with ID: 1061698530651144212");
                    }
                } else {
                    event.getHook().sendMessage("Please enter a player name.").setEphemeral(true).queue();
                }
            }
            case "setguild" -> {
                if (canUseCommand(event.getMember())) {
                    event.deferReply().queue();
                    OptionMapping guildNameOption = event.getOption("guild_name");
                    if (guildNameOption != null) {
                        event.getHook().sendMessage(setGuild(guildNameOption.getAsString(), event.getGuild())).queue();
                    } else {
                        event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }
            case "addally" -> {
                if (canUseCommand(event.getMember())) {
                    event.deferReply().queue();
                    OptionMapping addAllyNameOption = event.getOption("guild_name");
                    if (addAllyNameOption != null) {
                        event.getHook().sendMessage(addAlly(addAllyNameOption.getAsString(), event.getGuild())).queue();
                    } else {
                        event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }
            case "removeally" -> {
                if (canUseCommand(event.getMember())) {
                    event.deferReply().queue();
                    OptionMapping removeAllyNameOption = event.getOption("guild_name");
                    if (removeAllyNameOption != null) {
                        event.getHook().sendMessage(removeAlly(removeAllyNameOption.getAsString(), event.getGuild())).queue();
                    } else {
                        event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }
            case "trackguild" -> {
                if (canUseCommand(event.getMember())) {
                    event.deferReply().queue();
                    OptionMapping trackGuildNameOption = event.getOption("guild_name");
                    if (trackGuildNameOption != null) {
                        event.getHook().sendMessage(trackGuild(trackGuildNameOption.getAsString(), event.getGuild())).queue();
                    } else {
                        event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }
            case "untrackguild" -> {
                if (canUseCommand(event.getMember())) {
                    event.deferReply().queue();
                    OptionMapping untrackGuildNameOption = event.getOption("guild_name");
                    if (untrackGuildNameOption != null) {
                        event.getHook().sendMessage(untrackGuild(untrackGuildNameOption.getAsString(), event.getGuild())).queue();
                    } else {
                        event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }

            case "trackedguilds" -> {
                event.deferReply().queue();

                OptionMapping timezoneOption = event.getOption("timezone");

                currentTrackedPage = 0;
                trackedPages.clear();

                if (timezoneOption == null) {
                    event.getHook().sendMessage(trackedGuilds("UTC"))
                            .addActionRow(
                                    Button.primary("previousPage", Emoji.fromFormatted("⬅️")),
                                    Button.primary("nextPage", Emoji.fromFormatted("➡️"))
                            )
                            .queue();
                } else {
                    event.getHook().sendMessage(trackedGuilds(timezoneOption.getAsString()))
                            .addActionRow(
                                    Button.primary("previousPage", Emoji.fromFormatted("⬅️")),
                                    Button.primary("nextPage", Emoji.fromFormatted("➡️"))
                            )
                            .queue();
                }
            }
            default -> event.reply("Unknown command.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("nextPage")) {
            if (trackedPages.size() == 1) {
                event.editMessage(trackedHeader + trackedPages.get(0)).queue();
            } else if (currentTrackedPage < trackedPages.size() - 1) {
                event.editMessage(trackedHeader + trackedPages.get(currentTrackedPage + 1)).queue();
                currentTrackedPage++;
            } else if (currentTrackedPage == trackedPages.size() - 1) {
                event.editMessage(trackedHeader + trackedPages.get(0)).queue();
                currentTrackedPage = 0;
            }
        } else if (event.getComponentId().equals("previousPage")) {
            if (trackedPages.size() == 1) {
                event.editMessage(trackedHeader + trackedPages.get(0)).queue();
            } else if (currentTrackedPage > 0) {
                event.editMessage(trackedHeader + trackedPages.get(currentTrackedPage - 1)).queue();
                currentTrackedPage--;
            } else if (currentTrackedPage == 0) {
                event.editMessage(trackedHeader + trackedPages.get(trackedPages.size() - 1)).queue();
                currentTrackedPage = trackedPages.size() - 1;
            }
        }
    }

    /**
     * Updates the ranks of everyone in the Discord server assuming their username/nickname
     * matches someone in either the main Wynncraft guild or an ally guild.
     * @param guild The current Discord server.
     * @return The message to be sent as a response to the command.
     */
    private String updateRanks(Guild guild) {
        List<Member> discordMembers = new ArrayList<>();
        List<String> updatedMembers = new ArrayList<>();

        //Remove all bots from the Discord members to verify.
        for (Member member : guild.getMembers()) {
            if (!member.getUser().isBot()) {
                discordMembers.add(member);
            }
        }

        //Update the guild variables, making sure the main one has at least been set.
        if (setGuilds()) {
            return "Make sure to set your guild with /setguild <guildname>.";
        }

        int rolesUpdated = 0;
        boolean hasUpdated;

        //Loop through every member of the main Wynncraft guild.
        for (int i = 0; i < mainGuild.getMembers().length; i++) {
            if (discordMembers.size() > 0) {
                for (Member member : discordMembers) {
                    //Retrieve the nickname of the member.
                    String nick = null;

                    if (member.getNickname() != null) {
                        nick = member.getNickname();
                    }

                    if (mainGuild.getMembers()[i].getName().equalsIgnoreCase(member.getUser().getName()) || mainGuild.getMembers()[i].getName().toLowerCase().equalsIgnoreCase(nick)) {
                        //Found a matching Discord member to a guild member.
                        rolesToAdd.clear();
                        rolesToRemove.clear();

                        rolesToAdd.add(memberOfRole);
                        rolesToRemove.add(unverifiedRole);
                        rolesToRemove.add(allyRole);
                        rolesToRemove.add(allyOwnerRole);

                        //Attempt to change the nickname of the user to match their Wynncraft name.
                        try {
                            member.modifyNickname(mainGuild.getMembers()[i].getName()).queue();
                            System.out.println("Verified " + member.getUser().getName() + " as Guild member " + mainGuild.getMembers()[i].getName());
                        } catch (HierarchyException e) {
                            System.out.println("Verified " + member.getUser().getName() + " as Guild member " + mainGuild.getMembers()[i].getName() + "(Could not change nickname)");
                        }

                        hasUpdated = false;
                        String uuid = mainGuild.getMembers()[i].getUuid();

                        //Set guild ranks.
                        switch (mainGuild.getMembers()[i].getRank()) {
                            case OWNER -> {
                                if (needsRole(member, ownerRole)) {
                                    System.out.println(member.getUser().getName() + " is the Owner of the Guild.");
                                    SetGuildRankRoles(ownerRole);
                                    hasUpdated = true;
                                }
                            }
                            case CHIEF -> {
                                if (needsRole(member, chiefRole)) {
                                    System.out.println(member.getUser().getName() + " is a Chief of the Guild.");
                                    SetGuildRankRoles(chiefRole);
                                    hasUpdated = true;
                                }
                            }
                            case STRATEGIST -> {
                                if (needsRole(member, strategistRole)) {
                                    System.out.println(member.getUser().getName() + " is a Strategist of the Guild.");
                                    SetGuildRankRoles(strategistRole);
                                    hasUpdated = true;
                                }
                            }
                            case CAPTAIN -> {
                                if (needsRole(member, captainRole)) {
                                    System.out.println(member.getUser().getName() + " is a Captain of the Guild.");
                                    SetGuildRankRoles(captainRole);
                                    hasUpdated = true;
                                }
                            }
                            case RECRUITER -> {
                                if (needsRole(member, recruiterRole)) {
                                    System.out.println(member.getUser().getName() + " is a Recruiter of the Guild.");
                                    SetGuildRankRoles(recruiterRole);
                                    hasUpdated = true;
                                }
                            }
                            case RECRUIT -> {
                                if (needsRole(member, recruitRole)) {
                                    System.out.println(member.getUser().getName() + " is a Recruit of the Guild.");
                                    SetGuildRankRoles(recruitRole);
                                    hasUpdated = true;
                                }
                            }
                        }

                        //Set player roles and determine if changes were made.
                        rolesUpdated = setPlayerRoles(rolesUpdated, hasUpdated, member, uuid);

                        //Update their Discord roles.
                        guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();

                        //Remove from the list as they no longer need to be verified.
                        discordMembers.remove(member);

                        if (hasUpdated) {
                            updatedMembers.add(member.getUser().getName());
                        }

                        break;
                    }
                }
            }
        }

        //Loop through all ally guilds to check for matching members.
        for (WynncraftGuild allyGuild : allyGuilds) {
            for (int j = 0; j < allyGuild.getMembers().length; j++) {
                if (discordMembers.size() > 0) {
                    for (Member member : discordMembers) {
                        //Get their Discord nickname if they have one.
                        String nick = null;

                        if (member.getNickname() != null) {
                            nick = member.getNickname();
                        }

                        if (allyGuild.getMembers()[j].getName().equalsIgnoreCase(member.getUser().getName()) || allyGuild.getMembers()[j].getName().toLowerCase().equalsIgnoreCase(nick)) {
                            //Found matching member.
                            rolesToAdd.clear();
                            rolesToRemove.clear();

                            rolesToRemove.add(memberOfRole);
                            rolesToRemove.add(unverifiedRole);

                            //Attempt to change nickname.
                            try {
                                member.modifyNickname(allyGuild.getMembers()[j].getName()).queue();
                                System.out.println("Verified " + member.getUser().getName() + " as Ally Guild member " + allyGuild.getMembers()[j].getName());
                            } catch (HierarchyException e) {
                                System.out.println("Verified " + member.getUser().getName() + " as Ally Guild member " + allyGuild.getMembers()[j].getName() + "(Could not change nickname)");
                            }

                            hasUpdated = false;
                            String uuid = allyGuild.getMembers()[j].getUuid();

                            //Give ally roles.
                            switch (allyGuild.getMembers()[j].getRank()) {
                                case OWNER -> {
                                    if (needsRole(member, allyOwnerRole)) {
                                        System.out.println(member.getUser().getName() + " is the Owner of an Ally Guild.");
                                        SetGuildRankRoles(allyOwnerRole);
                                        SetAllyRankRoles(allyOwnerRole);
                                        hasUpdated = true;
                                    }
                                }
                                case CHIEF, STRATEGIST, CAPTAIN, RECRUITER, RECRUIT -> {
                                    if (needsRole(member, allyRole)) {
                                        System.out.println(member.getUser().getName() + " is a member of an Ally Guild.");
                                        SetGuildRankRoles(allyRole);
                                        SetAllyRankRoles(allyRole);
                                        hasUpdated = true;
                                    }
                                }
                            }

                            //Set player roles and determine if changes were made.
                            rolesUpdated = setPlayerRoles(rolesUpdated, hasUpdated, member, uuid);

                            //Remove from the list as no longer need to be verified.
                            discordMembers.remove(member);

                            //Update Discord roles.
                            guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();

                            if (hasUpdated) {
                                updatedMembers.add(member.getUser().getName());
                            }

                            break;
                        }
                    }
                }
            }
        }

        //For all members that weren't verified. Add unverified role if need be and remove all other rank related roles.
        for (Member member : discordMembers) {
            if(needsRole(member, unverifiedRole)) {
                rolesToAdd.clear();
                rolesToRemove.clear();

                SetUnverifiedRoles();
                rolesUpdated += 1;

                guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();

                updatedMembers.add(member.getUser().getName());

                System.out.println(member.getUser().getName() + " unverified.");
            }
        }

        //Determine bot response message.
        if (rolesUpdated == 0) {
            return "Updated roles for " + rolesUpdated + " members!";
        } else if (rolesUpdated == 1) {
            return "Updated roles for " + rolesUpdated + " members!\n(" + updatedMembers.get(0) + ")";
        } else {
            StringBuilder updatedMembersString = new StringBuilder();

            for (int i = 0; i < (updatedMembers.size() - 1); i++) {
                updatedMembersString.append(updatedMembers.get(i)).append(", ");
            }

            updatedMembersString.append(updatedMembers.get(updatedMembers.size() - 1));

            return "Updated roles for " + rolesUpdated + " members!\n(" + updatedMembersString + ")";
        }
    }

    /**
     * Verify a specific member of the Discord server with the roles of the given
     * Wynncraft player username.
     * @param playerName The name of the Wynncraft player they want the roles of.
     * @param guild The current Discord server.
     * @param member The member who is being verified.
     * @return A string message of the result of the verification.
     */
    private String verify(String playerName, Guild guild, Member member) {
        //Set main and ally guilds.
        if (setGuilds()) {
            return "Make sure to set your guild with /setguild <guildname>.";
        }

        boolean verified = false;
        boolean isAlly = false;

        //Loops through all guild members.
        for (int i = 0; i < mainGuild.getMembers().length; i++) {
            if (mainGuild.getMembers()[i].getName().equalsIgnoreCase(playerName)) {
                //Found a member of the guild with a name matching the one to be verified as.
                rolesToAdd.clear();
                rolesToRemove.clear();

                verified = true;

                rolesToAdd.add(memberOfRole);
                rolesToRemove.add(unverifiedRole);
                rolesToRemove.add(allyRole);
                rolesToRemove.add(allyOwnerRole);

                String uuid = mainGuild.getMembers()[i].getUuid();

                //Sets guild rank roles.
                switch (mainGuild.getMembers()[i].getRank()) {
                    case OWNER -> {
                        if (needsRole(member, ownerRole)) {
                            System.out.println(member.getUser().getName() + " is the Owner of the Guild.");
                            SetGuildRankRoles(ownerRole);
                        }
                    }
                    case CHIEF -> {
                        if (needsRole(member, chiefRole)) {
                            System.out.println(member.getUser().getName() + " is a Chief of the Guild.");
                            SetGuildRankRoles(chiefRole);
                        }
                    }
                    case STRATEGIST -> {
                        if (needsRole(member, strategistRole)) {
                            System.out.println(member.getUser().getName() + " is a Strategist of the Guild.");
                            SetGuildRankRoles(strategistRole);
                        }
                    }
                    case CAPTAIN -> {
                        if (needsRole(member, captainRole)) {
                            System.out.println(member.getUser().getName() + " is a Captain of the Guild.");
                            SetGuildRankRoles(captainRole);
                        }
                    }
                    case RECRUITER -> {
                        if (needsRole(member, recruiterRole)) {
                            System.out.println(member.getUser().getName() + " is a Recruiter of the Guild.");
                            SetGuildRankRoles(recruiterRole);
                        }
                    }
                    case RECRUIT -> {
                        if (needsRole(member, recruitRole)) {
                            System.out.println(member.getUser().getName() + " is a Recruit of the Guild.");
                            SetGuildRankRoles(recruitRole);
                        }
                    }
                }

                //Sets player roles.
                setPlayerRoles(member, uuid);

                guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();

                break;
            }
        }

        //If they have not been verified yet, try ally guilds.
        if (!verified) {
            //Loop through all ally guilds.
            for (WynncraftGuild allyGuild : allyGuilds) {
                //Loop through all members of current ally guild.
                for (int j = 0; j < allyGuild.getMembers().length; j++) {
                    if (allyGuild.getMembers()[j].getName().equalsIgnoreCase(playerName)) {
                        //Found matching member.
                        rolesToAdd.clear();
                        rolesToRemove.clear();

                        verified = true;
                        isAlly = true;

                        guild.removeRoleFromMember(member, memberOfRole).queue();
                        guild.removeRoleFromMember(member, unverifiedRole).queue();

                        String uuid = allyGuild.getMembers()[j].getUuid();

                        //Apply ally roles.
                        switch (allyGuild.getMembers()[j].getRank()) {
                            case OWNER -> {
                                if (needsRole(member, allyOwnerRole)) {
                                    System.out.println(member.getUser().getName() + " is the Owner of an Ally Guild.");
                                    SetGuildRankRoles(allyOwnerRole);
                                    SetAllyRankRoles(allyOwnerRole);
                                }
                            }
                            case CHIEF, STRATEGIST, CAPTAIN, RECRUITER, RECRUIT -> {
                                if (needsRole(member, allyRole)) {
                                    System.out.println(member.getUser().getName() + " is a member of an Ally Guild.");
                                    SetGuildRankRoles(allyRole);
                                    SetAllyRankRoles(allyRole);
                                }
                            }
                        }

                        //Set player roles.
                        setPlayerRoles(member, uuid);

                        guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();

                        break;
                    }
                }
            }
        }

        //If the given player name was not part of the main or an ally guild, apply unverified role and remove all rank roles.
        if (!verified) {
            if(needsRole(member, unverifiedRole)) {
                rolesToAdd.clear();
                rolesToRemove.clear();

                SetUnverifiedRoles();

                guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
            }

            return playerName + " is not a member of Chiefs Of Corkus or its allies.";
        }

        //Determine bot response message.
        if (isAlly) {
            try {
                member.modifyNickname(playerName).queue();
                return "Verified " + member.getUser().getName() + " as Ally Guild member " + playerName;
            } catch (Exception e) {
                return "Verified " + member.getUser().getName() + " as Ally Guild member " + playerName + "(Could not change nickname)";
            }
        } else {
            try {
                member.modifyNickname(playerName).queue();
                return "Verified " + member.getUser().getName() + " as Guild member " + playerName;
            } catch (Exception e) {
                return "Verified " + member.getUser().getName() + " as Guild member " + playerName + "(Could not change nickname)";
            }
        }
    }

    /**
     * Sets the player roles for a given member.
     * @param rolesUpdated How many roles have currently been updated.
     * @param hasUpdated Whether this member has already been updated.
     * @param member The member being updated.
     * @param uuid The UUID or the given member.
     * @return The updated value of rolesUpdated if changed, otherwise the same.
     */
    private int setPlayerRoles(int rolesUpdated, boolean hasUpdated, Member member, String uuid) {
        //Sets the player currently being used to get roles.
        SetPlayer(uuid);

        //Sets player roles.
        switch(player.getMeta().getTag().getValue()) {
            case CHAMPION -> {
                if (needsRole(member, championRole)) {
                    System.out.println(member.getUser().getName() + " is a CHAMPION.");
                    SetWynnRankRoles(championRole);
                    hasUpdated = true;
                }
            }
            case HERO -> {
                if (needsRole(member, heroRole)) {
                    System.out.println(member.getUser().getName() + " is a HERO.");
                    SetWynnRankRoles(heroRole);
                    hasUpdated = true;
                }
            }
            case VIPPLUS -> {
                if (needsRole(member, vipPlusRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP+.");
                    SetWynnRankRoles(vipPlusRole);
                    hasUpdated = true;
                }
            }
            case VIP -> {
                if (needsRole(member, vipRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP.");
                    SetWynnRankRoles(vipRole);
                    hasUpdated = true;
                }
            }
        }

        if (player.getMeta().isVeteran()) {
            if (needsRole(member, vetRole)) {
                System.out.println(member.getUser().getName() + " is a Vet.");
                rolesToAdd.add(vetRole);
                hasUpdated = true;
            }
        }

        if (hasUpdated) {
            rolesUpdated += 1;
        }

        return rolesUpdated;
    }

    /**
     * Updates the roles of the given member based on the player.
     * @param member The member to update.
     * @param uuid The UUID of the player to be verified as.
     */
    private void setPlayerRoles(Member member, String uuid) {
        //Set the player to be verified as.
        SetPlayer(uuid);

        //Updates player roles.
        switch(player.getMeta().getTag().getValue()) {
            case CHAMPION -> {
                if (needsRole(member, championRole)) {
                    System.out.println(member.getUser().getName() + " is a CHAMPION.");
                    SetWynnRankRoles(championRole);
                }
            }
            case HERO -> {
                if (needsRole(member, heroRole)) {
                    System.out.println(member.getUser().getName() + " is a HERO.");
                    SetWynnRankRoles(heroRole);
                }
            }
            case VIPPLUS -> {
                if (needsRole(member, vipPlusRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP+.");
                    SetWynnRankRoles(vipPlusRole);
                }
            }
            case VIP -> {
                if (needsRole(member, vipRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP.");
                    SetWynnRankRoles(vipRole);
                }
            }
        }

        if (player.getMeta().isVeteran()) {
            if (needsRole(member, vetRole)) {
                System.out.println(member.getUser().getName() + " is a Vet.");
                rolesToAdd.add(vetRole);
            }
        }
    }

    /**
     * Gets the name of the Wynncraft guild from the file.
     */
    private void getGuildName() {
        try {
            Scanner scanner = new Scanner(guildFile);

            if (scanner.hasNextLine()) {
                guildName = scanner.nextLine();
            }

            scanner.close();
        } catch (java.io.IOException ex) {
            guildName = "";
        }
    }

    /**
     * Gets the names or all ally guilds from the file.
     */
    private void getAllyGuilds() {
        try {
            if (allyFile.exists()) {
                Scanner scanner = new Scanner(allyFile);

                while (scanner.hasNextLine()) {
                    WynncraftGuild allyGuild = wynnAPI.v1().guildStats(scanner.nextLine()).run();

                    allyGuilds.add(allyGuild);

                    System.out.println("Added Ally guild " + allyGuild.getName());
                }

                scanner.close();
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sets up all the Wynncraft guilds for verification.
     * @return Whether the main guild was found or not.
     */
    private boolean setGuilds() {
        getGuildName();

        if (guildName.equals("")) {
            return true;
        }

        getAllyGuilds();

        mainGuild = wynnAPI.v1().guildStats(guildName).run();

        return false;
    }

    /**
     * Sets which Wynncraft rank roles to add or not.
     * @param roleToAdd The one role to add.
     */
    private void SetWynnRankRoles(Role roleToAdd) {
        for (Role rankRole : rankRoles) {
            if (rankRole != roleToAdd) {
                rolesToRemove.add(rankRole);
            } else {
                rolesToAdd.add(rankRole);
            }
        }
    }

    /**
     * Sets which guild rank roles to add or not.
     * @param roleToAdd The one role to add.
     */
    private void SetGuildRankRoles(Role roleToAdd) {
        for (Role rankRole : guildRoles) {
            if (rankRole != roleToAdd) {
                rolesToRemove.add(rankRole);
            } else {
                rolesToAdd.add(rankRole);
            }
        }
    }

    /**
     * Sets which ally rank roles to add or not.
     * @param roleToAdd The one role to add.
     */
    private void SetAllyRankRoles(Role roleToAdd) {
        for (Role rankRole : allyRoles) {
            if (rankRole != roleToAdd) {
                rolesToRemove.add(rankRole);
            } else {
                rolesToAdd.add(rankRole);
            }
        }
    }

    /**
     * Sets the unverified role to be added and all other rank related roles to be removed.
     */
    private void SetUnverifiedRoles() {
        rolesToAdd.add(unverifiedRole);
        rolesToRemove.add(vipRole);
        rolesToRemove.add(vipPlusRole);
        rolesToRemove.add(heroRole);
        rolesToRemove.add(championRole);
        rolesToRemove.add(vetRole);
        rolesToRemove.add(recruitRole);
        rolesToRemove.add(recruiterRole);
        rolesToRemove.add(captainRole);
        rolesToRemove.add(strategistRole);
        rolesToRemove.add(chiefRole);
        rolesToRemove.add(ownerRole);
        rolesToRemove.add(memberOfRole);
        rolesToRemove.add(allyRole);
        rolesToRemove.add(allyOwnerRole);
    }

    /**
     * Sets the player that is currently being verified.
     * @param uuid The UUID of the player to check.
     */
    private void SetPlayer(String uuid) {
        player = wynnAPI.v2().player().statsUUID(uuid).run()[0];
    }

    /**
     * Adds a new guild to have its average online players be tracked.
     * @param guildName The guild to be tracked.
     * @param guild The current Discord server.
     * @return The message to be sent back.
     */
    private String trackGuild(String guildName, Guild guild) {
        try {
            //Get all the current online members in the guild.
            int currentMembers = getOnlineMembers(wynnAPI.v1().guildStats(guildName).run());

            try {
                //Use Guild ID to create directory with name unique to the current server.
                Files.createDirectories(Path.of("/home/opc/CC-117/" + guild.getId()));

                if (trackedFile.createNewFile()) {
                    System.out.println("File created.");
                } else {
                    System.out.println("File already exists.");
                }

                Scanner scanner = new Scanner(trackedFile);
                String currentLine;
                List<String> lineSplit;

                while (scanner.hasNextLine()) {
                    currentLine = scanner.nextLine();
                    lineSplit = Arrays.asList(currentLine.split(","));
                    if (lineSplit.get(0).equals(guildName)) {
                        return guildName + " is already being tracked.";
                    }
                }

                Instant instant = Instant.now();

                Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "tracked.txt"), (guildName + "," + currentMembers + "," + 1 + "," + instant.atZone(ZoneOffset.UTC).getHour() + "," + instant.atZone(ZoneOffset.UTC).getHour() + "," + currentMembers + "," + currentMembers + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (java.io.IOException ex) {
                return ex.toString();
            }

            return guildName + "'s average online players are now being tracked.";
        } catch (APIResponseException ex) {
            return guildName + " is not a valid Guild.";
        } catch (APIRateLimitExceededException ex) {
            return "Hit limit of 750 players checked.";
        }
    }

    /**
     * Gets the number of people currently online in a specific guild.
     * @param guild The guild to check.
     * @return The number of people online.
     */
    private int getOnlineMembers(WynncraftGuild guild) {
        int currentlyOnline = 0;
        List<String> guildMembersNames = new ArrayList<>();

        //Retrieve the names of the members of the guild.
        for (WynncraftGuildMember player : guild.getMembers()) {
            guildMembersNames.add(player.getName());
        }

        //Gets all the players on each server on Wynncraft.
        WynncraftOnlinePlayers onlineServers = wynnAPI.v1().onlinePlayers().run();

        //Loops through each server and then loops through the players on that server to see if they are in the guild.
        for (WynncraftServerOnlinePlayers onlineServer : onlineServers.getOnlinePlayers()) {
            for (String playerName : onlineServer.getPlayers()) {
                if (guildMembersNames.contains(playerName)) {
                    currentlyOnline++;
                }
            }
        }

        return currentlyOnline;
    }

    /**
     * Updates the average number of players online for each tracked guild.
     * @param guild The current Discord server.
     */
    private void updateOnlineAverage(Guild guild) {
        try {
            Scanner scanner = new Scanner(trackedFile);
            String currentLine;
            List<String> lineSplit;
            String currentGuildName;
            double currentGuildAverage;
            int currentGuildChecks;
            int currentGuildActiveHour;
            int currentGuildDeadHour;
            int currentGuildHighestOnline;
            int currentGuildLowestOnline;
            double newAverage;

            if (!trackedFile.exists()) {
                System.out.println("No file exists.");
            }

            if (tempFile.createNewFile()) {
                System.out.println("File created.");
            } else {
                System.out.println("File already exists.");
            }

            //Loop through each line in file, get the guild name, current average and how many times the average has been checked.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                lineSplit = Arrays.asList(currentLine.split(","));
                currentGuildName = lineSplit.get(0);
                currentGuildAverage = Double.parseDouble(lineSplit.get(1));
                currentGuildChecks = Integer.parseInt(lineSplit.get(2));
                currentGuildActiveHour = Integer.parseInt(lineSplit.get(3));
                currentGuildDeadHour = Integer.parseInt(lineSplit.get(4));
                currentGuildHighestOnline = Integer.parseInt(lineSplit.get(5));
                currentGuildLowestOnline = Integer.parseInt(lineSplit.get(6));

                //Get current online count.
                int onlineMembers = getOnlineMembers(wynnAPI.v1().guildStats(currentGuildName).run());

                //Update the average. If it has been running for a month, 672 hours, then reset but keep the previous months average as a starting base.
                if (currentGuildChecks < 672) {
                    newAverage = currentGuildAverage * (currentGuildChecks - 1) / currentGuildChecks + (double) onlineMembers / currentGuildChecks;
                } else {
                    currentGuildChecks = 0;
                    newAverage = currentGuildAverage + (double) onlineMembers / 2.0;
                }

                Instant instant = Instant.now();

                if (onlineMembers >= currentGuildHighestOnline) {
                    currentGuildActiveHour = instant.atZone(ZoneOffset.UTC).getHour();
                    currentGuildHighestOnline = onlineMembers;
                }

                if (onlineMembers <= currentGuildLowestOnline) {
                    currentGuildDeadHour = instant.atZone(ZoneOffset.UTC).getHour();
                    currentGuildLowestOnline = onlineMembers;
                }

                //Make string ready to be saved.
                currentLine = currentGuildName + "," + newAverage + "," + (currentGuildChecks + 1) + "," + currentGuildActiveHour + "," + currentGuildDeadHour + "," + currentGuildHighestOnline + "," + currentGuildLowestOnline;

                //Write new average.
                Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt"), (currentLine + "\n").getBytes(), StandardOpenOption.APPEND);
            }

            scanner.close();

            renameTrackedFile();

        } catch (java.io.IOException ex) {
            System.out.println("Error accessing file");
        } catch (Exception ex) {
            ex.printStackTrace();

            TextChannel channel = guild.getTextChannelById("1061698530651144212");

            if (channel != null) {
                channel.sendMessage("Checks the logs, something broke").queue();
            }

            File logFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "logs.txt");

            try {
                if (logFile.createNewFile()) {
                    System.out.println("File created.");
                } else {
                    System.out.println("File already exists.");
                }

                FileWriter logFileWriter = new FileWriter("/home/opc/CC-117/" + guild.getId() + "/" + "logs.txt");
                logFileWriter.write(String.valueOf(ex));
                logFileWriter.close();
            } catch (java.io.IOException exx) {
                exx.printStackTrace();
            }
        }
    }

    /**
     * Removes tracked guild from the file.
     * @param guildName Guild to untrack.
     * @param guild The current Discord server.
     * @return Message to send back.
     */
    private String untrackGuild(String guildName, Guild guild) {
        try {
            Scanner scanner = new Scanner(trackedFile);
            String currentLine;
            String currentGuild;

            if (tempFile.createNewFile()) {
                System.out.println("File created.");
            } else {
                System.out.println("File already exists.");
            }

            //Loop through every line in tracked file and if it does not match the name of the
            //guild requested to remove as tracked, add it to the temp file.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                currentGuild = Arrays.asList(currentLine.split(",")).get(0);

                if (!currentGuild.equals(guildName)) {
                    Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt"), (currentLine + "\n").getBytes(), StandardOpenOption.APPEND);
                }
            }

            scanner.close();

            renameTrackedFile();

        } catch (java.io.IOException ex) {
            return "No tracked guilds found: " + ex;
        }

        return "Removed " + guildName + " as tracked.";
    }

    /**
     * Renames the temp file to the tracked file.
     */
    private void renameTrackedFile() {
        //Delete old tracked file and rename the temp file to the tracked file.
        if (trackedFile.delete()) {
            System.out.println("Tracked file deleted successfully.");
        } else {
            System.out.println("Unable to delete tracked file.");
        }

        if (tempFile.renameTo(trackedFile)) {
            System.out.println("Temp file renamed successfully.");
        } else {
            System.out.println("Unable to rename temp file.");
        }
    }

    /**
     * Shows a formatted string of the average online players of each tracked guild.
     * @return The message to send back.
     */
    private String trackedGuilds(String timezone) {
        trackedHeader = "```Guild Name          Average Online Members          Active Hour (" + timezone + ")    Dead Hour (" + timezone + ")\n------------------------------------------------------------------------------------------\n";
        StringBuilder guildAverages = new StringBuilder();

        try {
            Scanner scanner = new Scanner(trackedFile);
            String currentLine;
            List<String> lineSplit;
            String currentGuildName;
            double currentGuildAverage;
            int currentGuildActiveHour;
            int currentGuildDeadHour;
            List<GuildAverageMembers> averageMembers = new ArrayList<>();

            if (!trackedFile.exists()) {
                return "No tracked guilds.";
            }

            //Loop through every line in the file. Get the name and average online players.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                lineSplit = Arrays.asList(currentLine.split(","));
                currentGuildName = lineSplit.get(0);
                currentGuildAverage = Double.parseDouble(lineSplit.get(1));
                currentGuildActiveHour = Integer.parseInt(lineSplit.get(3));
                currentGuildDeadHour = Integer.parseInt(lineSplit.get(4));

                //Create an object that can be sorted by average.
                averageMembers.add(new GuildAverageMembers(currentGuildName, currentGuildAverage, currentGuildActiveHour, currentGuildDeadHour, timezone));
            }

            scanner.close();

            //Sort the guilds by highest average online players.
            averageMembers.sort(Collections.reverseOrder());

            int counter = 0;

            //Create the string message to be sent.
            for (GuildAverageMembers members : averageMembers) {
                if (counter == 10) {
                    guildAverages.append("```");
                    trackedPages.add(guildAverages.toString());
                    guildAverages = new StringBuilder();

                    guildAverages.append(members.getAverageString());
                    counter = 1;
                } else {
                    guildAverages.append(members.getAverageString());
                    counter++;
                }
            }

            if (counter != 10) {
                guildAverages.append("```");
                trackedPages.add(guildAverages.toString());
            }
        } catch (java.io.IOException ex) {
            return "No tracked guilds found: " + ex;
        }

        return trackedHeader + trackedPages.get(0);
    }

    /**
     * Stores the name of the Wynncraft Guild into a file so that the bot can work
     * on different servers if need be.
     * @param guildName The name of the guild to store in the file.
     * @param guild The Discord server to get the ID from, so it can store it in a unique file.
     * @return Message to say guild set or not.
     */
    private String setGuild(String guildName, Guild guild) {
        try {
            if (guildFile.createNewFile()) {
                System.out.println("File created.");
            } else {
                System.out.println("File already exists.");
            }

            try {
                wynnAPI.v1().guildStats(guildName).run();
            } catch (APIResponseException ex) {
                return guildName + " is not a valid Guild.";
            }

            FileWriter guildFileWriter = new FileWriter("/home/opc/CC-117/" + guild.getId() + "/" + "guild.txt");
            guildFileWriter.write(guildName);
            guildFileWriter.close();
        } catch (java.io.IOException ex) {
            return ex.toString();
        }

        return "Set " + guildName + " as Guild.";
    }

    /**
     * Stores the name(s) of any Allies to the Wynncraft Guild into a file so that
     * allies can easily be added/removed.
     * @param guildName Name of the Ally guild.
     * @param guild The current Discord server so the file can be created/read uniquely.
     * @return Message to say Ally added or not.
     */
    private String addAlly(String guildName, Guild guild) {
        try {
            //Use Guild ID to create directory with name unique to the current server.
            Files.createDirectories(Path.of("/home/opc/CC-117/" + guild.getId()));

            if (allyFile.createNewFile()) {
                System.out.println("File created.");
            } else {
                System.out.println("File already exists.");
            }

            try {
                wynnAPI.v1().guildStats(guildName).run();
            } catch (APIResponseException ex) {
                return guildName + " is not a valid Guild.";
            }

            Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "allies.txt"), (guildName + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (java.io.IOException ex) {
            return ex.toString();
        }

        return "Added " + guildName + " as an Ally.";
    }

    /**
     * Removes the name of an Ally to the Wynncraft Guild in the file so that
     * any members of that Guild will no longer have ally roles.
     * @param guildName Name of the no longer Ally guild.
     * @param guild The current Discord server so that the correct ally file can be found.
     * @return Message to say Ally removed or not.
     */
    private String removeAlly(String guildName, Guild guild) {
        try {
            Scanner scanner = new Scanner(allyFile);
            String currentLine;

            if (tempFile.createNewFile()) {
                System.out.println("File created.");
            } else {
                System.out.println("File already exists.");
            }

            //Loop through every line in ally file and if it does not match the name of the
            //guild requested to remove as an ally, add it to the temp file.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                if (!currentLine.equals(guildName)) {
                    Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt"), (currentLine + "\n").getBytes(), StandardOpenOption.APPEND);
                }
            }

            scanner.close();

            //Delete old ally file and rename the temp file to the ally file.
            if (allyFile.delete()) {
                System.out.println("Ally file deleted successfully.");
            } else {
                System.out.println("Unable to delete ally file.");
            }

            if (tempFile.renameTo(allyFile)) {
                System.out.println("Temp file renamed successfully.");
            } else {
                System.out.println("Unable to rename temp file.");
            }

        } catch (java.io.IOException ex) {
            return "No allies found: " + ex;
        }

        return "Removed " + guildName + " as an Ally.";
    }

    /**
     * Checks if a member in a Discord server needs a specified role or not.
     * @param member The member to check.
     * @param role The role to check.
     * @return Whether they have the role or not. False if they have it already, true if they don't.
     */
    private boolean needsRole(Member member, Role role) {
        List<Role> memberRoles = member.getRoles();
        return !memberRoles.contains(role);
    }

    /**
     * Checks if a member has permission to use certain commands, must be an Owner or a Chief.
     * @param member The member to check.
     * @return Whether they have permission or not.
     */
    private boolean canUseCommand(Member member) {
        List<Role> memberRoles = member.getRoles();

        return memberRoles.contains(ownerRole) || memberRoles.contains(chiefRole);
    }
}
