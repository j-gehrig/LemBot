package tech.goodquestion.lembot.command.impl.database;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.goodquestion.lembot.command.IBotCommand;
import tech.goodquestion.lembot.database.CommandHelper;
import tech.goodquestion.lembot.database.QueryHelper;
import tech.goodquestion.lembot.entity.OccurredException;
import tech.goodquestion.lembot.library.EmbedColorHelper;

import java.awt.*;
import java.sql.SQLException;

public final class TopActiveChannelsCommand implements IBotCommand {

    @Override
    public void dispatch(final Message message, final TextChannel channel, final Member sender, final String[] args) {

        try {
            MessageEmbed messageEmbed = QueryHelper.getTopChannels().setColor(Color.decode(EmbedColorHelper.TOP_CHANNELS)).setTitle("Die 5 aktivsten Channels").setThumbnail("https://cdn.discordapp.com/attachments/943983785547030598/1226480271780614154/channel.png?ex=6624ebc3&is=661276c3&hm=f1f339793655f68644bcd4bf7e2e7f80c87ee3de0578ff56cd4c0c7c5313976a&").build();
            message.replyEmbeds(messageEmbed).queue();

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
            CommandHelper.logException(OccurredException.getOccurredExceptionData(sqlException, this.getClass().getName()));
        }
    }

    @Override
    public String getName() {
        return "topc";
    }

    @Override
    public String getDescription() {
        return "`topc`: Die 5 aktivsten Channels";
    }

    @Override
    public boolean isPermitted(final Member member) {
        return true;
    }

    @Override
    public String getHelpList() {
        return "stats";
    }
}