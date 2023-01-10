# CC-117

This is a Discord bot for the Discord server containing the guild members of the Wynncraft MMORPG Minecraft server, Chiefs Of Corkus.
The bot applies roles to the users based on their Discord username or nickname, whichever one matches the in-game username of a member of the guild.

## Commands

The bot currently features 2 commands, /updateranks and /verify.

/updateranks will loop through all members of the guild and check to see if someone in the Discord server has either a matching username, or nickname.
If and when it finds someone, it will look at that guild member to see what roles need to be applied. It will also removed the unverified role which is given
to all members when they first join the server.

/verify takes in 1 input which is the Minecraft username or who they want to verify as. This then does the same as /updateranks, but for that individual user, 
rather than the whole server.

## Wynncraft Public API

The bot reads the data from the Wynncraft Public API located here: https://docs.wynncraft.com/
It reads data relating to guilds, and their members, including their guild rank, personal rank and veteran status.
