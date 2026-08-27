package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.hook.VoiceChatManager;
import com.chatutils.utils.ColorUtil;
import de.maxhenkel.voicechat.api.Group;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class VoiceChatInfoCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public VoiceChatInfoCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.voicechat.admin") && !sender.hasPermission("chatutils.vcinfo")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-available"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-info-usage"));
            return true;
        }

        String query = String.join(" ", args);
        Group group = vcManager.findGroup(query);

        if (group == null) {
            Map<String, String> pl = Map.of("input", query);
            playerNotFoundMessage(sender, pl);
            return true;
        }

        String groupName = group.getName() != null ? group.getName() : "İsimsiz";
        String passwordTag = group.hasPassword() ? "§cŞifreli (Passworded)" : "§aAçık (Şifresiz)";
        List<Player> members = vcManager.getPlayersInGroup(group);
        String memberList = members.isEmpty() ? "§7Yok" : members.stream().map(Player::getName).collect(Collectors.joining("§7, §f"));

        Set<UUID> spyingAdmins = vcManager.getSpyingAdminsForGroup(group.getId());
        String spyList = spyingAdmins.isEmpty() ? "§7Yok" : spyingAdmins.stream()
                .map(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    return p != null ? p.getName() : uuid.toString().substring(0, 8);
                })
                .collect(Collectors.joining("§7, §e"));

        sender.sendMessage(ColorUtil.colorize("&8&m----------------&r &6&lSES GRUBU BİLGİSİ &8&m----------------"));
        sender.sendMessage(ColorUtil.colorize("&8» &eGrup Adı: &f" + groupName));
        sender.sendMessage(ColorUtil.colorize("&8» &eGrup UUID: &7" + group.getId()));
        sender.sendMessage(ColorUtil.colorize("&8» &eŞifre Durumu: " + passwordTag));
        sender.sendMessage(ColorUtil.colorize("&8» &eGrup Türü: &b" + VoiceChatManager.formatGroupType(group.getType())));
        sender.sendMessage(ColorUtil.colorize("&8» &eKalıcı mı: &f" + (group.isPersistent() ? "§aEvet" : "§7Hayır")));
        sender.sendMessage(ColorUtil.colorize("&8» &eGizli mi (UI): &f" + (group.isHidden() ? "§aEvet" : "§7Hayır")));
        sender.sendMessage(ColorUtil.colorize("&8» &eÜyeler (" + members.size() + "): &f" + memberList));
        sender.sendMessage(ColorUtil.colorize("&8» &eGizli Dinleyen Yetkililer (" + spyingAdmins.size() + "): &e" + spyList));
        sender.sendMessage(ColorUtil.colorize("&8&m--------------------------------------------------"));

        return true;
    }

    private void playerNotFoundMessage(CommandSender sender, Map<String, String> pl) {
        sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-group-not-found", pl));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.voicechat.admin") && !sender.hasPermission("chatutils.vcinfo")) {
            return Collections.emptyList();
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (Group g : vcManager.getGroups()) {
                if (g.getName() != null && g.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    list.add(g.getName());
                }
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                Group pg = vcManager.getPlayerGroup(p);
                if (pg != null && p.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    list.add(p.getName());
                }
            }
            return list;
        }

        return Collections.emptyList();
    }
}
