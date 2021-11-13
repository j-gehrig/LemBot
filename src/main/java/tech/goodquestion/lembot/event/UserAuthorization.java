package tech.goodquestion.lembot.event;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.goodquestion.lembot.config.Config;
import tech.goodquestion.lembot.database.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAuthorization extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {

        long joinedUserId = event.getMember().getIdLong();

        String userVerificationCheck = "SELECT activ FROM muted_user WHERE id_discord = ? ORDER BY muted_on DESC LIMIT 1;";

        Connection connection = DatabaseConnector.openConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(userVerificationCheck)) {

            preparedStatement.setLong(1, joinedUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            long userId = event.getMember().getIdLong();

            if (resultSet.next()) {

                int number = resultSet.getInt(1);

                if (number == 1) {
                    event.getGuild().addRoleToMember(userId, Config.getInstance().getRoles().getMuteRole()).queue();
                }
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}