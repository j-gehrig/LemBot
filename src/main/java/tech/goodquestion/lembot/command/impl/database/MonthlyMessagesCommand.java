package tech.goodquestion.lembot.command.impl.database;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.goodquestion.lembot.command.IBotCommand;
import tech.goodquestion.lembot.database.QueryHelper;
import tech.goodquestion.lembot.entity.UserData;
import tech.goodquestion.lembot.library.EmbedColorHelper;
import tech.goodquestion.lembot.library.Helper;

public class MonthlyMessagesCommand implements IBotCommand {
    @Override
    public void dispatch(final Message message, final TextChannel channel, final Member sender, final String[] args) {

        final UserData userData = new UserData();
        userData.userId = sender.getIdLong();
        final String embedTitle = "Monthly Nachrichten";

        Helper.getAmount(userData, QueryHelper.AMOUNT_MONTHLY_MESSAGES, QueryHelper.NEXT_HIGHER_USER_AMOUNT_MONTHLY_MESSAGES);
        Helper.sendAmount(userData, EmbedColorHelper.FLOOD, "monthly Nachrichten", message, embedTitle);
    }

    @Override
    public String getName() {
        return "hmmm";
    }

    @Override
    public String getDescription() {
        return "`hmmm`: Anzahl deiner geschickten Nachrichten in den letzten 30 Tagen";
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