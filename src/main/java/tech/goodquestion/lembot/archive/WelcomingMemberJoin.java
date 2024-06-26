package tech.goodquestion.lembot.archive;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.goodquestion.lembot.config.Config;
import tech.goodquestion.lembot.library.Helper;


public final class WelcomingMemberJoin extends ListenerAdapter {

    @SuppressWarnings("null")
@Override
    public void onGuildMemberJoin(@SuppressWarnings("null") @NotNull final GuildMemberJoinEvent event) {

        final String welcomeMessage = """
                Hallo [member], Willkommen auf **GoodQuestion (GQ)**!

                Du kannst Dir im Kanal [channel] Rollen zuweisen.

                """;

        final String newMemberAsMention = event.getMember().getAsMention();

        final String personalizedWelcomeMessage = welcomeMessage
                .replace("[member]", newMemberAsMention)
                .replace("[channel]", Config.getInstance().getChannelConfig().getSelfRolesChannel().getAsMention());

       // Config.getInstance().getChannelConfig().getNewArrivalsChannel().sendMessage(personalizedWelcomeMessage).queue();

        Config.getInstance().getChannelConfig().getJoinLeftChannel()
                .sendMessage(":arrow_right: User " + newMemberAsMention + " ist am " + Helper.getCurrentCETDateTime() + " **gejoint**")
                .queue();
    }
}
