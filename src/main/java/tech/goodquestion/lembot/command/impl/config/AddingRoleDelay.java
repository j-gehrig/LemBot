package tech.goodquestion.lembot.command.impl.config;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.goodquestion.lembot.command.IBotCommand;

import java.io.IOException;

public class AddingRoleDelay implements IBotCommand {
    @Override
    public void dispatch(Message message, TextChannel channel, Member sender, String[] args) throws IOException {

    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return null;
    }


    @Override
    public String getHelpList() {
        return "config";
    }
}
