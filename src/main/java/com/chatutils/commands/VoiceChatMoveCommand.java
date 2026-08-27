package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.hook.VoiceChatManager;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class VoiceChatMoveCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public VoiceChatMoveCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.voicechat.admin") && !sender.hasPermission("chatutils.vcmove")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-available"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-move-usage"));
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        VoicechatConnection targetConn = vcManager.getServerApi().getConnectionOf(targetPlayer.getUniqueId());
        if (targetConn == null || !targetConn.isConnected()) {
            Map<String, String> pl = Map.of("target", targetPlayer.getName());
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-target-not-connected", pl));
            return true;
        }

        String groupQuery = args[1];

        // Gruptan çıkarma
        if (groupQuery.equalsIgnoreCase("leave") || groupQuery.equalsIgnoreCase("cikar") || groupQuery.equalsIgnoreCase("ayril")) {
            if (!targetConn.isInGroup()) {
                Map<String, String> pl = Map.of("target", targetPlayer.getName());
                sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-target-not-in-group", pl));
                return true;
            }
            vcManager.leaveGroup(targetPlayer);
            Map<String, String> pl = Map.of("target", targetPlayer.getName(), "staff", sender.getName());
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-move-leave-success", pl));
            return true;
        }

        Group targetGroup = vcManager.findGroup(groupQuery);
        if (targetGroup == null) {
            Map<String, String> pl = Map.of("input", groupQuery);
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-group-not-found", pl));
            return true;
        }

        boolean success = vcManager.joinGroupVisible(targetPlayer, targetGroup);
        if (success) {
            String groupName = targetGroup.getName() != null ? targetGroup.getName() : "Grup";
            Map<String, String> pl = Map.of("target", targetPlayer.getName(), "group", groupName, "staff", sender.getName());
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-move-success", pl));
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cOyuncu gruba taşınırken bir sorun oluştu.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.voicechat.admin") && !sender.hasPermission("chatutils.vcmove")) {
            return Collections.emptyList();
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>(List.of("leave"));
            for (Group g : vcManager.getGroups()) {
                if (g.getName() != null && g.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    list.add(g.getName());
                }
            }
            return list;
        }

        return Collections.emptyList();
    }
}
