package tech.goodquestion.lembot.command.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.goodquestion.lembot.command.IBotCommand;
import tech.goodquestion.lembot.lib.EmbedColorHelper;
import tech.goodquestion.lembot.lib.Helper;

public class CodeBlockHelpCommand implements IBotCommand {

    @Override
    public void dispatch(Message message, TextChannel channel, Member sender, String[] args) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        Helper.createEmbed(embedBuilder, "Farbige Codeblöcke",
                "So sendest du farbige Codeblöcke: \n python nprint('test')```",
                EmbedColorHelper.HIGHLIGHTED_CODE_BLOCK);
        embedBuilder.setImage("https://cdn.discordapp.com/attachments/919074434021736507/921164390340898906/Bildschirmfoto_2021-12-16_um_23.18.15.png");

        channel.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "hcb";
    }

    @Override
    public String getDescription() {
        return "`?hcb`: farbige Codeblöcke ";
    }
}
