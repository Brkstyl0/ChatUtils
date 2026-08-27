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

public class VoiceChatJoinCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public VoiceChatJoinCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chatutils.voicechat.admin") && !player.hasPermission("chatutils.vcjoin")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-available"));
            return true;
        }

        VoicechatConnection conn = vcManager.getServerApi().getConnectionOf(player.getUniqueId());
        if (conn == null || !conn.isConnected()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-connected"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-join-usage"));
            return true;
        }

        boolean visibleFlag = false;
        String query;

        if (args.length >= 2 && (args[1].equalsIgnoreCase("--visible") || args[1].equalsIgnoreCase("-v"))) {
            visibleFlag = true;
            query = args[0];
        } else if (args[0].equalsIgnoreCase("--visible") || args[0].equalsIgnoreCase("-v")) {
            if (args.length >= 2) {
                visibleFlag = true;
                query = args[1];
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-join-usage"));
                return true;
            }
        } else {
            query = String.join(" ", args);
        }

        Group group = vcManager.findGroup(query);
        if (group == null) {
            Map<String, String> pl = Map.of("input", query);
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-group-not-found", pl));
            return true;
        }

        String groupName = group.getName() != null ? group.getName() : "Grup";
        String passwordTag = group.hasPassword() ? plugin.getConfigManager().getMessage("vc-password-locked") : plugin.getConfigManager().getMessage("vc-password-open");
        int memberCount = vcManager.getPlayersInGroup(group).size();

        // Eğer oyuncu Vanish'te ise her zaman Stealth (Gizli) olarak bağlan
        boolean isVanished = plugin.getVanishManager().isVanished(player);
        boolean useStealth = !visibleFlag || isVanished;

        Map<String, String> placeholders = Map.of(
                "group", groupName,
                "password", passwordTag,
                "count", String.valueOf(memberCount),
                "type", VoiceChatManager.formatGroupType(group.getType())
        );

        if (useStealth) {
            // GİZLİ DİNLEME (Stealth Ghost Spy) - SOL ÜSTTE KESİNLİKLE GÖZÜKMEZ
            boolean success = vcManager.startSpying(player, group, false);
            if (success) {
                List<String> lines = plugin.getConfigManager().getMessageList("vc-join-stealth-success", placeholders);
                for (String line : lines) {
                    player.sendMessage(line);
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cGruba gizli bağlanırken bir sorun oluştu.");
            }
        } else {
            // GÖRÜNÜR RESMİ GİRİŞ (Sol üstte gözükür)
            boolean success = vcManager.joinGroupVisible(player, group);
            if (success) {
                List<String> lines = plugin.getConfigManager().getMessageList("vc-join-visible-success", placeholders);
                for (String line : lines) {
                    player.sendMessage(line);
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cGruba katılırken bir sorun oluştu.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.voicechat.admin") && !sender.hasPermission("chatutils.vcjoin")) {
            return Collections.emptyList();
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>();

            for (Group g : vcManager.getGroups()) {
                if (g.getName() != null && g.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    suggestions.add(g.getName());
                }
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                Group pg = vcManager.getPlayerGroup(p);
                if (pg != null && p.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    suggestions.add(p.getName());
                }
            }

            return suggestions;
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase(Locale.ROOT);
            return List.of("--visible").stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
