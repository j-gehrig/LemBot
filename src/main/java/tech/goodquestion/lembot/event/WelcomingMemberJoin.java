package tech.goodquestion.lembot.event;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.goodquestion.lembot.config.Config;
import tech.goodquestion.lembot.lib.Helper;


public class WelcomingMemberJoin extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull final GuildMemberJoinEvent event) {

        if (event.getUser().isBot()) return;

        final String welcomeMessage = """
                Hallo [member], Willkommen auf **GoodQuestion (GQ)**!

                Du kannst Dir im Kanal [channel] Rollen zuweisen.

                """;
        final String avatarUrl = event.getUser().getEffectiveAvatarUrl();
        final String newMember = event.getMember().getAsMention();

        final String output = welcomeMessage
                .replace("[member]", newMember)
                .replace("[channel]", Config.getInstance().getChannel().getSelfRolesChannel().getAsMention());

        Config.getInstance().getChannel().getNewArrivalsChannel().sendMessage(output).queue();

        Config.getInstance().getChannel().getLogChannel().sendMessage("---> User " + newMember + " ist am " + Helper.getGermanDateTime() + " **gejoint**").queue();
        Config.getInstance().getChannel().getLogChannel().sendMessage(avatarUrl).queue();

    }
}
