/*
 * DiscordSRV - https://github.com/DiscordSRV/DiscordSRV
 *
 * Copyright (C) 2016 - 2022 Austin "Scarsz" Shapiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package github.scarsz.discordsrv.commands;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.objects.MetaData;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.*;
import net.dv8tion.jda.api.entities.User;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandIgnore {

    @Command(commandNames = { "ignore" },
            helpMessage = "Ignores a discord user, so you won't receive their messages in-game",
            permission = "discordsrv.ignore"
    )
    public static void execute(CommandSender sender, String[] args) {
        AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
        if (manager == null) {
            MessageUtil.sendMessage(sender, LangUtil.Message.UNABLE_TO_LINK_ACCOUNTS_RIGHT_NOW.toString());
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(DiscordSRV.getPlugin(), () -> executeAsync(sender, args, manager));
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    private static void executeAsync(CommandSender sender, String[] args, AccountLinkManager manager) {
        // assume manual link
        User user = null;
        try {
            user = DiscordUtil.getJda().getUserById(args[0]);
        } catch (IllegalArgumentException ignored) {}

        if (user == null) {
            try {
                user = DiscordUtil.getJda().getUserByTag(args[0]);
            } catch (IllegalArgumentException ignored) {}
        }

        if (user == null) {
            MessageUtil.sendMessage(sender, ChatColor.RED + "Discord user could not be found");
            return;
        }

        if(handleIgnoringDiscordUser( ((Player) sender).getUniqueId(), user.getName() ))
        {
            if(manager.getMetaDataByUUIDBypassCache(((Player) sender).getUniqueId()).getIgnored().contains(user.getName()))
                sender.sendMessage(LangUtil.Message.DISCORD_USER_IGNORE_LIST_ADDED.toString().replace("%name%", user.getName()));
            else
                sender.sendMessage(LangUtil.Message.DISCORD_USER_IGNORE_LIST_REMOVED.toString().replace("%name%", user.getName()));
        }
        else
            sender.sendMessage("Failed to mute discord user " + user.getName());
    }


    private static boolean handleIgnoringDiscordUser(UUID uuid, String user)
    {
        AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
        MetaData metaData = manager.getMetaDataByUUID(uuid);

        List<String> ignoredUsers;
        if(metaData != null)
            ignoredUsers = metaData.getIgnored();
        else
        {
            metaData = new MetaData();
            ignoredUsers = new ArrayList<>();
        }

        if(ignoredUsers.contains(user))
            ignoredUsers.remove(user);
        else
            ignoredUsers.add(user);

        metaData.setIgnored(ignoredUsers);
        manager.updateChackedMetaData(uuid, metaData);
        return manager.saveMetaData(uuid);
    }
}
