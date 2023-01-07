import discord
import responses
import bot_token

async def send_message(guild_members, guild_roles, message, user_message, is_private):
    try:
        response = await responses.handle_response(guild_members, guild_roles, user_message)
        await message.author.send(response) if is_private else await message.channel.send(response)
    except Exception as e:
        print("Exception: \n")
        print(e)

def run_discord_bot():
    token = bot_token.token
    intents = discord.Intents.default()
    intents.message_content = True
    intents.members = True
    client = discord.Client(intents=intents)

    @client.event
    async def on_ready():
        print(f'{client.user} is now running!')

    @client.event
    async def on_message(message):
        if message.author == client.user:
            return

        username = str(message.author)
        user_message = str(message.content)
        channel = str(message.channel)

        if user_message[0:2] == 'c!':
            if user_message[2] == '?':
                user_message = user_message[3:]
                await send_message(message.guild.members, message.guild.roles, message, user_message, is_private=True)
            else:
                user_message = user_message[2:]
                await send_message(message.guild.members, message.guild.roles, message, user_message, is_private=False)

    client.run(token)
