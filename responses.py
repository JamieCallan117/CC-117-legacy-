import wynnapi
from discord.utils import get


async def handle_response(members, roles, message):
    p_message = message.split()

    members = list(members)

    for member in members:
        if member.bot:
            members.remove(member)

    if p_message[0] == "member":
        json = wynnapi.guild_json('Chiefs Of Corkus')
        for i in range(0, len(json['members'])):
            if json['members'][i]["name"] == p_message[1]:
                return json['members'][i]

        return "No member found with that username"

    if p_message[0] == "updateranks":
        g_json = wynnapi.guild_json('Chiefs Of Corkus')
        a1_json = wynnapi.guild_json('The Broken Gasmask')
        a2_json = wynnapi.guild_json('Overseers of Light')

        ally_guilds = [a1_json, a2_json]

        owner = get(roles, name="Owner")
        chief = get(roles, name="Chief")
        strategist = get(roles, name="Strategist")
        captain = get(roles, name="Captain")
        recruiter = get(roles, name="Recruiter")
        recruit = get(roles, name="Recruit")
        champion = get(roles, name="CHAMPION")
        hero = get(roles, name="HERO")
        vip_plus = get(roles, name="VIP+")
        vip = get(roles, name="VIP")
        vet = get(roles, name="Vet.")

        ally = get(roles, name="Ally")
        ool_member = get(roles, name="OoL Member")
        ally_owner = get(roles, name="Ally Guild Owner")

        roles_updated = 0

        for i in range(0, len(g_json['members'])):
            if len(members) > 0:
                for member in members:
                    print("Are Guild member %s and Discord member %s the same?" % (g_json['members'][i]["name"], member.name))
                    if g_json['members'][i]["name"] == member.name or g_json['members'][i]["name"] == member.nick:
                        print("The same")
                        has_updated = False
                        uuid = g_json['members'][i]['uuid']

                        match g_json['members'][i]["rank"]:
                            case "OWNER":
                                if get(member.roles, name="Owner") is None:
                                    print(member.name + " is the Owner of the Guild.")
                                    await member.add_roles(owner)
                                    await member.remove_roles(chief, strategist, captain, recruiter, recruit)
                                    has_updated = True
                            case "CHIEF":
                                if get(member.roles, name="Chief") is None:
                                    print(member.name + " is a Chief of the Guild.")
                                    await member.add_roles(chief)
                                    await member.remove_roles(owner, strategist, captain, recruiter, recruit)
                                    has_updated = True
                            case "STRATEGIST":
                                if get(member.roles, name="Strategist") is None:
                                    print(member.name + " is a Strategist of the Guild.")
                                    await member.add_roles(strategist)
                                    await member.remove_roles(owner, chief, captain, recruiter, recruit)
                                    has_updated = True
                            case "CAPTAIN":
                                if get(member.roles, name="Captain") is None:
                                    print(member.name + " is a Captain of the Guild.")
                                    await member.add_roles(captain)
                                    await member.remove_roles(owner, chief, strategist, recruiter, recruit)
                                    has_updated = True
                            case "RECRUITER":
                                if get(member.roles, name="Recruiter") is None:
                                    print(member.name + " is a Recruiter of the Guild.")
                                    await member.add_roles(recruiter)
                                    await member.remove_roles(owner, chief, strategist, captain, recruit)
                                    has_updated = True
                            case "RECRUIT":
                                if get(member.roles, name="Recruit") is None:
                                    print(member.name + " is a Recruit of the Guild.")
                                    await member.add_roles(recruit)
                                    await member.remove_roles(owner, chief, strategist, captain, recruiter)
                                    has_updated = True

                        p_json = wynnapi.player_json(uuid)

                        match p_json['data'][0]['meta']['tag']["value"]:
                            case "VIP":
                                if get(member.roles, name="VIP") is None:
                                    print(member.name + " is a VIP.")
                                    await member.add_roles(vip)
                                    await member.remove_roles(vip_plus, hero, champion)
                                    has_updated = True
                            case "VIP+":
                                if get(member.roles, name="VIP+") is None:
                                    print(member.name + " is a VIP+.")
                                    await member.add_roles(vip_plus)
                                    await member.remove_roles(vip, hero, champion)
                                    has_updated = True
                            case "HERO":
                                if get(member.roles, name="HERO") is None:
                                    print(member.name + " is a HERO.")
                                    await member.add_roles(hero)
                                    await member.remove_roles(vip, vip_plus, champion)
                                    has_updated = True
                            case "CHAMPION":
                                if get(member.roles, name="CHAMPION") is None:
                                    print(member.name + " is a CHAMPION.")
                                    await member.add_roles(champion)
                                    await member.remove_roles(vip, vip_plus, hero)
                                    has_updated = True

                        if p_json['data'][0]['meta']["veteran"]:
                            if get(member.roles, name="Vet.") is None:
                                print(member.name + " is a Vet.")
                                await member.add_roles(vet)
                                has_updated = True

                        if has_updated:
                            roles_updated += 1

                        members.remove(member)

                        break

        for i in range(0, len(ally_guilds)):
            for j in range(0, len(ally_guilds[i]['members'])):
                if len(members) > 0:
                    for member in members:
                        print("Are Guild member %s and Discord member %s the same?" % (ally_guilds[i]['members'][j]["name"], member.name))
                        if ally_guilds[i]['members'][j]["name"] == member.name or ally_guilds[i]['members'][j]["name"] == member.nick:
                            print("The same")
                            has_updated = False
                            uuid = ally_guilds[i]['members'][j]['uuid']

                            match ally_guilds[i]['members'][j]["rank"]:
                                case "OWNER":
                                    if get(member.roles, name="Ally Guild Owner") is None:
                                        print(member.name + " is the Owner of an Ally Guild.")
                                        await member.add_roles(ally_owner)
                                        await member.remove_roles(ally)
                                        has_updated = True
                                case "CHIEF" | "STRATEGIST" | "CAPTAIN" | "RECRUITER" | "RECRUIT":
                                    if get(member.roles, name="Ally") is None:
                                        print(member.name + " is a Ally of an Ally Guild.")
                                        await member.add_roles(ally)
                                        await member.remove_roles(ally_owner)
                                        has_updated = True

                            p_json = wynnapi.player_json(uuid)

                            match p_json['data'][0]['meta']['tag']["value"]:
                                case "VIP":
                                    if get(member.roles, name="VIP") is None:
                                        print(member.name + " is a VIP.")
                                        await member.add_roles(vip)
                                        await member.remove_roles(vip_plus, hero, champion)
                                        has_updated = True
                                case "VIP+":
                                    if get(member.roles, name="VIP+") is None:
                                        print(member.name + " is a VIP+.")
                                        await member.add_roles(vip_plus)
                                        await member.remove_roles(vip, hero, champion)
                                        has_updated = True
                                case "HERO":
                                    if get(member.roles, name="HERO") is None:
                                        print(member.name + " is a HERO.")
                                        await member.add_roles(hero)
                                        await member.remove_roles(vip, vip_plus, champion)
                                        has_updated = True
                                case "CHAMPION":
                                    if get(member.roles, name="CHAMPION") is None:
                                        print(member.name + " is a CHAMPION.")
                                        await member.add_roles(champion)
                                        await member.remove_roles(vip, vip_plus, hero)
                                        has_updated = True

                            if p_json['data'][0]['meta']["veteran"]:
                                if get(member.roles, name="Vet.") is None:
                                    print(member.name + " is a Vet.")
                                    await member.add_roles(vet)
                                    has_updated = True

                            if has_updated:
                                roles_updated += 1

                            members.remove(member)

                            break

        if roles_updated == 0:
            return "No roles updated."
        elif roles_updated == 1:
            return "Updated roles for %d member." % roles_updated
        else:
            return "Updated roles for %d members." % roles_updated

    return "Command %s not recognised" % p_message[0]
