package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.hook.VoiceChatManager;
import com.chatutils.utils.ColorUtil;
import de.maxhenkel.voicechat.api.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class VoiceChatListCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public VoiceChatListCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.voicechat.admin") && !sender.hasPermission("chatutils.vcgroups")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null || !vcManager.isAvailable()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-not-available"));
            return true;
        }

        Collection<Group> groups = vcManager.getGroups();
        if (groups.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vc-list-empty"));
            return true;
        }

        sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("vc-list-header")));

        for (Group g : groups) {
            String groupName = g.getName() != null ? g.getName() : "İsimsiz";
            String passwordTag = g.hasPassword() ? plugin.getConfigManager().getMessage("vc-password-locked") : plugin.getConfigManager().getMessage("vc-password-open");
            List<Player> members = vcManager.getPlayersInGroup(g);
            String memberListStr = members.isEmpty() ? "Boş" : members.stream().map(Player::getName).collect(Collectors.joining(", "));

            String itemFormat = plugin.getConfigManager().getMessage("vc-list-item", Map.of(
                    "group", groupName,
                    "password", passwordTag,
                    "count", String.valueOf(members.size()),
                    "members", memberListStr,
                    "type", VoiceChatManager.formatGroupType(g.getType())
            ));

            if (sender instanceof Player) {
                Player player = (Player) sender;

                Component itemComp = Component.text(ColorUtil.colorize(itemFormat));

                Component stealthBtn = Component.text(ColorUtil.colorize(" &8[&a&lGİZLİ GİR&8]"))
                        .clickEvent(ClickEvent.runCommand("/vcjoin " + groupName))
                        .hoverEvent(HoverEvent.showText(Component.text(ColorUtil.colorize("&aTıkla ve bu gruba &eGİZLİ (Sol üstte gözükmeden)&a bağlan!"))));

                Component visibleBtn = Component.text(ColorUtil.colorize(" &8[&6Görünür&8]"))
                        .clickEvent(ClickEvent.runCommand("/vcjoin " + groupName + " --visible"))
                        .hoverEvent(HoverEvent.showText(Component.text(ColorUtil.colorize("&6Tıkla ve bu gruba normal görünür olarak katıl."))));

                player.sendMessage(itemComp.append(stealthBtn).append(visibleBtn));
            } else {
                sender.sendMessage(ColorUtil.colorize(itemFormat));
            }
        }

        sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("vc-list-footer")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
