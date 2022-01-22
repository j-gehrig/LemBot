package tech.goodquestion.lembot.command.impl.db;

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

public class TopActiveChannelsCommand implements IBotCommand {

    @Override
    public void dispatch(Message message, TextChannel channel, Member sender, String[] args) {


        try {
            MessageEmbed messageEmbed = QueryHelper.getTopChannels()
                    .setColor(Color.decode(EmbedColorHelper.TOP_CHANNELS))
                    .setTitle("Die 5 aktivsten Channels")
                    .setThumbnail("https://cdn.discordapp.com/attachments/819694809765380146/872673996280303616/Bildschirmfoto_2021-08-05_um_04.54.26.png")
                    .build();
            channel.sendMessageEmbeds(messageEmbed).queue();

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
        return "`?topc`: Die 5 aktivsten Channels";
    }
}