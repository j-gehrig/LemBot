package tech.goodquestion.lembot.database;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.goodquestion.lembot.entity.OccurredException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QueryHelper {


    public static final String TOP_CHANNELS = "SELECT channel_name, COUNT(id_channel) FROM `channel` GROUP BY id_channel ORDER BY COUNT(id_channel) DESC LIMIT 5;";
    public static final String NEXT_BUMP_TIME = "SELECT TIME(TIMESTAMPADD(HOUR,2, bumped_on)) FROM user_bump_time ORDER BY bumped_on DESC LIMIT 1";
    public static final String NEXT_BUMP = "SELECT TIMESTAMPDIFF(MINUTE,CURRENT_TIMESTAMP, TIMESTAMPADD(HOUR,2, bumped_on)) FROM user_bump_time ORDER BY bumped_on DESC LIMIT 1";
    public static final String ACTIVE_USER_RECORD = "SELECT MAX(user_online.amount) FROM user_online;";
    public static final String TOP_MONTHLY_BUMPER = "SELECT username, COUNT(user_bump_time.id_discord) FROM `user_bump_time` INNER JOIN user_bump ON user_bump_time.id_discord = user_bump.id_discord  WHERE bumped_on > (SELECT DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 MONTH)) GROUP BY user_bump_time.id_discord ORDER BY COUNT(user_bump_time.id_discord) DESC LIMIT 3";
    public static final String TOP_MONTHLY_FLOODER = "SELECT username, COUNT(user_message_content.id_discord) FROM `user_message_content` INNER JOIN user_message ON user_message_content.id_discord = user_message.id_discord  WHERE posted_on > (SELECT DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 MONTH)) GROUP BY user_message.id_discord ORDER BY COUNT(user_message_content.id_discord) DESC LIMIT 3;";
    public static final String TOP_BUMPER = "SELECT username, number_bumps FROM user_bump ORDER BY number_bumps DESC, username LIMIT 3;";
    public static final String TOP_FLOODER = "SELECT username, number_message FROM user_message ORDER BY number_message DESC LIMIT 3;";
    public static final String AMOUNT_MESSAGES = "SELECT number_message FROM user_message WHERE id_discord = ?";
    public static final String NEXT_HIGHER_USER_AMOUNT_MESSAGES = "SELECT id_discord, number_message FROM user_message WHERE number_message > ? ORDER BY number_message, username LIMIT 1";
    public static final String SPAM_VERIFICATION = "SELECT COUNT(DISTINCT id_channel) FROM `channel` INNER JOIN user_message_content ON channel.id_message = user_message_content.id_message WHERE user_message_content.id_discord = ? AND content = ? AND posted_on >= NOW() - INTERVAL 1 MINUTE";
    public static final String SPAM_DATA = "SELECT id_channel, channel.id_message FROM `channel` INNER JOIN user_message_content ON channel.id_message = user_message_content.id_message WHERE user_message_content.id_discord = ? AND content = ? AND posted_on >= NOW() - INTERVAL 1 MINUTE";
    public static final String AMOUNT_SPAM_MESSAGES = "SELECT COUNT(id_discord) FROM user_message_content WHERE id_discord = ? AND content = ? AND posted_on >= NOW() - INTERVAL 30 SECOND";
    private static final String HOPPING_CHECK = "SELECT COUNT(id_discord) FROM voice_join WHERE id_discord = ? AND joined_at >= NOW() - INTERVAL 30 SECOND";
    private static String MESSAGE_COUNT = "SELECT COUNT(user_message_content.id_discord) + 40000 FROM user_message_content";
    private static String ADMINS_MENTIONED = "SELECT mention FROM staff WHERE role_name = 'Administrator' AND mention != '<@739143338975952959>' ORDER BY staff_since;";
    private static String MODERATORS_MENTIONED = "SELECT mention FROM staff WHERE role_name = 'Moderator' ORDER BY staff_since;";
    public static final List<String> adminsAsMention = new ArrayList<>();
    public static final List<String> moderatorsAsMention = new ArrayList<>();

    private QueryHelper() {

    }


    //string query
    private static EmbedBuilder getTop(String[] fieldNames, String[] fieldIcons) throws SQLException {

        try (Connection connection = DatabaseConnector.openConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(QueryHelper.TOP_CHANNELS)) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription("");

            int top = 1;

            while (resultSet.next()) {
                if (top == 1) {
                    embed.addField(fieldNames[0], fieldIcons[0] + resultSet.getString(1), false);
                }
                if (top == 2) {
                    embed.addField(fieldNames[1], fieldIcons[1] + resultSet.getString(1), false);
                }
                if (top == 3) {
                    embed.addField(fieldNames[2], fieldIcons[2] + resultSet.getString(1), false);
                }
                top++;
            }
            return embed;
        }
    }


    public static EmbedBuilder getTopActiveChannels() throws SQLException {
        return getTop(new String[]{"**TOP 1**", "**TOP 2**", "**TOP 3**"}, new String[]{"", "", ""});
    }


    public static Time getNextBumpTime() {

        Time nextBumpTime = null;

        try (Connection connection = DatabaseConnector.openConnection(); PreparedStatement preparedStatement = connection.prepareStatement(NEXT_BUMP_TIME)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                nextBumpTime = resultSet.getTime(1);
            }
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }

        return nextBumpTime;
    }

    public static int getMinutesToNextBump() {

        int minutesBeforeBump = 0;

        try (Connection connection = DatabaseConnector.openConnection(); PreparedStatement preparedStatement = connection.prepareStatement(NEXT_BUMP)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                minutesBeforeBump = resultSet.getInt(1);
            }
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());

            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }

        return minutesBeforeBump;
    }

    public static int getActiveUserRecord() {

        return getServerData(ACTIVE_USER_RECORD);
    }


    public static boolean isActiveUserRecord(int approximatePresentMember) {


        try (Connection connection = DatabaseConnector.openConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(ACTIVE_USER_RECORD)) {


            if (resultSet.next()) {

                int currentActiveUseRecord = resultSet.getInt(1);

                if (approximatePresentMember > currentActiveUseRecord) {
                    return true;
                }
            }

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());

            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }
        return false;
    }

    public static boolean isSpammer(long userId, String messageContent) {

        try (Connection connection = DatabaseConnector.openConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SPAM_VERIFICATION)) {

            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, messageContent);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                int amountResult = resultSet.getInt(1);

                if (amountResult >= 3) {
                    return true;
                }
            }

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
        return false;
    }


    public static void deleteSpammerMessages(GuildMessageReceivedEvent event, long userId, String messageContent) {


        try (Connection connection = DatabaseConnector.openConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SPAM_DATA)) {


            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, messageContent);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Objects.requireNonNull(event.getGuild()
                        .getTextChannelById(resultSet.getLong("id_channel")))
                        .deleteMessageById(resultSet.getLong("id_message"))
                        .queue();

            }

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());

            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }

        event.getMessage().delete().queue();
    }

    public static boolean areToManyMessages(long userId, String messageContent) {

        try (Connection connection = DatabaseConnector.openConnection(); PreparedStatement preparedStatement = connection.prepareStatement(AMOUNT_SPAM_MESSAGES)) {

            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, messageContent);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                int amountResult = resultSet.getInt(1);

                if (amountResult >= 10) {
                    return true;
                }
            }

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());

            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }
        return false;
    }

    public static boolean isHopper(long userId) {


        try (Connection connection = DatabaseConnector.openConnection(); PreparedStatement preparedStatement = connection.prepareStatement(HOPPING_CHECK)) {

            preparedStatement.setLong(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                int amountHops = resultSet.getInt(1);

                if (amountHops >= 3) {
                    return true;
                }
            }

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());

            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }
        return false;

    }


    public static int getMessagesCount() {

        return getServerData(MESSAGE_COUNT);
    }

    public static int getServerData(String serverData) {
        try (Connection connection = DatabaseConnector.openConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(serverData)) {

            if (resultSet.next()) {

                return resultSet.getInt(1);

            }

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());

            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }
        return 0;
    }

    public static List<String> getAdminsAsMention() {

        if (adminsAsMention.size() > 0) return adminsAsMention;

        return getStaffAsMention(ADMINS_MENTIONED, adminsAsMention);

    }

    public static List<String> getStaffAsMention(String adminIds, List<String> adminsAsMention) {
        try (Connection connection = DatabaseConnector.openConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(adminIds)) {


            while (resultSet.next()) {

                adminsAsMention.add(resultSet.getString(1));

            }

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());

            CommandsHelper.logException(OccurredException.getOccurredExceptionData(sqlException, QueryHelper.class.getName()));
        }

        return adminsAsMention;
    }

    public static List<String> getModeratorsAsMention() {

        if (moderatorsAsMention.size() > 0) return moderatorsAsMention;

        return getStaffAsMention(MODERATORS_MENTIONED, moderatorsAsMention);

    }
}
