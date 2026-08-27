package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.hook.VoiceChatManager;
import de.maxhenkel.voicechat.api.Group;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VoiceChatLeaveCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public VoiceChatLeaveCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chatutils.voicechat.admin") && !player.hasPermission("chatutils.vcleave")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-available"));
            return true;
        }

        boolean wasSpying = vcManager.isSpying(player);
        Group playerGroup = vcManager.getPlayerGroup(player);

        if (!wasSpying && playerGroup == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-in-group"));
            return true;
        }

        String groupName = "Grup";
        if (wasSpying) {
            Group spied = vcManager.getGroup(vcManager.getSpiedGroupId(player.getUniqueId()));
            if (spied != null && spied.getName() != null) {
                groupName = spied.getName();
            }
        } else if (playerGroup.getName() != null) {
            groupName = playerGroup.getName();
        }

        vcManager.leaveGroup(player);

        Map<String, String> pl = Map.of("group", groupName);
        player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-leave-success", pl));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
