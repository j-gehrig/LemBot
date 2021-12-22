package tech.goodquestion.lembot.event;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tech.goodquestion.lembot.config.Config;
import tech.goodquestion.lembot.database.CommandHelper;
import tech.goodquestion.lembot.database.QueryHelper;
import tech.goodquestion.lembot.entity.Sanction;

import java.util.List;
import java.util.Objects;

public class SpamDetection extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {


        final long userId = event.getMessage().getAuthor().getIdLong();
        final boolean senderIsBot = event.getMessage().getAuthor().isBot();
        final boolean senderIsStaff = Objects.requireNonNull(event.getMessage().getMember()).hasPermission(Permission.MESSAGE_MANAGE);
        final Member member = event.getMember();
        final String userAsMention = event.getMessage().getAuthor().getAsMention();
        final String messageContent = event.getMessage().getContentRaw();
        assert member != null;
        final List<Role> userRoles = member.getRoles();

        final Sanction sanction = new Sanction();
        sanction.userId = userId;
        sanction.author = "LemBot#1207";
        sanction.userTag = event.getMessage().getAuthor().getAsTag();
        sanction.userName = event.getMessage().getAuthor().getName();
        sanction.reason = "Spam";
        sanction.channelName = event.getMessage().getChannel().getName();

        if (senderIsBot || senderIsStaff) return;


        if (QueryHelper.isSpammer(userId, messageContent)) {

            disconnect(member);

            QueryHelper.deleteScammerMessages(event, userId, messageContent);


            for (final Role role : userRoles) {

                event.getGuild().removeRoleFromMember(userId, role).queue();
            }



            final Role mutedRole = Config.getInstance().getRole().getMuteRole();

            assert mutedRole != null;
            event.getGuild().addRoleToMember(userId, mutedRole).queue();

            CommandHelper.logUserMute(sanction);
            event.getChannel().sendMessage("Du wurdest aufgrund verdächtigem Verhalten durch den Bot **gemutet** " + userAsMention + ".").queue();


        }

        if (QueryHelper.areToManyMessages(userId, messageContent)) {

            disconnect(member);

            for (final Role role : userRoles) {

                event.getGuild().removeRoleFromMember(userId, role).queue();
            }

            final Role mutedRole = Config.getInstance().getRole().getMuteRole();

            assert mutedRole != null;
            event.getGuild().addRoleToMember(userId, mutedRole).queue();

            CommandHelper.deleteSpammerMessages(event,userId);
            CommandHelper.logUserMute(sanction);

            event.getChannel().sendMessage("Du wurdest aufgrund verdächtigem Verhalten durch den Bot **gemutet** " + userAsMention + ".").queue();
        }


    }


    private void disconnect(Member member) {

        if (member.getVoiceState().inVoiceChannel()) {
            member.getGuild().kickVoiceMember(member).queue();
        }
    }
}
