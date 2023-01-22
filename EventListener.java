import me.bed0.jWynn.WynncraftAPI;
import me.bed0.jWynn.api.v1.guild.WynncraftGuild;
import me.bed0.jWynn.api.v2.player.WynncraftPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
    WynncraftGuild mainGuild;
    String guildName = "";
    List<WynncraftGuild> allyGuilds = new ArrayList<>();
    List<Role> rolesToAdd = new ArrayList<>();
    List<Role> rolesToRemove = new ArrayList<>();
    WynncraftPlayer player;

    /**
     * Adds the unverified role to a new member when they join the server.
     * Also mentions them in the verify channel to get them to go there and verify themselves.
     * @param event The event of the member joining to get information from.
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
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
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        //Update role variables and paths.
        if (event.getGuild() != null) {
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

            guildFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "guild.txt");
            allyFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "allies.txt");
        }
        else {
            return;
        }

        //Determine which command was used.
        switch (event.getName()) {
            case "updateranks" -> {
                event.deferReply().queue();
                event.getHook().sendMessage(updateRanks(event.getGuild())).queue();
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
                event.deferReply().queue();
                OptionMapping guildNameOption = event.getOption("guild_name");
                if (guildNameOption != null) {
                    event.getHook().sendMessage(setGuild(guildNameOption.getAsString(), event.getGuild())).queue();
                } else {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }
            }
            case "addally" -> {
                event.deferReply().queue();
                OptionMapping addAllyNameOption = event.getOption("guild_name");
                if (addAllyNameOption != null) {
                    event.getHook().sendMessage(addAlly(addAllyNameOption.getAsString(), event.getGuild())).queue();
                } else {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }
            }
            case "removeally" -> {
                event.deferReply().queue();
                OptionMapping removeAllyNameOption = event.getOption("guild_name");
                if (removeAllyNameOption != null) {
                    event.getHook().sendMessage(removeAlly(removeAllyNameOption.getAsString(), event.getGuild())).queue();
                } else {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }
            }
            default -> event.reply("Unknown command.").queue();
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
                        } catch (Exception e) {
                            System.out.println("Verified " + member.getUser().getName() + " as Guild member " + mainGuild.getMembers()[i].getName() + "(Could not change nickname)");
                        }

                        hasUpdated = false;
                        String uuid = mainGuild.getMembers()[i].getUuid();

                        //Set guild ranks.
                        switch (mainGuild.getMembers()[i].getRank()) {
                            case OWNER -> {
                                if (hasRole(member, ownerRole)) {
                                    System.out.println(member.getUser().getName() + " is the Owner of the Guild.");
                                    SetGuildRankRoles(ownerRole);
                                    hasUpdated = true;
                                }
                            }
                            case CHIEF -> {
                                if (hasRole(member, chiefRole)) {
                                    System.out.println(member.getUser().getName() + " is a Chief of the Guild.");
                                    SetGuildRankRoles(chiefRole);
                                    hasUpdated = true;
                                }
                            }
                            case STRATEGIST -> {
                                if (hasRole(member, strategistRole)) {
                                    System.out.println(member.getUser().getName() + " is a Strategist of the Guild.");
                                    SetGuildRankRoles(strategistRole);
                                    hasUpdated = true;
                                }
                            }
                            case CAPTAIN -> {
                                if (hasRole(member, captainRole)) {
                                    System.out.println(member.getUser().getName() + " is a Captain of the Guild.");
                                    SetGuildRankRoles(captainRole);
                                    hasUpdated = true;
                                }
                            }
                            case RECRUITER -> {
                                if (hasRole(member, recruiterRole)) {
                                    System.out.println(member.getUser().getName() + " is a Recruiter of the Guild.");
                                    SetGuildRankRoles(recruiterRole);
                                    hasUpdated = true;
                                }
                            }
                            case RECRUIT -> {
                                if (hasRole(member, recruitRole)) {
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
                            } catch (Exception e) {
                                System.out.println("Verified " + member.getUser().getName() + " as Ally Guild member " + allyGuild.getMembers()[j].getName() + "(Could not change nickname)");
                            }

                            hasUpdated = false;
                            String uuid = allyGuild.getMembers()[j].getUuid();

                            //Give ally roles.
                            switch (allyGuild.getMembers()[j].getRank()) {
                                case OWNER -> {
                                    if (hasRole(member, allyOwnerRole)) {
                                        System.out.println(member.getUser().getName() + " is the Owner of an Ally Guild.");
                                        SetGuildRankRoles(allyOwnerRole);
                                        SetAllyRankRoles(allyOwnerRole);
                                        hasUpdated = true;
                                    }
                                }
                                case CHIEF, STRATEGIST, CAPTAIN, RECRUITER, RECRUIT -> {
                                    if (hasRole(member, allyRole)) {
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
            if(hasRole(member, unverifiedRole)) {
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
                        if (hasRole(member, ownerRole)) {
                            System.out.println(member.getUser().getName() + " is the Owner of the Guild.");
                            SetGuildRankRoles(ownerRole);
                        }
                    }
                    case CHIEF -> {
                        if (hasRole(member, chiefRole)) {
                            System.out.println(member.getUser().getName() + " is a Chief of the Guild.");
                            SetGuildRankRoles(chiefRole);
                        }
                    }
                    case STRATEGIST -> {
                        if (hasRole(member, strategistRole)) {
                            System.out.println(member.getUser().getName() + " is a Strategist of the Guild.");
                            SetGuildRankRoles(strategistRole);
                        }
                    }
                    case CAPTAIN -> {
                        if (hasRole(member, captainRole)) {
                            System.out.println(member.getUser().getName() + " is a Captain of the Guild.");
                            SetGuildRankRoles(captainRole);
                        }
                    }
                    case RECRUITER -> {
                        if (hasRole(member, recruiterRole)) {
                            System.out.println(member.getUser().getName() + " is a Recruiter of the Guild.");
                            SetGuildRankRoles(recruiterRole);
                        }
                    }
                    case RECRUIT -> {
                        if (hasRole(member, recruitRole)) {
                            System.out.println(member.getUser().getName() + " is a Recruit of the Guild.");
                            SetGuildRankRoles(recruitRole);
                        }
                    }
                }

                //Sets player roles.
                setPlayerRoles(member, uuid);

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
                                if (hasRole(member, allyOwnerRole)) {
                                    System.out.println(member.getUser().getName() + " is the Owner of an Ally Guild.");
                                    SetGuildRankRoles(allyOwnerRole);
                                    SetAllyRankRoles(allyOwnerRole);
                                }
                            }
                            case CHIEF, STRATEGIST, CAPTAIN, RECRUITER, RECRUIT -> {
                                if (hasRole(member, allyRole)) {
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
            if(hasRole(member, unverifiedRole)) {
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
                if (hasRole(member, championRole)) {
                    System.out.println(member.getUser().getName() + " is a CHAMPION.");
                    SetWynnRankRoles(championRole);
                    hasUpdated = true;
                }
            }
            case HERO -> {
                if (hasRole(member, heroRole)) {
                    System.out.println(member.getUser().getName() + " is a HERO.");
                    SetWynnRankRoles(heroRole);
                    hasUpdated = true;
                }
            }
            case VIPPLUS -> {
                if (hasRole(member, vipPlusRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP+.");
                    SetWynnRankRoles(vipPlusRole);
                    hasUpdated = true;
                }
            }
            case VIP -> {
                if (hasRole(member, vipRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP.");
                    SetWynnRankRoles(vipRole);
                    hasUpdated = true;
                }
            }
        }

        if (player.getMeta().isVeteran()) {
            if (hasRole(member, vetRole)) {
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
                if (hasRole(member, championRole)) {
                    System.out.println(member.getUser().getName() + " is a CHAMPION.");
                    SetWynnRankRoles(championRole);
                }
            }
            case HERO -> {
                if (hasRole(member, heroRole)) {
                    System.out.println(member.getUser().getName() + " is a HERO.");
                    SetWynnRankRoles(heroRole);
                }
            }
            case VIPPLUS -> {
                if (hasRole(member, vipPlusRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP+.");
                    SetWynnRankRoles(vipPlusRole);
                }
            }
            case VIP -> {
                if (hasRole(member, vipRole)) {
                    System.out.println(member.getUser().getName() + " is a VIP.");
                    SetWynnRankRoles(vipRole);
                }
            }
        }

        if (player.getMeta().isVeteran()) {
            if (hasRole(member, vetRole)) {
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
     * Stores the name of the Wynncraft Guild into a file so that the bot can work
     * on different servers if need be.
     * @param guildName The name of the guild to store in the file.
     * @param guild The Discord server to get the ID from, so it can store it in a unique file.
     * @return Message to say guild set or not.
     */
    private String setGuild(String guildName, Guild guild) {
        try {
            //Use Guild ID to create directory with name unique to the current server.
            Files.createDirectories(Path.of("/home/opc/CC-117/" + guild.getId()));

            if (guildFile.createNewFile()) {
                System.out.println("File created.");
            } else {
                System.out.println("File already exists.");
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
        File tempFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt");

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
     * Checks if a member in a Discord server has a specified role or not.
     * @param member The member to check.
     * @param role The role to check.
     * @return Whether they have the role or not.
     */
    private boolean hasRole(Member member, Role role) {
        List<Role> memberRoles = member.getRoles();
        return !memberRoles.contains(role);
    }
}
