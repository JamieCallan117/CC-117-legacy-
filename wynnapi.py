import requests


def guild_json(guild_name):
    r = requests.get('https://api.wynncraft.com/public_api.php?action=guildStats&command=%s' % guild_name)

    return r.json()


def player_json(player_uuid):
    r = requests.get('https://api.wynncraft.com/v2/player/%s/stats' % player_uuid)

    return r.json()
