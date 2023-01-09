import discord
import responses
import bot_token
from discord import app_commands
from discord.ext import commands


def run_discord_bot():
    token = bot_token.token
    intents = discord.Intents.default()
    intents.message_content = True
    intents.members = True
    client = commands.Bot(command_prefix="c!", intents=intents)

    @client.event
    async def on_ready():
        print(f'{client.user} is now running!')
        try:
            synced = await client.tree.sync()
            print("Synced %d command(s)" % len(synced))
        except Exception as e:
            print("Exception: \n")
            print(e)

    @client.event
    async def on_member_join(member):
        await responses.add_unverified(member, member.guild.roles)

    @client.tree.command(name="updateranks")
    async def update_ranks(interaction: discord.Interaction):
        await interaction.response.defer()
        await interaction.followup.send(
            await responses.update_ranks(interaction.guild.members, interaction.guild.roles))

    @client.tree.command(name="verify")
    @app_commands.describe(player_name="Player name")
    async def verify(interaction: discord.Interaction, player_name: str):
        await interaction.response.defer()
        await interaction.followup.send(await responses.verify(interaction.user, interaction.guild.roles,
                                                               player_name))

    client.run(token)
