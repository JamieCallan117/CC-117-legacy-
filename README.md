# CC-117

This is a Discord bot for the Discord server containing the guild members of the Wynncraft MMORPG Minecraft server, Chiefs Of Corkus.
The bot applies roles to the users based on their Discord username or nickname, whichever one matches the in-game username of a member of the guild.
It also tracks the average number of players online to chosen guilds and estimates their most and least active hours.

## Commands

The bot currently features 9 commands, /setguild, /addally, /removeally, /updateranks, /verify, /trackguild, /untrackguild /trackedguilds and /activehours.

/setguild takes in 1 input which is the name of the guild to use as the current servers main guild. This will be used for applying roles to members of the Discord server.

/addally takes in 1 input which is the name of a guild that is an ally to the main guild. This is used to apply ally roles to those in an allied guild.

/removeally takes in 1 input which removes an existing ally from the list of allies.

/updateranks will loop through all members of the main guild and its allies and check to see if someone in the Discord server has either a matching username, or nickname.
If and when it finds someone, it will look at that guild member to see what roles need to be applied. It will also removed the unverified role which is given
to all members when they first join the server.

/verify takes in 1 input which is the Minecraft username or who they want to verify as. This then does the same as /updateranks, but for that individual user, 
rather than the whole server.

/trackguild takes in 1 input which is the name of a guild to track their average online players.

/untrackguild takes in 1 input and removes a guild from the list of guilds to be tracked.

/trackedguilds displays a formatted list of all of the tracked guilds, sorted from highest average online player count to lowest.

/activehours takes in 2 inputs, with the first being the guild to view active hours for, this can be via a prefix or a case-insensitive version of the guild name. The second input is not required but is for the timezone you want to view the active hours in. The command will display a message of the active/dead hours for the guild and the average of players online during those hours who are at least the Captain rank in the guild.

## Wynncraft Public API

The bot reads the data from the Wynncraft Public API, docs located here: https://docs.wynncraft.com/
It reads data relating to guilds, and their members, including their guild rank, personal rank and veteran status.
