import wynnapi
from discord.utils import get

async def handle_response(members, roles, message):
    p_message = message.split()

    if p_message[0] == "member":
        for i in range(0, len(wynnapi.json['members'])):
            if wynnapi.json['members'][i]["name"] == p_message[1]:
                return wynnapi.json['members'][i]

        return "No member found with that username"

    if p_message[0] == "test":
        print(wynnapi.json['data'][0])
        test = "Username: "
        test += wynnapi.json['data'][0]["username"]
        test += ", Rank:"
        test += wynnapi.json['data'][0]['meta']['tag']["value"]
        test += ", Veteran:"
        test += str(wynnapi.json['data'][0]['meta']["veteran"])
        return test

    if p_message[0] == "updateranks":
        owner = get(roles, name="Owner")
        chief = get(roles, name="Chief")
        strategist = get(roles, name="Strategist")
        captain = get(roles, name="Captain")
        recruiter = get(roles, name="Recruiter")
        recruit = get(roles, name="Recruit")

        rolesUpdated = 0

        for i in range(0, len(wynnapi.json['members'])):
            for member in members:
                if wynnapi.json['members'][i]["name"] == member.name or wynnapi.json['members'][i]["name"] == member.nick:
                    uuid = wynnapi.json['members'][i]['uuid']

                    match wynnapi.json['members'][i]["rank"]:
                        case "OWNER":
                            if get(member.roles, name="Owner") is None:
                                print(member.name + " is the Owner of the Guild.")
                                await member.add_roles(owner)
                                await member.remove_roles(chief, strategist, captain, recruiter, recruit)
                                rolesUpdated += 1
                        case "CHIEF":
                            if get(member.roles, name="Chief") is None:
                                print(member.name + " is a Chief of the Guild.")
                                await member.add_roles(chief)
                                await member.remove_roles(owner, strategist, captain, recruiter, recruit)
                                rolesUpdated += 1
                        case "STRATEGIST":
                            if get(member.roles, name="Strategist") is None:
                                print(member.name + " is a Strategist of the Guild.")
                                await member.add_roles(strategist)
                                await member.remove_roles(owner, chief, captain, recruiter, recruit)
                                rolesUpdated += 1
                        case "CAPTAIN":
                            if get(member.roles, name="Captain") is None:
                                print(member.name + " is a Captain of the Guild.")
                                await member.add_roles(captain)
                                await member.remove_roles(owner, chief, strategist, recruiter, recruit)
                                rolesUpdated += 1
                        case "RECRUITER":
                            if get(member.roles, name="Recruiter") is None:
                                print(member.name + " is a Recruiter of the Guild.")
                                await member.add_roles(recruiter)
                                await member.remove_roles(owner, chief, strategist, captain, recruit)
                                rolesUpdated += 1
                        case "RECRUIT":
                            if get(member.roles, name="Recruit") is None:
                                print(member.name + " is a Recruit of the Guild.")
                                await member.add_roles(recruit)
                                await member.remove_roles(owner, chief, strategist, captain, recruiter)
                                rolesUpdated += 1



        if rolesUpdated == 0:
            return "No roles updated."
        elif rolesUpdated == 1:
            return "Updated roles for %d member." % rolesUpdated
        else:
            return "Updated roles for %d members." % rolesUpdated

    return "Command %s not recognised" % p_message[0]