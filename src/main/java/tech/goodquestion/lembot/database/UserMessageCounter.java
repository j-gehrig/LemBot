package tech.goodquestion.lembot.database;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tech.goodquestion.lembot.entity.OccurredException;
import tech.goodquestion.lembot.library.Helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class UserMessageCounter extends ListenerAdapter {

    @Override
    public void onMessageReceived(@SuppressWarnings("null") final MessageReceivedEvent event) {

        String userMessageContent = event.getMessage().getContentRaw();
        final String userId = Objects.requireNonNull(event.getMember()).getId();
        @SuppressWarnings("null") final String userName = event.getMember().getEffectiveName();
        final String messageId = event.getMessageId();

        final boolean containsAttachment = !event.getMessage().getAttachments().isEmpty();

        if (containsAttachment) {
            userMessageContent += "\n__Bildurl__\n" + event.getMessage().getAttachments().get(0).getUrl();
        }

        int numberMessage = 1;

        if (userMessageContent.isEmpty() || event.getAuthor().isBot()) return;

        Connection connection = DatabaseConnector.openConnection();
        final String insertMessageData = "INSERT INTO user_message (id_discord, username, number_message) VALUES (?,?,?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertMessageData)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, userName);
            preparedStatement.setInt(3, numberMessage);

            final String isUserInDB = "SELECT id_discord FROM user_message WHERE id_discord = ? ";
            PreparedStatement prepareStatementOne = connection.prepareStatement(isUserInDB);
            prepareStatementOne.setString(1, userId);
            ResultSet resultSet = prepareStatementOne.executeQuery();

            if (resultSet.next()) {
                final String currentNumberMessage = "UPDATE user_message SET number_message = (number_message +1) WHERE id_discord = ?";
                PreparedStatement updatePStatement = connection.prepareStatement(currentNumberMessage);
                updatePStatement.setString(1, userId);
                updatePStatement.executeUpdate();

            } else {
                preparedStatement.executeUpdate();
            }

            insertData(userMessageContent, userId, messageId);

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
            CommandHelper.logException(OccurredException.getOccurredExceptionData(sqlException, this.getClass().getName()));
        }

    }

    private void insertData(final String userMessage, final String userId, final String messageId) {

        Connection connection = DatabaseConnector.openConnection();
        final String userMessageData = "INSERT INTO user_message_content (id_message, id_discord, content) VALUES (?,?,?)";

        try (PreparedStatement insertPStatement = connection.prepareStatement(userMessageData)) {

            insertPStatement.setString(1, messageId);
            insertPStatement.setString(2, userId);
            insertPStatement.setBlob(3, Helper.changeCharacterEncoding(insertPStatement, userMessage));
            insertPStatement.executeUpdate();

        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
            CommandHelper.logException(OccurredException.getOccurredExceptionData(sqlException, this.getClass().getName()));
        }
    }
}