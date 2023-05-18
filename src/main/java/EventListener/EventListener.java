package EventListener;

import MessageObjects.GuildAverageMembers;
import MessageObjects.PlayerLastLogin;
import MessageObjects.PossibleGuilds;
import MessageType.ButtonedMessage;
import MessageType.MessageType;
import me.bed0.jWynn.WynncraftAPI;
import me.bed0.jWynn.api.common.GuildRank;
import me.bed0.jWynn.api.v1.guild.WynncraftGuild;
import me.bed0.jWynn.api.v1.guild.WynncraftGuildMember;
import me.bed0.jWynn.api.v1.network.WynncraftOnlinePlayers;
import me.bed0.jWynn.api.v1.network.WynncraftServerOnlinePlayers;
import me.bed0.jWynn.api.v2.player.WynncraftPlayer;
import me.bed0.jWynn.exceptions.APIRateLimitExceededException;
import me.bed0.jWynn.exceptions.APIRequestException;
import me.bed0.jWynn.exceptions.APIResponseException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.*;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.*;
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
    File prefixFile;
    WynncraftGuild mainGuild;
    String guildName = "";
    List<WynncraftGuild> allyGuilds = new ArrayList<>();
    List<Role> rolesToAdd = new ArrayList<>();
    List<Role> rolesToRemove = new ArrayList<>();
    WynncraftPlayer player;
    List<ButtonedMessage> buttonedMessages = new ArrayList<>();
    private WynncraftOnlinePlayers onlineServers;
    private List<PossibleGuilds> possibleGuilds = new ArrayList<>();

    /**
     * When bot starts in a server, set up file paths and start the thread to run updateOnlineAverage and
     * updateRanks every hour. Also remove old messages from the pagedMessages list.
     * @param event The event when a bot is ready in a guild.
     */
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        super.onGuildReady(event);

        guildFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "guild.txt");
        allyFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "allies.txt");
        trackedFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "tracked.txt");
        tempFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "temp.txt");
        prefixFile = new File("/home/opc/CC-117/" + event.getGuild().getId() + "/" + "prefixes.txt");

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

        //Run once on start
        updateOnlinePlayers(event.getGuild());

        try {
            //Run this every hour
            ScheduledExecutorService updateExecutor = Executors.newSingleThreadScheduledExecutor();
            updateExecutor.scheduleWithFixedDelay(() -> {
                int currentMinute = Instant.now().atZone(ZoneOffset.UTC).getMinute();

                //If currently on an exact hour, eg 13:00, 23:00 etc then enter the if statement.
                if (currentMinute == 0) {
                    //Thread to run the updateRanks and updateOnlineAverage methods.
                    Thread updateRanks = new Thread(() -> {
                        System.out.println("Running thread at " + Instant.now().atZone(ZoneOffset.UTC).getHour() + ":" + Instant.now().atZone(ZoneOffset.UTC).getMinute() + " on " + Instant.now().atZone(ZoneOffset.UTC).getDayOfMonth() + "/" + Instant.now().atZone(ZoneOffset.UTC).getMonthValue() + "/" + Instant.now().atZone(ZoneOffset.UTC).getYear());
                        String response = updateRanks(event.getGuild());
                        //Run this here to ensure it's updated before online average is calculated again.
                        updateOnlinePlayers(event.getGuild());
                        updateOnlineAverage(event.getGuild());

                        if (!response.equals("Updated roles for 0 members!")) {
                            TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");

                            if (channel != null) {
                                channel.sendMessage(response).queue();
                            } else {
                                System.out.println("Unable to find channel with ID: 1061698530651144212");
                            }
                        } else {
                            System.out.println(response);
                        }

                        RemoveOldButtons();

                        System.out.println("Finished running thread at " + Instant.now().atZone(ZoneOffset.UTC).getHour() + ":" + Instant.now().atZone(ZoneOffset.UTC).getMinute() + " on " + Instant.now().atZone(ZoneOffset.UTC).getDayOfMonth() + "/" + Instant.now().atZone(ZoneOffset.UTC).getMonthValue() + "/" + Instant.now().atZone(ZoneOffset.UTC).getYear());
                    });

                    //Runs the thread.
                    updateRanks.start();
                }

                //If at xx:20 or xx:40 run this
                if (currentMinute == 10 || currentMinute == 20 || currentMinute == 30 || currentMinute == 40 || currentMinute == 50) {
                    Thread updateOnlinePlayers = new Thread(() -> {
                        updateOnlinePlayers(event.getGuild());

                        System.out.println("Updating online players at: " + Instant.now().atZone(ZoneOffset.UTC).getHour() + ":" + Instant.now().atZone(ZoneOffset.UTC).getMinute() + " on " + Instant.now().atZone(ZoneOffset.UTC).getDayOfMonth() + "/" + Instant.now().atZone(ZoneOffset.UTC).getMonthValue() + "/" + Instant.now().atZone(ZoneOffset.UTC).getYear());
                    });

                    //Runs the thread.
                    updateOnlinePlayers.start();
                }
            }, 0, 1, TimeUnit.MINUTES);
        } catch (APIResponseException ex) {
            ex.printStackTrace();

            TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");

            String message = "Response exception, guild probably no longer exists.";

            sendLogFile(ex, event.getGuild(), channel, message);
        } catch (APIRateLimitExceededException ex) {
            ex.printStackTrace();

            TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");

            String message = "Rate limit exceeded.";

            sendLogFile(ex, event.getGuild(), channel, message);
        } catch (Exception ex) {
            ex.printStackTrace();

            TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");

            String message = "Something broke :(";

            sendLogFile(ex, event.getGuild(), channel, message);
        }
    }

    private void sendLogFile(Exception exception, Guild guild, TextChannel channel, String message) {
        File logFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "logs.txt");

        FileOutputStream file = null;

        try {
            file = new FileOutputStream(logFile);

            PrintStream output = new PrintStream(file);

            exception.printStackTrace(output);

            if (channel != null) {
                FileUpload upload = FileUpload.fromData(new FileInputStream("/home/opc/CC-117/" + guild.getId() + "/" + "logs.txt"), "logs.txt");
                channel.sendMessage("Response exception, guild probably deleted").addFiles(upload).queue();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        TextChannel newChannel = event.getGuild().getTextChannelById("958833936186888262");

        if (verifyChannel != null) {
            //Send message to the verify channel mentioning the new member, then delete the message after 10 seconds.
            verifyChannel.sendMessage(event.getMember().getAsMention()).queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        } else {
            System.out.println("Could not locate verify channel with ID: 1061730979913404506");
        }

        if (newChannel != null) {
            //Send welcome message to the welcome channel.
            newChannel.sendMessage(event.getMember().getAsMention() + ", welcome to the official Chiefs Of Corkus guild Discord server! Watch out for the Mechs!").queue();
        } else {
            System.out.println("Could not locate verify channel with ID: 958833936186888262");
        }
    }

    /**
     * When a member of the Discord server leaves or is removed, send a message to the channel.
     * @param event The remove event.
     */
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        TextChannel newChannel = event.getGuild().getTextChannelById("958833936186888262");

        if (event.getMember() != null) {
            if (newChannel != null) {
                newChannel.sendMessage("**" + event.getMember().getEffectiveName() + "** fled from the mechs.").queue();
            } else {
                System.out.println("Could not locate verify channel with ID: 958833936186888262");
            }
        }
    }

    /**
     * Handle Discord members changing their nicknames to ensure they aren't changing to someone who already
     * exists in the server.
     * @param event Nickname change event.
     */
    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        //Check if the nickname they want is in use.
        if (!isValidNickname(event.getGuild(), event.getMember(), event.getNewNickname())) {
            //If the nickname they wanted is already in use, either change back to their old nickname or change to
            //their username if they didn't have a nickname before.
            if (event.getOldNickname() != null) {
                event.getMember().modifyNickname(event.getOldNickname()).queue();
            } else {
                event.getMember().modifyNickname(event.getMember().getUser().getName()).queue();
            }
        }
    }

    /**
     * Determine if a nickname is in use or not, also used for /verify to check they aren't verifying as someone
     * who already exists in the server.
     * @param guild The current Discord server.
     * @param member The member whose name is being changed.
     * @param newNickname The name the member claims to be.
     * @return True if the nickname is unique, false otherwise.
     */
    private boolean isValidNickname(Guild guild, Member member, String newNickname) {
        List<Member> discordMembers = guild.getMembers();

        //Check every member of the server.
        for (Member discordMember : discordMembers) {
            //Only check against members that aren't themselves.
            if (discordMember != member) {
                //Get their nickname.
                String nickname = null;

                if (discordMember.getNickname() != null) {
                    nickname = discordMember.getNickname();
                }

                //If the nickname they want equals someone else nickname or username, then return false.
                if (nickname == null) {
                    if (discordMember.getUser().getName().equalsIgnoreCase(newNickname)) {
                        return false;
                    }
                } else {
                    if (nickname.equalsIgnoreCase(newNickname)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Handles slash commands.
     * @param event Slash command event.
     */
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

                    //Check that the player they want to be verified as doesn't already exist.
                    if (!isValidNickname(event.getGuild(), event.getMember(), playerName)) {
                        event.getHook().sendMessage("The name " + playerName + " is already verified, if you believe this is a mistake please message Owen_Rocks_3 or ShadowCat117.").setEphemeral(true).queue();
                    } else {
                        String response = verify(playerName, event.getGuild(), event.getMember());
                        event.getHook().sendMessage(response).setEphemeral(true).queue();
                        TextChannel channel = event.getGuild().getTextChannelById("1061698530651144212");

                        if (channel != null) {
                            channel.sendMessage(response).queue();
                        } else {
                            System.out.println("Unable to find channel with ID: 1061698530651144212");
                        }
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
                        ButtonedMessage setGuildMessage = setGuild(guildNameOption.getAsString(), event.getGuild());

                        if (setGuildMessage.getComponentIds().isEmpty()) {
                            event.getHook().sendMessage(setGuildMessage.getText()).queue();
                        } else {
                            List<ItemComponent> components = new ArrayList<>();

                            for (String componentId : setGuildMessage.getComponentIds()) {
                                components.add(Button.primary(componentId, findGuildTag(componentId)));
                            }

                            event.getHook().sendMessage(setGuildMessage.getText()).addActionRow(components).queue(setGuildMessage::setMessage);

                            buttonedMessages.add(setGuildMessage);
                        }
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
                        ButtonedMessage addAllyMessage = addAlly(addAllyNameOption.getAsString(), event.getGuild());

                        if (addAllyMessage.getComponentIds().isEmpty()) {
                            event.getHook().sendMessage(addAllyMessage.getText()).queue();
                        } else {
                            List<ItemComponent> components = new ArrayList<>();

                            for (String componentId : addAllyMessage.getComponentIds()) {
                                components.add(Button.primary(componentId, findGuildTag(componentId)));
                            }

                            event.getHook().sendMessage(addAllyMessage.getText()).addActionRow(components).queue(addAllyMessage::setMessage);

                            buttonedMessages.add(addAllyMessage);
                        }
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
                        ButtonedMessage removeAllyMessage = removeAlly(removeAllyNameOption.getAsString(), event.getGuild());

                        if (removeAllyMessage.getComponentIds().isEmpty()) {
                            event.getHook().sendMessage(removeAllyMessage.getText()).queue();
                        } else {
                            List<ItemComponent> components = new ArrayList<>();

                            for (String componentId : removeAllyMessage.getComponentIds()) {
                                components.add(Button.primary(componentId, findGuildTag(componentId)));
                            }

                            event.getHook().sendMessage(removeAllyMessage.getText()).addActionRow(components).queue(removeAllyMessage::setMessage);

                            buttonedMessages.add(removeAllyMessage);
                        }
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
                        ButtonedMessage trackGuildMessage = trackGuild(trackGuildNameOption.getAsString(), event.getGuild());

                        if (trackGuildMessage.getComponentIds().isEmpty()) {
                            event.getHook().sendMessage(trackGuildMessage.getText()).queue();
                        } else {
                            List<ItemComponent> components = new ArrayList<>();

                            for (String componentId : trackGuildMessage.getComponentIds()) {
                                components.add(Button.primary(componentId, findGuildTag(componentId)));
                            }

                            event.getHook().sendMessage(trackGuildMessage.getText()).addActionRow(components).queue(trackGuildMessage::setMessage);

                            buttonedMessages.add(trackGuildMessage);
                        }
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
                        ButtonedMessage untrackGuildMessage = untrackGuild(untrackGuildNameOption.getAsString(), event.getGuild());

                        if (untrackGuildMessage.getComponentIds().isEmpty()) {
                            event.getHook().sendMessage(untrackGuildMessage.getText()).queue();
                        } else {
                            List<ItemComponent> components = new ArrayList<>();

                            for (String componentId : untrackGuildMessage.getComponentIds()) {
                                components.add(Button.primary(componentId, findGuildTag(componentId)));
                            }

                            event.getHook().sendMessage(untrackGuildMessage.getText()).addActionRow(components).queue(untrackGuildMessage::setMessage);

                            buttonedMessages.add(untrackGuildMessage);
                        }
                    } else {
                        event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }

            case "trackedguilds" -> {
                event.deferReply().queue();

                ButtonedMessage pagedMessage = trackedGuilds();
                event.getHook().sendMessage(pagedMessage.getPage(0))
                        .addActionRow(
                                Button.primary("previousPage", Emoji.fromFormatted("⬅️")),
                                Button.primary("nextPage", Emoji.fromFormatted("➡️"))
                        )
                        .queue((pagedMessage::setMessage));

                buttonedMessages.add(pagedMessage);
            }

            case "activehours" -> {
                event.deferReply().queue();

                OptionMapping activeHoursGuildNameOption = event.getOption("guild_name");
                if (activeHoursGuildNameOption != null) {
                    OptionMapping timezoneOption = event.getOption("timezone");

                    ButtonedMessage activeHoursMessage;

                    if (timezoneOption == null) {
                        activeHoursMessage = activeHours(activeHoursGuildNameOption.getAsString(), "UTC", event);
                    } else {
                        activeHoursMessage = activeHours(activeHoursGuildNameOption.getAsString(), timezoneOption.getAsString(), event);
                    }

                    if (activeHoursMessage.getComponentIds().isEmpty()) {
                        event.getHook().sendMessage(activeHoursMessage.getText()).queue();
                    } else {
                        List<ItemComponent> components = new ArrayList<>();

                        for (String componentId : activeHoursMessage.getComponentIds()) {
                            components.add(Button.primary(componentId, findGuildTag(componentId)));
                        }

                        event.getHook().sendMessage(activeHoursMessage.getText()).addActionRow(components).queue(activeHoursMessage::setMessage);

                        buttonedMessages.add(activeHoursMessage);
                    }
                } else {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }
            }

            case "lastlogins" -> {
                event.deferReply().queue();
                OptionMapping lastLoginsGuildNameOption = event.getOption("guild_name");
                if (lastLoginsGuildNameOption != null) {
                    ButtonedMessage lastLoginsMessage = lastLogins(lastLoginsGuildNameOption.getAsString());

                    if (lastLoginsMessage.getComponentIds().isEmpty()) {
                        if (lastLoginsMessage.getPages().size() > 1) {
                            event.getHook().sendMessage(lastLoginsMessage.getPage(0))
                                    .addActionRow(
                                            Button.primary("previousPage", Emoji.fromFormatted("⬅️")),
                                            Button.primary("nextPage", Emoji.fromFormatted("➡️"))
                                    )
                                    .queue((lastLoginsMessage::setMessage));
                        } else {
                            event.getHook().sendMessage(lastLoginsMessage.getPage(0)).queue((lastLoginsMessage::setMessage));
                        }
                    } else {
                        List<ItemComponent> components = new ArrayList<>();

                        for (String componentId : lastLoginsMessage.getComponentIds()) {
                            components.add(Button.primary(componentId, findGuildTag(componentId)));
                        }

                        event.getHook().sendMessage(lastLoginsMessage.getText()).addActionRow(components).queue(lastLoginsMessage::setMessage);
                    }

                    buttonedMessages.add(lastLoginsMessage);
                } else {
                    event.getHook().sendMessage("Please enter a Guild name.").setEphemeral(true).queue();
                }
            }

            case "updateprefixes" -> {
                if (canUseCommand(event.getMember())) {
                    event.deferReply().queue();
                    event.getHook().sendMessage(updateGuildPrefixes(event.getGuild())).queue();
                } else {
                    event.reply("Sorry you must be a Chief to use this command").setEphemeral(true).queue();
                }
            }

            default -> event.reply("Unknown command.").setEphemeral(true).queue();
        }
    }

    /**
     * Removes buttons from messages with them.
     */
    private void RemoveOldButtons() {
        for (ButtonedMessage bm : buttonedMessages) {
            bm.getMessage().editMessage(bm.getMessage().getContentRaw()).setComponents().queue();
        }

        buttonedMessages.clear();
    }

    /**
     * When a button on a message is clicked.
     * @param event Button interaction event.
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getMessageId();
        ButtonedMessage currentMessage = null;

        //Find the PagedMessage object for the interacted message.
        for (ButtonedMessage bm : buttonedMessages) {
            if (bm.getMessage().getId().equals(id)) {
                currentMessage = bm;
                break;
            }
        }

        //If the message is older than an hour, or bot has been restarted since original command
        //was run, display this message.
        if (currentMessage == null) {
            event.editMessage("Data expired.").setComponents().queue();
            return;
        }

        //Get new page of contents.
        if (event.getComponentId().equals("nextPage")) {
            if (currentMessage.pageCount() == 1) {
                event.editMessage(currentMessage.getPage(0)).queue();
            } else if (currentMessage.getCurrentPage() < currentMessage.pageCount() - 1) {
                event.editMessage(currentMessage.getPage(currentMessage.getCurrentPage() + 1)).queue();
                currentMessage.setCurrentPage(currentMessage.getCurrentPage() + 1);
            } else if (currentMessage.getCurrentPage() == currentMessage.pageCount() - 1) {
                event.editMessage(currentMessage.getPage(0)).queue();
                currentMessage.setCurrentPage(0);
            }
        } else if (event.getComponentId().equals("previousPage")) {
            if (currentMessage.pageCount() == 1) {
                event.editMessage(currentMessage.getPage(0)).queue();
            } else if (currentMessage.getCurrentPage() > 0) {
                event.editMessage(currentMessage.getPage(currentMessage.getCurrentPage() - 1)).queue();
                currentMessage.setCurrentPage(currentMessage.getCurrentPage() - 1);
            } else if (currentMessage.getCurrentPage() == 0) {
                event.editMessage(currentMessage.getPage(currentMessage.pageCount() - 1)).queue();
                currentMessage.setCurrentPage(currentMessage.pageCount() - 1);
            }
        } else {
            switch (currentMessage.getMessageType()) {
                case ACTIVE_HOURS -> event.editMessage(currentMessage.getPageByGuild(event.getComponentId())).setComponents().queue();
                case ADD_ALLY -> event.editMessage(addAlly(event.getComponentId(), event.getGuild()).getText()).setComponents().queue();
                case LAST_LOGINS -> {
                    currentMessage.getMessage().editMessage("Getting last logins for " + event.getComponentId() + "..").setComponents().queue();
                    event.deferEdit().queue();
                    buttonedMessages.remove(currentMessage);
                    ButtonedMessage guildLastLogins = lastLogins(event.getComponentId());
                    buttonedMessages.add(guildLastLogins);

                    guildLastLogins.setMessage(currentMessage.getMessage());

                    if (guildLastLogins.getPages().size() > 1) {
                        List<ItemComponent> components = new ArrayList<>();

                        components.add(Button.primary("previousPage", Emoji.fromFormatted("⬅️")));
                        components.add(Button.primary("nextPage", Emoji.fromFormatted("➡️")));

                        event.getHook().editOriginal(guildLastLogins.getPage(0)).setActionRow(components).queue();
                    } else {
                        event.getHook().editOriginal(guildLastLogins.getPage(0)).queue();
                    }
                }
                case REMOVE_ALLY -> event.editMessage(removeAlly(event.getComponentId(), event.getGuild()).getText()).setComponents().queue();
                case SET_GUILD -> event.editMessage(setGuild(event.getComponentId(), event.getGuild()).getText()).setComponents().queue();
                case TRACK_GUILD -> event.editMessage(trackGuild(event.getComponentId(), event.getGuild()).getText()).setComponents().queue();
                case UNTRACK_GUILD -> event.editMessage(untrackGuild(event.getComponentId(), event.getGuild()).getText()).setComponents().queue();
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
        System.out.println("Updating ranks");
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
                        nick = nick.split(" \\[")[0];
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
                        if (!member.getEffectiveName().equals(mainGuild.getMembers()[i].getName())) {
                            try {
                                member.modifyNickname(mainGuild.getMembers()[i].getName()).queue();
                                System.out.println("Verified " + member.getUser().getName() + " as Guild member " + mainGuild.getMembers()[i].getName());
                            } catch (HierarchyException e) {
                                System.out.println("Verified " + member.getUser().getName() + " as Guild member " + mainGuild.getMembers()[i].getName() + "(Could not change nickname)");
                            }
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
                        hasUpdated = setPlayerRoles(hasUpdated, member, uuid);

                        //Update their Discord roles.
                        guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();

                        //Remove from the list as they no longer need to be verified.
                        discordMembers.remove(member);

                        if (hasUpdated) {
                            rolesUpdated++;
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
                            nick = nick.split(" \\[")[0];
                        }

                        if (allyGuild.getMembers()[j].getName().equalsIgnoreCase(member.getUser().getName()) || allyGuild.getMembers()[j].getName().toLowerCase().equalsIgnoreCase(nick)) {
                            //Found matching member.
                            rolesToAdd.clear();
                            rolesToRemove.clear();

                            rolesToRemove.add(memberOfRole);
                            rolesToRemove.add(unverifiedRole);

                            String suffix = findGuildTag(allyGuild.getName());

                            //Attempt to change nickname.
                            if (!member.getEffectiveName().equals(allyGuild.getMembers()[j].getName() + " [" + suffix + "]")) {
                                try {
                                    member.modifyNickname(allyGuild.getMembers()[j].getName() + " [" + suffix + "]").queue();
                                    System.out.println("Verified " + member.getUser().getName() + " as Ally Guild member " + allyGuild.getMembers()[j].getName());
                                } catch (HierarchyException e) {
                                    System.out.println("Verified " + member.getUser().getName() + " as Ally Guild member " + allyGuild.getMembers()[j].getName() + "(Could not change nickname)");
                                }
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
                            hasUpdated = setPlayerRoles(hasUpdated, member, uuid);

                            //Remove from the list as no longer need to be verified.
                            discordMembers.remove(member);

                            //Update Discord roles.
                            guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();

                            if (hasUpdated) {
                                rolesUpdated++;
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
        String suffix = "";

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

                        suffix = findGuildTag(allyGuild.getName());

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
                member.modifyNickname(playerName + " [" + suffix + "]").queue();
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
     * @param hasUpdated Whether this member has already been updated.
     * @param member The member being updated.
     * @param uuid The UUID or the given member.
     * @return The updated value of rolesUpdated if changed, otherwise the same.
     */
    private boolean setPlayerRoles(boolean hasUpdated, Member member, String uuid) {
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

        return hasUpdated;
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
    private ButtonedMessage trackGuild(String guildName, Guild guild) {
        String wynncraftGuild = findGuild(guildName);

        if (wynncraftGuild.equals("MultiplePossibilities")) {
            StringBuilder builder = new StringBuilder("Multiple guilds found with the prefix: " + guildName + ".\n");
            List<String> componentIds = new ArrayList<>();

            for (PossibleGuilds possibleGuild : possibleGuilds) {
                builder.append(possibleGuild.getFormattedString());
                componentIds.add(possibleGuild.getName());

                if (possibleGuilds.indexOf(possibleGuild) != possibleGuilds.size() - 1) {
                    builder.append("\n");
                } else {
                    builder.append("\nClick button to choose guild.");
                }
            }

            return new ButtonedMessage(builder.toString(), componentIds, MessageType.TRACK_GUILD);
        }

        try {
            //Get all the current online members in the guild.
            int currentMembers = getOnlineMembers(wynnAPI.v1().guildStats(wynncraftGuild).run());
            int onlineCaptains = getOnlineCaptains(wynnAPI.v1().guildStats(wynncraftGuild).run());

            saveGuildPrefix(wynncraftGuild, guild);

            try {
                //Use Guild ID to create directory with name unique to the current server.
                Files.createDirectories(Path.of("/home/opc/CC-117/" + guild.getId()));

                if (trackedFile.createNewFile()) {
                    System.out.println("Tracked file created.");
                } else {
                    System.out.println("Tracked file already exists.");
                }

                Scanner scanner = new Scanner(trackedFile);
                String currentLine;
                List<String> lineSplit;

                while (scanner.hasNextLine()) {
                    currentLine = scanner.nextLine();
                    lineSplit = Arrays.asList(currentLine.split(","));
                    if (lineSplit.get(0).equals(wynncraftGuild)) {
                        return new ButtonedMessage(wynncraftGuild + " is already being tracked.");
                    }
                }

                scanner.close();

                int hour = Instant.now().atZone(ZoneOffset.UTC).getHour();
                StringBuilder trackedLine = new StringBuilder(wynncraftGuild + "," + currentMembers + "," + 1 + ",(");

                for (int i = 0; i < 24; i++) {
                    if (i != hour) {
                        trackedLine.append(-1);
                    } else {
                        trackedLine.append(currentMembers);
                    }

                    if (i < 23) {
                        trackedLine.append(";");
                    }
                }

                trackedLine.append("),(");

                for (int i = 0; i < 24; i++) {
                    if (i != hour) {
                        trackedLine.append(-1);
                    } else {
                        trackedLine.append(onlineCaptains);
                    }

                    if (i < 23) {
                        trackedLine.append(";");
                    }
                }

                trackedLine.append(")\n");

                Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "tracked.txt"), trackedLine.toString().getBytes(), StandardOpenOption.APPEND);
            } catch (java.io.IOException ex) {
                return new ButtonedMessage(ex.toString());
            }

            return new ButtonedMessage(wynncraftGuild + "'s average online players are now being tracked.");
        } catch (APIResponseException ex) {
            return new ButtonedMessage(guildName + " is not a valid Guild.");
        } catch (APIRateLimitExceededException ex) {
            return new ButtonedMessage("Hit limit of 750 players checked.");
        }
    }

    /**
     * Saves the guild prefix to a file so that when some commands are ran, they don't require the full guild name.
     * @param guildName The name of the Wynncraft guild.
     * @param guild The current Discord server.
     */
    private void saveGuildPrefix(String guildName, Guild guild)  {
        //Retrieves the guild prefix.
        String prefix = wynnAPI.v1().guildStats(guildName).run().getPrefix();

        if (findGuild(prefix) != null) {
            return;
        }

        try {
            if (!prefixFile.exists()) {
                System.out.println("Prefix file does not exist.");
            }

            if (prefixFile.createNewFile()) {
                System.out.println("Prefix file created.");
            } else {
                System.out.println("Prefix file already exists.");
            }

            //Writes the guild name followed by its prefix to the file.
            Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "prefixes.txt"), (guildName + "," + prefix + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String updateGuildPrefixes(Guild guild) {
        String[] allGuilds = wynnAPI.v1().guildList().run().getList();

        try {
            if (!prefixFile.exists()) {
                System.out.println("Prefix file does not exist.");
            }

            if (prefixFile.createNewFile()) {
                System.out.println("Prefix file created");
            } else {
                System.out.println("Prefix file already exists");
            }

            Scanner scanner = new Scanner(prefixFile);
            String currentLine;
            String currentName;
            boolean prefixExists = false;
            int guildsAdded = 0;

            for (String guildName : allGuilds) {
                while(scanner.hasNextLine()) {
                    currentLine = scanner.nextLine();
                    currentName = Arrays.asList(currentLine.split(",")).get(0);

                    if (guildName.equals(currentName)) {
                        prefixExists = true;
                        break;
                    }
                }

                if (!prefixExists) {
                    try {
                        String prefix = wynnAPI.v1().guildStats(guildName).run().getPrefix();

                        guildsAdded++;

                        System.out.println("Adding [" + prefix + "] " + guildName);

                        Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "prefixes.txt"), (guildName + "," + prefix + "\n").getBytes(), StandardOpenOption.APPEND);
                    } catch (APIRateLimitExceededException ex) {
                        System.out.println("Rate limit exceeded");
                        break;
                    }
                }

                prefixExists = false;

                scanner = new Scanner(prefixFile);
            }

            scanner.close();

            return "Added prefixes for " + guildsAdded + " guilds.";
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Error saving prefixes to file.";
        }
    }

    private void updateOnlinePlayers(Guild guild) {
        try {
            onlineServers = wynnAPI.v1().onlinePlayers().run();
        } catch (APIRateLimitExceededException ex) {
            TextChannel channel = guild.getTextChannelById("1061698530651144212");

            String message = "Rate limit exceeded";

            sendLogFile(ex, guild, channel, message);
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
     * Gets the number of players online in a guild that are at least the captain rank.
     * @param guild The Wynncraft guild to check.
     * @return The number of online captain's or above.
     */
    private int getOnlineCaptains(WynncraftGuild guild) {
        int currentlyOnline = 0;
        List<String> guildMembersNames = new ArrayList<>();
        List<String> onlineGuildMembersNames = new ArrayList<>();

        //Retrieve the names of the members of the guild.
        for (WynncraftGuildMember player : guild.getMembers()) {
            guildMembersNames.add(player.getName());
        }

        //Loops through each server and then loops through the players on that server to see if they are in the guild.
        for (WynncraftServerOnlinePlayers onlineServer : onlineServers.getOnlinePlayers()) {
            for (String playerName : onlineServer.getPlayers()) {
                if (guildMembersNames.contains(playerName)) {
                    onlineGuildMembersNames.add(playerName);
                }
            }
        }

        //Loops through all members and sees if they have the captain rank or above.
        for (WynncraftGuildMember member : guild.getMembers()) {
            if (onlineGuildMembersNames.contains(member.getName())) {
                if (member.getRank() == GuildRank.OWNER || member.getRank() == GuildRank.CHIEF || member.getRank() == GuildRank.STRATEGIST || member.getRank() == GuildRank.CAPTAIN) {
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
            double newAverage;
            List<Integer> onlineHours;
            List<Integer> onlineCaptains;

            if (!trackedFile.exists()) {
                System.out.println("Tracked file does not exist.");
            }

            if (tempFile.createNewFile()) {
                System.out.println("Temp file created.");
            } else {
                System.out.println("Temp file already exists.");
            }

            //Loop through each line in file, get the guild name, current average and how many times the average has been checked.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                lineSplit = Arrays.asList(currentLine.split(","));
                currentGuildName = lineSplit.get(0);
                currentGuildAverage = Double.parseDouble(lineSplit.get(1));
                currentGuildChecks = Integer.parseInt(lineSplit.get(2));
                String tempHours = lineSplit.get(3);
                String tempCaptains = lineSplit.get(4);

                tempHours = tempHours.replace("(", "");
                tempHours = tempHours.replace(")", "");

                tempCaptains = tempCaptains.replace("(", "");
                tempCaptains = tempCaptains.replace(")", "");

                List<String> onlineHoursStr = Arrays.asList(tempHours.split(";"));
                List<String> onlineCaptainsStr = Arrays.asList(tempCaptains.split(";"));

                onlineHours = onlineHoursStr.stream().map(Integer::parseInt).toList();
                onlineCaptains = onlineCaptainsStr.stream().map(Integer::parseInt).toList();

                int currentOnline = getOnlineMembers(wynnAPI.v1().guildStats(currentGuildName).run());
                int currentOnlineCaptains = getOnlineCaptains(wynnAPI.v1().guildStats(currentGuildName).run());

                int hour = Instant.now().atZone(ZoneOffset.UTC).getHour();
                StringBuilder stringBuilder = new StringBuilder("(");

                for (int i = 0; i < onlineHours.size(); i++) {
                    if (i != hour) {
                        stringBuilder.append(onlineHours.get(i));
                    } else {
                        stringBuilder.append(currentOnline);
                    }

                    if (i < onlineHours.size() - 1) {
                        stringBuilder.append(";");
                    }
                }

                stringBuilder.append("),(");

                for (int i = 0; i < onlineCaptains.size(); i++) {
                    if (i != hour) {
                        stringBuilder.append(onlineCaptains.get(i));
                    } else {
                        stringBuilder.append(currentOnlineCaptains);
                    }

                    if (i < onlineCaptains.size() - 1) {
                        stringBuilder.append(";");
                    }
                }

                stringBuilder.append(")");

                //Update the average. If it has been running for a week, 168 hours, then reset but keep the previous weeks average as a starting base.
                if (currentGuildChecks < 168) {
                    newAverage = currentGuildAverage * (currentGuildChecks - 1) / currentGuildChecks + (double) currentOnline / currentGuildChecks;
                } else {
                    currentGuildChecks = 0;
                    newAverage = currentGuildAverage + (double) currentOnline / 2.0;
                }

                //Make string ready to be saved.
                currentLine = currentGuildName + "," + newAverage + "," + (currentGuildChecks + 1) + "," + stringBuilder;

                //Write new average.
                Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt"), (currentLine + "\n").getBytes(), StandardOpenOption.APPEND);
            }

            scanner.close();

            renameTrackedFile();

        } catch (java.io.IOException ex) {
            System.out.println("Error accessing file");
        } catch (APIResponseException ex) {
            ex.printStackTrace();

            TextChannel channel = guild.getTextChannelById("1061698530651144212");

            File logFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "logs.txt");

            try {
                if (logFile.createNewFile()) {
                    System.out.println("Log file created.");
                } else {
                    System.out.println("Log file already exists.");

                    if (logFile.delete()) {
                        System.out.println("Old log file deleted");
                    } else {
                        System.out.println("Failed to delete old log file, returning.");

                        if (tempFile.delete()) {
                            System.out.println("Temp file deleted successfully.");
                        } else {
                            System.out.println("Unable to delete temp file.");
                        }

                        return;
                    }
                }

                String message = "Response exception, guild probably no longer exists.";

                sendLogFile(ex, guild, channel, message);

                if (tempFile.delete()) {
                    System.out.println("Temp file deleted successfully.");
                } else {
                    System.out.println("Unable to delete temp file.");
                }
            } catch (java.io.IOException exx) {
                exx.printStackTrace();
            }
        } catch (APIRateLimitExceededException ex) {
            ex.printStackTrace();

            TextChannel channel = guild.getTextChannelById("1061698530651144212");

            File logFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "logs.txt");

            try {
                if (logFile.createNewFile()) {
                    System.out.println("Log file created.");
                } else {
                    System.out.println("Log file already exists.");

                    if (logFile.delete()) {
                        System.out.println("Old log file deleted");
                    } else {
                        System.out.println("Failed to delete old log file, returning.");

                        if (tempFile.delete()) {
                            System.out.println("Temp file deleted successfully.");
                        } else {
                            System.out.println("Unable to delete temp file.");
                        }

                        return;
                    }
                }

                String message = "Rate limit exceeded.";

                sendLogFile(ex, guild, channel, message);

                if (tempFile.delete()) {
                    System.out.println("Temp file deleted successfully.");
                } else {
                    System.out.println("Unable to delete temp file.");
                }
            } catch (java.io.IOException exx) {
                exx.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            TextChannel channel = guild.getTextChannelById("1061698530651144212");

            File logFile = new File("/home/opc/CC-117/" + guild.getId() + "/" + "logs.txt");

            try {
                if (logFile.createNewFile()) {
                    System.out.println("Log file created.");
                } else {
                    System.out.println("Log file already exists.");

                    if (logFile.delete()) {
                        System.out.println("Old log file deleted");
                    } else {
                        System.out.println("Failed to delete old log file, returning.");

                        if (tempFile.delete()) {
                            System.out.println("Temp file deleted successfully.");
                        } else {
                            System.out.println("Unable to delete temp file.");
                        }

                        return;
                    }
                }

                String message = "Something broke :(";

                sendLogFile(ex, guild, channel, message);

                if (tempFile.delete()) {
                    System.out.println("Temp file deleted successfully.");
                } else {
                    System.out.println("Unable to delete temp file.");
                }
            } catch (java.io.IOException exx) {
                exx.printStackTrace();
            }
        }

        System.out.println("Averages updated");
    }

    /**
     * Displays the active hours for the specified guild.
     * @param inputGuild The guild to check.
     * @param timezone The timezone to view the active hours in.
     * @return The string message to display.
     */
    private ButtonedMessage activeHours(String inputGuild, String timezone, SlashCommandInteractionEvent event) {
        try {
            Scanner scanner = new Scanner(trackedFile);
            String currentLine;
            String guildLine = "";
            StringBuilder message = new StringBuilder("```");

            if (!trackedFile.exists()) {
                return new ButtonedMessage("No guilds are being tracked");
            }

            String guildName = findGuild(inputGuild);

            if (guildName == null) {
                return new ButtonedMessage(inputGuild + " is an unknown guild.");
            }

            if (guildName.equals("MultiplePossibilities")) {
                StringBuilder builder = new StringBuilder("Multiple guilds found with the prefix: " + inputGuild + ".\n");

                List<String> componentIds = new ArrayList<>();
                List<String> possiblePages = new ArrayList<>();

                for (PossibleGuilds possibleGuild : possibleGuilds) {
                    builder.append(possibleGuild.getFormattedString());
                    componentIds.add(possibleGuild.getName());

                    possiblePages.add(activeHours(possibleGuild.getName(), timezone, event).getText());

                    if (possibleGuilds.indexOf(possibleGuild) != possibleGuilds.size() - 1) {
                        builder.append("\n");
                    } else {
                        builder.append("\nChoose which guild you want from the buttons below.");
                    }
                }

                return new ButtonedMessage(builder.toString(), componentIds, MessageType.ACTIVE_HOURS, possiblePages);
            }

            //Find the guild in tracked guilds file.
            while(scanner.hasNextLine()) {
                currentLine = scanner.nextLine();

                if (Arrays.asList(currentLine.split(",")).get(0).equals(guildName)) {
                    guildLine = currentLine;
                    break;
                }
            }

            scanner.close();

            if (guildLine.equals("")) {
                return new ButtonedMessage(inputGuild + " is not being tracked");
            }

            //Creates the message to be displayed.
            message.append(guildName).append(" active/dead hours (").append(timezone).append(")\n\n");

            //Get the hours and captains online for the guild.
            String tempHours = Arrays.asList(guildLine.split(",")).get(3);
            String tempCaptains = Arrays.asList(guildLine.split(",")).get(4);

            tempHours = tempHours.replace("(", "");
            tempHours = tempHours.replace(")", "");

            tempCaptains = tempCaptains.replace("(", "");
            tempCaptains = tempCaptains.replace(")", "");

            //Convert hours and captains online to list.
            List<String> onlineHoursStr = Arrays.asList(tempHours.split(";"));
            List<String> onlineCaptainsStr = Arrays.asList(tempCaptains.split(";"));

            //Convert to list of integers.
            List<Integer> onlineHours = onlineHoursStr.stream().map(Integer::parseInt).toList();
            List<Integer> onlineCaptains = onlineCaptainsStr.stream().map(Integer::parseInt).toList();

            int sum = onlineHours.stream().mapToInt(Integer::intValue).sum();

            if (sum == -24) {
                message.append("No active/dead hours recorded, please try again later.");
            } else {
                int maximumOnline = -1;
                int minimumOnline = Integer.MAX_VALUE;

                //Find peak activity.
                for (int online : onlineHours) {
                    if (online > maximumOnline) {
                        maximumOnline = online;
                    }
                }

                //Find lowest activity.
                for (int online : onlineHours) {
                    if (online != -1 && online < minimumOnline) {
                        minimumOnline = online;
                    }
                }

                List<Integer> maxIndices = new ArrayList<>();
                List<Integer> minIndices = new ArrayList<>();

                //Find hours with peak activity.
                for (int i = 0; i < onlineHours.size(); i++) {
                    if (onlineHours.get(i) == maximumOnline) {
                        maxIndices.add(i);
                    }
                }

                //Find hours with least activity.
                for (int i = 0; i < onlineHours.size(); i++) {
                    if (onlineHours.get(i) == minimumOnline) {
                        minIndices.add(i);
                    }
                }

                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);

                List<Integer> maxCaptains = new ArrayList<>();
                List<Integer> minCaptains = new ArrayList<>();

                //Handle different timezones.
                if (timezone.equals("UTC")) {

                    //Add captains online at peak hours.
                    for (Integer maxIndex : maxIndices) {
                        maxCaptains.add(onlineCaptains.get(maxIndex));
                    }

                    //Add captains online at dead hours.
                    for (Integer minIndex : minIndices) {
                        minCaptains.add(onlineCaptains.get(minIndex));
                    }

                    //Calculate average captains online.
                    int maxCaptainsSum = maxCaptains.stream().mapToInt(Integer::intValue).sum();
                    int minCaptainsSum = minCaptains.stream().mapToInt(Integer::intValue).sum();

                    double maxCaptainsAverage = (double) maxCaptainsSum / (double) maxCaptains.size();
                    double minCaptainsAverage = (double) minCaptainsSum / (double) minCaptains.size();

                    message.append("Active Hours (").append(maximumOnline).append(" players online)\n");

                    //Add active hours to message.
                    for (int i = 0; i < maxIndices.size(); i++) {
                        message.append(maxIndices.get(i)).append(":00");

                        if (i < maxIndices.size() - 1) {
                            message.append(", ");
                        }
                    }

                    message.append(" with an average of ").append(df.format(maxCaptainsAverage)).append(" Captain+'s online.\n\n");

                    message.append("Dead Hours (").append(minimumOnline).append(" players online)\n");

                    //Add dead hours to message.
                    for (int i = 0; i < minIndices.size(); i++) {
                        message.append(minIndices.get(i)).append(":00");

                        if (i < minIndices.size() - 1) {
                            message.append(", ");
                        }
                    }

                    message.append(" with an average of ").append(df.format(minCaptainsAverage)).append(" Captain+'s online.\n\n");
                } else {

                    for (Integer maxIndex : maxIndices) {
                        maxCaptains.add(onlineCaptains.get(maxIndex));
                    }

                    for (Integer minIndex : minIndices) {
                        minCaptains.add(onlineCaptains.get(minIndex));
                    }

                    int maxCaptainsSum = maxCaptains.stream().mapToInt(Integer::intValue).sum();
                    int minCaptainsSum = minCaptains.stream().mapToInt(Integer::intValue).sum();

                    double maxCaptainsAverage = (double) maxCaptainsSum / (double) maxCaptains.size();
                    double minCaptainsAverage = (double) minCaptainsSum / (double) minCaptains.size();

                    message.append("Active Hours (").append(maximumOnline).append(" players online)\n");

                    //Applies the selected timezone
                    applyTimezone(maxIndices, timezone);

                    for (int i = 0; i < maxIndices.size(); i++) {
                        message.append(maxIndices.get(i)).append(":00");

                        if (i < maxIndices.size() - 1) {
                            message.append(", ");
                        }
                    }

                    message.append(" with an average of ").append(df.format(maxCaptainsAverage)).append(" Captain+'s online.\n\n");

                    message.append("Dead Hours (").append(minimumOnline).append(" players online)\n");

                    applyTimezone(minIndices, timezone);

                    for (int i = 0; i < minIndices.size(); i++) {
                        message.append(minIndices.get(i)).append(":00");

                        if (i < minIndices.size() - 1) {
                            message.append(", ");
                        }
                    }

                    message.append(" with an average of ").append(df.format(minCaptainsAverage)).append(" Captain+'s online.\n\n");
                }
            }

            message.append("```");

            return new ButtonedMessage(message.toString());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return new ButtonedMessage("No tracked guilds");
        }
    }

    /**
     * Applies the timezone to list of active hours.
     * @param times The hours to convert.
     * @param timezone The timezone to convert to.
     */
    private void applyTimezone(List<Integer> times, String timezone) {
        switch (timezone) {
            case "BST" -> {
                for (int i = 0; i < times.size(); i++) {
                    int hour = times.get(i) + 1;

                    if (hour == 24) {
                        hour = 0;
                    }

                    times.set(i, hour);
                }
            }
            case "EDT" -> {
                for (int i = 0; i < times.size(); i++) {
                    int hour = times.get(i) - 4;

                    if (hour < 0) {
                        hour = hour + 24;
                    }

                    times.set(i, hour);
                }
            }
            case "EST" -> {
                for (int i = 0; i < times.size(); i++) {
                    int hour = times.get(i) - 5;

                    if (hour < 0) {
                        hour = hour + 24;
                    }

                    times.set(i, hour);
                }
            }
            case "PDT" -> {
                for (int i = 0; i < times.size(); i++) {
                    int hour = times.get(i) - 7;

                    if (hour < 0) {
                        hour = hour + 24;
                    }

                    times.set(i, hour);
                }
            }
            case "PST" -> {
                for (int i = 0; i < times.size(); i++) {
                    int hour = times.get(i) - 8;

                    if (hour < 0) {
                        hour = hour + 24;
                    }

                    times.set(i, hour);
                }
            }
        }
    }

    private ButtonedMessage lastLogins(String inputGuild) {
        String guildName = findGuild(inputGuild);
        StringBuilder lastLogins = new StringBuilder();
        List<String> lastLoginPages = new ArrayList<>();
        List<PlayerLastLogin> playerLastLogins = new ArrayList<>();
        boolean displayColours = false;

        if (guildName == null) {
            lastLoginPages.add("Guild does not exist");
            return new ButtonedMessage(lastLoginPages);
        }

        if (guildName.equals("MultiplePossibilities")) {
            StringBuilder builder = new StringBuilder("Multiple guilds found with the prefix: " + inputGuild + ".\n");

            List<String> componentIds = new ArrayList<>();

            for (PossibleGuilds possibleGuild : possibleGuilds) {
                builder.append(possibleGuild.getFormattedString());
                componentIds.add(possibleGuild.getName());

                if (possibleGuilds.indexOf(possibleGuild) != possibleGuilds.size() - 1) {
                    builder.append("\n");
                } else {
                    builder.append("\nClick button to choose guild.");
                }
            }

            lastLoginPages.add(builder.toString());
            return new ButtonedMessage(builder.toString(), componentIds, MessageType.LAST_LOGINS);
        }

        if (guildName.equals("Chiefs Of Corkus")) {
            displayColours = true;
        }

        WynncraftGuildMember[] members = wynnAPI.v1().guildStats(guildName).run().getMembers();

        for (WynncraftGuildMember member : members) {
            WynncraftPlayer player;
            Date lastLogin;
            boolean isOnline;

            try {
                player = wynnAPI.v2().player().statsUUID(member.getUuid()).run()[0];
                lastLogin = player.getMeta().getLastJoin();
                isOnline = player.getMeta().getLocation().isOnline();
            } catch (APIRequestException ex) {
                System.out.println("Failed to find player: " + member.getName());
                continue;
            }

            if (isOnline) {
                playerLastLogins.add(new PlayerLastLogin(player.getUsername(), member.getRank(), true, displayColours));
            } else {
                LocalDateTime today = LocalDateTime.now();

                LocalDate localDate = lastLogin.toInstant()
                        .atZone(ZoneId.of("Europe/London"))
                        .toLocalDate();

                LocalDateTime lastLoginDate = localDate.atStartOfDay();

                long inactiveDays = Duration.between(lastLoginDate, today).toDays();

                playerLastLogins.add(new PlayerLastLogin(player.getUsername(), member.getRank(), inactiveDays, displayColours));
            }
        }

        Collections.sort(playerLastLogins);

        int counter = 0;

        lastLogins.append("```diff\n");

        for (PlayerLastLogin players : playerLastLogins) {
            if (counter == 30) {
                lastLogins.append("```");
                lastLoginPages.add(String.valueOf(lastLogins));
                lastLogins = new StringBuilder();
                lastLogins.append("```diff\n");
                lastLogins.append(players.toString());
                counter = 1;
            } else {
                lastLogins.append(players.toString());
                counter++;
            }
        }

        if (counter != 30) {
            lastLogins.append("```");
            lastLoginPages.add(String.valueOf(lastLogins));
        }

        return new ButtonedMessage(lastLoginPages);
    }

    /**
     * Takes an input from a guild name command and sees if it matches a guild in the file ignoring case or seeing if
     * it's a prefix.
     * @param input The guild to find.
     * @return The case-sensitive guild name.
     */
    private String findGuild(String input) {
        try {
            //See if the guild name is valid, otherwise the exception will be caught.
            return wynnAPI.v1().guildStats(input).run().getName();
        } catch (APIResponseException ex) {
            try {
                Scanner scanner = new Scanner(prefixFile);
                String currentLine;
                String currentName;
                String currentPrefix;
                possibleGuilds = new ArrayList<>();

                //Loop through the file and see if the input matches the guild name or prefix ignoring case and if so
                //return the name in the file.
                while(scanner.hasNextLine()) {
                    currentLine = scanner.nextLine();
                    currentName = Arrays.asList(currentLine.split(",")).get(0);
                    currentPrefix = Arrays.asList(currentLine.split(",")).get(1);

                    if (input.equalsIgnoreCase(currentName)) {
                        possibleGuilds.add(new PossibleGuilds(currentPrefix, currentName));
                    }

                    if (input.equalsIgnoreCase(currentPrefix)) {
                        possibleGuilds.add(new PossibleGuilds(currentPrefix, currentName));
                    }
                }

                scanner.close();

                if (possibleGuilds.size() == 1) {
                    return possibleGuilds.get(0).getName();
                } else if (possibleGuilds.size() > 1) {
                    return "MultiplePossibilities";
                }

                return null;
            } catch (FileNotFoundException exx) {
                return null;
            }
        }
    }

    private String findGuildTag(String input) {
        try {
            Scanner scanner = new Scanner(prefixFile);
            String currentLine;
            String currentName;
            String currentPrefix;

            //Loop through the file and see if the input matches the guild name or prefix ignoring case and if so
            //return the prefix in the file.
            while(scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                currentName = Arrays.asList(currentLine.split(",")).get(0);
                currentPrefix = Arrays.asList(currentLine.split(",")).get(1);

                if (input.equalsIgnoreCase(currentName)) {
                    scanner.close();
                    return currentPrefix;
                }
            }

            scanner.close();

            return null;
        } catch (FileNotFoundException exx) {
            return null;
        }
    }

    /**
     * Removes tracked guild from the file.
     * @param guildName Guild to untrack.
     * @param guild The current Discord server.
     * @return Message to send back.
     */
    private ButtonedMessage untrackGuild(String guildName, Guild guild) {
        String wynncraftGuild = findGuild(guildName);

        if (wynncraftGuild == null) {
            return new ButtonedMessage(guildName + " is an unknown guild.");
        }

        if (wynncraftGuild.equals("MultiplePossibilities")) {
            StringBuilder builder = new StringBuilder("Multiple guilds found with the prefix: " + guildName + ".\n");
            List<String> componentIds = new ArrayList<>();

            for (PossibleGuilds possibleGuild : possibleGuilds) {
                builder.append(possibleGuild.getFormattedString());
                componentIds.add(possibleGuild.getName());

                if (possibleGuilds.indexOf(possibleGuild) != possibleGuilds.size() - 1) {
                    builder.append("\n");
                } else {
                    builder.append("\nClick button to choose guild.");
                }
            }

            return new ButtonedMessage(builder.toString(), componentIds, MessageType.UNTRACK_GUILD);
        }

        try {
            Scanner scanner = new Scanner(trackedFile);
            String currentLine;
            String currentGuild;

            if (tempFile.createNewFile()) {
                System.out.println("Temp file created.");
            } else {
                System.out.println("Temp file already exists.");
            }

            //Loop through every line in tracked file and if it does not match the name of the
            //guild requested to remove as tracked, add it to the temp file.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                currentGuild = Arrays.asList(currentLine.split(",")).get(0);

                if (!currentGuild.equals(wynncraftGuild)) {
                    Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt"), (currentLine + "\n").getBytes(), StandardOpenOption.APPEND);
                }
            }

            scanner.close();

            renameTrackedFile();

        } catch (java.io.IOException ex) {
            return new ButtonedMessage("No tracked guilds found: " + ex);
        }

        return new ButtonedMessage("Removed " + wynncraftGuild + " as tracked.");
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
    private ButtonedMessage trackedGuilds() {
        String trackedHeader = "```Guild Name          Average Online Members          Currently Online\n--------------------------------------------------------------------\n";
        StringBuilder guildAverages = new StringBuilder();
        List<String> trackedPages = new ArrayList<>();
        guildAverages.append(trackedHeader);

        try {
            Scanner scanner = new Scanner(trackedFile);
            String currentLine;
            List<String> lineSplit;
            String currentGuildName;
            double currentGuildAverage;
            int currentMembers;
            List<GuildAverageMembers> averageMembers = new ArrayList<>();

            if (!trackedFile.exists()) {
                trackedPages.add("No tracked guilds");
                return new ButtonedMessage("No tracked guilds");
            }

            //Loop through every line in the file. Get the name and average online players.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                lineSplit = Arrays.asList(currentLine.split(","));
                currentGuildName = lineSplit.get(0);
                currentGuildAverage = Double.parseDouble(lineSplit.get(1));

                currentMembers = getOnlineMembers(wynnAPI.v1().guildStats(currentGuildName).run());

                //Create an object that can be sorted by average.
                averageMembers.add(new GuildAverageMembers(currentGuildName, currentGuildAverage, currentMembers));
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

                    guildAverages.append(trackedHeader);

                    guildAverages.append(members.getAverageString());
                    counter = 1;
                } else {
                    guildAverages.append(members.getAverageString());
                    counter++;
                }
            }

            if (counter != 0) {
                guildAverages.append("```");
                trackedPages.add(guildAverages.toString());
            }
        } catch (java.io.IOException ex) {
            trackedPages.add("No tracked guilds");
            return new ButtonedMessage("No tracked guilds");
        }

        return new ButtonedMessage(trackedPages);
    }

    /**
     * Stores the name of the Wynncraft Guild into a file so that the bot can work
     * on different servers if need be.
     * @param guildName The name of the guild to store in the file.
     * @param guild The Discord server to get the ID from, so it can store it in a unique file.
     * @return Message to say guild set or not.
     */
    private ButtonedMessage setGuild(String guildName, Guild guild) {
        try {
            if (guildFile.createNewFile()) {
                System.out.println("Guild file created.");
            } else {
                System.out.println("Guild file already exists.");
            }

            String wynncraftGuild = findGuild(guildName);

            if (wynncraftGuild == null) {
                return new ButtonedMessage(guildName + " is an unknown guild.");
            } else if (wynncraftGuild.equals(guildName)) {
                return new ButtonedMessage(guildName + " is already set as your guild.");
            }

            if (wynncraftGuild.equals("MultiplePossibilities")) {
                StringBuilder builder = new StringBuilder("Multiple guilds found with the prefix: " + guildName + ".\n");
                List<String> componentIds = new ArrayList<>();

                for (PossibleGuilds possibleGuild : possibleGuilds) {
                    builder.append(possibleGuild.getFormattedString());
                    componentIds.add(possibleGuild.getName());

                    if (possibleGuilds.indexOf(possibleGuild) != possibleGuilds.size() - 1) {
                        builder.append("\n");
                    } else {
                        builder.append("\nClick button to choose guild.");
                    }
                }

                return new ButtonedMessage(builder.toString(), componentIds, MessageType.SET_GUILD);
            }

            saveGuildPrefix(wynncraftGuild, guild);

            FileWriter guildFileWriter = new FileWriter("/home/opc/CC-117/" + guild.getId() + "/" + "guild.txt");
            guildFileWriter.write(wynncraftGuild);
            guildFileWriter.close();

            return new ButtonedMessage("Set " + wynncraftGuild + " as Guild.");
        } catch (java.io.IOException ex) {
            return new ButtonedMessage(ex.toString());
        }
    }

    /**
     * Stores the name(s) of any Allies to the Wynncraft Guild into a file so that
     * allies can easily be added/removed.
     * @param guildName Name of the Ally guild.
     * @param guild The current Discord server so the file can be created/read uniquely.
     * @return Message to say Ally added or not.
     */
    private ButtonedMessage addAlly(String guildName, Guild guild) {
        String wynncraftGuild = findGuild(guildName);

        if (wynncraftGuild == null) {
            return new ButtonedMessage(guildName + " is an unknown guild.");
        }

        if (wynncraftGuild.equals("MultiplePossibilities")) {
            StringBuilder builder = new StringBuilder("Multiple guilds found with the prefix: " + guildName + ".\n");

            List<String> componentIds = new ArrayList<>();

            for (PossibleGuilds possibleGuild : possibleGuilds) {
                builder.append(possibleGuild.getFormattedString());
                componentIds.add(possibleGuild.getName());

             if (possibleGuilds.indexOf(possibleGuild) != possibleGuilds.size() - 1) {
                    builder.append("\n");
                } else {
                    builder.append("\nClick button to choose guild.");
                }
            }

            return new ButtonedMessage(builder.toString(), componentIds, MessageType.ADD_ALLY);
        }

        try {
            //Use Guild ID to create directory with name unique to the current server.
            Files.createDirectories(Path.of("/home/opc/CC-117/" + guild.getId()));

            if (allyFile.createNewFile()) {
                System.out.println("Ally file created.");
            } else {
                System.out.println("Ally file already exists.");
            }

            Scanner scanner = new Scanner(allyFile);
            String currentLine;

            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                if (currentLine.equals(wynncraftGuild)) {
                    scanner.close();
                    return new ButtonedMessage(wynncraftGuild + " is already an ally.");
                }
            }

            scanner.close();

            saveGuildPrefix(wynncraftGuild, guild);

            Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "allies.txt"), (wynncraftGuild + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (java.io.IOException ex) {
            return new ButtonedMessage(ex.toString());
        }

        return new ButtonedMessage("Added " + wynncraftGuild + " as an Ally.");
    }

    /**
     * Removes the name of an Ally to the Wynncraft Guild in the file so that
     * any members of that Guild will no longer have ally roles.
     * @param guildName Name of the no longer Ally guild.
     * @param guild The current Discord server so that the correct ally file can be found.
     * @return Message to say Ally removed or not.
     */
    private ButtonedMessage removeAlly(String guildName, Guild guild) {
        String wynncraftGuild = findGuild(guildName);
        boolean removed = false;

        if (wynncraftGuild == null) {
            return new ButtonedMessage(guildName + " is an unknown guild.");
        }

        if (wynncraftGuild.equals("MultiplePossibilities")) {
            StringBuilder builder = new StringBuilder("Multiple guilds found with the prefix: " + guildName + ".\n");
            List<String> componentIds = new ArrayList<>();

            for (PossibleGuilds possibleGuild : possibleGuilds) {
                builder.append(possibleGuild.getFormattedString());
                componentIds.add(possibleGuild.getName());

                if (possibleGuilds.indexOf(possibleGuild) != possibleGuilds.size() - 1) {
                    builder.append("\n");
                } else {
                    builder.append("\nClick button to choose guild.");
                }
            }

            return new ButtonedMessage(builder.toString(), componentIds, MessageType.REMOVE_ALLY);
        }

        try {
            Scanner scanner = new Scanner(allyFile);
            String currentLine;

            if (tempFile.createNewFile()) {
                System.out.println("Temp file created.");
            } else {
                System.out.println("Temp file already exists.");
            }

            //Loop through every line in ally file and if it does not match the name of the
            //guild requested to remove as an ally, add it to the temp file.
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                if (!currentLine.equals(wynncraftGuild)) {
                    Files.write(Path.of("/home/opc/CC-117/" + guild.getId() + "/" + "temp.txt"), (currentLine + "\n").getBytes(), StandardOpenOption.APPEND);
                } else {
                    removed = true;
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
            return new ButtonedMessage("No allies found: " + ex);
        }

        if (removed) {
            return new ButtonedMessage("Removed " + wynncraftGuild + " as an Ally.");
        } else {
            return new ButtonedMessage(wynncraftGuild + " is not an Ally.");
        }
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
        return true;
//        List<Role> memberRoles = member.getRoles();
//
//        return memberRoles.contains(ownerRole) || memberRoles.contains(chiefRole);
    }
}
