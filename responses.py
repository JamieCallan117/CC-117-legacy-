import wynnapi
from discord.utils import get


async def update_ranks(members, roles):
    members = list(members)

    for member in members:
        if member.bot:
            members.remove(member)

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
    unverified = get(roles, name="Unverified")
    member_of = get(roles, name="Member of Chiefs of Corkus")

    ally = get(roles, name="Ally")
    # ool_member = get(roles, name="OoL Member")
    ally_owner = get(roles, name="Ally Guild Owner")

    roles_updated = 0

    for i in range(0, len(g_json['members'])):
        if len(members) > 0:
            for member in members:
                nick = ""
                if member.nick is not None:
                    nick = member.nick
                if g_json['members'][i]["name"].lower() == member.name.lower() or g_json['members'][i][
                    "name"].lower() == nick.lower():
                    await member.add_roles(member_of)
                    await member.remove_roles(unverified)

                    try:
                        await member.edit(nick=g_json['members'][i]["name"])
                        print("Verified %s as Guild member %s." % (member.name, g_json['members'][i]["name"]))
                    except Exception as e:
                        print("Verified %s as Guild member %s. (Could not change nickname)" % (
                        member.name, g_json['members'][i]["name"]))

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
                    nick = ""
                    if member.nick is not None:
                        nick = member.nick
                    if ally_guilds[i]['members'][j]["name"].lower() == member.name.lower() or \
                            ally_guilds[i]['members'][j]["name"].lower() == nick.lower():
                        await member.add_roles(member_of)
                        await member.remove_roles(unverified)

                        try:
                            await member.edit(nick=ally_guilds[i]['members'][j]["name"])
                            print("Verified %s as Guild member %s." % (member.name, ally_guilds[i]['members'][j]["name"]))
                        except Exception as e:
                            print("Verified %s as Guild member %s. (Could not change nickname)" % (
                            member.name, ally_guilds[i]['members'][j]["name"]))

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

    print("%d members not found in guild" % len(members))
    for member in members:
        print("%s not found in Guild." % member.name)
        if get(member.roles, name="Unverified") is None:
            await member.add_roles(unverified)
            await member.remove_roles(vip, vip_plus, hero, champion, vet, recruit, recruiter, captain, strategist,
                                      chief, owner, member_of, ally, ally_owner)
            roles_updated += 1

    if roles_updated == 0:
        return "No roles updated."
    elif roles_updated == 1:
        return "Updated roles for %d member." % roles_updated
    else:
        return "Updated roles for %d members." % roles_updated


async def verify(member, roles, player_name):
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
    unverified = get(roles, name="Unverified")
    member_of = get(roles, name="Member of Chiefs of Corkus")

    ally = get(roles, name="Ally")
    # ool_member = get(roles, name="OoL Member")
    ally_owner = get(roles, name="Ally Guild Owner")

    has_updated = False

    for i in range(0, len(g_json['members'])):
        if g_json['members'][i]["name"].lower() == player_name.lower():
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
            break

    for i in range(0, len(ally_guilds)):
        for j in range(0, len(ally_guilds[i]['members'])):
            if ally_guilds[i]['members'][j]["name"].lower() == player_name.lower():
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

                break

    if not has_updated:
        if get(member.roles, name="Unverified") is None:
            await member.add_roles(unverified)
            await member.remove_roles(vip, vip_plus, hero, champion, vet, recruit, recruiter, captain, strategist,
                                      chief, owner, member_of, ally, ally_owner)
            return "%s is not a member of Chief's Of Corkus, or its allies." % player_name
    else:
        await member.add_roles(member_of)
        await member.remove_roles(unverified)

    try:
        await member.edit(nick=player_name)
    except Exception as e:
        return "Verified %s as Guild member %s. (Could not change nickname)" % (member.name, player_name)

    return "Verified %s as Guild member %s" % (member.name, player_name)
