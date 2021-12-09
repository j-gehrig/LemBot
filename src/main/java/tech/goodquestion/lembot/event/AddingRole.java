package tech.goodquestion.lembot.event;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.goodquestion.lembot.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AddingRole extends ListenerAdapter {

    Map<Long, ScheduledFuture<?>> tasks = new HashMap<>();

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {

        Member member = event.getMember();
        Guild guild = member.getGuild();
        Role codingRole = Config.getInstance().getRoles().getCodingRole();

        int delay = 5;
        assert codingRole != null;
        ScheduledFuture<?> task = guild.addRoleToMember(member, codingRole).queueAfter(delay, TimeUnit.MINUTES);
        tasks.put(member.getIdLong(), task);
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {

        Member member = event.getMember();
        Guild guild = member.getGuild();
        Role codingRole = Config.getInstance().getRoles().getCodingRole();
        Role hackingRole = Config.getInstance().getRoles().getHackingRole();
        Role mutedRole = Config.getInstance().getRoles().getMuteRole();
        List<Role> memberAddedRoles = member.getRoles();


        for (Role addedRole : memberAddedRoles) {

            assert codingRole != null;
            assert hackingRole != null;
            assert mutedRole != null;

            boolean hasAlreadyAccess = addedRole.getIdLong() == codingRole.getIdLong() || addedRole.getIdLong() == hackingRole.getIdLong();
            boolean isMuted = addedRole.getIdLong() == mutedRole.getIdLong();

            if (hasAlreadyAccess || isMuted) {

                ScheduledFuture<?> task = tasks.remove(member.getIdLong());
                if (task != null) task.cancel(false);
                return;
            }
        }

        assert codingRole != null;
        guild.addRoleToMember(member, codingRole).queue();
        ScheduledFuture<?> task = tasks.remove(member.getIdLong());
        if (task != null) task.cancel(false);
    }

}