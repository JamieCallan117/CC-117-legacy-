import requests

r = requests.get('https://api.wynncraft.com/public_api.php?action=guildStats&command=Chiefs Of Corkus')
#r = requests.get('https://api.wynncraft.com/v2/player/ShadowCat117/stats')

json = r.json()
