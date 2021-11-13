package tech.goodquestion.lembot.command.impl.db;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.goodquestion.lembot.command.IBotCommand;
import tech.goodquestion.lembot.database.DatabaseConnector;
import tech.goodquestion.lembot.lib.Helper;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TopBumperCommand implements IBotCommand {

    @Override
    public void dispatch(Message message, TextChannel channel, Member sender, String[] args) {

        Connection connection = DatabaseConnector.openConnection();

        String topBumper = "SELECT username FROM user_bump ORDER BY number_bumps DESC, username LIMIT 3;";

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(topBumper)) {

            EmbedBuilder topBumperEmbed = new EmbedBuilder();

            String embedTitle = "User mit den meisten Bumps";
            String embedDescription = "";
            String embedThumbnail = "https://cdn.discordapp.com/attachments/819694809765380146/869248076446052402/Bildschirmfoto_2021-07-26_um_17.48.08.png";
            Color embedColor = Color.cyan;

            Helper.addTopToEmbed(resultSet, topBumperEmbed, embedTitle, embedDescription, embedThumbnail, embedColor, channel);

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    @Override
    public String getName() {
        return "topb";
    }

    @Override
    public String getDescription() {
        return "`?topb`: Zeigt die Top Bumper des Servers an";
    }
}