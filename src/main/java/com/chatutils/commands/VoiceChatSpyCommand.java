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

public class VoiceChatSpyCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public VoiceChatSpyCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chatutils.voicechat.admin") && !player.hasPermission("chatutils.vcspy")) {
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
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-spy-usage"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // 1. Dinlemeyi Durdurma
        if (sub.equals("stop") || sub.equals("dur") || sub.equals("kapat") || sub.equals("leave")) {
            if (!vcManager.isSpying(player)) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-in-group"));
                return true;
            }
            UUID spiedId = vcManager.getSpiedGroupId(player.getUniqueId());
            Group spiedGroup = vcManager.getGroup(spiedId);
            String groupName = (spiedGroup != null && spiedGroup.getName() != null) ? spiedGroup.getName() : "Grup";

            vcManager.stopSpying(player);
            Map<String, String> pl = Map.of("group", groupName);
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-leave-success", pl));
            return true;
        }

        // 2. Gruba Konuşma Modunu Açma/Kapatma
        if (sub.equals("talk") || sub.equals("konus")) {
            if (!vcManager.isSpying(player)) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cÖnce bir grubu gizlice dinlemeye başlamalısınız! Örnek: §e/vcspy <grup>");
                return true;
            }

            boolean currentTalk = vcManager.isSpyTalkEnabled(player.getUniqueId());
            boolean newTalk = !currentTalk;

            if (args.length >= 2) {
                String val = args[1].toLowerCase(Locale.ROOT);
                if (val.equals("on") || val.equals("ac") || val.equals("true") || val.equals("aç")) {
                    newTalk = true;
                } else if (val.equals("off") || val.equals("kapat") || val.equals("false")) {
                    newTalk = false;
                }
            }

            vcManager.setSpyTalkEnabled(player.getUniqueId(), newTalk);
            if (newTalk) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§a[VoiceChat] Gizli grupta mikrofonunuz §eAÇILDI§a. Konuştuğunuzda gruptakiler sesinizi duyacaktır (Fakat sol üstte gözükmezsiniz).");
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§e[VoiceChat] Gizli grupta mikrofonunuz §cKAPATILDI§e. Artık tam hayalet (salt dinleyici) modundasınız.");
            }
            return true;
        }

        // 3. Yeni Grubu Gizlice Dinlemeye Başlama
        String query = String.join(" ", args);
        Group group = vcManager.findGroup(query);
        if (group == null) {
            Map<String, String> pl = Map.of("input", query);
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-group-not-found", pl));
            return true;
        }

        boolean success = vcManager.startSpying(player, group, false);
        if (success) {
            String groupName = group.getName() != null ? group.getName() : "Grup";
            String passwordTag = group.hasPassword() ? plugin.getConfigManager().getMessage("vc-password-locked") : plugin.getConfigManager().getMessage("vc-password-open");
            int memberCount = vcManager.getPlayersInGroup(group).size();

            Map<String, String> placeholders = Map.of(
                    "group", groupName,
                    "password", passwordTag,
                    "count", String.valueOf(memberCount),
                    "type", VoiceChatManager.formatGroupType(group.getType())
            );

            List<String> lines = plugin.getConfigManager().getMessageList("vc-join-stealth-success", placeholders);
            for (String line : lines) {
                player.sendMessage(line);
            }
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cGruba gizli bağlanırken bir sorun oluştu.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.voicechat.admin") && !sender.hasPermission("chatutils.vcspy")) {
            return Collections.emptyList();
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>(List.of("stop", "talk"));

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

        if (args.length == 2 && args[0].equalsIgnoreCase("talk")) {
            String input = args[1].toLowerCase(Locale.ROOT);
            List<String> opts = List.of("on", "off");
            List<String> filtered = new ArrayList<>();
            for (String opt : opts) {
                if (opt.startsWith(input)) filtered.add(opt);
            }
            return filtered;
        }

        return Collections.emptyList();
    }
}
