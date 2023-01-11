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

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        //Test server
//        Role unverifiedRole = event.getGuild().getRoleById("1061802077283688560");

        //Real server
        Role unverifiedRole = event.getGuild().getRoleById("1061791662541647913");
        event.getGuild().addRoleToMember(event.getMember(), unverifiedRole).queue();

        //Test server
        //TextChannel channel = event.getGuild().getTextChannelById("1062856380219916318");

        //Real server
        TextChannel channel = event.getGuild().getTextChannelById("1061730979913404506");
        channel.sendMessage(event.getMember().getAsMention()).queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        //Test server
//        ownerRole = event.getGuild().getRoleById("1061270640155443210");
//        chiefRole = event.getGuild().getRoleById("1061270649533911060");
//        strategistRole = event.getGuild().getRoleById("1061270902467207209");
//        captainRole = event.getGuild().getRoleById("1061270911745020005");
//        recruiterRole = event.getGuild().getRoleById("1061270914999795843");
//        recruitRole = event.getGuild().getRoleById("1061270917784813609");
//        championRole = event.getGuild().getRoleById("1061378409038614629");
//        heroRole = event.getGuild().getRoleById("1061378363656249374");
//        vipPlusRole = event.getGuild().getRoleById("1061378329900498974");
//        vipRole = event.getGuild().getRoleById("1061378281112350801");
//        vetRole = event.getGuild().getRoleById("1061378442152644728");
//        unverifiedRole = event.getGuild().getRoleById("1061802077283688560");
//        memberOfRole = event.getGuild().getRoleById("1061802116424933466");
//        allyRole = event.getGuild().getRoleById("1061634370475151390");
//        allyOwnerRole = event.getGuild().getRoleById("1061634378289119264");

        //Real server
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

        switch (event.getName()) {
            case "updateranks":
                event.deferReply().queue();
                event.getHook().sendMessage(updateRanks(event.getGuild())).queue();
                break;
            case "verify":
                try {
                    event.deferReply().setEphemeral(true).queue();
                    String response = verify(event.getOption("player_name").getAsString(), event.getGuild(), event.getMember());
                    event.getHook().sendMessage(response).setEphemeral(true).queue();
                    TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");
                    channel.sendMessage(response).queue();
                } catch (NullPointerException ex) {
                    event.getHook().sendMessage("Please enter a player name.").setEphemeral(true).queue();
                }

                break;
            case "setguild":
                try {
                    event.deferReply().queue();
                    event.getHook().sendMessage(setGuild(event.getOption("guild_name").getAsString(), event.getGuild())).queue();
                } catch (NullPointerException ex) {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }

                break;
            case "addally":
                try {
                    event.deferReply().queue();
                    event.getHook().sendMessage(addAlly(event.getOption("guild_name").getAsString(), event.getGuild())).queue();
                } catch (NullPointerException ex) {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }

                break;
            case "removeally":
                try {
                    event.deferReply().queue();
                    event.getHook().sendMessage(removeAlly(event.getOption("guild_name").getAsString(), event.getGuild())).queue();
                } catch (NullPointerException ex) {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }

                break;
        }
    }

    private String updateRanks(Guild guild) {
        List<Member> discordMembers = new ArrayList<Member>();

        for (Member member : guild.getMembers()) {
            if (!member.getUser().isBot()) {
                discordMembers.add(member);
            }
        }

        File guildFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "guild.txt");
        File allyFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "allies.txt");

        String guildName = "";

        try {
            Scanner scanner = new Scanner(guildFile);

            if (scanner.hasNextLine()) {
                guildName = scanner.nextLine();
            }

            scanner.close();
        } catch (java.io.IOException ex) {
            return "Make sure to set your guild with /setguild <guildname>.";
        }

        List<WynncraftGuild> allyGuilds = new ArrayList<WynncraftGuild>();

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
            return "Make sure to set your guild allies with /addally <guildname>.";
        }

        WynncraftGuild mainGuild = wynnAPI.v1().guildStats(guildName).run();

        int rolesUpdated = 0;
        boolean hasUpdated;

        for (int i = 0; i < mainGuild.getMembers().length; i++) {
            if (discordMembers.size() > 0) {
                for (Member member : discordMembers) {
                    String nick = null;

                    if (member.getNickname() != null) {
                        nick = member.getNickname();
                    }

                    if (mainGuild.getMembers()[i].getName().equalsIgnoreCase(member.getUser().getName()) || mainGuild.getMembers()[i].getName().toLowerCase().equalsIgnoreCase(nick)) {
                        guild.addRoleToMember(member, memberOfRole).queue();
                        guild.removeRoleFromMember(member, unverifiedRole).queue();
                        guild.removeRoleFromMember(member, allyRole).queue();
                        guild.removeRoleFromMember(member, allyOwnerRole).queue();

                        try {
                            member.modifyNickname(mainGuild.getMembers()[i].getName()).queue();
                            System.out.println("Verified " + member.getUser().getName() + " as Guild member " + mainGuild.getMembers()[i].getName());
                        } catch (Exception e) {
                            System.out.println("Verified " + member.getUser().getName() + " as Guild member " + mainGuild.getMembers()[i].getName() + "(Could not change nickname)");
                        }

                        hasUpdated = false;
                        String uuid = mainGuild.getMembers()[i].getUuid();

                        switch (mainGuild.getMembers()[i].getRank()) {
                            case OWNER -> {
                                if (!hasRole(member, ownerRole)) {
                                    System.out.println(member.getUser().getName() + " is the Owner of the Guild.");
                                    guild.addRoleToMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, recruiterRole).queue();
                                    guild.removeRoleFromMember(member, recruitRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case CHIEF -> {
                                if (!hasRole(member, chiefRole)) {
                                    System.out.println(member.getUser().getName() + " is a Chief of the Guild.");
                                    guild.addRoleToMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, recruiterRole).queue();
                                    guild.removeRoleFromMember(member, recruitRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case STRATEGIST -> {
                                if (!hasRole(member, strategistRole)) {
                                    System.out.println(member.getUser().getName() + " is a Strategist of the Guild.");
                                    guild.addRoleToMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, recruiterRole).queue();
                                    guild.removeRoleFromMember(member, recruitRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case CAPTAIN -> {
                                if (!hasRole(member, captainRole)) {
                                    System.out.println(member.getUser().getName() + " is a Captain of the Guild.");
                                    guild.addRoleToMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, recruiterRole).queue();
                                    guild.removeRoleFromMember(member, recruitRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case RECRUITER -> {
                                if (!hasRole(member, recruiterRole)) {
                                    System.out.println(member.getUser().getName() + " is a Recruiter of the Guild.");
                                    guild.addRoleToMember(member, recruiterRole).queue();
                                    guild.removeRoleFromMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, recruitRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case RECRUIT -> {
                                if (!hasRole(member, recruitRole)) {
                                    System.out.println(member.getUser().getName() + " is a Recruit of the Guild.");
                                    guild.addRoleToMember(member, recruitRole).queue();
                                    guild.removeRoleFromMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, recruiterRole).queue();
                                    hasUpdated = true;
                                }
                            }
                        }

                        WynncraftPlayer player = wynnAPI.v2().player().statsUUID(uuid).run()[0];

                        switch(player.getMeta().getTag().getValue()) {
                            case CHAMPION -> {
                                if (!hasRole(member, championRole)) {
                                    System.out.println(member.getUser().getName() + " is a CHAMPION.");
                                    guild.addRoleToMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, vipPlusRole).queue();
                                    guild.removeRoleFromMember(member, vipRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case HERO -> {
                                if (!hasRole(member, heroRole)) {
                                    System.out.println(member.getUser().getName() + " is a HERO.");
                                    guild.addRoleToMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, vipPlusRole).queue();
                                    guild.removeRoleFromMember(member, vipRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case VIPPLUS -> {
                                if (!hasRole(member, vipPlusRole)) {
                                    System.out.println(member.getUser().getName() + " is a VIP+.");
                                    guild.addRoleToMember(member, vipPlusRole).queue();
                                    guild.removeRoleFromMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, vipRole).queue();
                                    hasUpdated = true;
                                }
                            }
                            case VIP -> {
                                if (!hasRole(member, vipRole)) {
                                    System.out.println(member.getUser().getName() + " is a VIP.");
                                    guild.addRoleToMember(member, vipRole).queue();
                                    guild.removeRoleFromMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, vipPlusRole).queue();
                                    hasUpdated = true;
                                }
                            }
                        }

                        if (player.getMeta().isVeteran()) {
                            if (!hasRole(member, vetRole)) {
                                System.out.println(member.getUser().getName() + " is a Vet.");
                                guild.addRoleToMember(member, vetRole).queue();
                                hasUpdated = true;
                            }
                        }

                        if (hasUpdated) {
                            rolesUpdated += 1;
                        }

                        discordMembers.remove(member);

                        break;
                    }
                }
            }
        }

        for (int i = 0; i < allyGuilds.size(); i++) {
            for (int j = 0; j < allyGuilds.get(i).getMembers().length; j++) {
                if (discordMembers.size() > 0) {
                    for (Member member : discordMembers) {
                        String nick = null;

                        if (member.getNickname() != null) {
                            nick = member.getNickname();
                        }

                        if (allyGuilds.get(i).getMembers()[j].getName().equalsIgnoreCase(member.getUser().getName()) || allyGuilds.get(i).getMembers()[j].getName().toLowerCase().equalsIgnoreCase(nick)) {
                            guild.removeRoleFromMember(member, memberOfRole).queue();
                            guild.removeRoleFromMember(member, unverifiedRole).queue();

                            try {
                                member.modifyNickname(allyGuilds.get(i).getMembers()[j].getName()).queue();
                                System.out.println("Verified " + member.getUser().getName() + " as Ally Guild member " + allyGuilds.get(i).getMembers()[j].getName());
                            } catch (Exception e) {
                                System.out.println("Verified " + member.getUser().getName() + " as Ally Guild member " + allyGuilds.get(i).getMembers()[j].getName() + "(Could not change nickname)");
                            }

                            hasUpdated = false;
                            String uuid = allyGuilds.get(i).getMembers()[j].getUuid();

                            switch (allyGuilds.get(i).getMembers()[j].getRank()) {
                                case OWNER -> {
                                    if (!hasRole(member, allyOwnerRole)) {
                                        System.out.println(member.getUser().getName() + " is the Owner of an Ally Guild.");
                                        guild.addRoleToMember(member, allyOwnerRole).queue();
                                        guild.removeRoleFromMember(member, memberOfRole).queue();
                                        guild.removeRoleFromMember(member, allyRole).queue();
                                        guild.removeRoleFromMember(member, ownerRole).queue();
                                        guild.removeRoleFromMember(member, chiefRole).queue();
                                        guild.removeRoleFromMember(member, strategistRole).queue();
                                        guild.removeRoleFromMember(member, captainRole).queue();
                                        guild.removeRoleFromMember(member, recruiterRole).queue();
                                        guild.removeRoleFromMember(member, recruitRole).queue();
                                        hasUpdated = true;
                                    }
                                }
                                case CHIEF, STRATEGIST, CAPTAIN, RECRUITER, RECRUIT -> {
                                    if (!hasRole(member, allyRole)) {
                                        System.out.println(member.getUser().getName() + " is a member of an Ally Guild.");
                                        guild.addRoleToMember(member, allyRole).queue();
                                        guild.removeRoleFromMember(member, memberOfRole).queue();
                                        guild.removeRoleFromMember(member, allyOwnerRole).queue();
                                        guild.removeRoleFromMember(member, ownerRole).queue();
                                        guild.removeRoleFromMember(member, chiefRole).queue();
                                        guild.removeRoleFromMember(member, strategistRole).queue();
                                        guild.removeRoleFromMember(member, captainRole).queue();
                                        guild.removeRoleFromMember(member, recruiterRole).queue();
                                        guild.removeRoleFromMember(member, recruitRole).queue();
                                        hasUpdated = true;
                                    }
                                }
                            }

                            WynncraftPlayer player = wynnAPI.v2().player().statsUUID(uuid).run()[0];

                            switch(player.getMeta().getTag().getValue()) {
                                case CHAMPION -> {
                                    if (!hasRole(member, championRole)) {
                                        System.out.println(member.getUser().getName() + " is a CHAMPION.");
                                        guild.addRoleToMember(member, championRole).queue();
                                        guild.removeRoleFromMember(member, heroRole).queue();
                                        guild.removeRoleFromMember(member, vipPlusRole).queue();
                                        guild.removeRoleFromMember(member, vipRole).queue();
                                        hasUpdated = true;
                                    }
                                }
                                case HERO -> {
                                    if (!hasRole(member, heroRole)) {
                                        System.out.println(member.getUser().getName() + " is a HERO.");
                                        guild.addRoleToMember(member, heroRole).queue();
                                        guild.removeRoleFromMember(member, championRole).queue();
                                        guild.removeRoleFromMember(member, vipPlusRole).queue();
                                        guild.removeRoleFromMember(member, vipRole).queue();
                                        hasUpdated = true;
                                    }
                                }
                                case VIPPLUS -> {
                                    if (!hasRole(member, vipPlusRole)) {
                                        System.out.println(member.getUser().getName() + " is a VIP+.");
                                        guild.addRoleToMember(member, vipPlusRole).queue();
                                        guild.removeRoleFromMember(member, championRole).queue();
                                        guild.removeRoleFromMember(member, heroRole).queue();
                                        guild.removeRoleFromMember(member, vipRole).queue();
                                        hasUpdated = true;
                                    }
                                }
                                case VIP -> {
                                    if (!hasRole(member, vipRole)) {
                                        System.out.println(member.getUser().getName() + " is a VIP.");
                                        guild.addRoleToMember(member, vipRole).queue();
                                        guild.removeRoleFromMember(member, championRole).queue();
                                        guild.removeRoleFromMember(member, heroRole).queue();
                                        guild.removeRoleFromMember(member, vipPlusRole).queue();
                                        hasUpdated = true;
                                    }
                                }
                            }

                            if (player.getMeta().isVeteran()) {
                                if (!hasRole(member, vetRole)) {
                                    System.out.println(member.getUser().getName() + " is a Vet.");
                                    guild.addRoleToMember(member, vetRole).queue();
                                    hasUpdated = true;
                                }
                            }

                            if (hasUpdated) {
                                rolesUpdated += 1;
                            }

                            discordMembers.remove(member);

                            break;
                        }
                    }
                }
            }
        }

        return "Updated roles for " + rolesUpdated + " members!";
    }

    private String verify(String playerName, Guild guild, Member member) {
        File guildFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "guild.txt");
        File allyFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "allies.txt");

        String guildName = "";

        try {
            Scanner scanner = new Scanner(guildFile);

            if (scanner.hasNextLine()) {
                guildName = scanner.nextLine();
            }

            scanner.close();
        } catch (java.io.IOException ex) {
            return "Make sure to set your guild with /setguild <guildname>.";
        }

        List<WynncraftGuild> allyGuilds = new ArrayList<WynncraftGuild>();

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
            return "Make sure to set your guild allies with /addally <guildname>.";
        }

        WynncraftGuild mainGuild = wynnAPI.v1().guildStats(guildName).run();

        boolean verified = false;
        boolean isAlly = false;

        for (int i = 0; i < mainGuild.getMembers().length; i++) {
            if (mainGuild.getMembers()[i].getName().equalsIgnoreCase(playerName)) {
                verified = true;

                guild.addRoleToMember(member, memberOfRole).queue();
                guild.removeRoleFromMember(member, unverifiedRole).queue();
                guild.removeRoleFromMember(member, allyRole).queue();
                guild.removeRoleFromMember(member, allyOwnerRole).queue();

                String uuid = mainGuild.getMembers()[i].getUuid();

                switch (mainGuild.getMembers()[i].getRank()) {
                    case OWNER -> {
                        if (!hasRole(member, ownerRole)) {
                            System.out.println(member.getUser().getName() + " is the Owner of the Guild.");
                            guild.addRoleToMember(member, ownerRole).queue();
                            guild.removeRoleFromMember(member, chiefRole).queue();
                            guild.removeRoleFromMember(member, strategistRole).queue();
                            guild.removeRoleFromMember(member, captainRole).queue();
                            guild.removeRoleFromMember(member, recruiterRole).queue();
                            guild.removeRoleFromMember(member, recruitRole).queue();
                        }
                    }
                    case CHIEF -> {
                        if (!hasRole(member, chiefRole)) {
                            System.out.println(member.getUser().getName() + " is a Chief of the Guild.");
                            guild.addRoleToMember(member, chiefRole).queue();
                            guild.removeRoleFromMember(member, ownerRole).queue();
                            guild.removeRoleFromMember(member, strategistRole).queue();
                            guild.removeRoleFromMember(member, captainRole).queue();
                            guild.removeRoleFromMember(member, recruiterRole).queue();
                            guild.removeRoleFromMember(member, recruitRole).queue();
                        }
                    }
                    case STRATEGIST -> {
                        if (!hasRole(member, strategistRole)) {
                            System.out.println(member.getUser().getName() + " is a Strategist of the Guild.");
                            guild.addRoleToMember(member, strategistRole).queue();
                            guild.removeRoleFromMember(member, ownerRole).queue();
                            guild.removeRoleFromMember(member, chiefRole).queue();
                            guild.removeRoleFromMember(member, captainRole).queue();
                            guild.removeRoleFromMember(member, recruiterRole).queue();
                            guild.removeRoleFromMember(member, recruitRole).queue();
                        }
                    }
                    case CAPTAIN -> {
                        if (!hasRole(member, captainRole)) {
                            System.out.println(member.getUser().getName() + " is a Captain of the Guild.");
                            guild.addRoleToMember(member, captainRole).queue();
                            guild.removeRoleFromMember(member, ownerRole).queue();
                            guild.removeRoleFromMember(member, chiefRole).queue();
                            guild.removeRoleFromMember(member, strategistRole).queue();
                            guild.removeRoleFromMember(member, recruiterRole).queue();
                            guild.removeRoleFromMember(member, recruitRole).queue();
                        }
                    }
                    case RECRUITER -> {
                        if (!hasRole(member, recruiterRole)) {
                            System.out.println(member.getUser().getName() + " is a Recruiter of the Guild.");
                            guild.addRoleToMember(member, recruiterRole).queue();
                            guild.removeRoleFromMember(member, ownerRole).queue();
                            guild.removeRoleFromMember(member, chiefRole).queue();
                            guild.removeRoleFromMember(member, strategistRole).queue();
                            guild.removeRoleFromMember(member, captainRole).queue();
                            guild.removeRoleFromMember(member, recruitRole).queue();
                        }
                    }
                    case RECRUIT -> {
                        if (!hasRole(member, recruitRole)) {
                            System.out.println(member.getUser().getName() + " is a Recruit of the Guild.");
                            guild.addRoleToMember(member, recruitRole).queue();
                            guild.removeRoleFromMember(member, ownerRole).queue();
                            guild.removeRoleFromMember(member, chiefRole).queue();
                            guild.removeRoleFromMember(member, strategistRole).queue();
                            guild.removeRoleFromMember(member, captainRole).queue();
                            guild.removeRoleFromMember(member, recruiterRole).queue();
                        }
                    }
                }

                WynncraftPlayer player = wynnAPI.v2().player().statsUUID(uuid).run()[0];

                switch(player.getMeta().getTag().getValue()) {
                    case CHAMPION -> {
                        if (!hasRole(member, championRole)) {
                            System.out.println(member.getUser().getName() + " is a CHAMPION.");
                            guild.addRoleToMember(member, championRole).queue();
                            guild.removeRoleFromMember(member, heroRole).queue();
                            guild.removeRoleFromMember(member, vipPlusRole).queue();
                            guild.removeRoleFromMember(member, vipRole).queue();
                        }
                    }
                    case HERO -> {
                        if (!hasRole(member, heroRole)) {
                            System.out.println(member.getUser().getName() + " is a HERO.");
                            guild.addRoleToMember(member, heroRole).queue();
                            guild.removeRoleFromMember(member, championRole).queue();
                            guild.removeRoleFromMember(member, vipPlusRole).queue();
                            guild.removeRoleFromMember(member, vipRole).queue();
                        }
                    }
                    case VIPPLUS -> {
                        if (!hasRole(member, vipPlusRole)) {
                            System.out.println(member.getUser().getName() + " is a VIP+.");
                            guild.addRoleToMember(member, vipPlusRole).queue();
                            guild.removeRoleFromMember(member, championRole).queue();
                            guild.removeRoleFromMember(member, heroRole).queue();
                            guild.removeRoleFromMember(member, vipRole).queue();
                        }
                    }
                    case VIP -> {
                        if (!hasRole(member, vipRole)) {
                            System.out.println(member.getUser().getName() + " is a VIP.");
                            guild.addRoleToMember(member, vipRole).queue();
                            guild.removeRoleFromMember(member, championRole).queue();
                            guild.removeRoleFromMember(member, heroRole).queue();
                            guild.removeRoleFromMember(member, vipPlusRole).queue();
                        }
                    }
                }

                if (player.getMeta().isVeteran()) {
                    if (!hasRole(member, vetRole)) {
                        System.out.println(member.getUser().getName() + " is a Vet.");
                        guild.addRoleToMember(member, vetRole).queue();
                    }
                }

                break;
            }
        }

        if (!verified) {
            for (int i = 0; i < allyGuilds.size(); i++) {
                for (int j = 0; j < allyGuilds.get(i).getMembers().length; j++) {
                    if (allyGuilds.get(i).getMembers()[j].getName().equalsIgnoreCase(playerName)) {
                        verified = true;
                        isAlly = true;

                        guild.removeRoleFromMember(member, memberOfRole).queue();
                        guild.removeRoleFromMember(member, unverifiedRole).queue();

                        String uuid = allyGuilds.get(i).getMembers()[j].getUuid();

                        switch (allyGuilds.get(i).getMembers()[j].getRank()) {
                            case OWNER -> {
                                if (!hasRole(member, allyOwnerRole)) {
                                    System.out.println(member.getUser().getName() + " is the Owner of an Ally Guild.");
                                    guild.addRoleToMember(member, allyOwnerRole).queue();
                                    guild.removeRoleFromMember(member, memberOfRole).queue();
                                    guild.removeRoleFromMember(member, allyRole).queue();
                                    guild.removeRoleFromMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, recruiterRole).queue();
                                    guild.removeRoleFromMember(member, recruitRole).queue();
                                }
                            }
                            case CHIEF, STRATEGIST, CAPTAIN, RECRUITER, RECRUIT -> {
                                if (!hasRole(member, allyRole)) {
                                    System.out.println(member.getUser().getName() + " is a member of an Ally Guild.");
                                    guild.addRoleToMember(member, allyRole).queue();
                                    guild.removeRoleFromMember(member, memberOfRole).queue();
                                    guild.removeRoleFromMember(member, allyOwnerRole).queue();
                                    guild.removeRoleFromMember(member, ownerRole).queue();
                                    guild.removeRoleFromMember(member, chiefRole).queue();
                                    guild.removeRoleFromMember(member, strategistRole).queue();
                                    guild.removeRoleFromMember(member, captainRole).queue();
                                    guild.removeRoleFromMember(member, recruiterRole).queue();
                                    guild.removeRoleFromMember(member, recruitRole).queue();
                                }
                            }
                        }

                        WynncraftPlayer player = wynnAPI.v2().player().statsUUID(uuid).run()[0];

                        switch(player.getMeta().getTag().getValue()) {
                            case CHAMPION -> {
                                if (!hasRole(member, championRole)) {
                                    System.out.println(member.getUser().getName() + " is a CHAMPION.");
                                    guild.addRoleToMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, vipPlusRole).queue();
                                    guild.removeRoleFromMember(member, vipRole).queue();
                                }
                            }
                            case HERO -> {
                                if (!hasRole(member, heroRole)) {
                                    System.out.println(member.getUser().getName() + " is a HERO.");
                                    guild.addRoleToMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, vipPlusRole).queue();
                                    guild.removeRoleFromMember(member, vipRole).queue();
                                }
                            }
                            case VIPPLUS -> {
                                if (!hasRole(member, vipPlusRole)) {
                                    System.out.println(member.getUser().getName() + " is a VIP+.");
                                    guild.addRoleToMember(member, vipPlusRole).queue();
                                    guild.removeRoleFromMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, vipRole).queue();
                                }
                            }
                            case VIP -> {
                                if (!hasRole(member, vipRole)) {
                                    System.out.println(member.getUser().getName() + " is a VIP.");
                                    guild.addRoleToMember(member, vipRole).queue();
                                    guild.removeRoleFromMember(member, championRole).queue();
                                    guild.removeRoleFromMember(member, heroRole).queue();
                                    guild.removeRoleFromMember(member, vipPlusRole).queue();
                                }
                            }
                        }

                        if (player.getMeta().isVeteran()) {
                            if (!hasRole(member, vetRole)) {
                                System.out.println(member.getUser().getName() + " is a Vet.");
                                guild.addRoleToMember(member, vetRole).queue();
                            }
                        }

                        break;
                    }
                }
            }
        }

        if (!verified) {
            if(!hasRole(member, unverifiedRole)) {
                guild.addRoleToMember(member, unverifiedRole).queue();
                guild.removeRoleFromMember(member, vipRole).queue();
                guild.removeRoleFromMember(member, vipPlusRole).queue();
                guild.removeRoleFromMember(member, heroRole).queue();
                guild.removeRoleFromMember(member, championRole).queue();
                guild.removeRoleFromMember(member, vetRole).queue();
                guild.removeRoleFromMember(member, recruitRole).queue();
                guild.removeRoleFromMember(member, recruiterRole).queue();
                guild.removeRoleFromMember(member, captainRole).queue();
                guild.removeRoleFromMember(member, strategistRole).queue();
                guild.removeRoleFromMember(member, chiefRole).queue();
                guild.removeRoleFromMember(member, ownerRole).queue();
                guild.removeRoleFromMember(member, memberOfRole).queue();
                guild.removeRoleFromMember(member, allyRole).queue();
                guild.removeRoleFromMember(member, allyOwnerRole).queue();
            }

            return playerName + " is not a member of Chiefs Of Corkus or its allies.";
        }

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

    private String setGuild(String guildName, Guild guild) {
        try {
            Files.createDirectories(Path.of("/home/opc/CC-117/" + guild.getId()));

            File guildFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "guild.txt");

            if (!guildFile.exists()) {
                guildFile.createNewFile();
            }

            FileWriter guildFileWriter = new FileWriter("/home/opc/CC-117/" + guild.getId() + "/" + "guild.txt");
            guildFileWriter.write(guildName);
            guildFileWriter.close();
        } catch (java.io.IOException ex) {
            return ex.toString();
        }

        return "Set " + guildName + " as Guild.";
    }

    private String addAlly(String guildName, Guild guild) {
        try {
            Files.createDirectories(Path.of("/home/opc/CC-117/" + guild.getId()));

            File allyFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "allies.txt");

            if (!allyFile.exists()) {
                allyFile.createNewFile();
            }

            Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "allies.txt"), (guildName + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (java.io.IOException ex) {
            return ex.toString();
        }

        return "Added " + guildName + " as an Ally.";
    }

    private String removeAlly(String guildName, Guild guild) {
        File allyFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "allies.txt");
        File tempFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt");

        try {
            Scanner scanner = new Scanner(allyFile);
            String currentLine;

            tempFile.createNewFile();

            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                if (!currentLine.equals(guildName)) {
                    Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt"), (currentLine + "\n").getBytes(), StandardOpenOption.APPEND);
                }
            }

            scanner.close();

            allyFile.delete();

            tempFile.renameTo(allyFile);

        } catch (java.io.IOException ex) {
            return "No allies found: " + ex;
        }

        return "Removed " + guildName + " as an Ally.";
    }

    private boolean hasRole(Member member, Role role) {
        List<Role> memberRoles = member.getRoles();
        return memberRoles.contains(role);
    }
}
